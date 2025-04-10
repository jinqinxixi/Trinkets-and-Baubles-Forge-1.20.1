package com.jinqinxixi.trinketsandbaubles.capability.api;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;
/**
 * 猫妖种族能力接口
 * 定义了猫妖特有的能力方法
 */
@AutoRegisterCapability
public interface IFaelesCapability extends IBaseRaceCapability {
    /**
     * 处理猫妖跳跃增强效果
     * 在玩家跳跃时触发，提供额外的跳跃高度
     */
    void onJump();

    /**
     * 处理猫妖喝牛奶的特殊效果
     * 当玩家喝牛奶时触发，提供多个增益效果
     * - 跳跃提升效果
     * - 伤害提升效果
     * - 速度提升效果
     */
    void onDrinkMilk();

    /**
     * 处理猫妖的墙上攀爬能力
     * 检查玩家是否靠近墙壁并处理攀爬逻辑
     */
    void handleWallClimb();
}