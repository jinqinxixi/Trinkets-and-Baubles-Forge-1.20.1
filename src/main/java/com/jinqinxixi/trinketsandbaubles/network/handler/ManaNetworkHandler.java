package com.jinqinxixi.trinketsandbaubles.network.handler;

import com.jinqinxixi.trinketsandbaubles.network.message.ManaMessage.ManaSyncMessage;
import net.minecraft.server.level.ServerPlayer;

public class ManaNetworkHandler {
    // 发送魔力同步消息到客户端
    public static void syncManaToClient(ServerPlayer player, float mana, float maxMana) {
        NetworkHandler.sendToClient(new ManaSyncMessage(mana, maxMana), player);
    }
}