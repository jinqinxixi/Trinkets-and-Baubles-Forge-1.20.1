package com.jinqinxixi.trinketsandbaubles.mixin;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public class CameraMixin {

    @ModifyVariable(
            method = "getMaxZoom",
            at = @At("HEAD"),
            argsOnly = true
    )
    private double modifyMaxZoom(double startingDistance) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            try {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft != null && minecraft.player != null &&
                        minecraft.options.getCameraType() != CameraType.FIRST_PERSON) {

                    Player player = minecraft.player;
                    double cameraScale = startingDistance;

                    // 检查各个种族效果
                    MobEffectInstance effect;

                    // 泰坦效果特殊处理 - 300% - 100% = 200%
                    effect = player.getEffect(ModEffects.TITAN.get());
                    if (effect != null) {
                        // 先按配置放大到300%
                        double scaleUp = startingDistance * ModConfig.TITAN_SCALE_FACTOR.get(); // 比如3.0倍
                        // 然后减少100%
                        return scaleUp - startingDistance; // 最终效果是200%的距离
                    }

                    // 龙族效果
                    effect = player.getEffect(ModEffects.DRAGON.get());
                    if (effect != null) {
                        double scale = ModConfig.DRAGON_SCALE_FACTOR.get();
                        cameraScale = startingDistance * scale;
                    }

                    // 矮人族效果
                    effect = player.getEffect(ModEffects.DWARVES.get());
                    if (effect != null) {
                        double scale = ModConfig.DWARVES_SCALE_FACTOR.get();
                        cameraScale = startingDistance * Math.max(0.5, scale);
                    }

                    // 精灵族效果
                    effect = player.getEffect(ModEffects.ELVES.get());
                    if (effect != null) {
                        double scale = ModConfig.ELVES_SCALE_FACTOR.get();
                        cameraScale = startingDistance * Math.max(0.5, scale);
                    }

                    // 猫妖族效果
                    effect = player.getEffect(ModEffects.FAELES.get());
                    if (effect != null) {
                        double scale = ModConfig.FAELES_SCALE_FACTOR.get();
                        cameraScale = startingDistance * Math.max(0.6, scale);
                    }

                    // 精灵露族效果
                    effect = player.getEffect(ModEffects.FAIRY_DEW.get());
                    if (effect != null) {
                        double scale = ModConfig.FAIRY_DEW_SCALE_FACTOR.get();
                        cameraScale = startingDistance * Math.max(0.3, scale);
                    }

                    // 哥布林族效果
                    effect = player.getEffect(ModEffects.GOBLIN.get());
                    if (effect != null) {
                        double scale = ModConfig.GOBLIN_SCALE_FACTOR.get();
                        cameraScale = startingDistance * Math.max(0.5, scale);
                    }

                    return Math.max(startingDistance * 0.3, cameraScale);
                }
            } catch (Exception e) {
                System.out.println("Error in CameraMixin: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return startingDistance;
    }
}