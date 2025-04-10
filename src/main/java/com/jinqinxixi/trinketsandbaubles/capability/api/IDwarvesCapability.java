package com.jinqinxixi.trinketsandbaubles.capability.api;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;
/**
 * 矮人种族能力接口
 * 继承自基础种族能力接口 {@link IBaseRaceCapability}
 * 矮人种族的特殊能力（挖矿经验加成）在实现类中处理，不需要额外的接口方法
 */
@AutoRegisterCapability
public interface IDwarvesCapability extends IBaseRaceCapability {

}