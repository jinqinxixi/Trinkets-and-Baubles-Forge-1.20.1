package com.jinqinxixi.trinketsandbaubles.network.handler;

import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay;
import com.jinqinxixi.trinketsandbaubles.network.message.ManaMessage.ManaSyncMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

@OnlyIn(Dist.CLIENT)
public class ClientManaNetworkHandler {
    public static void handleManaSync(ManaSyncMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ManaData.setMana(player, message.getMana());
            ManaData.setMaxMana(player, message.getMaxMana());
            ManaHudOverlay.getInstance().updateManaData(message.getMana(), message.getMaxMana());
        }
    }
}