package com.jinqinxixi.trinketsandbaubles.network.message.Messages;

import com.jinqinxixi.trinketsandbaubles.items.baubles.PolarizedStoneItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public class PolarizedStoneToggleMessage {
    private final boolean isDeflectionToggle;

    public PolarizedStoneToggleMessage(boolean isDeflectionToggle) {
        this.isDeflectionToggle = isDeflectionToggle;
    }

    public static void encode(PolarizedStoneToggleMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.isDeflectionToggle);
    }

    public static PolarizedStoneToggleMessage decode(FriendlyByteBuf buf) {
        return new PolarizedStoneToggleMessage(buf.readBoolean());
    }

    public static void handle(PolarizedStoneToggleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null) {
                // 只检查饰品栏中的偏振石
                CuriosApi.getCuriosInventory(serverPlayer).ifPresent(handler -> {
                    handler.findFirstCurio(item -> item.getItem() instanceof PolarizedStoneItem).ifPresent(slotResult -> {
                        ItemStack stack = slotResult.stack();
                        if (message.isDeflectionToggle) {
                            boolean newDeflection = !stack.getOrCreateTag().getBoolean(PolarizedStoneItem.DEFLECTION_MODE_TAG);
                            stack.getOrCreateTag().putLong(PolarizedStoneItem.LAST_UPDATE_TAG, System.currentTimeMillis());
                            stack.getOrCreateTag().putBoolean(PolarizedStoneItem.DEFLECTION_MODE_TAG, newDeflection);
                            serverPlayer.displayClientMessage(Component.translatable(
                                    "item.trinketsandbaubles.polarized_stone.deflection_" + (newDeflection ? "on" : "off")), true);
                        } else {
                            boolean newAttraction = !stack.getOrCreateTag().getBoolean(PolarizedStoneItem.ATTRACTION_MODE_TAG);
                            stack.getOrCreateTag().putBoolean(PolarizedStoneItem.ATTRACTION_MODE_TAG, newAttraction);
                            serverPlayer.displayClientMessage(Component.translatable(
                                    "item.trinketsandbaubles.polarized_stone.attraction_" + (newAttraction ? "on" : "off")), true);
                        }
                    });
                });
            }
        });
        context.setPacketHandled(true);
    }

    public boolean isDeflectionToggle() {
        return isDeflectionToggle;
    }
}