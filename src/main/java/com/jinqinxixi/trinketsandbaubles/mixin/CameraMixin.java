package com.jinqinxixi.trinketsandbaubles.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.jinqinxixi.trinketsandbaubles.capability.shrink.ModCapabilities;

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

                    return minecraft.player.getCapability(ModCapabilities.SHRINK_CAPABILITY)
                            .map(cap -> {
                                if (cap.isShrunk()) {
                                    float scale = cap.scale();
                                    return startingDistance * Math.max(0.3F, scale);
                                }
                                return startingDistance;
                            })
                            .orElse(startingDistance);
                }
            } catch (Exception e) {
                // 添加日志输出以便调试
                System.out.println("Error in CameraMixin: " + e.getMessage());
                e.printStackTrace();
            }
            return startingDistance;
        }
        return startingDistance;
    }
}