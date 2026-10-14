package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.util.RaceScaleHelper;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 种族体型的碰撞箱与眼睛高度调整。
 *
 * <p>监听 {@link EntityEvent.Size}，在游戏每次计算实体尺寸时，把缩放因子乘进宽度和高度，并
 * 同步调整眼睛高度。缩放值来自种族 capability（见 {@link RaceScaleHelper#getActiveScale}），
 * 不依赖第三方缩放模组。
 *
 * <p>该事件在服务端和客户端都会触发，所以两边碰撞箱一致；渲染侧另有
 * {@code RaceRenderHandler} 负责视觉缩放。
 */
@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
public final class RaceSizeHandler {

    private RaceSizeHandler() {
    }

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        float scale = RaceScaleHelper.getActiveScale(player);
        if (scale <= 0.0f || Math.abs(scale - 1.0f) < 0.001f) {
            return;
        }

        EntityDimensions base = event.getNewSize();
        float width = clampWidth(base.width * scale);
        float height = clampHeight(base.height * scale);
        event.setNewSize(base.fixed
                ? EntityDimensions.fixed(width, height)
                : EntityDimensions.scalable(width, height));
        event.setNewEyeHeight(Math.max(event.getNewEyeHeight() * scale, 0.2F));
    }

    /**
     * 限制碰撞箱最小/最大宽度。
     *
     * <p>碰撞箱过窄（如仙女 0.6×0.25=0.15）会触发 Minecraft 碰撞检测的隐性最小宽度问题 —— 贴
     * 方块时像有「斥力」、要蹲几下才贴紧。这里限制玩家最小 0.3，对齐既有行为。
     */
    private static float clampWidth(float width) {
        return net.minecraft.util.Mth.clamp(width, 0.3F, 5.4F);
    }

    /** 限制碰撞箱高度。 */
    private static float clampHeight(float height) {
        return net.minecraft.util.Mth.clamp(height, 0.45F, 5.4F);
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            RaceScaleHelper.onEntityRemoved(player);
        }
    }
}
