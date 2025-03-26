package com.jinqinxixi.trinketsandbaubles.mixin;

import com.jinqinxixi.trinketsandbaubles.capability.shrink.ModCapabilities;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract EntityDimensions getDimensions(Pose pose);

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public Level level;

    @Inject(at = @At("RETURN"), method = "canEnterPose", cancellable = true)
    public void isPoseClear(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof Player) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap -> {
                if (cap.isShrunk()) {
                    float scale = cap.scale();
                    EntityDimensions entitysize = this.getDimensions(pose);
                    entitysize = entitysize.scale(scale);
                    float f = entitysize.width / 2.0F;
                    Vec3 vector3d = new Vec3(this.getX() - (double) f, this.getY(), this.getZ() - (double) f);
                    Vec3 vector3d1 = new Vec3(this.getX() + (double) f, this.getY() + (double) entitysize.height, this.getZ() + (double) f);
                    AABB box = new AABB(vector3d, vector3d1);
                    cir.setReturnValue(this.level.noCollision(livingEntity, box.deflate(1.0E-7D)));
                }
            });
        }
    }
}

//    @Inject(
//            method = "canAddPassenger(Lnet/minecraft/world/entity/Entity;)Z",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void onCanAddPassenger(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
//        // 仅当当前实体是狼时处理
//        if (((Entity)(Object)this).getType() == EntityType.WOLF) {
//            Wolf wolf = (Wolf)(Object)this;
//            if (passenger instanceof Player player) {
//                // 只有玩家有哥布林效果时才能骑乘
//                if (!player.hasEffect(ModEffects.GOBLIN.get())) {
//                    cir.setReturnValue(false);
//                    return;
//                }
//                if (wolf.isTame()) {
//                    cir.setReturnValue(!wolf.isVehicle());
//                }
//            }
//        }
//    }
//}