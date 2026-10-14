package com.jinqinxixi.trinketsandbaubles.capability.network;

import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * A player's changes to how their race looks, sent from the client that made them.
 *
 * <p>The appearance screen runs on the client and every control on it writes straight into the
 * capability that client holds. That copy is a client's own; the server has its own, and the server's
 * is the one that is saved. Without this packet the two never meet — the colours a player picks are
 * shown until they leave the world and are then gone, because what was written to disk was the
 * server's copy, which nothing had told about the change.
 *
 * <p>一次发整套外观而不是逐字段发送：整套发送更简单、也不会乱序。拖颜色滑块会产生一连串变化，
 * 逐字段的包可能让两个变化落地顺序颠倒。
 *
 * <p>Six numbers and a flag, written in the order the appearance screen's controls appear.
 */
public class UpdateAppearancePacket
{
    private final String raceId;
    private final boolean showTraits;
    private final int traitVariant;
    private final int primaryTraitColor;
    private final int secondaryTraitColor;
    private final int auxTraitColor;
    private final int auxTraitVariant;
    private final int traitColorOption;

    public UpdateAppearancePacket(AbstractRaceCapability race)
    {
        this.raceId = race.getRaceId().toLowerCase();
        this.showTraits = race.showTraits();
        this.traitVariant = race.traitVariant();
        this.primaryTraitColor = race.primaryTraitColor();
        this.secondaryTraitColor = race.secondaryTraitColor();
        this.auxTraitColor = race.auxTraitColor();
        this.auxTraitVariant = race.auxTraitVariant();
        this.traitColorOption = race.traitColorOption();
    }

    public UpdateAppearancePacket(FriendlyByteBuf buf)
    {
        this.raceId = buf.readUtf();
        this.showTraits = buf.readBoolean();
        this.traitVariant = buf.readVarInt();
        this.primaryTraitColor = buf.readInt();
        this.secondaryTraitColor = buf.readInt();
        this.auxTraitColor = buf.readInt();
        this.auxTraitVariant = buf.readVarInt();
        this.traitColorOption = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeUtf(raceId);
        buf.writeBoolean(showTraits);
        buf.writeVarInt(traitVariant);
        buf.writeInt(primaryTraitColor);
        buf.writeInt(secondaryTraitColor);
        buf.writeInt(auxTraitColor);
        buf.writeVarInt(auxTraitVariant);
        buf.writeVarInt(traitColorOption);
    }

    /**
     * Rebuilds the packet from the wire.
     *
     * <p>A named method rather than the constructor itself, which is what the channel's registration
     * expects: it takes a factory that reads a buffer, and a constructor reference cannot be handed
     * over without also picking which constructor is meant.
     */
    public static UpdateAppearancePacket decode(FriendlyByteBuf buf)
    {
        return new UpdateAppearancePacket(buf);
    }

    /**
     * Writes the change onto the server's copy and sends it back out.
     *
     * <p>The reply is what makes the two sides agree: the client that asked is told what the server
     * now holds, which is the same value, and so is every other client that can see the player. A
     * change that was never echoed would leave the sender's own copy and the server's in step by
     * luck.
     */
    public void handle(Supplier<NetworkEvent.Context> ctx)
    {
        NetworkEvent.Context context = ctx.get();
        ServerPlayer sender = context.getSender();

        if (sender != null)
        {
            context.enqueueWork(() ->
            {
                var capability = ModCapabilities.RACE_CAPABILITIES.get(raceId);

                if (capability == null)
                {
                    // A race this server does not have. Left alone rather than treated as an error:
                    // a client running a newer version may name one.
                    return;
                }

                sender.getCapability(capability).ifPresent(present ->
                {
                    if (present instanceof AbstractRaceCapability race)
                    {
                        // Written without going through the setters, which each sync back to the
                        // client — on the server that is the right thing and here it would answer a
                        // packet with the packet it is already answering.
                        race.applyAppearance(showTraits, traitVariant, primaryTraitColor,
                                secondaryTraitColor, auxTraitColor, auxTraitVariant, traitColorOption);

                        RaceCapabilityNetworking.sendToPlayer(race, sender);
                    }
                });
            });
        }

        context.setPacketHandled(true);
    }
}