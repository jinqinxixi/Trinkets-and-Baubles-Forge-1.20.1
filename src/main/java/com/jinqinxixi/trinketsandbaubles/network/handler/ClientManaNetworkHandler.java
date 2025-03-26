package com.jinqinxixi.trinketsandbaubles.network.handler;

import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaHudOverlay;
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
            System.out.println("收到魔力同步数据：" + message.getMana() + "/" + message.getMaxMana());
            ManaData.setMana(player, message.getMana());
            ManaData.setMaxMana(player, message.getMaxMana());
            ManaHudOverlay.getInstance().updateManaData(message.getMana(), message.getMaxMana());
        }
    }
}