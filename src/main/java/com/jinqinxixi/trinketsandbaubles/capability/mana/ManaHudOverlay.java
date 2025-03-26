package com.jinqinxixi.trinketsandbaubles.capability.mana;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class ManaHudOverlay {
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation(TrinketsandBaublesMod.MOD_ID, "textures/gui/icons.png");

    private boolean initialSyncReceived = false;
    private int cachedMana = -1;
    private int cachedMaxMana = -1;
    private static ManaHudOverlay instance;

    private boolean isVertical = false;

    // 拖动相关变量
    public boolean canDrag = false;
    private double currentX = -1;
    private double currentY = -1;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean isFollowingMouse = false;

    private static final int HORIZONTAL_BAR_WIDTH = 182;
    private static final int HORIZONTAL_BAR_HEIGHT = 5;
    private static final int VERTICAL_BAR_WIDTH = 5;
    private static final int VERTICAL_BAR_HEIGHT = 182;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static ManaHudOverlay getInstance() {
        if (instance == null) {
            instance = new ManaHudOverlay();
        }
        return instance;
    }
    private ManaHudOverlay() {
        loadConfig();  // 在构造函数中加载配置
    }

    // 配置类定义
    private static class HudConfig {
        double x = -1;
        double y = -1;
        boolean vertical = false;
    }

    public void handleEscapeKey() {
        if (canDrag) {
            toggleDragMode();  // 复用现有的toggleDragMode方法
        }
    }

    // 保存配置的方法
    private void saveConfig() {
        HudConfig config = new HudConfig();
        config.x = currentX;
        config.y = currentY;
        config.vertical = isVertical;

        File configFile = new File("config/trinketsandbaubles_mana_hud.json");
        try {
            // 确保配置目录存在
            configFile.getParentFile().mkdirs();

            // 保存配置到文件
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 加载配置的方法（在类的构造函数中调用）
    private void loadConfig() {
        File configFile = new File("config/trinketsandbaubles_mana_hud.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                HudConfig config = GSON.fromJson(reader, HudConfig.class);
                if (config != null) {
                    currentX = config.x;
                    currentY = config.y;
                    isVertical = config.vertical;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void toggleDragMode() {
        canDrag = !canDrag;
        isFollowingMouse = false;

        Minecraft minecraft = Minecraft.getInstance();

        if (canDrag) {
            minecraft.mouseHandler.releaseMouse();
        } else {
            minecraft.mouseHandler.grabMouse();
            // 退出拖动模式时保存配置
            saveConfig();
        }

        if (minecraft.player != null) {
            Component message = Component.translatable(
                    canDrag ?
                            "message.trinketsandbaubles.mana_hud.drag_mode.enabled" :
                            "message.trinketsandbaubles.mana_hud.drag_mode.disabled"
            ).withStyle(canDrag ? ChatFormatting.GREEN : ChatFormatting.RED);

            minecraft.player.displayClientMessage(message, true);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!canDrag) return false;

        int currentWidth = isVertical ? VERTICAL_BAR_WIDTH : HORIZONTAL_BAR_WIDTH;
        int currentHeight = isVertical ? VERTICAL_BAR_HEIGHT : HORIZONTAL_BAR_HEIGHT;

        // 检查鼠标是否在魔力条范围内
        boolean isOver = mouseX >= currentX && mouseX <= currentX + currentWidth &&
                mouseY >= currentY && mouseY <= currentY + currentHeight;

        // 如果鼠标在范围内或者正在跟随鼠标
        if (isOver || isFollowingMouse) {
            if (button == 0) { // 左键
                isFollowingMouse = !isFollowingMouse;
                if (isFollowingMouse) {
                    dragOffsetX = mouseX - currentX;
                    dragOffsetY = mouseY - currentY;
                }
            } else if (button == 1) { // 右键切换方向
                // 保存当前状态和相对位置比例
                boolean wasFollowing = isFollowingMouse;
                double relativeX = (mouseX - currentX) / (double)currentWidth;
                double relativeY = (mouseY - currentY) / (double)currentHeight;

                // 切换方向
                isVertical = !isVertical;

                // 计算新尺寸
                int newWidth = isVertical ? VERTICAL_BAR_WIDTH : HORIZONTAL_BAR_WIDTH;
                int newHeight = isVertical ? VERTICAL_BAR_HEIGHT : HORIZONTAL_BAR_HEIGHT;

                // 计算新的中心点
                double centerX = currentX + currentWidth / 2.0;
                double centerY = currentY + currentHeight / 2.0;

                // 更新位置，保持中心点不变
                currentX = centerX - newWidth / 2.0;
                currentY = centerY - newHeight / 2.0;

                // 恢复跟随状态
                isFollowingMouse = wasFollowing;

                if (isFollowingMouse) {
                    // 根据保存的相对位置计算新的鼠标偏移
                    double newMouseX = currentX + relativeX * newWidth;
                    double newMouseY = currentY + relativeY * newHeight;
                    dragOffsetX = newMouseX - currentX;
                    dragOffsetY = newMouseY - currentY;

                    // 通过立即触发一次mouseDragged来更新位置
                    mouseDragged(mouseX, mouseY);
                }
            }
            return true;
        }

        if (button == 0) {
            isFollowingMouse = false;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        if (!canDrag || !isFollowingMouse) return false;

        // 使用鼠标位置减去偏移量来更新位置
        double newX = mouseX - dragOffsetX;
        double newY = mouseY - dragOffsetY;

        Window window = Minecraft.getInstance().getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        // 根据当前方向使用正确的尺寸
        int currentWidth = isVertical ? VERTICAL_BAR_WIDTH : HORIZONTAL_BAR_WIDTH;
        int currentHeight = isVertical ? VERTICAL_BAR_HEIGHT : HORIZONTAL_BAR_HEIGHT;

        // 保持在屏幕内
        currentX = Math.max(0, Math.min(newX, screenWidth - currentWidth));
        currentY = Math.max(0, Math.min(newY, screenHeight - currentHeight));

        return true;
    }

    public void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !initialSyncReceived) {
            return;
        }

        // 如果魔力是满的，并且不在拖动模式下，就不显示
        if (cachedMana >= cachedMaxMana && !canDrag) {
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        if (currentX == -1 || currentY == -1) {
            currentX = screenWidth / 2 - 91;
            currentY = screenHeight - 50;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int renderX = (int) Math.round(currentX);
        int renderY = (int) Math.round(currentY);

        if (!isVertical) {
            if (canDrag) {
                guiGraphics.fill(renderX - 1, renderY - 1,
                        renderX + HORIZONTAL_BAR_WIDTH + 1, renderY + HORIZONTAL_BAR_HEIGHT + 1,
                        0x80FFFFFF);
            }

            guiGraphics.blit(GUI_ICONS_LOCATION, renderX, renderY, 0, 84,
                    HORIZONTAL_BAR_WIDTH, HORIZONTAL_BAR_HEIGHT);

            if (cachedMana > 0) {
                int manaWidth = (int) ((float) cachedMana / cachedMaxMana * HORIZONTAL_BAR_WIDTH);
                guiGraphics.blit(GUI_ICONS_LOCATION, renderX, renderY, 0, 89,
                        manaWidth, HORIZONTAL_BAR_HEIGHT);
            }
        } else {
            if (canDrag) {
                guiGraphics.fill(renderX - 1, renderY - 1,
                        renderX + VERTICAL_BAR_WIDTH + 1, renderY + VERTICAL_BAR_HEIGHT + 1,
                        0x80FFFFFF);
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(renderX, renderY + VERTICAL_BAR_HEIGHT, 0);

            float angle = (float) -Math.PI / 2;
            Quaternionf rotation = new Quaternionf().rotationZ(angle);
            guiGraphics.pose().mulPose(rotation);

            guiGraphics.blit(GUI_ICONS_LOCATION,
                    0, 0,
                    0, 84,
                    HORIZONTAL_BAR_WIDTH, HORIZONTAL_BAR_HEIGHT
            );

            if (cachedMana > 0) {
                int manaWidth = (int) ((float) cachedMana / cachedMaxMana * HORIZONTAL_BAR_WIDTH);
                guiGraphics.blit(GUI_ICONS_LOCATION,
                        0, 0,
                        0, 89,
                        manaWidth, HORIZONTAL_BAR_HEIGHT
                );
            }

            guiGraphics.pose().popPose();
        }

        // 绘制文本
        String manaText = cachedMana + "/" + cachedMaxMana;
        float scale = 0.75f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);

        float scaledX, scaledY;

        if (!isVertical) {
            scaledX = (renderX + HORIZONTAL_BAR_WIDTH/2 - minecraft.font.width(manaText) * scale / 2) / scale;
            scaledY = (renderY + HORIZONTAL_BAR_HEIGHT + 5) / scale;
        } else {
            scaledX = (renderX + VERTICAL_BAR_WIDTH/2 - minecraft.font.width(manaText) * scale / 2) / scale;
            scaledY = (renderY + VERTICAL_BAR_HEIGHT + 5) / scale;
        }

        guiGraphics.drawString(
                minecraft.font,
                manaText,
                (int) scaledX,
                (int) scaledY,
                0xFF1D8EF4,
                true
        );

        guiGraphics.pose().popPose();

        // 拖动模式状态文本
        if (canDrag) {
            Component statusText = Component.translatable(
                    "message.trinketsandbaubles.mana_hud.drag_mode.tooltip"
            ).withStyle(ChatFormatting.GREEN);

            int textX = 5;
            int textY = 5;
            int padding = 2;

            guiGraphics.fill(
                    textX - padding,
                    textY - padding,
                    textX + minecraft.font.width(statusText) + padding,
                    textY + minecraft.font.lineHeight + padding,
                    0x80000000
            );

            guiGraphics.drawString(
                    minecraft.font,
                    statusText,
                    textX,
                    textY,
                    0xFFFFFF,
                    true
            );
        }

        RenderSystem.disableBlend();
    }

    public void updateManaData(int mana, int maxMana) {
        this.cachedMana = mana;
        this.cachedMaxMana = maxMana;
        this.initialSyncReceived = true;
    }

    public void reset() {
        this.initialSyncReceived = false;
        this.cachedMana = -1;
        this.cachedMaxMana = -1;
    }
}