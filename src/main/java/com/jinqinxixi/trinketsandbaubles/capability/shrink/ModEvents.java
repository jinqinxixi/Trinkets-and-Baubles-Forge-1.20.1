package com.jinqinxixi.trinketsandbaubles.capability.shrink;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if(event.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap -> {
                if(cap.isShrunk()) {
                    EntityDimensions oldSize = event.getNewSize();
                    EntityDimensions newSize = oldSize.scale(cap.scale());

                    // 设置新的尺寸
                    event.setNewSize(newSize);
                    event.setNewEyeHeight(event.getNewEyeHeight() * cap.scale());
                }
            });
        }
    }

    // 添加一个实体加入世界的事件处理器，确保在正确的时机应用缩放
    @SubscribeEvent
    public static void onEntityJoinWorld(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living) {
            living.getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap -> {
                if (cap.isShrunk()) {
                    // 强制更新大小
                    living.refreshDimensions();
                }
            });
        }
    }
}