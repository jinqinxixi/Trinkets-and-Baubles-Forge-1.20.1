package com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage;

import com.jinqinxixi.trinketsandbaubles.util.GreedyEyesSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 「切目标请求」专用消息（客户端 → 服务端，无参）。
 *
 * <p>客户端按 J 键（{@code TOGGLE_DRAGONS_EYE_MODE}）发出；服务端 handle 调
 * {@code GreedyEyesSystem.handleCycleTargetRequest(player)} 做单键正向循环。
 * 原三参构造与组式目标列表字段已删除（见 PLAN 第 6.5 节）。
 */
public class UpdateTargetsMessage {

    public UpdateTargetsMessage() {
    }

    public static void encode(UpdateTargetsMessage message, FriendlyByteBuf buf) {
        // 无字段，无需写入
    }

    public static UpdateTargetsMessage decode(FriendlyByteBuf buf) {
        return new UpdateTargetsMessage();
    }

    public static void handle(UpdateTargetsMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                GreedyEyesSystem.handleCycleTargetRequest(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
