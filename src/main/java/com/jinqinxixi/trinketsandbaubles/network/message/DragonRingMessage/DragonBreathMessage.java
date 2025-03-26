package com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage;

import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class DragonBreathMessage {
    public static DragonBreathMessage decode(FriendlyByteBuf buf) {
        return new DragonBreathMessage();
    }

    public void encode(FriendlyByteBuf buf) {}

    public static void handle(DragonBreathMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null && sender.hasEffect(ModEffects.DRAGON.get())) {
                sender.getPersistentData().putBoolean("DragonBreathActive", true);
                // 直接同步到所有客户端，包括发送者
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender),
                        new SyncDragonBreathMessage(true, sender.getId())
                );
            }
        });
        context.setPacketHandled(true);
    }
}