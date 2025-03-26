package com.jinqinxixi.trinketsandbaubles.client.renderer;

import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsRingItem;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class DragonsEyeRenderer {
    private static final int MAX_RENDER = 200;
    private static final int RANGE_SQR = 32 * 32;

    // 自定义渲染类型，实现透视效果
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
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.findFirstCurio(stack ->
                                stack.getItem() instanceof DragonsEyeItem ||
                                        stack.getItem() instanceof DragonsRingItem)
                        .ifPresent(curio -> {
                            ItemStack stack = curio.stack();
                            List<BlockPos> targets = new ArrayList<>();
                            if (stack.hasTag()) {
                                ListTag targetsList = stack.getOrCreateTag().getList(DragonsEyeItem.TAG_DRAGONS_EYE_TARGETS, Tag.TAG_COMPOUND);
                                for (int i = 0; i < targetsList.size(); i++) {
                                    CompoundTag posTag = targetsList.getCompound(i);
                                    targets.add(new BlockPos(
                                            posTag.getInt("X"),
                                            posTag.getInt("Y"),
                                            posTag.getInt("Z")
                                    ));
                                }
                            }
                            renderCustomOutlines(event.getPoseStack(), player, targets);
                        });
            });
        }
    }

    private static void renderCustomOutlines(PoseStack poseStack, Player player, List<BlockPos> positions) {
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lines = buffer.getBuffer(XRAY_LINES);

        // 设置渲染状态
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

        positions.stream()
                .filter(pos -> player.blockPosition().distSqr(pos) <= RANGE_SQR)
                .limit(MAX_RENDER)
                .forEach(pos -> {
                    AABB box = new AABB(pos).inflate(0.001);
                    double distance = Math.sqrt(player.blockPosition().distSqr(pos));
                    int adjustedAlpha = Math.max(96, (int)(255 * (1.0 - distance / Math.sqrt(RANGE_SQR))));
                    renderBox(lines, pose, box, pos, adjustedAlpha);
                });

        poseStack.popPose();

        // 结束批处理
        buffer.endBatch(XRAY_LINES);

        // 恢复渲染状态
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(1.0F);
    }

    private static void renderBox(VertexConsumer consumer, Matrix4f pose, AABB box, BlockPos pos, int alpha) {
        final AtomicBoolean isChestMode = new AtomicBoolean(false);
        final AtomicInteger groupIndex = new AtomicInteger(0);

        if (Minecraft.getInstance().player != null) {
            CuriosApi.getCuriosInventory(Minecraft.getInstance().player).ifPresent(handler -> {
                handler.findFirstCurio(stack ->
                                stack.getItem() instanceof DragonsEyeItem ||
                                        stack.getItem() instanceof DragonsRingItem)
                        .ifPresent(curio -> {
                            ItemStack stack = curio.stack();
                            CompoundTag nbt = stack.getOrCreateTag();

                            isChestMode.set(nbt.getBoolean(DragonsEyeItem.TAG_TARGET_MODE));
                            groupIndex.set(nbt.getInt(DragonsEyeItem.TAG_ORE_GROUP_INDEX));

                            int[] colors;
                            if (stack.getItem() instanceof DragonsEyeItem) {
                                colors = DragonsEyeItem.getColorForGroup(groupIndex.get(), isChestMode.get());
                            } else {
                                colors = DragonsRingItem.getColorForGroup(groupIndex.get(), isChestMode.get());
                            }

                            drawBoxWithDoublePass(consumer, pose, box, colors[0], colors[1], colors[2], alpha);
                        });
            });
        }
    }

    private static void drawBoxWithDoublePass(VertexConsumer consumer, Matrix4f pose, AABB box,
                                              int r, int g, int b, int alpha) {
        // 外层轮廓 - 更亮更明显
        drawBoxLines(consumer, pose, box.inflate(0.002), r, g, b, alpha, 0.0f);

        // 内层轮廓 - 较暗，帮助区分深度
        float innerAlpha = alpha * 0.5f;
        // 内层轮廓
        drawBoxLines(consumer, pose, box,
                (int)(r * 0.7), (int)(g * 0.7), (int)(b * 0.7),
                (int)innerAlpha, 0.0f);
    }

    private static void drawBoxLines(VertexConsumer consumer, Matrix4f pose, AABB box,
                                     int r, int g, int b, int alpha, float offset) {
        // 底部边框
        drawLine(consumer, pose, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, r, g, b, alpha, offset);

        // 顶部边框
        drawLine(consumer, pose, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, r, g, b, alpha, offset);
        drawLine(consumer, pose, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, r, g, b, alpha, offset);

        // 竖直边
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

        // 使用发光效果
        int lightValue = 15728880; // 最大光照值

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