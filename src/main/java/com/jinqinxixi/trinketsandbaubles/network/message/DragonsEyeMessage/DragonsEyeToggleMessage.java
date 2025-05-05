package com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage;

import com.jinqinxixi.trinketsandbaubles.util.ScanSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DragonsEyeToggleMessage {
    private final int actionType;

    public DragonsEyeToggleMessage(int actionType) {
        this.actionType = actionType;
    }

    public static void encode(DragonsEyeToggleMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.actionType);
    }

    public static DragonsEyeToggleMessage decode(FriendlyByteBuf buf) {
        return new DragonsEyeToggleMessage(buf.readInt());
    }

    public static void handle(DragonsEyeToggleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null) {
                switch (message.actionType) {
                    case 1 -> ScanSystem.toggleNightVision(serverPlayer);
                }
            }
        });
        context.setPacketHandled(true);
    }

    public int getActionType() {
        return actionType;
    }
}