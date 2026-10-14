package com.jinqinxixi.trinketsandbaubles.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A fading wisp at a dragon-breath impact point, drawn near the mouth as the breath lingers.
 */
@OnlyIn(Dist.CLIENT)
public class BreathWispParticle extends Particle
{
    private static final int START_FRAME = 16;
    private static final int LIFETIME = 15;
    private static final float HALF_SIZE = 0.375F;

    public BreathWispParticle(ClientLevel level, double x, double y, double z, int color)
    {
        super(level, x, y, z);
        this.rCol = (float) (color >> 16 & 0xFF) / 255.0F;
        this.gCol = (float) (color >> 8 & 0xFF) / 255.0F;
        this.bCol = (float) (color & 0xFF) / 255.0F;
        this.lifetime = 15;
        this.hasPhysics = false;
    }

    @Override
    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime)
        {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        float age = (float) this.age + partialTicks;
        int frame = 16 + Math.min(14, (int) (age * 15.0F / 15.0F));
        float alpha = Math.max(0.0F, 1.0F - age / 15.0F);
        float u0 = (float) (frame % 16) / 16.0F;
        ParticleGeometry.billboard(buffer, camera, this.x, this.y, this.z, HALF_SIZE,
                u0, u0 + 0.0625F, 0.0F, 0.5F, this.rCol, this.gCol, this.bCol, alpha);
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ModParticleRenderTypes.FIRE_BREATH;
    }
}