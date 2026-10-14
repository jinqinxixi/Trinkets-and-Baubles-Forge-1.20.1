package com.jinqinxixi.trinketsandbaubles.mixin;

import com.jinqinxixi.trinketsandbaubles.util.RaceScaleHelper;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让影子随种族体型缩放。
 *
 * <p>Forge 1.20.1 的影子半径是 {@code EntityRenderer.shadowRadius} 字段，渲染器构造时固定（玩家
 * 为 0.5），不随实体尺寸变 —— 与 NeoForge 不同，NeoForge 在 {@code getShadowRadius} 里乘了
 * {@code entity.getScale()}。所以体型缩放后影子仍是原大小：泰坦的影子太小、仙女的影子太大。
 *
 * <p>这里在 {@code EntityRenderDispatcher.render} 调 {@code renderShadow} 前，把半径乘上玩家的
 * 体型缩放因子。实体是玩家且处于激活种族时按体型缩放，其余实体保持原样。
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin
{
    /** 本帧正在渲染的实体，供影子半径修改读取。 */
    @Unique
    private Entity trinketsandbaubles$renderingEntity;

    @Inject(method = "render", at = @At("HEAD"))
    private <E extends Entity> void trinketsandbaubles$captureEntity(
            E entity, double x, double y, double z, float yRot, float partialTick,
            com.mojang.blaze3d.vertex.PoseStack pose,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light,
            CallbackInfo ci)
    {
        this.trinketsandbaubles$renderingEntity = entity;
    }

    /**
     * 把影子半径乘上体型缩放。renderShadow 的最后一个参数是半径（index 6）。
     */
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V"
            ),
            index = 6
    )
    private float trinketsandbaubles$scaleShadowRadius(float radius)
    {
        Entity entity = this.trinketsandbaubles$renderingEntity;
        if (entity instanceof LivingEntity living)
        {
            float scale = RaceScaleHelper.getActiveScale(living);
            if (scale > 0.0f && Math.abs(scale - 1.0f) > 0.001f)
            {
                return radius * scale;
            }
        }
        return radius;
    }
}
