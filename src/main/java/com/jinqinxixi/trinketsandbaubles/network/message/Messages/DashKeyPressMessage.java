package com.jinqinxixi.trinketsandbaubles.network.message.Messages;

import com.jinqinxixi.trinketsandbaubles.items.baubles.ArcingOrbItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public class DashKeyPressMessage {
    // 空构造函数
    public DashKeyPressMessage() {}

    // 从 ByteBuf 解码
    public static DashKeyPressMessage decode(FriendlyByteBuf buf) {
        return new DashKeyPressMessage();
    }

    // 编码到 ByteBuf
    public void encode(FriendlyByteBuf buf) {
    }

    // 处理消息
    public static void handle(DashKeyPressMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                    handler.findFirstCurio(stack -> stack.getItem() instanceof ArcingOrbItem)
                            .ifPresent(slotResult -> {
                                ItemStack stack = slotResult.stack();
                                if (stack.getItem() instanceof ArcingOrbItem arcingOrb) {
                                    arcingOrb.handleDash(player, stack);
                                }
                            });
                });
            }
        });
        context.setPacketHandled(true);
    }
}