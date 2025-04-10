package com.jinqinxixi.trinketsandbaubles.modeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BleedingEffect extends MobEffect {
    // 常量定义
    private static final float DAMAGE_PER_SECOND = 1.0F;
    private static final int DAMAGE_INTERVAL = 20; // 1秒 = 20 ticks

    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            entity.hurt(entity.level().damageSources().magic(), DAMAGE_PER_SECOND);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每秒触发一次（每20 ticks）
        return duration % DAMAGE_INTERVAL == 0;
    }
}