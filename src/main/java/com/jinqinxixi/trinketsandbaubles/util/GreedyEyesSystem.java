package com.jinqinxixi.trinketsandbaubles.util;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.client.particles.ClientEffects;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.GreedyEyesSyncMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.UpdateTargetsMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 贪婪之眼核心系统（单键正向循环 + 频率扫描 + TreeMap 距离排序 + GREED 粒子 + 龙吼音效），
 * 按目标项目风格实现为单个 {@code @Mod.EventBusSubscriber} 静态类。
 *
 * <p>探测资格复用 {@link ScanSystem#canPlayerScan(Player)}（有激活龙族或佩戴龙眼）；免疫/夜视
 * 仍留在 {@link ScanSystem}，本类只负责寻宝探测。
 */
@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
public class GreedyEyesSystem {

    private static final int OFF = -1;
    private static final double GROWL_RANGE = 10.0;

    private static final Map<UUID, GreedyEyesState> PLAYER_STATES = new HashMap<>();

    /**
     * 每位玩家的贪婪之眼状态（服务端 + 客户端各存一份）。
     */
    public static class GreedyEyesState {
        public int targetIndex = -1;
        public String targetEntry = "";
        public int targetColor = 0;
        public boolean wasEmptyLastSync = true;
        public int tickCounter = 0;
        public boolean firstScanDone = false;
        /** 按距离排序的命中点（key=距离 distanceTo，value=世界坐标），用 TreeMap 存储。 */
        public final TreeMap<Double, Vec3> found = new TreeMap<>();
        /** 惰性解析缓存：配置 targets 引用变化时重建。 */
        public List<? extends String> targetSource = null;
        public List<TreasureTarget> targets = List.of();

        public boolean isToggled() {
            return targetIndex > -1;
        }
    }

    public static GreedyEyesState getPlayerState(Player player) {
        return PLAYER_STATES.computeIfAbsent(player.getUUID(), k -> new GreedyEyesState());
    }

    // ===================== 服务端 =====================

    @SubscribeEvent
    public static void onServerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        GreedyEyesState state = getPlayerState(player);
        if (!ScanSystem.canPlayerScan(player)) {
            // 失去资格（卸下龙眼 / 取消龙族）：关闭探测并清空状态。
            state.targetIndex = OFF;
            state.targetEntry = "";
            state.targetColor = 0;
            state.found.clear();
            state.firstScanDone = false;
            state.wasEmptyLastSync = true;
            return;
        }
        if (!state.isToggled()) return;

        int vertical = ModConfig.GREEDY_EYES_RANGE_VERTICAL.get();
        int horizontal = ModConfig.GREEDY_EYES_RANGE_HORIZONTAL.get();
        if (vertical <= 0 || horizontal <= 0) return;

        int frequency = ModConfig.GREEDY_EYES_FREQUENCY.get();
        state.tickCounter++;
        // 首次强制扫（切目标后尽快反馈）+ 之后每 frequency tick 扫一次。
        boolean shouldScan = !state.firstScanDone || (state.tickCounter % frequency == 0);
        if (!shouldScan) return;

        // 扫描前扣魔（cost>0 才扣）。
        float cost = ModConfig.GREEDY_EYES_COST.get().floatValue();
        if (!trySpendMana(player, cost)) {
            // 魔力不足：关闭探测并发 off 消息。
            state.targetIndex = OFF;
            state.targetEntry = "";
            state.targetColor = 0;
            state.found.clear();
            state.wasEmptyLastSync = true;
            sendOffMessage(player);
            return;
        }

        scan(player, state);
        state.firstScanDone = true;
    }

    /**
     * 处理客户端 J 键发来的切目标请求（单键正向循环 off → 目标0 → … → 目标n-1 → off）。
     */
    public static void handleCycleTargetRequest(ServerPlayer player) {
        if (!ScanSystem.canPlayerScan(player)) return;

        GreedyEyesState state = getPlayerState(player);
        List<TreasureTarget> targetList = targets(state);
        int size = targetList.size();

        state.targetIndex = state.targetIndex + 1;
        if (state.targetIndex >= size) {
            state.targetIndex = OFF;
        }

        if (state.isToggled()) {
            TreasureTarget target = targetList.get(state.targetIndex);
            state.targetEntry = target.entry();
            state.targetColor = target.color();
            sendStatusOn(player, target);
        } else {
            state.targetEntry = "";
            state.targetColor = 0;
            sendOffMessage(player);
        }

        // 切目标后清掉旧命中、重置计数，触发下一次 tick 立即扫描一次（按完键尽快反馈）。
        state.found.clear();
        state.tickCounter = 0;
        state.firstScanDone = false;
    }

    private static List<TreasureTarget> targets(GreedyEyesState state) {
        List<? extends String> source = ModConfig.GREEDY_EYES_TARGETS.get();
        if (source != state.targetSource) {
            state.targetSource = source;
            state.targets = TreasureTarget.parseAll(source);
        }
        return state.targets;
    }

    /**
     * 服务端扫描：实体目标走 getEntities，方块目标遍历盒内整数坐标，命中点按
     * {@code distanceToSqr(origin)} 存入 TreeMap。
     */
    private static void scan(ServerPlayer player, GreedyEyesState state) {
        state.found.clear();
        List<TreasureTarget> targetList = targets(state);
        TreasureTarget target = state.isToggled() && state.targetIndex < targetList.size()
                ? targetList.get(state.targetIndex)
                : null;
        if (target == null) return;

        Vec3 origin = player.position();
        int horizontal = ModConfig.GREEDY_EYES_RANGE_HORIZONTAL.get();
        int vertical = ModConfig.GREEDY_EYES_RANGE_VERTICAL.get();
        AABB area = player.getBoundingBox().inflate(horizontal, vertical, horizontal);
        Level level = player.level();

        if (target.isEntity()) {
            for (Entity candidate : level.getEntities(target.entityType(), area, e -> true)) {
                state.found.put(candidate.position().distanceTo(origin), candidate.position());
            }
        } else {
            // 用 chunk 安全读取（{@link ChunkSafety.ChunkCache}）：chunk 未加载返回 null 跳过，不触发
            // 同步加载，避免扫描大片区域时把玩家周围 chunk 强制加载造成卡顿。
            ChunkSafety.ChunkCache chunks = new ChunkSafety.ChunkCache(level);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = Mth.floor(area.minX); x <= Mth.floor(area.maxX); x++) {
                for (int y = Mth.floor(area.minY); y <= Mth.floor(area.maxY); y++) {
                    for (int z = Mth.floor(area.minZ); z <= Mth.floor(area.maxZ); z++) {
                        pos.set(x, y, z);
                        BlockState blockState = chunks.getBlockState(pos);
                        if (blockState != null && !blockState.isAir() && target.matches(blockState)) {
                            Vec3 point = new Vec3(x, y, z);
                            state.found.put(point.distanceTo(origin), point);
                        }
                    }
                }
            }
        }

        // 只在「有结果」或「由有变无」时发同步，避免空转发包。
        boolean empty = state.found.isEmpty();
        if (!state.wasEmptyLastSync || !empty) {
            state.wasEmptyLastSync = empty;
            sendSync(player, state);
        }
    }

    /** 组装并发送同步包（positions 已按距离升序，来自 TreeMap.values()）。 */
    private static void sendSync(ServerPlayer player, GreedyEyesState state) {
        List<Vec3> positions = new ArrayList<>(state.found.values());
        NetworkHandler.sendToClient(
                new GreedyEyesSyncMessage(state.found.isEmpty(), state.targetEntry, state.targetColor, positions),
                player);
    }

    /**
     * 扫描扣魔，照 PLAN 第 6.7 节分层接入：龙族 active 走 {@link DragonCapability#spendMana(float)}
     * （其私有 ManaSystem 自动三选一）；仅佩戴龙眼（无龙族 active）走 {@link ManaData}。默认 cost=0
     * 跳过扣魔。
     */
    private static boolean trySpendMana(ServerPlayer player, float cost) {
        if (cost <= 0.0F) return true;

        var dragonCap = player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                .filter(cap -> cap instanceof DragonCapability)
                .map(cap -> (DragonCapability) cap);
        if (dragonCap.map(DragonCapability::isActive).orElse(false)) {
            return dragonCap.map(d -> d.spendMana(cost)).orElse(false);
        }

        if (ManaData.hasMana(player, cost)) {
            ManaData.consumeMana(player, cost);
            return true;
        }
        return false;
    }

    private static void sendStatusOn(ServerPlayer player, TreasureTarget target) {
        String name = target.displayName();
        if (name.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("xat.ability.greedy_eyes.treasurefinder.notfound",
                            Component.literal(target.entry())), true);
        } else {
            // 方块/实体目标的 displayName 是翻译 key，需要先翻译再作参数。
            Component targetComponent = target.isDisplayNameTranslatable()
                    ? Component.translatable(name)
                    : Component.literal(name);
            player.displayClientMessage(
                    Component.translatable("xat.ability.greedy_eyes.treasurefinder.on",
                            targetComponent, Component.translatable("xat.tooltip.on")), true);
        }
    }

    private static void sendOffMessage(ServerPlayer player) {
        player.displayClientMessage(
                Component.translatable("xat.ability.greedy_eyes.treasurefinder.off",
                        Component.translatable("xat.tooltip.off")), true);
    }

    // ===================== 客户端 =====================

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null && KeyBindings.TOGGLE_DRAGONS_EYE_MODE.consumeClick()) {
            // 客户端 onKeyPress 分支：按键瞬间清掉本地命中，立即隐藏旧粒子。
            getPlayerState(player).found.clear();
            NetworkHandler.INSTANCE.sendToServer(new UpdateTargetsMessage());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        GreedyEyesState state = getPlayerState(player);
        // 客户端不维护 targetIndex（客户端 onKeyPress 只清 found，toggle 在服务端）。
        // tickAbility 的 `isClientSide || isToggled` 短路语义：客户端只要 found 非空就显示，
        // 空则 showFound 内部直接跳过；节奏按 counter（首次立即 + 之后每 frequency tick）。
        int frequency = ModConfig.GREEDY_EYES_FREQUENCY.get();
        state.tickCounter++;
        if (!(!state.firstScanDone || (state.tickCounter % frequency == 0))) return;
        showFound(player, state);
        state.firstScanDone = true;
    }

    /**
     * 客户端接收服务端同步包后回填本地缓存。
     */
    @OnlyIn(Dist.CLIENT)
    public static void applySyncFromServer(GreedyEyesSyncMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        GreedyEyesState state = getPlayerState(minecraft.player);

        state.found.clear();
        state.wasEmptyLastSync = message.isEmpty();
        state.targetEntry = message.getTargetEntry();
        state.targetColor = message.getTargetColor();

        // positions 已按距离升序；客户端按本地位置重算距离作 key，沿用 TreeMap 排序语义。
        // 客户端不维护 targetIndex（客户端 onKeyPress 也只清 found，不做 toggle）。
        Vec3 origin = minecraft.player.position();
        for (Vec3 point : message.getPositions()) {
            state.found.put(point.distanceTo(origin), point);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void showFound(Player player, GreedyEyesState state) {
        if (!state.found.isEmpty() && player.isAlive()) {
            Map.Entry<Double, Vec3> closest = state.found.firstEntry();
            if (closest.getKey() > 1.8) {
                growl(player, closest.getValue(), closest.getKey());
            }

            if (ModConfig.GREEDY_EYES_CLOSEST.get()) {
                spawnParticles(closest.getValue(), 3);
                return;
            }

            int count = 0;
            int limit = ModConfig.GREEDY_EYES_PARTICLES.get();
            for (Vec3 point : state.found.values()) {
                if (count >= limit) {
                    break;
                }
                spawnParticles(point, 3);
                count += 3;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnParticles(Vec3 point, int amount) {
        Minecraft minecraft = Minecraft.getInstance();
        GreedyEyesState state = minecraft.player != null ? getPlayerState(minecraft.player) : null;
        int color = state != null ? state.targetColor : 0;
        for (int i = 0; i < amount; i++) {
            ClientEffects.play(6, point.x, point.y, point.z, 0.0, 0.0, 0.0, color, 1.0F, 1.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void growl(Player player, Vec3 point, double distance) {
        if (ModConfig.GREEDY_EYES_GROWL_ACTIVATION.get().isActive(player.isShiftKeyDown())) {
            float volume = ModConfig.GREEDY_EYES_VOLUME.get() / 100.0F;
            float scaled = Mth.clamp((float) (1.0 - distance / GROWL_RANGE) * volume, 0.0F, 1.0F);
            player.level().playSound(player, point.x, point.y, point.z,
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, scaled, 1.0F);
        }
    }

    // ===================== 生命周期 =====================

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PLAYER_STATES.remove(event.getEntity().getUUID());
    }
}
