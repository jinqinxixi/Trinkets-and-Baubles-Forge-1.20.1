//package com.jinqinxixi.trinketsandbaubles.Mixin;
//
//import com.jinqinxixi.trinketsandbaubles.ModEffects.ModEffects;
//import net.minecraft.world.damagesource.DamageSource;
//import net.minecraft.world.damagesource.DamageTypes;
//import net.minecraft.world.entity.animal.Wolf;
//import net.minecraft.world.entity.player.Player;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(Player.class)
//public class PlayerFallDamageMixin {
//
//    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
//    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
//        Player player = (Player)(Object)this;
//
//        // 检查是否是摔落伤害，玩家是否骑在狼上，以及是否有哥布林效果
//        if (source.is(DamageTypes.FALL) &&
//                player.getVehicle() instanceof Wolf &&
//                player.hasEffect(ModEffects.GOBLIN.get())) {
//            // 取消摔落伤害
//            cir.setReturnValue(false);
//        }
//    }
//}