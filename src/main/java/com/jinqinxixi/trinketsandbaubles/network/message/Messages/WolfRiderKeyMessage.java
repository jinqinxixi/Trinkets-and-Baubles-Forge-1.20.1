package com.jinqinxixi.trinketsandbaubles.network.message.Messages;

import com.jinqinxixi.trinketsandbaubles.capability.impl.GoblinsCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 哥布林骑狼按键：骑乘中冲锋，或对准驯服狼骑上去。
 */
public class WolfRiderKeyMessage {
    public WolfRiderKeyMessage() {}

    public static WolfRiderKeyMessage decode(FriendlyByteBuf buf) {
        return new WolfRiderKeyMessage();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(WolfRiderKeyMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(ModCapabilities.GOBLINS_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        ((GoblinsCapability) cap).handleWolfRiderKey();
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}