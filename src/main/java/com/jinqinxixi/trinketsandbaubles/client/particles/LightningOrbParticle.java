package com.jinqinxixi.trinketsandbaubles.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * A crackling ball of lightning, drawn as two jittered polylines (core + glow) with a greed glow
 * particle riding on top.
 */
@OnlyIn(Dist.CLIENT)
public class LightningOrbParticle extends Particle
{
    private static final float CORE_HALF_WIDTH = 0.02F;
    private static final float GLOW_HALF_WIDTH = 0.08F;

    public LightningOrbParticle(ClientLevel level, double x, double y, double z, int color, float alpha)
    {
        super(level, x, y, z);
        this.rCol = (float) (color >> 16 & 0xFF) / 255.0F;
        this.gCol = (float) (color >> 8 & 0xFF) / 255.0F;
        this.bCol = (float) (color & 0xFF) / 255.0F;
        this.alpha = alpha;
        this.lifetime = 16;
        this.hasPhysics = false;
    }

    public static void spawn(ClientLevel level, double x, double y, double z, int color, float alpha)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.particleEngine.add(new LightningOrbParticle(level, x, y, z, color, alpha));
        mc.particleEngine.add(new GreedParticle(level, x, y + 0.2, z, color, alpha, 0.5F, true));
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
        List<Vec3> points = new ArrayList<>(this.lifetime);
        Vec3 point = new Vec3(this.x, this.y + 0.2, this.z);
        points.add(point);

        for (int i = 1; i < this.lifetime; i++)
        {
            point = ParticleGeometry.jitter(point, this.random);
            points.add(point);
        }

        ParticleGeometry.polyline(buffer, camera, points, CORE_HALF_WIDTH,
                this.rCol, this.gCol, this.bCol, this.alpha);
        ParticleGeometry.polyline(buffer, camera, points, GLOW_HALF_WIDTH,
                this.rCol, this.gCol, this.bCol, this.alpha * 0.5F);
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ModParticleRenderTypes.TRANSLUCENT_COLOR;
    }
}