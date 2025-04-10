package com.jinqinxixi.trinketsandbaubles.capability.api;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * 精灵族能力接口
 */
@AutoRegisterCapability
public interface IFairyCapability extends IBaseRaceCapability {
    /**
     * 处理墙上攀爬
     */
    void handleWallClimb();

    /**
     * 更新飞行能力
     */
    void updateFlightAbility();

    /**
     * 检查飞行能力是否启用
     * @return 是否启用飞行
     */
    boolean isFlightEnabled();

    /**
     * 切换飞行能力的状态
     */
    void toggleFlight();
}