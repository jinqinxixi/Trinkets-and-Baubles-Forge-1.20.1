package com.jinqinxixi.trinketsandbaubles.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A soft glow that shrinks through its sixteen frames, used around the greed eye and the lightning
 * orb.
 */
@OnlyIn(Dist.CLIENT)
public class GreedParticle extends Particle
{
    private static final int FRAMES = 16;
    private final float halfSize;
    private final boolean depthTest;

    public GreedParticle(ClientLevel level, double x, double y, double z,
                         int color, float alpha, float halfSize, boolean depthTest)
    {
        super(level, x, y, z);
        this.rCol = (float) (color >> 16 & 0xFF) / 255.0F;
        this.gCol = (float) (color >> 8 & 0xFF) / 255.0F;
        this.bCol = (float) (color & 0xFF) / 255.0F;
        this.alpha = alpha;
        this.halfSize = halfSize;
        this.depthTest = depthTest;
        this.lifetime = 16;
        this.hasPhysics = false;
    }

    public GreedParticle(ClientLevel level, double x, double y, double z, int color)
    {
        this(level, x, y, z, color, 1.0F, 0.25F, false);
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
        int frame = (int) (((float) this.age + partialTicks) * 15.0F / (float) this.lifetime);
        if (frame < 16)
        {
            float u0 = (float) frame / 16.0F;
            ParticleGeometry.billboard(buffer, camera, this.x, this.y, this.z, this.halfSize,
                    u0, u0 + 0.0625F, 0.0F, 1.0F, this.rCol, this.gCol, this.bCol, this.alpha);
        }
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return this.depthTest ? ModParticleRenderTypes.GREED_DEPTH : ModParticleRenderTypes.GREED;
    }
}