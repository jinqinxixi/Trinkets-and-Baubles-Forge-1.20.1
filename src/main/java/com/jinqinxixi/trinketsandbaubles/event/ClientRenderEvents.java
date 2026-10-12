package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Drawing that happens during a frame.
 *
 * <p>The game bus, which is the default and is what carries events posted while the world runs. A
 * setup event belongs to the other bus and is listened for in {@link ClientRenderSetup}; a class can
 * only name one of the two, so the layer registration lives there rather than here.
 */
@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID, value = Dist.CLIENT)
public class ClientRenderEvents {
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        ManaHudOverlay.getInstance().render(event.getGuiGraphics());
    }
}