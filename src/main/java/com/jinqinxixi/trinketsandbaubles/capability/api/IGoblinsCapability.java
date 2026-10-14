package com.jinqinxixi.trinketsandbaubles.capability.api;

import com.jinqinxixi.trinketsandbaubles.entity.AlphaWolf;
import net.minecraft.world.entity.animal.Wolf;

/**
 * 哥布林族能力接口
 */
public interface IGoblinsCapability extends IBaseRaceCapability {
    /**
     * 处理骑狼按键：骑乘中的冲锋攻击，或对准驯服狼骑上去。
     */
    void handleWolfRiderKey();

    /**
     * 把一只驯服狼替换为可骑乘的 AlphaWolf 并骑上去。成功则原狼被暂存、下骑时恢复。
     *
     * @return 是否成功骑上
     */
    boolean mountWolf(Wolf wolf);

    /**
     * 处理伤害减免
     * @param damage 原始伤害值
     * @param isFireOrExplosion 是否是火焰或爆炸伤害
     * @return 处理后的伤害值
     */
    float handleDamage(float damage, boolean isFireOrExplosion);
}