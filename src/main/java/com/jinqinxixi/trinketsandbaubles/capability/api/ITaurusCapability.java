package com.jinqinxixi.trinketsandbaubles.capability.api;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * 金牛族能力接口
 *
 * <p>金牛是坦克型种族：血量、护甲、护甲韧性、击退抗性更高，攻高而移速/游速略降。特有技能是
 * 「踏击」——冲刺撞向目标，碰撞造成伤害并击退。
 */
@AutoRegisterCapability
public interface ITaurusCapability extends IBaseRaceCapability
{
    /**
     * 处理踏击（冲撞）：朝玩家视线方向冲刺，路径上的敌人受到伤害并被击退。
     *
     * @param charge 蓄力比例 0~1，决定冲刺速度、伤害与击退力度（平方曲线）
     */
    void handleStampede(float charge);
}