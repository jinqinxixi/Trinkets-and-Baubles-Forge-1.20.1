package com.jinqinxixi.trinketsandbaubles.network.message;

import com.jinqinxixi.trinketsandbaubles.capability.glide.GlideHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端 → 客户端：告诉客户端「这个烟花火箭 id 是滑翔推进生成的，要静音」。
 *
 * <p>滑翔推进的静音火箭记录（{@code GlideHandler.SILENT_ROCKETS}）是服务端内存集合，而火箭的声音
 * 和粒子在客户端播放。单人时两端同一 JVM、集合共享所以静音生效；专用服务器时客户端集合是空的，
 * 声音/粒子照常播放。这个包把火箭 id 同步给客户端，客户端加入本地集合，服务器里也能静音。
 */
public class SilentRocketPacket
{
    private final int rocketId;

    public SilentRocketPacket(int rocketId)
    {
        this.rocketId = rocketId;
    }

    public SilentRocketPacket(FriendlyByteBuf buf)
    {
        this.rocketId = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeVarInt(rocketId);
    }

    public static SilentRocketPacket decode(FriendlyByteBuf buf)
    {
        return new SilentRocketPacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        GlideHandler.markSilentRocket(rocketId)));
        context.setPacketHandled(true);
    }
}