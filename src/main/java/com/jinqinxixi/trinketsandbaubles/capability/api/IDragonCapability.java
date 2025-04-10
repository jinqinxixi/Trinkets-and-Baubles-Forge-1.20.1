package com.jinqinxixi.trinketsandbaubles.capability.api;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * 龙族能力接口，定义了龙族特有的能力操作方法
 */
@AutoRegisterCapability
public interface IDragonCapability extends IBaseRaceCapability {
    /**
     * 切换飞行能力的开启状态
     * 当关闭时会强制停止飞行
     */
    void toggleFlight();

    /**
     * 切换夜视能力的开启状态
     * 当关闭时会立即移除夜视效果
     */
    void toggleNightVision();

    /**
     * 切换龙息能力的开启状态
     * 状态改变时会同步到所有客户端
     */
    void toggleDragonBreath();

    /**
     * 更新玩家的飞行能力状态
     * 根据当前的 flightEnabled 状态和玩家游戏模式设置适当的飞行速度和权限
     */
    void updateFlightAbility();

    /**
     * @return 飞行能力是否启用
     */
    boolean isFlightEnabled();

    /**
     * @return 夜视能力是否启用
     */
    boolean isNightVisionEnabled();

    /**
     * @return 龙息能力是否激活
     */
    boolean isDragonBreathActive();
}