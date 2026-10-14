package com.jinqinxixi.trinketsandbaubles.util;

import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 种族体型缩放工具类。
 *
 * <p>体型缩放不依赖第三方库，而是：碰撞箱由 {@code EntityEvent.Size} 事件被动改，渲染由
 * {@code RenderPlayerEvent} 事件被动改。本类负责平滑过渡 —— 维护每个实体的「当前缩放值」和
 * 「目标缩放值」，每 tick 把当前值朝目标值逼近一步，到达目标即清理。碰撞箱与渲染每 tick 读到的
 * 是平滑中的当前值，所以视觉和碰撞一起缓慢变化。
 *
 * <p>步进速度为每 tick 0.01（scale 单位）：泰坦（1.0→3.0）约 10 秒，仙女（1.0→0.25）约 3.75 秒。
 */
public class RaceScaleHelper {

    /** 每 tick 逼近目标的步长（scale 单位，百分数域 ±1 的等价写法）。 */
    private static final float STEP_PER_TICK = 0.01f;

    /** 平滑中的当前缩放值，按实体 UUID 存。 */
    private static final Map<UUID, Float> CURRENT = new ConcurrentHashMap<>();

    /** 平滑的目标缩放值，按实体 UUID 存。 */
    private static final Map<UUID, Float> TARGET = new ConcurrentHashMap<>();

    private RaceScaleHelper() {
    }

    /**
     * 目标缩放值：激活种族读它的 scaleFactor，否则 1.0（正常体型）。
     *
     * <p>这是「应该变成什么样」的答案，与平滑中的当前值分开。登录时 capability 从 NBT 恢复、
     * 而本类的平滑表还是空的，此时 {@link #getActiveScale} 会回退到这里，保证体型至少正确。
     */
    public static float getTargetScale(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return 1.0f;
        }

        for (var entry : ModCapabilities.RACE_CAPABILITIES.entrySet()) {
            IBaseRaceCapability race = (IBaseRaceCapability) player.getCapability(entry.getValue())
                    .resolve().orElse(null);
            if (race != null && race.isActive()) {
                return race.getScaleFactor();
            }
        }

        return 1.0f;
    }

    /**
     * 当前生效的缩放值：平滑中的当前值，若尚无平滑记录则回退到目标值，并顺手把当前体型记录
     * 进 {@link #CURRENT}（惰性初始化）。这样登录恢复、连续变身等场景，下一次启动平滑时能拿到
     * 正确的「当前体型」作起点，而不是从 1.0 硬起。
     */
    public static float getActiveScale(LivingEntity entity) {
        if (entity == null) {
            return 1.0f;
        }
        UUID id = entity.getUUID();
        Float current = CURRENT.get(id);
        if (current != null) {
            return current;
        }
        float target = getTargetScale(entity);
        CURRENT.put(id, target);
        return target;
    }

    /**
     * 瞬间设置缩放（无过渡）。
     */
    public static void setModelScale(LivingEntity entity, float scale) {
        if (entity == null) {
            return;
        }
        UUID id = entity.getUUID();
        CURRENT.put(id, scale);
        TARGET.put(id, scale);
        entity.refreshDimensions();
    }

    /** 重置到正常体型。 */
    public static void resetModelScale(LivingEntity entity) {
        setModelScale(entity, 1.0f);
    }

    /**
     * 启动平滑过渡。当前值保持不动（以「最后一次已知体型」为起点），目标值设为 scale；之后每
     * tick 由 {@link #tick} 推进。起点取 {@link #CURRENT} 里保留的值；若从未记录过（初始变身，
     * 从人类体型起），则从 1.0 起。到达目标后 {@code CURRENT} 也不清除，作为「当前实际体型」
     * 持续记录，这样连续变身（泰坦→仙女）时下一段平滑能从 3.0 起，而不是瞬间跳回 1.0。
     *
     * @param entity 目标实体
     * @param scale  目标缩放值
     * @param ticks  过渡 tick 数（当前实现用固定步长，忽略此参数，保留以兼容旧调用）
     */
    public static void setSmoothModelScale(LivingEntity entity, float scale, int ticks) {
        if (entity == null) {
            return;
        }
        UUID id = entity.getUUID();
        float current = CURRENT.containsKey(id) ? CURRENT.get(id) : 1.0f;
        CURRENT.put(id, current);
        TARGET.put(id, scale);
        entity.refreshDimensions();
    }

    /**
     * 每 tick 推进一步平滑。由 {@code LivingTickEvent} 调用，服务端和客户端都会执行，因此
     * 碰撞箱（双端）和渲染（客户端）看到的当前值同步逼近。
     *
     * <p>到达目标后只清理 {@code TARGET}（表示无待过渡目标），保留 {@code CURRENT} 作为当前
     * 体型的持久记录，供下一段平滑取起点。实体消失时由 {@link #onEntityRemoved} 清理。
     *
     * @return 是否因尺寸变化而刷新了碰撞箱
     */
    public static boolean tick(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        UUID id = entity.getUUID();

        Float target = TARGET.get(id);
        if (target == null) {
            return false;
        }

        float current = CURRENT.getOrDefault(id, target);
        float next = stepTowards(current, target);

        if (Math.abs(next - current) > 0.0001f) {
            CURRENT.put(id, next);
            entity.refreshDimensions();
            return true;
        }

        // 已到达目标：当前体型定型为目标值，清掉目标标记，保留 CURRENT。这里必须再刷一次碰撞箱——
        // 否则碰撞箱停留在倒数第二个中间值，而 getActiveScale 已返回最终值，两者不一致，贴墙时
        // 碰撞箱和渲染错位，表现为「卡住」。
        CURRENT.put(id, target);
        TARGET.remove(id);
        entity.refreshDimensions();
        return false;
    }

    private static float stepTowards(float current, float target) {
        float diff = target - current;
        if (Math.abs(diff) <= STEP_PER_TICK) {
            return target;
        }
        return current + Math.copySign(STEP_PER_TICK, diff);
    }

    /** 获取当前生效的模型缩放值。 */
    public static float getCurrentModelScale(LivingEntity entity) {
        return getActiveScale(entity);
    }

    /**
     * 按「当前激活种族」重新计算目标体型并启动平滑。
     *
     * <p>供客户端收到同步包后调用：同步包里带的 scale 是该种族自己的 scaleFactor，但在「移除
     * 种族」的同步包里，这个值是被移除种族（如龙的 1.2）的旧值，不是玩家现在应有的体型。正确
     * 的目标要重新遍历所有种族、找真正激活的那个（比如刚切到的仙女 0.25），而不是直接用同步包。
     */
    public static void refreshTarget(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        setSmoothModelScale(entity, getTargetScale(entity), 20);
    }

    /**
     * 清理实体的平滑记录，在实体从世界移除时调用，避免 UUID 表无界增长。
     */
    public static void onEntityRemoved(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        UUID id = entity.getUUID();
        CURRENT.remove(id);
        TARGET.remove(id);
    }
}
