package com.jinqinxixi.trinketsandbaubles.capability.network;


import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
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

    private SyncRaceCapabilityPacket(String raceId, boolean isActive, float scaleFactor) {
        this.raceId = raceId;
        this.isActive = isActive;
        this.scaleFactor = scaleFactor;
    }

    // 将数据写入缓冲区
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(raceId);
        buf.writeBoolean(isActive);
        buf.writeFloat(scaleFactor);
    }

    // 从缓冲区读取数据
    public static SyncRaceCapabilityPacket decode(FriendlyByteBuf buf) {
        return new SyncRaceCapabilityPacket(
                buf.readUtf(),
                buf.readBoolean(),
                buf.readFloat()
        );
    }

    // 处理接收到的数据包
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 确保我们在客户端处理这个数据包
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleOnClient());
        });
        ctx.get().setPacketHandled(true);
    }

    // 客户端处理逻辑
    // 客户端处理逻辑
    private void handleOnClient() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // 根据种族ID获取对应的能力并更新
        switch (raceId) {
            case "dwarves" -> player.getCapability(ModCapabilities.DWARVES_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(isActive, scaleFactor);
                }
            });
            case "elves" -> player.getCapability(ModCapabilities.ELVES_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(isActive, scaleFactor);
                }
            });
            case "faeles" -> player.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(isActive, scaleFactor);
                }
            });
            case "titan" -> player.getCapability(ModCapabilities.TITAN_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(isActive, scaleFactor);
                }
            });
        case "dragon" -> player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
            if (cap instanceof AbstractRaceCapability abstractCap) {
                abstractCap.updateStateOnly(isActive, scaleFactor);
            }
        });
        case "fairy" -> player.getCapability(ModCapabilities.FAIRY_CAPABILITY).ifPresent(cap -> {
            if (cap instanceof AbstractRaceCapability abstractCap) {
                abstractCap.updateStateOnly(isActive, scaleFactor);
            }
        });
        case "goblins" -> player.getCapability(ModCapabilities.GOBLINS_CAPABILITY).ifPresent(cap -> {
            if (cap instanceof AbstractRaceCapability abstractCap) {
                abstractCap.updateStateOnly(isActive, scaleFactor);
            }
        });
        }
    }
}