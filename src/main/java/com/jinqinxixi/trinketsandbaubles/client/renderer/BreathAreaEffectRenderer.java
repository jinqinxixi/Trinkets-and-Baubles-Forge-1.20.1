package com.jinqinxixi.trinketsandbaubles.client.renderer;

import com.jinqinxixi.trinketsandbaubles.entity.BreathAreaEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * An invisible area's renderer: a valid renderer that draws nothing. The client needs one so the
 * dispatcher's {@code shouldRender} does not hit a {@code null} renderer; the visual is the vanilla
 * lingering cloud, not this entity.
 */
@OnlyIn(Dist.CLIENT)
public class BreathAreaEffectRenderer extends EntityRenderer<BreathAreaEffect>
{
    public BreathAreaEffectRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(BreathAreaEffect entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffers, int light)
    {
    }

    @Override
    public ResourceLocation getTextureLocation(BreathAreaEffect entity)
    {
        return null;
    }
}