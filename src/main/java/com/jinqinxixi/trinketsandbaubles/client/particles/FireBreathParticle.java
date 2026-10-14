package com.jinqinxixi.trinketsandbaubles.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The dragon's fire breath: a sprite-strip flame that billboards towards the camera, shrinking as it
 * ages. Drawn from the dragon_breath texture's two rows of sixteen frames.
 */
@OnlyIn(Dist.CLIENT)
public class FireBreathParticle extends Particle
{
    private static final float TILE_U = 0.0625F;
    private static final float TILE_V = 0.5F;
    private final float baseScale;

    public FireBreathParticle(ClientLevel level, double x, double y, double z,
                              double xSpeed, double ySpeed, double zSpeed, int color, float scale)
    {
        super(level, x, y, z);
        this.xd = this.xd * 0.01 + xSpeed;
        this.yd = this.yd * 0.01 + ySpeed;
        this.zd = this.zd * 0.01 + zSpeed;
        this.x = this.x + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.y = this.y + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.z = this.z + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.rCol = (float) (color >> 16 & 0xFF) / 255.0F;
        this.gCol = (float) (color >> 8 & 0xFF) / 255.0F;
        this.bCol = (float) (color & 0xFF) / 255.0F;
        this.alpha = 1.0F;
        this.baseScale = scale;
        this.lifetime = 31;
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
        else
        {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.96;
            this.yd *= 0.96;
            this.zd *= 0.96;
            if (this.onGround)
            {
                this.xd *= 0.7;
                this.zd *= 0.7;
            }
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        float progress = ((float) this.age + partialTicks) / (float) this.lifetime;
        int frame = (int) (((float) this.age + partialTicks) * 30.0F / (float) this.lifetime);
        if (frame <= this.lifetime - 1)
        {
            float scale = this.baseScale * (1.0F - progress * progress * 0.5F);
            int column = frame < 16 ? frame : frame - 15;
            float v0 = frame < 16 ? 0.5F : 0.0F;
            float u0 = (float) column * 0.0625F;
            double px = this.xo + (this.x - this.xo) * (double) partialTicks;
            double py = this.yo + (this.y - this.yo) * (double) partialTicks;
            double pz = this.zo + (this.z - this.zo) * (double) partialTicks;
            ParticleGeometry.billboard(buffer, camera, px, py, pz, scale,
                    u0, u0 + 0.0625F, v0, v0 + 0.5F, this.rCol, this.gCol, this.bCol, this.alpha);
        }
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ModParticleRenderTypes.FIRE_BREATH;
    }
}