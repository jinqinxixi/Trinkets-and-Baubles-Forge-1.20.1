package com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage;

import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsRingItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public class DragonsEyeToggleMessage {
    private final int actionType;

    public DragonsEyeToggleMessage(int actionType) {
        this.actionType = actionType;
    }

    public static void encode(DragonsEyeToggleMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.actionType);
    }

    public static DragonsEyeToggleMessage decode(FriendlyByteBuf buf) {
        return new DragonsEyeToggleMessage(buf.readInt());
    }

    public static void handle(DragonsEyeToggleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null) {
                CuriosApi.getCuriosInventory(serverPlayer).ifPresent(handler -> {
                    // 检查龙眼
                    handler.findFirstCurio(item -> item.getItem() instanceof DragonsEyeItem)
                            .ifPresent(slotResult -> {
                                ItemStack stack = slotResult.stack();
                                if (message.actionType == 0) {
                                    DragonsEyeItem.handleModeToggle(serverPlayer, stack);
                                } else {
                                    DragonsEyeItem.handleVisionToggle(serverPlayer, stack);
                                }
                            });

                    // 检查龙戒
                    handler.findFirstCurio(item -> item.getItem() instanceof DragonsRingItem)
                            .ifPresent(slotResult -> {
                                ItemStack stack = slotResult.stack();
                                if (message.actionType == 0) {
                                    DragonsRingItem.handleModeToggle(serverPlayer, stack);
                                }
                            });
                });
            }
        });
        context.setPacketHandled(true);
    }

    public int getActionType() {
        return actionType;
    }
}