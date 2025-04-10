package com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage;

import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncDragonBreathMessage {
    private final boolean isActive;
    private final int playerId;

    public SyncDragonBreathMessage(boolean isActive, int playerId) {
        this.isActive = isActive;
        this.playerId = playerId;
    }

    public static SyncDragonBreathMessage decode(FriendlyByteBuf buf) {
        return new SyncDragonBreathMessage(buf.readBoolean(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isActive);
        buf.writeInt(playerId);
    }

    public static void handle(SyncDragonBreathMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(message.playerId);
                if (entity instanceof Player player) {
                    player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                        if (message.isActive != cap.isDragonBreathActive()) {
                            cap.toggleDragonBreath();
                        }
                    });
                }
            }
        });
        context.setPacketHandled(true);
    }
}