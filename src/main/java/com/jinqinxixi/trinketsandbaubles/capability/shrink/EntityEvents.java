package com.jinqinxixi.trinketsandbaubles.capability.shrink;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
public class EntityEvents {
    @SubscribeEvent
    public static void onEntityJoinWorld(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living) {
            living.getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap -> {
                if (cap.isShrunk()) {
                    TrinketsandBaublesMod.LOGGER.debug("Entity joined world with shrink effect: {}",
                            living.getName().getString());
                    // 强制更新大小
                    living.refreshDimensions();
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        event.getEntity().getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap -> {
            if (cap.isShrunk()) {
                TrinketsandBaublesMod.LOGGER.debug("Player changed dimension with shrink effect: {}",
                        event.getEntity().getName().getString());
                // 强制更新大小
                event.getEntity().refreshDimensions();
            }
        });
    }
}