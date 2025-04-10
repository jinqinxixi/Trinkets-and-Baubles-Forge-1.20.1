package com.jinqinxixi.trinketsandbaubles.modeffects;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FireResistanceEffect extends MobEffect {

    public FireResistanceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF4400);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            // 检查是否是龙火伤害
            if (event.getSource().is(ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation("iceandfire", "dragon_fire")))) {

                // 检查玩家是否有这个效果
                if (player.hasEffect(ModEffects.FIRE_RESISTANCE.get())) {
                    // 取消伤害
                    event.setCanceled(true);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}