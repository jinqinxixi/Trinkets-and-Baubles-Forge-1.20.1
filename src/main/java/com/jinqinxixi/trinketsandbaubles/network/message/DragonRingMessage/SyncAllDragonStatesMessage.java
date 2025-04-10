package com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage;

import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class SyncAllDragonStatesMessage {
    private final boolean flightEnabled;
    private final boolean nightVisionEnabled;
    private final boolean dragonBreathActive;
    private final int playerId;

    public SyncAllDragonStatesMessage(boolean flightEnabled, boolean nightVisionEnabled,
                                      boolean dragonBreathActive, int playerId) {
        this.flightEnabled = flightEnabled;
        this.nightVisionEnabled = nightVisionEnabled;
        this.dragonBreathActive = dragonBreathActive;
        this.playerId = playerId;
    }

    public static SyncAllDragonStatesMessage decode(FriendlyByteBuf buf) {
        return new SyncAllDragonStatesMessage(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readInt()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(flightEnabled);
        buf.writeBoolean(nightVisionEnabled);
        buf.writeBoolean(dragonBreathActive);
        buf.writeInt(playerId);
    }

    public static void handle(SyncAllDragonStatesMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(message.playerId);
                if (entity instanceof Player player) {
                    player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                        if (message.flightEnabled != cap.isFlightEnabled()) {
                            cap.toggleFlight();
                        }
                        if (message.nightVisionEnabled != cap.isNightVisionEnabled()) {
                            cap.toggleNightVision();
                        }
                        if (message.dragonBreathActive != cap.isDragonBreathActive()) {
                            cap.toggleDragonBreath();
                        }
                    });
                }
            }
        });
        context.setPacketHandled(true);
    }
}