package com.jinqinxixi.trinketsandbaubles.capability.shrink;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class PlayerEvents {
    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof LivingEntity livingEntity) {
            evt.addCapability(new ResourceLocation(MOD_ID, "shrunk"),
                    new ShrinkCapabilityProvider(livingEntity));
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap ->
                    cap.sync(player));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        event.getEntity().getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap ->
                cap.sync(event.getEntity()));
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        event.getEntity().getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap ->
                cap.sync(event.getEntity()));
    }
}