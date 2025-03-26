package com.jinqinxixi.trinketsandbaubles.modEffects;

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
public class LightningResistanceEffect extends MobEffect {

    public LightningResistanceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE6E6FA); // 浅紫色
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {

            if (event.getSource().is(ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation("iceandfire", "dragon_lightning")))) {

                // 检查玩家是否有这个效果
                if (player.hasEffect(ModEffects.LIGHTNING_RESISTANCE.get())) {
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