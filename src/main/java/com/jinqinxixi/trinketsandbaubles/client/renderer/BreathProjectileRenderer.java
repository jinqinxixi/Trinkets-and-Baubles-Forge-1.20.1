package com.jinqinxixi.trinketsandbaubles.client.renderer;

import com.jinqinxixi.trinketsandbaubles.client.particles.ModParticleRenderTypes;
import com.jinqinxixi.trinketsandbaubles.entity.BreathProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Draws the flying dragon-breath ball as an animated fire sprite that always faces the camera.
 */
@OnlyIn(Dist.CLIENT)
public class BreathProjectileRenderer extends EntityRenderer<BreathProjectile>
{
    public static final int FRAME_COUNT = 31;
    private static final int GROWTH_FRAME_COUNT = 15;

    public BreathProjectileRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BreathProjectile entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffers, int light)
    {
        float age = (float) entity.tickCount + partialTicks;
        int frame = Math.min(14, (int) age);
        float alpha = age < 15.0F ? 1.0F : Math.max(0.0F, 1.0F - (age - 15.0F) / 16.0F);
        poseStack.pushPose();
        poseStack.translate(0.0, 0.5, 0.0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        renderFrame(poseStack, buffers.getBuffer(RenderType.entityTranslucent(ModParticleRenderTypes.FIRE_BREATH_TEXTURE)),
                frame, entity.getColor(), alpha, 1.0F);
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffers, light);
    }

    private static void renderFrame(PoseStack poseStack, VertexConsumer consumer, int frame,
                                    int color, float alpha, float scale)
    {
        int column = frame % 16;
        float v0 = frame < 16 ? 0.5F : 0.0F;
        float u0 = (float) column / 16.0F;
        float u1 = u0 + 0.0625F;
        float v1 = v0 + 0.5F;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = (int) (alpha * 255.0F);
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float h = 0.5F * scale;
        vertex(consumer, pose, normal, -h, -h, u0, v1, r, g, b, a);
        vertex(consumer, pose, normal, h, -h, u1, v1, r, g, b, a);
        vertex(consumer, pose, normal, h, h, u1, v0, r, g, b, a);
        vertex(consumer, pose, normal, -h, h, u0, v0, r, g, b, a);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                               float x, float y, float u, float v, int r, int g, int b, int a)
    {
        consumer.vertex(pose, x, y, 0.0F)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BreathProjectile entity)
    {
        return ModParticleRenderTypes.FIRE_BREATH_TEXTURE;
    }
}