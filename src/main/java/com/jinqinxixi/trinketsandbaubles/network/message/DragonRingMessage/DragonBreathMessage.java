package com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage;

import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

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
            if (sender != null) {
                sender.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        cap.toggleDragonBreath();
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}