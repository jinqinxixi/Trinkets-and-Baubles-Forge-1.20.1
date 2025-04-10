package com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage;

import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.impl.FairyCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DragonFlightToggleMessage {
    public static DragonFlightToggleMessage decode(FriendlyByteBuf buf) {
        return new DragonFlightToggleMessage();
    }

    public void encode(FriendlyByteBuf buf) {}

    public static void handle(DragonFlightToggleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                // 首先检查龙族能力
                sender.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap instanceof DragonCapability dragonCap && dragonCap.isActive()) {
                        dragonCap.toggleFlight();
                    }
                });

                // 再检查仙女能力
                sender.getCapability(ModCapabilities.FAIRY_CAPABILITY).ifPresent(cap -> {
                    if (cap instanceof FairyCapability fairyCap && fairyCap.isActive()) {
                        fairyCap.toggleFlight();
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}