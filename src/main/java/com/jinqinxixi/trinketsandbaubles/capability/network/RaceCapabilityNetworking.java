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

        // The way back: a player's changes to how their race looks, sent from the client that made
        // them. Without it the server never hears about a colour and saves the value it already had.
        INSTANCE.registerMessage(
                nextId(),
                UpdateAppearancePacket.class,
                UpdateAppearancePacket::encode,
                UpdateAppearancePacket::decode,
                UpdateAppearancePacket::handle
        );
    }

    /**
     * Sends a player's appearance changes to the server.
     *
     * <p>Called from the client, and only from there — it is a message in one direction only.
     */
    public static void sendAppearanceUpdate(com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability race) {
        if (INSTANCE != null && race != null) {
            INSTANCE.sendToServer(new UpdateAppearancePacket(race));
        }
    }

    /**
     * Sends one player's race data to every client tracking that player, and to the player themself.
     *
     * <p>种族外观、体型、元素是「这个玩家长什么样」，别的玩家
     * 也要能看见，所以不能只发给玩家自己——要发给所有正在渲染这个玩家的客户端。缺了这一步，多人
     * 时其他玩家看不到这个人的角/翅/体型/技能特效。
     */
    public static void sendToPlayer(IBaseRaceCapability capability, ServerPlayer player) {
        if (INSTANCE != null && player != null && capability != null) {
            INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SyncRaceCapabilityPacket(player.getId(), capability)
            );
        }
    }

    private static int nextId() {
        return packetId++;
    }
}