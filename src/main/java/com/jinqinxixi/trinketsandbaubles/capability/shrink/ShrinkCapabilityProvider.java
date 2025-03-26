package com.jinqinxixi.trinketsandbaubles.capability.shrink;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShrinkCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    private final IShrinkProvider backend;
    private final LazyOptional<IShrinkProvider> optionalData;

    public ShrinkCapabilityProvider(LivingEntity entity) {
        this.backend = new ShrinkProvider(entity);
        this.optionalData = LazyOptional.of(() -> backend);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.SHRINK_CAPABILITY.orEmpty(cap, optionalData);
    }

    @Override
    public CompoundTag serializeNBT() {
        return backend.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.deserializeNBT(nbt);
    }
}