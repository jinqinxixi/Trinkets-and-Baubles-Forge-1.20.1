package com.jinqinxixi.trinketsandbaubles.capability.api;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;
/**
 * 泰坦族能力接口
 * 定义了泰坦族特有的能力方法，包括：
 * - 水中移动能力
 * - 破坏植物能力
 * - 跳跃增强能力
 */
@AutoRegisterCapability
public interface ITitanCapability extends IBaseRaceCapability {
    /**
     * 处理泰坦在水中的特殊移动
     * 泰坦可以在水中移动且不受深水减速影响
     */
    void handleWaterMovement();

    /**
     * 处理泰坦破坏植物的特殊效果
     * 泰坦可以快速清除周围的植物，并获得额外资源
     */
    void handlePlantBreaking();

    /**
     * 处理泰坦的跳跃增强效果
     * 泰坦具有更高的跳跃高度和减少摔落伤害
     */
    void handleJump();
}