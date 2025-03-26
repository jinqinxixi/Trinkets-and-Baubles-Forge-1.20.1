package com.jinqinxixi.trinketsandbaubles.capability.shrink;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public interface IShrinkProvider {
    boolean isShrunk();
    void setShrunk(boolean shrunk);
    void sync(@Nonnull LivingEntity livingEntity);
    void shrink(@Nonnull LivingEntity livingEntity);
    void deShrink(@Nonnull LivingEntity livingEntity);
    EntityDimensions defaultEntitySize();
    float defaultEyeHeight();
    float scale();
    void setScale(float scale);
    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}