package com.jinqinxixi.trinketsandbaubles.capability.api;
/**
 * 精灵族能力接口
 * 定义了精灵族特有的能力方法
 */
public interface IElvesCapability extends IBaseRaceCapability {
    /**
     * 检查是否在森林生物群系
     */
    boolean isInForest();
}