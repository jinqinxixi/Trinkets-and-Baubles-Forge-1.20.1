package com.jinqinxixi.trinketsandbaubles.network.message.Messages;

import com.jinqinxixi.trinketsandbaubles.capability.glide.GlideHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * A glide starting, and a push being asked for.
 *
 * <p>Sent from the client, because both are things a player does — and both have to be acted on here,
 * which is the part that is easy to get wrong. A glide is part of the entity's state and the server
 * owns that state, so a client that starts gliding on its own is a client the server corrects on the
 * next tick. The message is how the server is told, and the handling below is what makes the
 * correction go the other way.
 *
 * <p>处理只有两个动作：记录滑翔状态、或执行一次推进。
 */
public class GlideMessage
{
    /** True when this is a request to push rather than the news that a glide has started. */
    private final boolean push;

    public GlideMessage(boolean push)
    {
        this.push = push;
    }

    public static GlideMessage decode(FriendlyByteBuf buf)
    {
        return new GlideMessage(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeBoolean(push);
    }

    public static void handle(GlideMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            ServerPlayer sender = context.getSender();

            if (sender == null)
            {
                return;
            }

            if (message.push)
            {
                GlideHandler.push(sender);
                return;
            }

            if (!sender.onGround()
                    && !sender.isInWater()
                    && !sender.hasEffect(MobEffects.LEVITATION)
                    && sender.getDeltaMovement().y < 0.0
                    && GlideHandler.hasGlidingRace(sender))
            {
                GlideHandler.startGliding(sender);
            }
        });

        context.setPacketHandled(true);
    }
}


