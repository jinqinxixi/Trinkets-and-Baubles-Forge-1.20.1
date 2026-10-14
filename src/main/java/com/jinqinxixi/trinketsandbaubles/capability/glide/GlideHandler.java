package com.jinqinxixi.trinketsandbaubles.capability.glide;

import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.impl.FairyCapability;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Gliding, and the push a player can pay for.
 *
 * <p>The gliding itself is not here. It is the game's: a player whose race glides is given the
 * gliding flag by a mixin on the client, and from that point the game's own dive, steering, animation
 * and camera handling all apply. Reinventing any of it here would be a second implementation of
 * something that already works, and a second set of numbers to keep in step.
 *
 * <p>What is here is the push — the one thing gliding cannot do on its own — and the bookkeeping
 * around it. A push costs mana and is aimed along the player's line of sight, which is what a firework
 * does and what makes it useful: looking down dives, looking up climbs.
 */
public final class GlideHandler
{
    /** How much mana one push costs, and how many ticks between pushes. */
    private static final float PUSH_MANA_COST = 20.0F;
    private static final int PUSH_COOLDOWN_TICKS = 10;

    /** Who has asked for a push, and how long they have left to wait. */
    private static final java.util.Map<java.util.UUID, Integer> PUSH_COOLDOWN =
            new java.util.concurrent.ConcurrentHashMap<>();

    /** Who is gliding, as the server knows it. */
    private static final java.util.Set<java.util.UUID> GLIDING =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** Rockets spawned by a glide push, so their sound and sparks can be muted. */
    private static final java.util.Set<Integer> SILENT_ROCKETS =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    private GlideHandler()
    {
    }

    /**
     * Whether this player's race glides.
     *
     * <p>The two races with wings, which are the two that can glide. Read from the
     * capabilities rather than from a name, so a race that gains flight later needs no change here.
     */
    public static boolean hasGlidingRace(Player player)
    {
        if (player == null)
        {
            return false;
        }

        var dragon = player.getCapability(ModCapabilities.DRAGON_CAPABILITY).resolve().orElse(null);

        if (dragon instanceof DragonCapability capability && capability.isActive())
        {
            return true;
        }

        var fairy = player.getCapability(ModCapabilities.FAIRY_CAPABILITY).resolve().orElse(null);

        return fairy instanceof FairyCapability capability && capability.isActive();
    }

    /**
     * Whether this player is somewhere a glide ends.
     *
     * <p>The game's own conditions, in one place because two injections ask them and both have to
     * answer the same way: the one that starts a glide, and the one that decides whether to keep one
     * going. Two copies would be two answers to one question.
     *
     * <p>Being on the ground is the first of them, and it is what stops the gliding that would
     * otherwise go on while a player holds forward across the ground.
     *
     * <p>Riding, water, lava and a levitation effect are the others, all of which the game itself
     * refuses a glide in.
     */
    public static boolean blocked(Player player)
    {
        return player == null
                || player.onGround()
                || player.isPassenger()
                || player.isInWater()
                || player.isInLava()
                || player.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
    }

    /** Whether this player is gliding, as the game sees it. */
    public static boolean isGliding(Player player)
    {
        return player != null && player.isFallFlying();
    }

    /**
     * Marks a player as gliding on the server, and sets the flag.
     *
     * <p>Called when the start packet arrives. The flag is set here and then held up every tick by
     * {@link #tickGliding(ServerPlayer)}, because the game clears it on its own — it looks at the
     * player's chest, sees no elytra, and writes false. A record of who is gliding is what survives
     * that clearing and what the tick writes back.
     */
    public static void startGliding(ServerPlayer player)
    {
        if (player == null)
        {
            return;
        }

        GLIDING.add(player.getUUID());
        player.startFallFlying();
    }

    /**
     * Ends a glide on the server, and clears the record.
     */
    public static void stopGliding(ServerPlayer player)
    {
        if (player == null)
        {
            return;
        }

        GLIDING.remove(player.getUUID());
        player.stopFallFlying();
    }

