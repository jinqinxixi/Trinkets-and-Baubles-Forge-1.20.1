package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.util.RaceScaleHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 种族体型的视觉缩放。
 *
 * <p>在渲染玩家前后用 {@code PoseStack.scale} 缩放模型。碰撞箱与眼睛高度由
 * {@link RaceSizeHandler} 负责，这里只管画出来的大小，两者读的是同一份缩放因子，所以视觉与
 * 碰撞一致。
 *
 * <p>仅客户端注册。
 */
@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID, value = Dist.CLIENT)
public final class RaceRenderHandler {

    /** 标记当前帧是否已压栈，避免 Post 时多余弹出。 */
    private static boolean scaled;

    private RaceRenderHandler() {
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        float scale = RaceScaleHelper.getActiveScale(player);
        if (scale <= 0.0f || Math.abs(scale - 1.0f) < 0.001f) {
            return;
        }

        event.getPoseStack().pushPose();
        scaled = true;
        event.getPoseStack().scale(scale, scale, scale);
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (scaled) {
            event.getPoseStack().popPose();
            scaled = false;
        }
    }
}
