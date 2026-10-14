package com.jinqinxixi.trinketsandbaubles.modeffects;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class IceResistanceEffet extends MobEffect {

    public IceResistanceEffet() {
        super(MobEffectCategory.BENEFICIAL, 0x8CDFFF); // 淡冰蓝色
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {

            // 冰免疫：冻结伤害 + 冰火传说的龙冰伤害。
            boolean ice = event.getSource().is(DamageTypeTags.IS_FREEZING)
                    || event.getSource().is(ResourceKey.create(Registries.DAMAGE_TYPE,
                            new ResourceLocation("iceandfire", "dragon_ice")));

            if (ice && player.hasEffect(ModEffects.ICE_RESISTANCE.get())) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}