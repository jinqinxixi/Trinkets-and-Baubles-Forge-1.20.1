package com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage;

import com.jinqinxixi.trinketsandbaubles.util.ScanSystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateTargetsMessage {
    private final ListTag targets;
    private final boolean isToggleRequest;
    private final boolean isTargetMode;
    private final int oreGroupIndex;

    // 构造函数 - 用于发送目标列表和状态
    public UpdateTargetsMessage(ListTag targets, boolean isTargetMode, int oreGroupIndex) {
        this.targets = targets;
        this.isToggleRequest = false;
        this.isTargetMode = isTargetMode;
        this.oreGroupIndex = oreGroupIndex;
    }

    // 构造函数 - 用于发送切换请求
    public UpdateTargetsMessage() {
        this.targets = new ListTag();
        this.isToggleRequest = true;
        this.isTargetMode = false;
        this.oreGroupIndex = -1;
    }

    public static void encode(UpdateTargetsMessage message, FriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        tag.put(ScanSystem.TAG_DRAGONS_EYE_TARGETS, message.targets);
        tag.putBoolean("isToggleRequest", message.isToggleRequest);
        tag.putBoolean("isTargetMode", message.isTargetMode);
        tag.putInt("oreGroupIndex", message.oreGroupIndex);
        buf.writeNbt(tag);
    }

    public static UpdateTargetsMessage decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag != null && tag.getBoolean("isToggleRequest")) {
            return new UpdateTargetsMessage();
        }
        return new UpdateTargetsMessage(
                tag != null ? tag.getList(ScanSystem.TAG_DRAGONS_EYE_TARGETS, 10) : new ListTag(),
                tag != null && tag.getBoolean("isTargetMode"),
                tag != null ? tag.getInt("oreGroupIndex") : -1
        );
    }

    public static void handle(UpdateTargetsMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (message.isToggleRequest) {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    ScanSystem.handleScanToggleRequest(player);
                }
            } else {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(message));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(UpdateTargetsMessage message) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player != null) {
            ScanSystem.PlayerScanState state = ScanSystem.getPlayerState(minecraft.player);
            state.isTargetMode = message.isTargetMode();
            state.oreGroupIndex = message.getOreGroupIndex();
            state.lastTargets = message.getTargets();
            minecraft.player.getPersistentData().put(ScanSystem.TAG_DRAGONS_EYE_TARGETS, message.getTargets());
        }
    }

    public boolean isTargetMode() { return isTargetMode; }
    public int getOreGroupIndex() { return oreGroupIndex; }
    public ListTag getTargets() { return targets; }
}