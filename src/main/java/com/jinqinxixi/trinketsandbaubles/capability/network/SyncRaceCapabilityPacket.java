package com.jinqinxixi.trinketsandbaubles.capability.network;

import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncRaceCapabilityPacket {
    private final String raceId;
    private final boolean isActive;
    private final float scaleFactor;

    public SyncRaceCapabilityPacket(IBaseRaceCapability capability) {
        this.raceId = capability.getRaceId();
        this.isActive = capability.isActive();
        this.scaleFactor = capability.getScaleFactor();
    }

    public SyncRaceCapabilityPacket(FriendlyByteBuf buf) {
        this.raceId = buf.readUtf();
        this.isActive = buf.readBoolean();
        this.scaleFactor = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(raceId);
        buf.writeBoolean(isActive);
        buf.writeFloat(scaleFactor);
    }

    public static SyncRaceCapabilityPacket decode(FriendlyByteBuf buf) {
        return new SyncRaceCapabilityPacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 确保我们在客户端处理这个数据包
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandler.handlePacket(this);
            });
        });
        ctx.get().setPacketHandled(true);
    }

    // Getters
    public String getRaceId() { return raceId; }
    public boolean isActive() { return isActive; }
    public float getScaleFactor() { return scaleFactor; }
}