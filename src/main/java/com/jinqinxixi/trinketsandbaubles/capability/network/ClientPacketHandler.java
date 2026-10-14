package com.jinqinxixi.trinketsandbaubles.capability.network;

import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {

    /**
     * Writes what the server just said onto this client's copy of the player's race.
     *
     * <p>Both halves of the packet are applied, because the packet is one description of one race
     * and a race that is in the right state but drawn the old way is a race that changed halfway.
     * The appearance in particular has no other route to the client: it is read while drawing a
     * frame, and drawing happens here.
     */
    @OnlyIn(Dist.CLIENT)
    public static void handlePacket(SyncRaceCapabilityPacket packet) {
        // 用 playerId 从客户端世界找到「数据所属的那个玩家」，把种族数据应用到他身上。不能固定
        // 取本地玩家——多人时别的玩家的种族数据要应用到别的玩家，否则
        // 大家都看不到彼此的角/翅/体型。
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (mc.level.getEntity(packet.getPlayerId()) instanceof Player player) {
            apply(player, packet);
        }
    }

    /**
     * The lookup table, spelled as what it is: one id per capability.
     *
     * <p>A chain of comparisons rather than a map, because the values are ordinary fields of
     * different generic types and a map holding them would have to hold them as something wider
     * than they are. Every branch does the same thing, which is what makes the repetition safe:
     * adding a race means adding a branch, and a race left out of this table is a race that syncs
     * nothing.
     */
    private static void apply(Player player, SyncRaceCapabilityPacket packet) {
        switch (packet.getRaceId()) {
            case "dwarves" -> applyTo(player, ModCapabilities.DWARVES_CAPABILITY, packet);
            case "elves" -> applyTo(player, ModCapabilities.ELVES_CAPABILITY, packet);
            case "faeles" -> applyTo(player, ModCapabilities.FAELES_CAPABILITY, packet);
            case "titan" -> applyTo(player, ModCapabilities.TITAN_CAPABILITY, packet);
            case "dragon" -> applyTo(player, ModCapabilities.DRAGON_CAPABILITY, packet);
            case "fairy" -> applyTo(player, ModCapabilities.FAIRY_CAPABILITY, packet);
            case "goblins" -> applyTo(player, ModCapabilities.GOBLINS_CAPABILITY, packet);
            case "taurus" -> applyTo(player, ModCapabilities.TAURUS_CAPABILITY, packet);
            default -> {
                // A race this client has never heard of. Left alone rather than treated as an
                // error: a server running a newer version may name one, and a client with no
                // features for a race it does not have is the honest outcome.
            }
        }
    }

    /**
     * Writes one packet onto one race.
     *
     * <p>Generic over the capability's own interface so that each field keeps the type it was
     * declared with, and the cast to the shared base happens once here rather than at every call.
     */
    private static <T extends IBaseRaceCapability> void applyTo(
            Player player, Capability<T> capability, SyncRaceCapabilityPacket packet) {
        player.getCapability(capability).ifPresent(cap -> {
            if (cap instanceof AbstractRaceCapability race) {
                race.updateStateOnly(packet.isActive(), packet.getScaleFactor());
                race.updateAppearance(
                        packet.isShowTraits(),
                        packet.getTraitVariant(),
                        packet.getPrimaryTraitColor(),
                        packet.getSecondaryTraitColor(),
                        packet.getAuxTraitColor(),
                        packet.getAuxTraitVariant(),
                        packet.getTraitColorOption());
            }
        });
    }
}