package com.jinqinxixi.trinketsandbaubles.capability.network;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class RaceCapabilityNetworking {
    private static final String PROTOCOL_VERSION = "2.6";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TrinketsandBaublesMod.MOD_ID, "races"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    public static void init() {
        // 确保在正确的时间注册消息
        INSTANCE.registerMessage(
                nextId(),
                SyncRaceCapabilityPacket.class,
                SyncRaceCapabilityPacket::encode,
                SyncRaceCapabilityPacket::decode,
                SyncRaceCapabilityPacket::handle
        );
    }

    public static void sendToPlayer(IBaseRaceCapability capability, ServerPlayer player) {
        if (INSTANCE != null && player != null && capability != null) {
            INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncRaceCapabilityPacket(capability)
            );
        }
    }

    private static int nextId() {
        return packetId++;
    }
}