package com.jinqinxixi.trinketsandbaubles.capability.base;

import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class BaseRaceCapabilityProvider<T extends IBaseRaceCapability> implements ICapabilitySerializable<CompoundTag> {
    private final T capability;
    private final LazyOptional<T> optional;
    private final Capability<T> capabilityType;

    public BaseRaceCapabilityProvider(T capability, Capability<T> capabilityType) {
        this.capability = capability;
        this.capabilityType = capabilityType;
        this.optional = LazyOptional.of(() -> capability);
    }

    @Override
    public <R> LazyOptional<R> getCapability(Capability<R> cap, Direction side) {
        return capabilityType.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return capability.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        capability.deserializeNBT(nbt);
    }
}