//package com.jinqinxixi.trinketsandbaubles.Mixin;
//
//import com.jinqinxixi.trinketsandbaubles.ModEffects.ModEffects;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.util.Mth;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.ai.attributes.AttributeInstance;
//import net.minecraft.world.entity.ai.attributes.AttributeModifier;
//import net.minecraft.world.entity.ai.attributes.Attributes;
//import net.minecraft.world.entity.animal.Wolf;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.phys.AABB;
//import net.minecraft.world.phys.Vec3;
//import net.minecraftforge.common.ForgeMod;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.UUID;
//
//import static com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID;
//
//@Mixin(Wolf.class)
//public abstract class WolfMovementMixin {
//
//    @Unique
//    private boolean isJumping;
//
//    @Unique
//    private int healingTicks = 0; // 用于控制治疗间隔
//
//
//    @Unique
//    private static final AttributeModifier STEP_MODIFIER = new AttributeModifier(
//            UUID.fromString("8D062387-C3E4-4FD7-B47A-32E54CCB13C6"),
//            "Goblin step height bonus",
//            0.4D,
//            AttributeModifier.Operation.ADDITION
//    );
//
//    @Inject(method = "tick", at = @At("TAIL"))
//    private void onTick(CallbackInfo ci) {
//        Wolf wolf = (Wolf) (Object) this;
//
//        if (wolf.isVehicle() && wolf.getFirstPassenger() instanceof Player rider) {
//            // 检查玩家是否还有哥布林效果
//            if (!rider.hasEffect(ModEffects.GOBLIN.get())) {
//                // 如果玩家失去了效果，强制下马
//                rider.stopRiding();
//                return;
//            }
//            // 添加步高加成
//            addStepHeight(wolf);
//
//            // 每秒恢复0.5颗心的生命值（每20 ticks检查一次）
//            if (++healingTicks >= 20) {
//                healingTicks = 0;
//                if (wolf.getHealth() < wolf.getMaxHealth()) {
//                    wolf.heal(1.0F);
//                }
//            }
//
//            // 获取玩家输入并应用速度系数
//            float strafe = rider.xxa * 0.5F;
//            float forward = rider.zza;
//            if (forward < 0) {
//                forward *= 0.5F;
//            }
//
//            // 更新朝向
//            float targetYRot = rider.getYRot();
//            wolf.setYRot(targetYRot);
//            wolf.yHeadRot = targetYRot;
//            wolf.yBodyRot = targetYRot;
//            wolf.yRotO = targetYRot;
//            wolf.setXRot(rider.getXRot() * 0.5F);
//
//            // 处理移动和跳跃
//            boolean isInAir = !wolf.onGround();
//
//            // 基础移动速度计算
//            float baseSpeed = 0.5F;
//            if (rider.isSprinting()) {
//                baseSpeed *= 1F;
//            }
//
//            // 计算移动方向
//            float rad = wolf.getYRot() * 0.017453292F;
//            float sin = Mth.sin(rad);
//            float cos = Mth.cos(rad);
//
//            // 计算目标速度
//            double mx = (strafe * cos - forward * sin) * baseSpeed;
//            double mz = (forward * cos + strafe * sin) * baseSpeed;
//
//            // 处理跳跃
//            if (((LivingEntityAccessor) rider).isJumping() && !isJumping && wolf.onGround()) {
//                double jumpPower = 0.42D;
//                // 跳跃时给予更大的前向推力
//                if (forward > 0) {
//                    mx *= 3.5;
//                    mz *= 3.5;
//                    // 获取狼前方的实体
//                    Vec3 lookVec = rider.getLookAngle();
//                    Vec3 wolfPos = wolf.position();
//                    AABB attackBox = wolf.getBoundingBox()
//                            .inflate(1.0, 1.0, 1.5) // 基础碰撞箱扩展
//                            .move(lookVec.x * 1.5, 0.5, lookVec.z * 1.5); // 向前和略微向上偏移
//
//                    // 获取碰撞箱内的所有实体
//                    for (Entity target : wolf.level().getEntities(wolf, attackBox,
//                            entity -> entity != rider && entity.isAttackable())) {
//                        // 对实体造成伤害
//                        target.hurt(wolf.damageSources().indirectMagic(wolf, rider), 25.0F);
//                        // 播放攻击音效
//                        wolf.playSound(SoundEvents.WOLF_GROWL, 1.0F, 1.0F);
//                        break; // 只对第一个实体造成伤害
//                    }
//                }
//                wolf.setDeltaMovement(mx, jumpPower, mz);
//                isJumping = true;
//            }
//
//            // 处理移动
//            if (Math.abs(forward) > 0.0F || Math.abs(strafe) > 0.0F) {
//                Vec3 currentMotion = wolf.getDeltaMovement();
//
//                if (isInAir) {
//                    // 空中移动控制
//                    double airControl = forward > 0 ? 0.05D : 0.025D;
//                    wolf.setDeltaMovement(
//                            currentMotion.x + mx * airControl,
//                            currentMotion.y,
//                            currentMotion.z + mz * airControl
//                    );
//                } else {
//                    // 地面移动 - 直接设置速度，不使用插值
//                    wolf.setDeltaMovement(mx, currentMotion.y, mz);
//                }
//
//                // 更新动画速度
//                float moveSpeed = (float) Math.sqrt(mx * mx + mz * mz);
//                wolf.walkAnimation.setSpeed(moveSpeed * 5F);
//            } else {
//                // 没有输入时立即停止
//                wolf.setDeltaMovement(0, wolf.getDeltaMovement().y, 0);
//                wolf.walkAnimation.setSpeed(0F);
//            }
//
//            // 重置跳跃状态
//            if (wolf.onGround() && !((LivingEntityAccessor) rider).isJumping()) {
//                isJumping = false;
//            }
//
//            // 停止AI
//            wolf.getNavigation().stop();
//            wolf.setTarget(null);
//            wolf.setLastHurtByMob(null);
//        } else {
//            removeStepHeight(wolf);
//            // 不是骑乘状态时重置状态
//            isJumping = false;
//            healingTicks = 0;
//        }
//    }
//
//    @Unique
//    private void addStepHeight(Wolf wolf) {
//        AttributeInstance attribute = wolf.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
//        if (attribute != null && !attribute.hasModifier(STEP_MODIFIER)) {
//            attribute.addTransientModifier(STEP_MODIFIER);
//        }
//    }
//
//    @Unique
//    private void removeStepHeight(Wolf wolf) {
//        AttributeInstance attribute = wolf.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
//        if (attribute != null && attribute.hasModifier(STEP_MODIFIER)) {
//            attribute.removeModifier(STEP_MODIFIER);
//        }
//    }
//}