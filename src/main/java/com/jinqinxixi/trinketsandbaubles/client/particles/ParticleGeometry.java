package com.jinqinxixi.trinketsandbaubles.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Draws the custom particles: flat billboards and jagged polylines.
 *
 * <p>Vanilla particles are not enough for the effect this mod wants — a fire breath that billboards a
 * sprite strip, and a lightning arc that draws a jagged line — so both are drawn here by hand, against
 * the camera. The shapes are the same two every particle needs: a quad facing the camera (billboard)
 * and a chain of segments (polyline).
 */
@OnlyIn(Dist.CLIENT)
final class ParticleGeometry
{
    static Vec3 jitter(Vec3 point, RandomSource random)
    {
        return point.add(offset(random), offset(random), offset(random));
    }

    private static double offset(RandomSource random)
    {
        return (double) (random.nextInt(4) * (random.nextBoolean() ? -1 : 1)) * 0.1;
    }

    static void polyline(VertexConsumer buffer, Camera camera, List<Vec3> points,
                         float halfWidth, float r, float g, float b, float a)
    {
        Vec3 cam = camera.getPosition();

        for (int i = 0; i < points.size() - 1; i++)
        {
            Vector3f start = relative(points.get(i), cam);
            Vector3f end = relative(points.get(i + 1), cam);
            Vector3f side = new Vector3f(end).sub(start).cross(start);
            if (!(side.lengthSquared() < 1.0E-8F))
            {
                side.normalize().mul(halfWidth);
                vertex(buffer, new Vector3f(start).add(side), r, g, b, a);
                vertex(buffer, new Vector3f(start).sub(side), r, g, b, a);
                vertex(buffer, new Vector3f(end).sub(side), r, g, b, a);
                vertex(buffer, new Vector3f(end).add(side), r, g, b, a);
            }
        }
    }

    static void billboard(VertexConsumer buffer, Camera camera,
                          double x, double y, double z,
                          float halfSize, float u0, float u1, float v0, float v1,
                          float r, float g, float b, float a)
    {
        Vec3 cam = camera.getPosition();
        float px = (float) (x - cam.x);
        float py = (float) (y - cam.y);
        float pz = (float) (z - cam.z);
        Quaternionf rotation = camera.rotation();
        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for (Vector3f corner : corners)
        {
            corner.rotate(rotation).mul(halfSize).add(px, py, pz);
        }

        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(u1, v1).color(r, g, b, a).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(u1, v0).color(r, g, b, a).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(u0, v0).color(r, g, b, a).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(u0, v1).color(r, g, b, a).endVertex();
    }

    private static Vector3f relative(Vec3 point, Vec3 cam)
    {
        return new Vector3f((float) (point.x - cam.x), (float) (point.y - cam.y), (float) (point.z - cam.z));
    }

    private static void vertex(VertexConsumer buffer, Vector3f pos, float r, float g, float b, float a)
    {
        buffer.vertex(pos.x(), pos.y(), pos.z()).color(r, g, b, a).endVertex();
    }

    private ParticleGeometry()
    {
    }
}