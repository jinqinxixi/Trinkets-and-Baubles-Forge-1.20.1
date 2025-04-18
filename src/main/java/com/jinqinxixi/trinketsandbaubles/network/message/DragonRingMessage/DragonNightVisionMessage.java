package com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage;

import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DragonNightVisionMessage {
    private final boolean enabled;
    public static final ResourceLocation ID = new ResourceLocation("trinketsandbaubles", "dragon_night_vision");

    public DragonNightVisionMessage(boolean enabled) {
        this.enabled = enabled;
    }

    public static DragonNightVisionMessage decode(FriendlyByteBuf buf) {
        return new DragonNightVisionMessage(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(enabled);
    }

    public static void handle(DragonNightVisionMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        cap.toggleNightVision();
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}