    /**
     * Holds a glide up every tick.
     *
     * <p>Called from the server tick. For each gliding player the flag is set again, which is the part
     * the game itself would undo — it clears the flag every tick because there is no elytra, and this
     * puts it back. The glide ends here too, on the game's own conditions: landing, entering water or
     * lava, riding, or losing the race.
     */
    public static void tickGliding(ServerPlayer player)
    {
        if (!GLIDING.contains(player.getUUID()))
        {
            return;
        }

        if (blocked(player) || !hasGlidingRace(player))
        {
            stopGliding(player);
            return;
        }

        if (!player.isFallFlying())
        {
            player.startFallFlying();
        }
    }

    /**
     * Gives the glide a push, paid for in mana.
     *
     * <p>Called on the server when a request arrives. The mana is spent here rather than on the
     * client, because mana is the server's.
     *
     * <p>The push itself is a firework rocket spawned on the player, which is the game's own way of
     * boosting a glide: the rocket attaches to the player and its tick does the accelerating, exactly
     * as if the player had used one by hand. Not writing the velocity here means not reimplementing
     * what the game already does, and getting the same feel for free.
     */
    public static void push(ServerPlayer player)
    {
        if (player == null || !player.isFallFlying())
        {
            return;
        }

        Integer cooldown = PUSH_COOLDOWN.get(player.getUUID());

        if (cooldown != null && cooldown > 0)
        {
            return;
        }

        if (!ManaData.hasMana(player, PUSH_MANA_COST))
        {
            player.displayClientMessage(
                    Component.translatable("message.trinketsandbaubles.glide.no_mana"), true);
            return;
        }

        ManaData.consumeMana(player, PUSH_MANA_COST);

        PUSH_COOLDOWN.put(player.getUUID(), PUSH_COOLDOWN_TICKS);

        // The boost is a firework rocket with no flight duration, attached to the player. Its own
        // tick pulls the player along, which is the game's firework-boost behaviour.
        net.minecraft.world.entity.projectile.FireworkRocketEntity rocket =
                new net.minecraft.world.entity.projectile.FireworkRocketEntity(
                        player.level(),
                        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FIREWORK_ROCKET),
                        player);

        // Added to the world first, because an entity's id is assigned when it joins the world — before
        // that it holds a placeholder, and recording the placeholder would record a rocket that never
        // matches the one now flying.
        player.level().addFreshEntity(rocket);

        // Record the real id, so the mixin can mute its launch sound and spark trail — a glide's push
        // should not sound like a rocket being lit.
        SILENT_ROCKETS.add(rocket.getId());

        // 同步给所有追踪该玩家的客户端：火箭声音/粒子在客户端播放，专用服务器下客户端自己的
        // SILENT_ROCKETS 是空的，必须把 id 发过去，客户端才能静音。
        com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new com.jinqinxixi.trinketsandbaubles.network.message.SilentRocketPacket(rocket.getId())
        );
    }

    /**
     * Whether a rocket is one a glide push spawned, and so should be silent.
     *
     * <p>Asked by the rocket's own mixin. The id is used rather than the entity because the mixin runs
     * while the rocket is being ticked, and comparing ids avoids touching the entity from a side it is
     * not ready to be touched from.
     */
    public static boolean isSilentRocket(int id)
    {
        return SILENT_ROCKETS.contains(id);
    }

    /**
     * 客户端收到同步包后，把这个火箭 id 加入本地集合。
     *
     * <p>滑翔推进的静音火箭记录在服务端内存里，但火箭声音/粒子在客户端播放。专用服务器下客户端
     * 自己的集合是空的，所以要让服务端在发射时把 id 同步过来（见 SilentRocketPacket），客户端才能
     * 像单人那样静音。
     */
    public static void markSilentRocket(int id)
    {
        SILENT_ROCKETS.add(id);
    }

    /** Forgets a rocket once it is gone, so the record does not grow forever. */
    public static void forgetRocket(int id)
    {
        SILENT_ROCKETS.remove(id);
    }

    /** Counts the push cooldowns down, once a tick. */
    public static void tickCooldowns()
    {
        PUSH_COOLDOWN.replaceAll((id, ticks) -> ticks - 1);
        PUSH_COOLDOWN.values().removeIf(ticks -> ticks <= 0);
    }

    /** Forgets a player, for a logout. */
    public static void forget(Player player)
    {
        if (player != null)
        {
            PUSH_COOLDOWN.remove(player.getUUID());
        }
    }
}