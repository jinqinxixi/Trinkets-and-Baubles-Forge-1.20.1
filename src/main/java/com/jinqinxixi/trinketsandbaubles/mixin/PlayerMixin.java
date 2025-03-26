package com.jinqinxixi.trinketsandbaubles.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import net.minecraft.world.item.PickaxeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

@Mixin(Player.class)
public class PlayerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("TrinketsAndBaubles");
    private long lastLogTime = 0;

    @Inject(
            method = "getDigSpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)F",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void onGetDigSpeed(BlockState state, @Nullable BlockPos pos, CallbackInfoReturnable<Float> cir) {
        Player player = (Player)(Object)this;

        if (player.level() == null) return;

        if (player.hasEffect(ModEffects.DWARVES.get())) {
            float originalSpeed = cir.getReturnValue();
            float hardnessMultiplier = 1.0f;

            if (player.getMainHandItem().getItem() instanceof PickaxeItem) {
                float blockHardness = state.getBlock().defaultDestroyTime();
                hardnessMultiplier += blockHardness * 0.5f;

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastLogTime > 1000) {
                    LOGGER.info("矮人效果触发 - 方块: {}, 基础速度: {}, 硬度: {}, 最终乘数: {}, 最终速度: {}",
                            state.getBlock().getDescriptionId(),
                            originalSpeed,
                            blockHardness,
                            hardnessMultiplier,
                            originalSpeed * hardnessMultiplier
                    );
                    lastLogTime = currentTime;
                }
            }

            cir.setReturnValue(originalSpeed * hardnessMultiplier);
        }
    }

    @Inject(method = "isSwimming", at = @At("HEAD"), cancellable = true)
    private void onIsSwimming(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (player.hasEffect(ModEffects.TITAN.get())) {
            cir.setReturnValue(false);
        }
    }
}