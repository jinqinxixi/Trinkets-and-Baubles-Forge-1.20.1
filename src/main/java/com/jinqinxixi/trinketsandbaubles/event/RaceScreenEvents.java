package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.client.gui.RacePropertiesButton;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Puts the way into the appearance screen on the inventory.
 *
 * <p>Added as the screen is built rather than mixed into it. A screen posts its own setup event and a
 * listener can add to it from outside, which avoids a mixin for what is one icon and keeps this
 * working if the inventory is ever rewritten.
 *
 * <p>Shown to every player, whether or not they are wearing a race. This followed that rule wrong once,
 * hiding it behind a check for an active race — which reads as the icon being missing rather than as
 * it having nothing to do, and takes away the one place a player can see what they are before
 * deciding.
 */
@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID, value = Dist.CLIENT)
public class RaceScreenEvents
{
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event)
    {
        if (event.getScreen() instanceof InventoryScreen screen)
        {
            event.addListener(new RacePropertiesButton(screen));
        }
    }
}