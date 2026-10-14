package com.jinqinxixi.trinketsandbaubles.network.message.Messages;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Tells nearby clients to play one of the mod's custom effects at a position. The server decides the
 * effect (it knows the fight), and the client draws it — the custom particles only exist client-side.
 */
public class EffectsRenderPacket
{
    public static final int LIGHTNING = 1;
    public static final int LIGHTNING_ORB = 2;
    public static final int SWEEP = 3;
    public static final int FIRE_BREATH = 4;
    public static final int EXPLOSION = 5;
    public static final int GREED = 6;
    public static final int LIGHTNING_ORB_SILENT = 7;

    private static final double SEND_RADIUS = 64.0;

    private final int effectID;
    private final int color;
    private final double x;
    private final double y;
    private final double z;
    private final double x2;
    private final double y2;
    private final double z2;
    private final float alpha;
    private final float intensity;

    public EffectsRenderPacket(int effectID, double x, double y, double z,
                               double x2, double y2, double z2, int color, float alpha, float intensity)
    {
        this.effectID = effectID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.color = color;
        this.alpha = alpha;
        this.intensity = intensity;
    }

    public EffectsRenderPacket(FriendlyByteBuf buf)
    {
        this.effectID = buf.readInt();
        this.color = buf.readInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.x2 = buf.readDouble();
        this.y2 = buf.readDouble();
        this.z2 = buf.readDouble();
        this.alpha = buf.readFloat();
        this.intensity = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(this.effectID);
        buf.writeInt(this.color);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeDouble(this.x2);
        buf.writeDouble(this.y2);
        buf.writeDouble(this.z2);
        buf.writeFloat(this.alpha);
        buf.writeFloat(this.intensity);
    }

    /** Broadcast to every player near the effect, so only those who could see it pay for it. */
    public void sendNear(ServerLevel level)
    {
        if (!level.isClientSide)
        {
            com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler.INSTANCE.send(
                    PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(x, y, z, SEND_RADIUS, level.dimension())),
                    this);
        }
    }

    public void sendNear(Entity entity)
    {
        if (!entity.level().isClientSide && entity.level() instanceof ServerLevel level)
        {
            sendNear(level);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                com.jinqinxixi.trinketsandbaubles.client.particles.ClientEffects.play(
                        this.effectID, this.x, this.y, this.z,
                        this.x2, this.y2, this.z2, this.color, this.alpha, this.intensity));
        context.setPacketHandled(true);
    }
}