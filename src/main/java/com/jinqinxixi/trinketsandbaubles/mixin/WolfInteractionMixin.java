//package com.jinqinxixi.trinketsandbaubles.Mixin;
//
//import com.jinqinxixi.trinketsandbaubles.ModEffects.ModEffects;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.entity.animal.Wolf;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.Level;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(Wolf.class)
//public abstract class WolfInteractionMixin {
//    @Inject(method = "mobInteract",
//            at = @At("HEAD"),
//            cancellable = true)
//    private void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
//        Wolf wolf = (Wolf)(Object)this;
//        Level level = wolf.level();
//
//        // 如果玩家有哥布林效果
//        if (player.hasEffect(ModEffects.GOBLINS.get()) && wolf.isTame()) {
//            // 普通右键骑乘
//            if (!player.isShiftKeyDown() && !wolf.isVehicle() && hand == InteractionHand.MAIN_HAND) {
//                if (!level.isClientSide) {
//                    player.startRiding(wolf, true);
//                    cir.setReturnValue(InteractionResult.SUCCESS);
//                } else {
//                    cir.setReturnValue(InteractionResult.sidedSuccess(true));
//                }
//            }
//            // 潜行右键时不处理，让原版交互生效
//            else if (player.isShiftKeyDown()) {
//                return; // 让原版的交互处理执行
//            }
//        }
//    }
//}