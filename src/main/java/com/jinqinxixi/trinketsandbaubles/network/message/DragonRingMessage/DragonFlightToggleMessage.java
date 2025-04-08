package com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage;

import com.jinqinxixi.trinketsandbaubles.modEffects.DragonsEffect;
import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DragonFlightToggleMessage {
    public static DragonFlightToggleMessage decode(FriendlyByteBuf buf) {
        return new DragonFlightToggleMessage();
    }

    public void encode(FriendlyByteBuf buf) {}

    public static void handle(DragonFlightToggleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null && sender.hasEffect(ModEffects.DRAGON.get())) {
                // 获取当前状态并切换
                CompoundTag data = sender.getPersistentData();
                boolean currentState = data.getBoolean("DragonFlightEnabled");
                boolean newState = !currentState;

                // 保存新状态
                data.putBoolean("DragonFlightEnabled", newState);

                // 更新飞行能力
                DragonsEffect.toggleFlight(sender);
            }
        });
        context.setPacketHandled(true);
    }
}