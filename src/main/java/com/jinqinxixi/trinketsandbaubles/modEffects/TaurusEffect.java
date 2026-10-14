package com.jinqinxixi.trinketsandbaubles.modeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 金牛族变身效果。
 *
 * <p>能力（属性加成、踏击）由 {@code TaurusCapability} 驱动；此效果本身只作为「玩家当前是金牛」
 * 的可见标记存在，不留图标、不显粒子。
 */
public class TaurusEffect extends MobEffect
{
    public TaurusEffect()
    {
        super(MobEffectCategory.BENEFICIAL, 0x8B5A2B); // 牛皮棕色
    }
}