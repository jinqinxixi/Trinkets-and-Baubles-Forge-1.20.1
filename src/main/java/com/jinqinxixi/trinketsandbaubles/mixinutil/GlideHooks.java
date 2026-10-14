package com.jinqinxixi.trinketsandbaubles.mixinutil;

import com.jinqinxixi.trinketsandbaubles.capability.glide.GlideHandler;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * The questions gliding asks, answered for a player whose race glides.
 *
 * <p>无鞘翅滑翔的核心问题：给种族滑翔能力时，每一 tick 都要回答游戏对「滑翔是否继续」的判定。
 *
 * <p>Two values are asked for and both are needed. The old one is the flag as it stood before this
 * tick, which is what "is a glide under way" actually means; the new one is what the game concluded
 * from the player's chest, which without an elytra is always false. Keeping a glide going means
 * answering from the old value, and the conditions below are what stop it.
 */
public class GlideHooks
{
    private GlideHooks()
    {
    }

    /**
     * Whether a glide continues this tick.
     *
     * @param entity  the player, whose state decides it
     * @param oldFlag whether a glide was under way before this tick
     * @param newFlag what the game concluded from what is on their chest
     * @return what to write into the gliding flag
     */
    public static boolean canFly(LivingEntity entity, boolean oldFlag, boolean newFlag)
    {
        boolean hasRace = GlideHandler.hasGlidingRace(entity instanceof Player player ? player : null);

        return decide(entity, oldFlag, newFlag, hasRace);
    }

    private static boolean decide(LivingEntity entity, boolean oldFlag, boolean newFlag, boolean hasRace)
    {
        // No race, no glide. Checked first, so losing the race ends the glide on the same tick.
        if (!hasRace)
        {
            return newFlag;
        }

        // A glide that has not started is left to the game.
        if (!oldFlag)
        {
            return newFlag;
        }

        // The conditions the game itself ends a glide on. Being on the ground is the first, and it is
        // what stops a player who has glided down and kept walking from staying in the pose.
        if (entity.onGround() || entity.isPassenger() || entity.hasEffect(MobEffects.LEVITATION))
        {
            return false;
        }

        return true;
    }

    /**
     * Starts a glide, for a player who is falling and may glide.
     *
     * <p>Every condition is the game's, and the entry point is the game's own — the one its elytra code
     * calls. Nothing is set by hand, which is what keeps the pose, the animation and the camera
     * handling working without a line written for them here.
     */
    public static boolean startFlight(Player player)
    {
        if (!player.onGround()
                && !player.isFallFlying()
                && !player.isInWater()
                && !player.hasEffect(MobEffects.LEVITATION)
                && player.getDeltaMovement().y < 0.0
                && GlideHandler.hasGlidingRace(player))
        {
            // 不在这里本地 startFallFlying：那样会让客户端自己先进入滑翔姿势，而服务端可能因
            // 已落地而拒绝请求，造成「客户端卡在滑翔、服务端却是地面」的错位（地上蠕动）。
            // 只上报请求，滑翔与否由服务端 startGliding 决定并通过同步回传。
            return true;
        }

        return false;
    }
}

