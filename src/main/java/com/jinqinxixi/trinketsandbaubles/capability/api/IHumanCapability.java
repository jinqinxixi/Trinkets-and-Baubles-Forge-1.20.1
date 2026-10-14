package com.jinqinxixi.trinketsandbaubles.capability.api;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * 人类种族能力接口。
 *
 * <p>人类是「默认/无种族」状态对应的种族（即 NONE）。它没有任何能力、没有属性加成、体型
 * 1.0、没有外观特征——它的意义只在于：无种族时仍有一个可调色的外观，装饰饰品（cosmetic）借它的
 * 颜色，调色界面也能调它。
 */
@AutoRegisterCapability
public interface IHumanCapability extends IBaseRaceCapability
{
}