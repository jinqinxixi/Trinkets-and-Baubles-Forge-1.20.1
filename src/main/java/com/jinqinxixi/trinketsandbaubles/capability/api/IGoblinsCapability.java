package com.jinqinxixi.trinketsandbaubles.capability.api;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
/**
 * 哥布林族能力接口
 */
public interface IGoblinsCapability extends IBaseRaceCapability {
    /**
     * 处理坐骑驯服
     * @param horse 要驯服的马
     */
    void handleMount(AbstractHorse horse);

    /**
     * 处理伤害减免
     * @param damage 原始伤害值
     * @param isFireOrExplosion 是否是火焰或爆炸伤害
     * @return 处理后的伤害值
     */
    float handleDamage(float damage, boolean isFireOrExplosion);
}