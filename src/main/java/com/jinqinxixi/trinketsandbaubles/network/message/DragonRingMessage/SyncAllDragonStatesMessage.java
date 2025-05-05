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
    private final boolean dragonBreathActive;
    private final int playerId;

    public SyncAllDragonStatesMessage(boolean flightEnabled, boolean dragonBreathActive, int playerId) {
        this.flightEnabled = flightEnabled;
        this.dragonBreathActive = dragonBreathActive;
        this.playerId = playerId;
    }

    public static SyncAllDragonStatesMessage decode(FriendlyByteBuf buf) {
        return new SyncAllDragonStatesMessage(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readInt()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(flightEnabled);
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
                    // 处理龙族能力
                    player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(dragonCap -> {
                        if (message.flightEnabled != dragonCap.isFlightEnabled()) {
                            dragonCap.toggleFlight();
                        }
                        if (message.dragonBreathActive != dragonCap.isDragonBreathActive()) {
                            dragonCap.toggleDragonBreath();
                        }
                    });

                    // 处理仙女能力
                    player.getCapability(ModCapabilities.FAIRY_CAPABILITY).ifPresent(fairyCap -> {
                        if (message.flightEnabled != fairyCap.isFlightEnabled()) {
                            fairyCap.toggleFlight();
                        }
                    });
                }
            }
        });
        context.setPacketHandled(true);
    }
}