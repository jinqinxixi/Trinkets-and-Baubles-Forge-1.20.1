package com.jinqinxixi.trinketsandbaubles.mixin;

import com.jinqinxixi.trinketsandbaubles.capability.glide.GlideHandler;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mutes the launch sound and spark trail of a rocket a glide's push spawned.
 *
 * <p>The push is a firework rocket spawned on the player, so the acceleration is the game's own — but
 * a rocket also plays a launch sound and trails sparks, and neither belongs to a pair of wings. Both
 * calls are answered here: a rocket this mod spawned gets no sound and no sparks, while every rocket a
 * player actually lights is left alone.
 *
 * <p>Both redirects are {@code require = 0} so that a game which moves either call costs only the
 * mute, never the whole mod.
 */
@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketMixin
{
    /** Cached the first time it is asked, so the record may be forgotten without losing the answer. */
    @Unique
    private boolean pocketnetwork$silent;

    @Unique
    private boolean pocketnetwork$silentKnown;

    /**
     * Whether this rocket was spawned by a glide push.
     *
     * <p>The answer is looked up by id on the first tick, then remembered on the rocket itself. The
     * remember matters: when the rocket explodes, the handler forgets its id, and the entity event that
     * spawns the smoke puff can arrive after that forgetting — asking the handler again would already
     * say "no" and the puff would be let through.
     */
    private boolean isSilent()
    {
        if (!pocketnetwork$silentKnown)
        {
            pocketnetwork$silentKnown = true;
            pocketnetwork$silent = GlideHandler.isSilentRocket(((Entity) (Object) this).getId());
        }

        return pocketnetwork$silent;
    }
    /**
     * Suppresses the launch sound for a glide's boost rocket.
     *
     * <p>Redirects the play-sound call the rocket makes on its first tick, replacing it with nothing
     * when the rocket is one this mod made.
     */
    @Redirect(
            method = "tick",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
            )
    )
    private void pocketnetwork$muteSound(Level level, net.minecraft.world.entity.player.Player player,
            double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch)
    {
        if (!isSilent())
        {
            level.playSound(player, x, y, z, sound, source, volume, pitch);
        }
    }

    /**
     * Suppresses the spark trail for a glide's boost rocket.
     */
    @Redirect(
            method = "tick",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void pocketnetwork$muteParticles(Level level, ParticleOptions particle,
            double x, double y, double z, double dx, double dy, double dz)
    {
        boolean silent = isSilent();
        if (!silent)
        {
            level.addParticle(particle, x, y, z, dx, dy, dz);
        }
    }

    /**
     * Suppresses the smoke puff a glide's boost rocket leaves when it is spent.
     *
     * <p>An empty rocket, when its time is up, fires entity event {@code 17}, which spawns a few
     * {@code POOF} smoke particles. Cancelling the whole event for a silent rocket removes the puff,
     * and the event does nothing else a glide's boost needs — the momentum has already been given.
     *
     * <p>The method is named with its descriptor because the entity has two overloads of
     * {@code handleEntityEvent} and the unqualified name would match the wrong one, leaving this
     * silently un-injected.
     */
    @Inject(
            method = "handleEntityEvent(B)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void pocketnetwork$cancelPuff(byte event, CallbackInfo ci)
    {
        if (event == 17)
        {
            if (isSilent())
            {
                ci.cancel();
            }
        }
    }

    /**
     * Cuts a glide push's boost to a third.
     *
     * <p>The game's firework boost pulls the player's speed toward one and a half times their looking
     * direction. A pair of wings should push less than a lit rocket, so the same arithmetic is kept and
     * only the target is reduced to a third of it — toward half the looking direction instead of one
     * and a half times it.
     *
     * <p>Only the rocket a glide spawned is scaled; a rocket a player lights keeps the game's full
     * strength. The {@code 1.5} constant is the boost target's multiplier, and it appears exactly
     * three times — once per axis — in the boost arithmetic.
     */
    @ModifyConstant(
            method = "tick",
            require = 0,
            constant = @Constant(doubleValue = 1.5D)
    )
    private double pocketnetwork$scaleBoost(double original)
    {
        return isSilent() ? 0.5D : original;
    }

    /**
     * Forgets a rocket when it explodes, so the record does not grow forever.
     *
     * <p>Every path that ends a rocket — its lifetime expiring, hitting something — goes through
     * {@code explode}, which is why the record is cleared here rather than at each end separately. A
     * rocket id left behind would be reused by the game for another entity and could mute it wrongly.
     */
    @Inject(method = "explode", at = @At("HEAD"), require = 0)
    private void pocketnetwork$forget(CallbackInfo ci)
    {
        GlideHandler.forgetRocket(((Entity) (Object) this).getId());
    }
}
