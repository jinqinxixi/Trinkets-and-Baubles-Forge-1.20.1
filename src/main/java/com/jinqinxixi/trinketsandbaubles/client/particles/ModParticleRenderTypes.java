package com.jinqinxixi.trinketsandbaubles.client.particles;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The render types the mod's custom particles draw with: plain colour (additive or translucent) for
 * the lightning, and textured quads for the fire breath and the greed glow.
 */
@OnlyIn(Dist.CLIENT)
public final class ModParticleRenderTypes
{
    public static final ResourceLocation GREED_TEXTURE =
            new ResourceLocation("trinketsandbaubles", "textures/particle/greed.png");
    public static final ResourceLocation FIRE_BREATH_TEXTURE =
            new ResourceLocation("trinketsandbaubles", "textures/particle/dragon_breath.png");
    public static final ParticleRenderType ADDITIVE_COLOR = new ColorType("trinketsandbaubles:additive_color", true);
    public static final ParticleRenderType TRANSLUCENT_COLOR = new ColorType("trinketsandbaubles:translucent_color", false);
    public static final ParticleRenderType GREED = new TexturedType("trinketsandbaubles:greed", GREED_TEXTURE, false);
    public static final ParticleRenderType GREED_DEPTH = new TexturedType("trinketsandbaubles:greed_depth", GREED_TEXTURE, true);
    public static final ParticleRenderType FIRE_BREATH = new TexturedType("trinketsandbaubles:fire_breath", FIRE_BREATH_TEXTURE, true);

    private ModParticleRenderTypes()
    {
    }

    private record ColorType(String name, boolean additive) implements ParticleRenderType
    {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager)
        {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableBlend();
            if (additive)
            {
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            }
            else
            {
                RenderSystem.defaultBlendFunc();
            }
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        }

        @Override
        public void end(Tesselator tesselator)
        {
            tesselator.end();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    private record TexturedType(String name, ResourceLocation texture, boolean depthTest) implements ParticleRenderType
    {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager)
        {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
            if (!depthTest)
            {
                RenderSystem.disableDepthTest();
            }
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        }

        @Override
        public void end(Tesselator tesselator)
        {
            tesselator.end();
            if (!depthTest)
            {
                RenderSystem.enableDepthTest();
            }
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}