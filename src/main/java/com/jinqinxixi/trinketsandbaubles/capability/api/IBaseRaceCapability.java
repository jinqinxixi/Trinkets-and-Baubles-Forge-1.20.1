package com.jinqinxixi.trinketsandbaubles.capability.api;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.INBTSerializable;

public interface IBaseRaceCapability extends INBTSerializable<CompoundTag> {
    void applyAttributes();

    void removeAttributes();

    void validateAndFixAttributes();

    /**
     * 检查种族能力是否激活
     * @return 如果种族能力当前处于激活状态则返回true
     */
    boolean isActive();

    /**
     * 设置种族能力的激活状态
     * @param active 是否激活
     */
    void setActive(boolean active);

    /**
     * 获取种族的体型缩放因子
     * @return 当前的体型缩放值
     */
    float getScaleFactor();

    /**
     * 设置种族的体型缩放因子
     * @param scale 新的体型缩放值
     */
    void setScaleFactor(float scale);

    /**
     * 处理种族能力的每tick更新
     */
    void tick();

    /**
     * 处理方块破坏事件的种族特殊效果
     * @param pos 被破坏方块的位置
     * @param block 被破坏的方块
     * @param level 服务器世界实例
     */
    void onBreakBlock(BlockPos pos, Block block, ServerLevel level);

    /**
     * 同步种族能力数据到客户端
     */
    void sync();

    /**
     * 获取种族ID
     * @return 种族的唯一标识符
     */
    String getRaceId();

    /**
     * 获取种族名称
     * @return 种族的显示名称
     */
    String getRaceName();

    /**
     * 获取永久魔力减少值
     * @return 当前的永久魔力减少量
     */
    float getPermanentManaDecrease();

    /**
     * 设置永久魔力减少值
     * @param value 新的永久魔力减少量
     */
    void setPermanentManaDecrease(float value);

    /**
     * 强制移除所有属性修改器
     */
    void forceRemoveAllModifiers();

    /**
     * 应用永久性魔力修改
     * @param amount 修改的数值
     * @param isBonus 是否是增益效果
     */
    void applyPermanentManaModifier(float amount, boolean isBonus);
}