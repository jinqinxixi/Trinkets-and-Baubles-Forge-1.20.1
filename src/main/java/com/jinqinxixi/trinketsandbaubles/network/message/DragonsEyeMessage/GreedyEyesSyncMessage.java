package com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage;

import com.jinqinxixi.trinketsandbaubles.util.GreedyEyesSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 贪婪之眼同步包（服务端 → 客户端），改用 FriendlyByteBuf 而非 NBT 组装。
 *
 * <p>字段：{@code empty}（本次扫描是否空手）、{@code targetEntry}（当前目标原文）、
 * {@code targetColor}（标记颜色）、{@code positions}（按距离排序的命中点列表）。
 */
public class GreedyEyesSyncMessage {
    private final boolean empty;
    private final String targetEntry;
    private final int targetColor;
    private final List<Vec3> positions;

    public GreedyEyesSyncMessage(boolean empty, String targetEntry, int targetColor, List<Vec3> positions) {
        this.empty = empty;
        this.targetEntry = targetEntry;
        this.targetColor = targetColor;
        this.positions = positions;
    }

    public boolean isEmpty() {
        return empty;
    }

    public String getTargetEntry() {
        return targetEntry;
    }

    public int getTargetColor() {
        return targetColor;
    }

    public List<Vec3> getPositions() {
        return positions;
    }

    public static void encode(GreedyEyesSyncMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.empty);
        buf.writeUtf(message.targetEntry);
        buf.writeInt(message.targetColor);
        buf.writeVarInt(message.positions.size());
        for (Vec3 point : message.positions) {
            buf.writeDouble(point.x);
            buf.writeDouble(point.y);
            buf.writeDouble(point.z);
        }
    }

    public static GreedyEyesSyncMessage decode(FriendlyByteBuf buf) {
        boolean empty = buf.readBoolean();
        String targetEntry = buf.readUtf();
        int targetColor = buf.readInt();
        int count = buf.readVarInt();
        List<Vec3> positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            positions.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        return new GreedyEyesSyncMessage(empty, targetEntry, targetColor, positions);
    }

    public static void handle(GreedyEyesSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(message)));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(GreedyEyesSyncMessage message) {
        GreedyEyesSystem.applySyncFromServer(message);
    }
}
