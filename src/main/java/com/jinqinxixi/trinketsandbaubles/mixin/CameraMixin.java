package com.jinqinxixi.trinketsandbaubles.mixin;

import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
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

                    // 泰坦能力特殊处理 - 300% - 100% = 200%
                    if (player.getCapability(ModCapabilities.TITAN_CAPABILITY)
                            .map(cap -> cap.isActive())
                            .orElse(false)) {
                        // 先按配置放大到300%
                        double scaleUp = startingDistance * RaceAttributesConfig.TITAN.TITAN_SCALE_FACTOR.get();
                        // 然后减少100%
                        return scaleUp - startingDistance; // 最终效果是200%的距离
                    }

                    // 龙族能力
                    if (player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                            .map(cap -> cap.isActive())
                            .orElse(false)) {
                        double scale = RaceAttributesConfig.DRAGON.DRAGON_SCALE_FACTOR.get();
                        cameraScale = startingDistance * scale;
                    }

                    // 矮人族能力
                    if (player.getCapability(ModCapabilities.DWARVES_CAPABILITY)
                            .map(cap -> cap.isActive())
                            .orElse(false)) {
                        double scale = RaceAttributesConfig.DWARVES.DWARVES_SCALE_FACTOR.get();
                        cameraScale = startingDistance * Math.max(0.5, scale);
                    }

                    // 精灵族能力
                    if (player.getCapability(ModCapabilities.ELVES_CAPABILITY)
                            .map(cap -> cap.isActive())
                            .orElse(false)) {
                        double scale = RaceAttributesConfig.ELVES.ELVES_SCALE_FACTOR.get();
                        cameraScale = startingDistance * Math.max(0.5, scale);
                    }

                    // 猫妖族能力
                    if (player.getCapability(ModCapabilities.FAELES_CAPABILITY)
                            .map(cap -> cap.isActive())
                            .orElse(false)) {
                        double scale = RaceAttributesConfig.FAELES.FAELES_SCALE_FACTOR.get();
                        cameraScale = startingDistance * Math.max(0.6, scale);
                    }

                    // 精灵露族能力
                    if (player.getCapability(ModCapabilities.FAIRY_CAPABILITY)
                            .map(cap -> cap.isActive())
                            .orElse(false)) {
                        double scale = RaceAttributesConfig.FAIRY.FAIRY_DEW_SCALE_FACTOR.get();
                        cameraScale = startingDistance * Math.max(0.3, scale);
                    }

                    // 哥布林族能力
                    if (player.getCapability(ModCapabilities.GOBLINS_CAPABILITY)
                            .map(cap -> cap.isActive())
                            .orElse(false)) {
                        double scale = RaceAttributesConfig.GOBLINS.GOBLIN_SCALE_FACTOR.get();
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