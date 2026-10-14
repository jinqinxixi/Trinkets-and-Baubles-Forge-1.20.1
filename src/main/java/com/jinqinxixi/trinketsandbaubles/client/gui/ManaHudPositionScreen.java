package com.jinqinxixi.trinketsandbaubles.client.gui;

import com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Where the mana bar is placed.
 *
 * <p>The bar follows the pointer while this screen is open and is put where the pointer was when the
 * screen closes. The bar is drawn at the pointer rather than dragged: the bar is small, the panel is
 * the whole screen, and a click anywhere is a decision about where it goes.
 *
 * <p>This screen is only about position. The bar keeps drawing the fill and colour it would draw in
 * the world, so what a player previews here is what they will get.
 */
@OnlyIn(Dist.CLIENT)
public class ManaHudPositionScreen extends Screen
{
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    public ManaHudPositionScreen()
    {
        super(Component.translatable("xat.config.client.mana_hud"));
    }

    @Override
    protected void init()
    {
        // Tells the bar it is being placed, which is one of the two things that keeps it drawn. Set
        // here rather than in the constructor so that it is on for exactly as long as the screen is.
        ManaHudOverlay.getInstance().setPreviewing(true);
    }

    @Override
    public void removed()
    {
        ManaHudOverlay.getInstance().setPreviewing(false);
        super.removed();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        // The bar at the pointer, so that a player sees exactly where it will land. Forced on while
        // this screen is open — a player placing the bar is almost certainly at full mana, and a bar
        // that had faded would leave them positioning nothing.
        ManaHudOverlay.getInstance().previewBarAt(graphics, mouseX, mouseY);

        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
    }

    /**
     * Takes the position and closes.
     *
     * <p>Clicking anywhere is the confirmation: there is nothing else
     * on this screen to click, and an extra button in the corner would be a second way of saying the
     * same thing.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        onClose();
        return true;
    }

    @Override
    public void onClose()
    {
        if (lastMouseX >= 0)
        {
            ManaHudOverlay.getInstance().setPosition(lastMouseX, lastMouseY);
        }

        super.onClose();
    }

    /**
     * Does not pause the game.
     *
     * <p>The bar is being previewed and the world behind it should keep running, which is what a
     * screen for placing a HUD element wants.
     */
    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}