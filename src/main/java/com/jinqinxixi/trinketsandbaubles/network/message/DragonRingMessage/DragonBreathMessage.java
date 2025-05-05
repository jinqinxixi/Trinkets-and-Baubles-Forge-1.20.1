package com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class DragonBreathMessage {
    public static DragonBreathMessage decode(FriendlyByteBuf buf) {
        return new DragonBreathMessage();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(DragonBreathMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                TrinketsandBaublesMod.LOGGER.info("Handling dragon breath activation message from player: {}",
                        sender.getName().getString());
                sender.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap instanceof DragonCapability dragonCap && cap.isActive()) {
                        // 检查设置状态是否成功
                        if (!dragonCap.setDragonBreathActive(true)) {
                            // 如果失败（比如魔力不足），立即发送停止消息给客户端
                            NetworkHandler.INSTANCE.send(
                                    PacketDistributor.PLAYER.with(() -> sender),
                                    new SyncDragonBreathMessage(false, sender.getId())
                            );
                        }
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}