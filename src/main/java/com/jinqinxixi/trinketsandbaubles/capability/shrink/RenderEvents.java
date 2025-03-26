package com.jinqinxixi.trinketsandbaubles.capability.shrink;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderEvents
{
    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event)
    {
        try
        {
            Player player = event.getEntity();

            player.getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(iShrinkProvider ->
            {
                if (iShrinkProvider.isShrunk())
                {
                    event.getPoseStack().pushPose();
                    float scale = iShrinkProvider.scale();

                    // 应用缩放
                    event.getPoseStack().scale(scale, scale, scale);

                    // 只处理特殊情况的位置偏移
                    if (player.isCrouching() && scale < 1F)
                    {
                        // 蹲下时只给一个小的向上偏移，避免陷入方块
                        event.getPoseStack().translate(0, 0.15F / scale, 0);
                    }
                    else if (player.isPassenger())
                    {
                        // 骑乘时给予小幅向上偏移，避免陷入坐骑
                        event.getPoseStack().translate(0, 0.3F / scale, 0);
                    }
                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event)
    {
        try
        {
            Player player = event.getEntity();
            player.getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(iShrinkProvider ->
            {
                if (iShrinkProvider.isShrunk())
                {
                    event.getPoseStack().popPose();
                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}