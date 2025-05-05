package com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class StopDragonBreathMessage {
    public static StopDragonBreathMessage decode(FriendlyByteBuf buf) {
        return new StopDragonBreathMessage();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(StopDragonBreathMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                TrinketsandBaublesMod.LOGGER.info("Handling dragon breath stop message from player: {}",
                        sender.getName().getString());
                sender.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap instanceof DragonCapability dragonCap && cap.isActive()) {
                        dragonCap.setDragonBreathActive(false);
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}