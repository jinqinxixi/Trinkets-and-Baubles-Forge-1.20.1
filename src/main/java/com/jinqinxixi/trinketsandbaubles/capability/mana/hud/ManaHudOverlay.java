package com.jinqinxixi.trinketsandbaubles.capability.mana.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class ManaHudOverlay {
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation(TrinketsandBaublesMod.MOD_ID, "textures/gui/mana_bar.png");

    /**
     * Whether the bar should be drawn this frame.
     *
     * <p>满蓝不隐藏，满蓝且八十帧未变才隐藏。这就是它「看起来会消失」的原因——魔力停止变化几秒后
     * 条淡出，下一次变化立刻回来。
     *
     * <p>Three things keep it up: mana that is not full, mana that is being spent, and the two
     * overrides — the position screen previewing it, and the always-shown setting.
     *
     * @param cost mana being spent this moment, which holds the bar up the way a change in level does
     */
    private boolean shouldDraw(float mana, float maxMana, float cost) {
        // Any change in the level resets the countdown, so the bar lingers after the last change
        // rather than after it first became full.
        if (mana != lastMana || maxMana != lastMaxMana) {
            lastMana = mana;
            lastMaxMana = maxMana;
            idleFrames = 0;
        }

        if (mana != maxMana || cost > 0.0F) {
            // Something to say: not full, or in the middle of being spent.
            idleFrames = 0;
            return true;
        }

        if (idleFrames < IDLE_FRAMES_BEFORE_HIDING) {
            idleFrames++;
            return true;
        }

        // Full and settled. Only the two overrides keep it up now.
        return previewing || (maxMana > 0.0F && alwaysShown);
    }

    /**
     * The mana being spent right now, held for a moment after it stops.
     *
     * <p>把最后一次消耗在归零后再保持二十帧，这样条不会在施法中途突然掉下去。计数用帧而不是秒。
     */
    private float currentCost() {
        float cost = lastRequestedCost;

        if (cost != lastCost) {
            lastCost = cost;
            costFrames = cost > 0.0F ? COST_HOLD_FRAMES : 0;
            idleFrames = 0;
        } else if (costFrames > 0 && --costFrames <= 0) {
            // 衰减到期时，缓存和显示值一起清零，而不是只清显示值。
            lastRequestedCost = 0.0F;
            lastCost = 0.0F;
        }

        return lastCost;
    }

    /** Counts frames a full bar has been left alone, for the fade. */
    private int idleFrames;

    private float lastMana = -1.0F;
    private float lastMaxMana = -1.0F;

    /** The cost last reported by the server. */
    private float lastRequestedCost;

    /** The cost being shown, which outlives the report for a few frames. */
    private float lastCost;

    private int costFrames;

    /** How long a full bar stays up before it fades, in frames. */
    private static final int IDLE_FRAMES_BEFORE_HIDING = 80;

    /** How long a spent cost keeps the bar up after spending stops, in frames. */
    private static final int COST_HOLD_FRAMES = 20;

    /** Set while the position screen is open, which forces the bar to be drawn. */
    private boolean previewing;

    /**
     * Records that mana is being spent, which holds the bar up.
     *
     * <p>This value is normally read from a small client-side cache the server fills. This mod has no
     * such channel, so nothing calls it yet: the bar still fades correctly, and it simply fades
     * without the last few frames of grace a cost would add. Wiring it needs a packet, which is a
     * separate piece of work.
     */
    public void setManaCost(float cost) {
        this.lastRequestedCost = cost;
    }

    /**
     * 立即清掉扣魔预览，不留衰减期。
     *
     * <p>{@link #setManaCost(float)} 设 0 之后，{@link #currentCost()} 的衰减缓存（{@code lastCost}
     * 与它的计数器）仍要再走一帧才归零；对「蓄满作废」这类预览从未成真的情形，那一帧的残影会被
     * 玩家读成「还在蓄力」。这个方法把预览和缓存一起清掉，条立刻回满。
     */
    public void clearManaCost() {
        this.lastRequestedCost = 0.0F;
        this.lastCost = 0.0F;
        this.costFrames = 0;
        this.idleFrames = 0;
    }

    private boolean shouldShowManaHud() {
        // 如果没有收到初始同步数据，不显示
        if (!initialSyncReceived) return false;

        // 检查是否使用Iron's Spells魔力系统
        try {
            // 如果配置启用了Iron's Spells魔力系统且模组确实存在
            if (ModConfig.USE_IRONS_SPELLS_MANA.get() &&
                    Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData") != null) {
                return false; // 不显示HUD
            }
        } catch (ClassNotFoundException e) {
            // Iron's Spells模组不存在，继续显示HUD
        }

        // 在其他情况下显示HUD
        return true;
    }
    private boolean initialSyncReceived = false;
    private float cachedMana = -1;
    private float cachedMaxMana = -1;
    private static ManaHudOverlay instance;

    private boolean isVertical = false;

    // 拖动相关变量
    public boolean canDrag = false;
    private double currentX = -1;
    private double currentY = -1;
    // 配置里保存的屏幕比例（0~1，对应 translatedX/translatedY）。非拖动时每帧按当前窗口大小
    // 换算回像素，窗口缩放时魔力条保持在屏幕上相同的相对位置。-1 表示还没有配置的比例。
    private double fractionX = -1;
    private double fractionY = -1;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean isFollowingMouse = false;

    /**
     * The bar's own size, and the size of the texture it is cut from.
     *
     * <p>The texture is a strip: the empty bar as the first row of sixteen pixels, then one filled
     * bar per colour, each also sixteen tall. The bar is as wide as the texture and one row tall, so
     * the whole of every row is the bar.
     */
    private static final int BAR_WIDTH = 106;
    private static final int BAR_HEIGHT = 16;

    /** The texture's full height, which the rows are measured against. */
    private static final int TEXTURE_WIDTH = 106;
    private static final int TEXTURE_HEIGHT = 128;

    /** Which filled row to use, counted in rows below the empty bar. */
    private static final int BAR_COLOUR = 0;

    /** Whether the numbers are drawn over the bar. */
    private static final boolean SHOW_TEXT = true;

    /**
     * Where the bar sits for a player who has never moved it, as fractions of the screen.
     *
     * <p>横坐标默认左移 0.19，让条避开快捷栏；否则条和数字会压在快捷栏前几个格子上。纵坐标让条
     * 停在底边上方。
     *
     * <p>按屏幕比例保存，这样条相对窗口的位置固定，而不是固定像素数。
     */
    private static final double DEFAULT_X_FRACTION = 0.02D;
    private static final double DEFAULT_Y_FRACTION = 0.94D;

    /** The bar turned upright is the same rectangle, so its footprint swaps. */
    private static final int HORIZONTAL_BAR_WIDTH = BAR_WIDTH;
    private static final int HORIZONTAL_BAR_HEIGHT = BAR_HEIGHT;
    private static final int VERTICAL_BAR_WIDTH = BAR_HEIGHT;
    private static final int VERTICAL_BAR_HEIGHT = BAR_WIDTH;

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
        // 位置按「屏幕比例」（0~1）保存，渲染时按当前窗口大小换算成像素。窗口缩放时
        // 魔力条保持在屏幕上相同的相对位置，不再因为存绝对像素而跑出屏幕外。
        double translatedX = -1;
        double translatedY = -1;
        boolean vertical = false;
        boolean alwaysShown = false;
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

        // 存屏幕比例：当前位置除以保存那一刻的窗口大小。窗口之后怎么变，比例不变，
        // 魔力条就始终在屏幕上相同的相对位置。
        Window window = Minecraft.getInstance().getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();
        config.translatedX = screenWidth > 0 ? currentX / (double) screenWidth : -1;
        config.translatedY = screenHeight > 0 ? currentY / (double) screenHeight : -1;
        fractionX = config.translatedX;
        fractionY = config.translatedY;

        config.vertical = isVertical;
        config.alwaysShown = alwaysShown;

        File configFile = new File("config/trinketsandbaubles_mana_hud.json");
        try {
            // 确保配置目录存在
            configFile.getParentFile().mkdirs();

            // 保存配置到文件
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            TrinketsandBaublesMod.LOGGER.error("保存魔力条配置失败", e);
        }
    }

    // 加载配置的方法（在类的构造函数中调用）
    private void loadConfig() {
        File configFile = new File("config/trinketsandbaubles_mana_hud.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                HudConfig config = GSON.fromJson(reader, HudConfig.class);
                if (config != null) {
                    isVertical = config.vertical;
                    alwaysShown = config.alwaysShown;

                    // 用屏幕比例还原位置：记下比例，渲染时按当前窗口大小每帧换算，窗口
                    // 缩放后魔力条仍在屏幕上相同的相对位置，而不是按旧像素值跑出屏幕外。
                    if (config.translatedX >= 0 && config.translatedY >= 0) {
                        fractionX = config.translatedX;
                        fractionY = config.translatedY;
                        currentX = -1;  // 让渲染帧按比例初始化
                        currentY = -1;
                    } else {
                        // 旧配置（绝对像素）：直接沿用，下次保存会转成比例。
                        currentX = config.x;
                        currentY = config.y;
                    }
                }
            } catch (IOException e) {
                TrinketsandBaublesMod.LOGGER.error("读取魔力条配置失败", e);
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
        if (minecraft.player == null || !initialSyncReceived) return;

        // The mod's own switch for handing mana over to another mod entirely.
        if (!shouldShowManaHud() && !canDrag) return;

        // Whether to draw at all: a full bar that has been left alone fades, and anything that
        // changes brings it back. The cost is clamped to the mana left, so a spent cost can never run
        // the bar past empty.
        float cost = Math.min(currentCost(), cachedMana);

        if (!shouldDraw(cachedMana, cachedMaxMana, cost) && !canDrag) return;

        Window window = minecraft.getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        // The position a player who has never moved the bar gets: a fraction of the screen, low and
        // towards the left.
        //
        // Held as fractions of the screen rather than as pixels so that it stays in the same place
        // relative to the window as the window changes size.
        //
        // 配置里有保存的比例时（非拖动状态），每帧按当前窗口大小换算：窗口缩放后魔力条跟着比例
        // 移动，永远停在屏幕上相同的相对位置，而不是按旧像素值跑出屏幕外。拖动时以像素为准。
        if (!canDrag && fractionX >= 0 && fractionY >= 0) {
            currentX = Math.round(screenWidth * fractionX);
            currentY = Math.round(screenHeight * fractionY);
        } else if (currentX == -1 || currentY == -1) {
            currentX = Math.round(screenWidth * DEFAULT_X_FRACTION);
            currentY = Math.round(screenHeight * DEFAULT_Y_FRACTION);
        }

        // Kept inside the screen: the value is held between zero and the screen's own size on each
        // axis.
        //
        // Not held back by the bar's own extent, which is the obvious-looking thing to subtract and
        // is not done here. The default sits the bar's top edge a twentieth of the way up from the
        // bottom, which does put the numbers over the hotbar — and that is the shipped behaviour, so
        // it is the behaviour here.
        currentX = Mth.clamp(currentX, 0.0D, screenWidth);
        currentY = Mth.clamp(currentY, 0.0D, screenHeight);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int renderX = (int) Math.round(currentX);
        int renderY = (int) Math.round(currentY);

        drawBar(guiGraphics, renderX, renderY, cost);

        // 拖动模式状态提示
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

    /**
     * Draws the bar.
     *
     * <p>The texture is a strip of sixteen-pixel rows: the empty bar first, then one filled bar per
     * colour. Which of the filled rows is used is what the colour option selects, and the fill is
     * drawn by taking a horizontal slice of that row rather than by scaling the whole image — a
     * stretched bar would smear its end cap across the middle.
     *
     * <p>Upright is the same bar turned a quarter turn about the point it is drawn at, which is what
     * {@code rotateAround} does: the turn is about that point rather than about the screen's corner,
     * so the bar stays where it was put. Turning it by translating first and rotating after — which
     * is the obvious way to write it and was written that way here once — moves the bar a whole
     * screen's worth sideways instead, because the rotation is then about the origin.
     */
    private void drawBar(GuiGraphics graphics, int x, int y, float cost) {
        // 填充量扣除「即将扣除」的预览魔力（drawBar 的 mana - cost 语义）。
        float remaining = Math.max(0.0F, cachedMana - cost);
        int filled = cachedMaxMana <= 0
                ? 0
                : (int) (BAR_WIDTH * (remaining / cachedMaxMana));

        graphics.pose().pushPose();

        if (isVertical) {
            graphics.pose().rotateAround(new Quaternionf().rotationZ((float) -Math.PI / 2), x, y, 0.0F);
        }

        // The empty bar: the first row of the strip, drawn whole.
        graphics.blit(GUI_ICONS_LOCATION, x, y, 0.0F, 0.0F,
                BAR_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // The filled part: a slice of the row the colour option selects. Skipped when there is
        // nothing to show, so an empty bar is not given a zero-width slice to draw.
        if (cachedMana > 0.0F && filled > 0) {
            int row = BAR_HEIGHT + BAR_COLOUR * BAR_HEIGHT;

            graphics.blit(GUI_ICONS_LOCATION, x, y, 0.0F, (float) row,
                    filled, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        if (SHOW_TEXT) {
            // Centred on the bar, and drawn inside the pose the bar was drawn in — which is what
            // makes the numbers turn with it. Drawn after the pose is popped, which is the obvious
            // place for a string and the wrong one here: upright text on a bar lying on its side
            // reads as two things drawn over each other rather than as one thing.
            String text = (int) cachedMana + "/" + (int) Math.max(cachedMaxMana, 0.0F);
            int textX = x + BAR_WIDTH / 2 - Minecraft.getInstance().font.width(text) / 2 + 1;
            int textY = y + BAR_HEIGHT / 2 - 4;

            graphics.drawString(Minecraft.getInstance().font, text, textX, textY, -1, true);
        }

        graphics.pose().popPose();
    }

    /**
     * Draws the bar at a given position without moving it.
     *
     * <p>For the screen that places it: the bar is drawn where the pointer is so that a player sees
     * where it would land, while the real position is only taken when that screen closes. Drawing it
     * here rather than moving the bar first means a player who changes their mind and presses escape
     * leaves the bar exactly where it was.
     */
    public void previewBarAt(GuiGraphics graphics, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        drawBar(graphics, x, y, 0.0F);

        RenderSystem.disableBlend();
    }

    public void updateManaData(float mana, float maxMana) {
        this.cachedMana = mana;
        this.cachedMaxMana = maxMana;
        this.initialSyncReceived = true;
    }

    public void reset() {
        this.initialSyncReceived = false;
        this.cachedMana = -1;
        this.cachedMaxMana = -1;
    }

    /** Whether the bar is drawn down the screen rather than across it. */
    public boolean isVertical() {
        return isVertical;
    }

    /** Turns the bar a quarter turn, and remembers it. */
    public void toggleVertical() {
        this.isVertical = !this.isVertical;
        saveConfig();
    }

    /** Whether the bar stays on screen once mana is full. */
    public boolean isAlwaysShown() {
        return alwaysShown;
    }

    /**
     * Keeps the bar up when mana is full, or lets it fade as it did before.
     *
     * <p>Off by default, which is why the bar seems to vanish: a full bar fades after a few seconds
     * unless this is on.
     */
    public void toggleAlwaysShown() {
        alwaysShown = !alwaysShown;
        saveConfig();
    }

    /**
     * Whether the bar stays visible when mana is full.
     *
     * <p>This setting lives with the mana HUD configuration, where it is the setting behind the
     * small A/^ button — A for kept up, ^ for allowed to fade.
     */
    private boolean alwaysShown = false;

    /**
     * Moves the bar to a screen position and remembers it.
     *
     * <p>Used by the position screen, which does not drag but lets the pointer place the bar and
     * takes the position when the screen closes.
     */
    public void setPosition(double x, double y) {
        this.currentX = x;
        this.currentY = y;
        saveConfig();
    }

    /** Forces the bar to be drawn, for as long as the position screen is open. */
    public void setPreviewing(boolean previewing) {
        this.previewing = previewing;
    }
}