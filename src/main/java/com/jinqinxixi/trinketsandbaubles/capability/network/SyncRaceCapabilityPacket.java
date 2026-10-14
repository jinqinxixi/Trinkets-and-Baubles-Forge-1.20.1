package com.jinqinxixi.trinketsandbaubles.capability.network;

import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.api.IRaceAppearance;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Everything the client needs to know about one player's race.
 *
 * <p>Carries the appearance as well as the state, because the two are read by different sides at
 * different times and neither can ask the other. The state decides what the race does — its
 * attributes, its scale — and is read on the server. The appearance decides what it looks like and
 * is read while drawing a frame, which happens on the client and cannot reach the server.
 *
 * <p>One packet rather than two, because they change together: a player turning their features off
 * has changed one thing about one race, and two packets could arrive in either order.
 */
public class SyncRaceCapabilityPacket {
    private final int playerId;
    private final String raceId;
    private final boolean isActive;
    private final float scaleFactor;

    /**
     * The appearance, or the defaults when the capability does not describe one.
     *
     * <p>Read through the interface rather than from the concrete class, so that a capability that
     * does not implement it still syncs — it simply reports the same defaults a client starts with,
     * which is the honest description of a race that has no appearance of its own.
     */
    private final boolean showTraits;
    private final int traitVariant;
    private final int primaryTraitColor;
    private final int secondaryTraitColor;
    private final int auxTraitColor;
    private final int auxTraitVariant;
    private final int traitColorOption;

    public SyncRaceCapabilityPacket(int playerId, IBaseRaceCapability capability) {
        this.playerId = playerId;
        this.raceId = capability.getRaceId();
        this.isActive = capability.isActive();
        this.scaleFactor = capability.getScaleFactor();

        if (capability instanceof IRaceAppearance appearance) {
            this.showTraits = appearance.showTraits();
            this.traitVariant = appearance.traitVariant();
            this.primaryTraitColor = appearance.primaryTraitColor();
            this.secondaryTraitColor = appearance.secondaryTraitColor();
            this.auxTraitColor = appearance.auxTraitColor();
            this.auxTraitVariant = appearance.auxTraitVariant();
            this.traitColorOption = appearance.traitColorOption();
        } else {
            this.showTraits = true;
            this.traitVariant = 0;
            this.primaryTraitColor = 0xFFFFFF;
            this.secondaryTraitColor = 0xFFFFFF;
            this.auxTraitColor = 0xFFFFFF;
            this.auxTraitVariant = 0;
            this.traitColorOption = 0;
        }
    }

    public SyncRaceCapabilityPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readVarInt();
        this.raceId = buf.readUtf();
        this.isActive = buf.readBoolean();
        this.scaleFactor = buf.readFloat();
        this.showTraits = buf.readBoolean();
        this.traitVariant = buf.readVarInt();
        this.primaryTraitColor = buf.readInt();
        this.secondaryTraitColor = buf.readInt();
        this.auxTraitColor = buf.readInt();
        this.auxTraitVariant = buf.readVarInt();
        this.traitColorOption = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(playerId);
        buf.writeUtf(raceId);
        buf.writeBoolean(isActive);
        buf.writeFloat(scaleFactor);
        buf.writeBoolean(showTraits);
        buf.writeVarInt(traitVariant);
        buf.writeInt(primaryTraitColor);
        buf.writeInt(secondaryTraitColor);
        buf.writeInt(auxTraitColor);
        buf.writeVarInt(auxTraitVariant);
        buf.writeVarInt(traitColorOption);
    }

    public static SyncRaceCapabilityPacket decode(FriendlyByteBuf buf) {
        return new SyncRaceCapabilityPacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 确保我们在客户端处理这个数据包
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandler.handlePacket(this);
            });
        });
        ctx.get().setPacketHandled(true);
    }

    // Getters
    public int getPlayerId() { return playerId; }
    public String getRaceId() { return raceId; }
    public boolean isActive() { return isActive; }
    public float getScaleFactor() { return scaleFactor; }
    public boolean isShowTraits() { return showTraits; }
    public int getTraitVariant() { return traitVariant; }
    public int getPrimaryTraitColor() { return primaryTraitColor; }
    public int getSecondaryTraitColor() { return secondaryTraitColor; }
    public int getAuxTraitColor() { return auxTraitColor; }
    public int getAuxTraitVariant() { return auxTraitVariant; }
    public int getTraitColorOption() { return traitColorOption; }
}