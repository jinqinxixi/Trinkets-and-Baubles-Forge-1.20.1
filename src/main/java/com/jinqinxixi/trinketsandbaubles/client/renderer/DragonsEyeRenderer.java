package com.jinqinxixi.trinketsandbaubles.client.renderer;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.util.ScanSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DragonsEyeRenderer {
    private static final RenderType XRAY_LINES = RenderType.create(
            "xray_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.DEBUG_LINES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setLayeringState(new RenderStateShard.LayeringStateShard("no_layering", () -> {
                        RenderSystem.disableDepthTest();
                        RenderSystem.depthMask(false);
                    }, () -> {
                        RenderSystem.enableDepthTest();
                        RenderSystem.depthMask(true);
                    }))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    }, () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }))
                    .setOutputState(new RenderStateShard.OutputStateShard("outline_target", () -> {}, () -> {}))
                    .createCompositeState(false)
    );


    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // 从玩家数据中获取目标
        List<BlockPos> targets = new ArrayList<>();
        CompoundTag data = player.getPersistentData();
        if (data.contains(ScanSystem.TAG_DRAGONS_EYE_TARGETS)) {
            ListTag targetsList = data.getList(ScanSystem.TAG_DRAGONS_EYE_TARGETS, Tag.TAG_COMPOUND);
            for (int i = 0; i < targetsList.size(); i++) {
                CompoundTag posTag = targetsList.getCompound(i);
                targets.add(new BlockPos(
                        posTag.getInt("X"),
                        posTag.getInt("Y"),
                        posTag.getInt("Z")
                ));
            }
        }

        if (!targets.isEmpty()) {
            renderCustomOutlines(event.getPoseStack(), player, targets);
        }
    }

    private static void renderCustomOutlines(PoseStack poseStack, Player player, List<BlockPos> positions) {
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lines = buffer.getBuffer(XRAY_LINES);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(3.0F);

        poseStack.pushPose();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f pose = poseStack.last().pose();

        int maxRender = ModConfig.MAX_RENDER_BLOCKS.get();
        int rangeSqr = ModConfig.RENDER_RANGE.get() * ModConfig.RENDER_RANGE.get();

        positions.stream()
                .filter(pos -> player.blockPosition().distSqr(pos) <= rangeSqr)
                .sorted(Comparator.comparingDouble(pos -> player.blockPosition().distSqr(pos)))
                .limit(maxRender)
                .forEach(pos -> {
                    AABB box = new AABB(pos).inflate(0.001);
                    double distance = Math.sqrt(player.blockPosition().distSqr(pos));
                    int adjustedAlpha = Math.max(96, (int)(255 * (1.0 - distance / Math.sqrt(rangeSqr))));
                    renderBox(lines, pose, box, player, adjustedAlpha);
                });

        poseStack.popPose();
        buffer.endBatch(XRAY_LINES);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(1.0F);
    }

    private static void renderBox(VertexConsumer consumer, Matrix4f pose, AABB box, Player player, int alpha) {
        int[] colors = ScanSystem.getColorForGroup(
                ScanSystem.getOreGroupIndex(player),
                ScanSystem.isTargetMode(player)
        );
        drawBoxWithDoublePass(consumer, pose, box, colors[0], colors[1], colors[2], alpha);
    }

    private static void drawBoxWithDoublePass(VertexConsumer consumer, Matrix4f pose, AABB box,
                                              int r, int g, int b, int alpha) {
        drawBoxLines(consumer, pose, box.inflate(0.002), r, g, b, alpha, 0.0f);
        float innerAlpha = alpha * 0.5f;
        drawBoxLines(consumer, pose, box,
                (int)(r * 0.7), (int)(g * 0.7), (int)(b * 0.7),
                (int)innerAlpha, 0.0f);
    }

    private static void drawBoxLines(VertexConsumer consumer, Matrix4f pose, AABB box,
                                     int r, int g, int b, int alpha, float offset) {
        drawLine(consumer, pose, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, r, g, b, alpha, offset);

        drawLine(consumer, pose, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, r, g, b, alpha, offset);

        drawLine(consumer, pose, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, r, g, b, alpha, offset);
    }

    private static void drawLine(VertexConsumer consumer, Matrix4f pose,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 int r, int g, int b, int alpha, float offset) {
        Vec3 normal = new Vec3(x2 - x1, y2 - y1, z2 - z1).normalize();
        int lightValue = 15728880;

        consumer.vertex(pose,
                        (float)x1 + offset * (float)normal.x,
                        (float)y1 + offset * (float)normal.y,
                        (float)z1 + offset * (float)normal.z)
                .color(r, g, b, alpha)
                .uv2(lightValue, lightValue)
                .normal((float)normal.x, (float)normal.y, (float)normal.z)
                .endVertex();

        consumer.vertex(pose,
                        (float)x2 + offset * (float)normal.x,
                        (float)y2 + offset * (float)normal.y,
                        (float)z2 + offset * (float)normal.z)
                .color(r, g, b, alpha)
                .uv2(lightValue, lightValue)
                .normal((float)normal.x, (float)normal.y, (float)normal.z)
                .endVertex();
    }
}