package com.jinqinxixi.trinketsandbaubles.mixin;

import com.jinqinxixi.trinketsandbaubles.util.RaceScaleHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复缩小体型后贴墙/潜行像有「斥力」的问题。
 *
 * <p>游戏在玩家尝试进入某个姿势（潜行、游泳、趴下）时会拿「该姿势的基础碰撞箱」做
 * {@code noCollision} 检测。缩小后这个基础碰撞箱与实际缩放后的碰撞箱
 * 不匹配，检测结果偏大，导致贴墙/贴地时被「弹开」。这里在检测前用缩放后的碰撞箱替换，让检测
 * 与实际碰撞一致。
 */
@Mixin(Entity.class)
public abstract class EntityPoseMixin
{
    @Inject(method = "canEnterPose", at = @At("HEAD"), cancellable = true)
    private void trinketsandbaubles$fixRacePoseCollision(Pose pose, CallbackInfoReturnable<Boolean> cir)
    {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player))
        {
            return;
        }

        float scale = RaceScaleHelper.getActiveScale(player);
        if (scale <= 0.0f || Math.abs(scale - 1.0f) < 0.001f)
        {
            return;
        }

        EntityDimensions base = player.getDimensions(pose);
        float width = clampWidth(base.width * scale);
        float height = clampHeight(base.height * scale);
        EntityDimensions scaled = base.fixed
                ? EntityDimensions.fixed(width, height)
                : EntityDimensions.scalable(width, height);

        var box = scaled.makeBoundingBox(player.position()).deflate(1.0E-7);
        cir.setReturnValue(player.level().noCollision(player, box));
    }

    private static float clampWidth(float width)
    {
        return net.minecraft.util.Mth.clamp(width, 0.3F, 5.4F);
    }

    private static float clampHeight(float height)
    {
        return net.minecraft.util.Mth.clamp(height, 0.45F, 5.4F);
    }
}