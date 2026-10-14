package com.jinqinxixi.trinketsandbaubles.client.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Plays one of the mod's effects on the client, keyed by an id sent from the server. Particles that
 * are custom (lightning, fire breath, greed glow) are added straight to the particle engine; a couple
 * just reuse a vanilla particle.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientEffects
{
    public static void play(int effectID, double x, double y, double z,
                            double x2, double y2, double z2, int color, float alpha, float intensity)
    {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level != null)
        {
            switch (effectID)
            {
                case 1 ->
                {
                    mc.particleEngine.add(new LightningParticle(level, x, y, z, x2, y2, z2, color, alpha, intensity));
                    playImpactSound(level, x, y, z);
                }
                case 2 ->
                {
                    LightningOrbParticle.spawn(level, x, y, z, color, alpha);
                    playImpactSound(level, x, y, z);
                }
                case 3 -> level.addParticle(ParticleTypes.SWEEP_ATTACK, x, y, z, 0.0, 0.0, 0.0);
                case 4 -> mc.particleEngine.add(new FireBreathParticle(level, x, y, z, 0.0, 0.0, 0.0, color, alpha));
                case 5 ->
                {
                    level.addParticle(ParticleTypes.EXPLOSION, x, y, z, 0.0, 0.0, 0.0);
                    level.playLocalSound(x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                            4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, true);
                }
                case 6 -> mc.particleEngine.add(new GreedParticle(level, x + level.random.nextDouble(), y + level.random.nextDouble(), z + level.random.nextDouble(), color));
                case 7 -> LightningOrbParticle.spawn(level, x, y, z, color, alpha);
            }
        }
    }

    public static void breathWisp(double x, double y, double z, int color)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null)
        {
            mc.particleEngine.add(new BreathWispParticle(mc.level, x, y, z, color));
        }
    }

    private static void playImpactSound(ClientLevel level, double x, double y, double z)
    {
        level.playLocalSound(x, y, z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 0.2F, 0.6F, true);
    }

    private ClientEffects()
    {
    }
}