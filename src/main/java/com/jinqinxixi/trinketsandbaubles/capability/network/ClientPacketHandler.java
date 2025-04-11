package com.jinqinxixi.trinketsandbaubles.capability.network;

import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {
    @OnlyIn(Dist.CLIENT)
    public static void handlePacket(SyncRaceCapabilityPacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // 根据种族ID获取对应的能力并更新
        switch (packet.getRaceId()) {
            case "dwarves" -> player.getCapability(ModCapabilities.DWARVES_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(packet.isActive(), packet.getScaleFactor());
                }
            });
            case "elves" -> player.getCapability(ModCapabilities.ELVES_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(packet.isActive(), packet.getScaleFactor());
                }
            });
            case "faeles" -> player.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(packet.isActive(), packet.getScaleFactor());
                }
            });
            case "titan" -> player.getCapability(ModCapabilities.TITAN_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(packet.isActive(), packet.getScaleFactor());
                }
            });
            case "dragon" -> player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(packet.isActive(), packet.getScaleFactor());
                }
            });
            case "fairy" -> player.getCapability(ModCapabilities.FAIRY_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(packet.isActive(), packet.getScaleFactor());
                }
            });
            case "goblins" -> player.getCapability(ModCapabilities.GOBLINS_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    abstractCap.updateStateOnly(packet.isActive(), packet.getScaleFactor());
                }
            });
        }
    }
}