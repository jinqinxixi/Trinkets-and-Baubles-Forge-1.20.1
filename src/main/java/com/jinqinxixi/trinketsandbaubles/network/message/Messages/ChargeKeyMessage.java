package com.jinqinxixi.trinketsandbaubles.network.message.Messages;

import com.jinqinxixi.trinketsandbaubles.items.baubles.ArcingOrbItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public class ChargeKeyMessage {
    public static final ResourceLocation ID = new ResourceLocation("trinketsandbaubles", "charge_key_press");

    public ChargeKeyMessage() {}

    // 从 ByteBuf 解码
    public static ChargeKeyMessage decode(FriendlyByteBuf buf) {
        return new ChargeKeyMessage();
    }

    // 编码到 ByteBuf
    public void encode(FriendlyByteBuf buf) {
    }

    // 处理消息
    public static void handle(ChargeKeyMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player instanceof ServerPlayer serverPlayer) {
                CuriosApi.getCuriosInventory(serverPlayer).ifPresent(handler -> {
                    handler.findFirstCurio(stack -> stack.getItem() instanceof ArcingOrbItem)
                            .ifPresent(slotResult -> {
                                ItemStack stack = slotResult.stack();
                                if (stack.getItem() instanceof ArcingOrbItem arcingOrb) {
                                    arcingOrb.handleCharge(serverPlayer, stack);
                                }
                            });
                });
            }
        });
        context.setPacketHandled(true);
    }
}