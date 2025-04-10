package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.DASH_KEY);
        event.register(KeyBindings.CHARGE_KEY);
        event.register(KeyBindings.ATTRACTION_TOGGLE_KEY);
        event.register(KeyBindings.DEFLECTION_TOGGLE_KEY);
        event.register(KeyBindings.MANA_HUD_POSITION_KEY );
        event.register(KeyBindings.TOGGLE_DRAGONS_EYE_MODE);
        event.register(KeyBindings.TOGGLE_DRAGONS_EYE_VISION);
        event.register(KeyBindings.DRAGON_NIGHT_VISION_KEY);
        event.register(KeyBindings.DRAGON_BREATH_KEY);
        event.register(KeyBindings.DRAGON_FLIGHT_TOGGLE_KEY);
    }
}