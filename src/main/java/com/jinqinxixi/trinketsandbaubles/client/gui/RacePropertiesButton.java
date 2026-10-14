package com.jinqinxixi.trinketsandbaubles.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The button that opens the race appearance screen, drawn as an icon on the inventory.
 *
 * <p>An icon rather than a labelled button: ten pixels square in the inventory's own coordinates,
 * over the panel beside the player preview. It shows the top half of a two-part texture and swaps to
 * the bottom half while the pointer is over it, so it lights up under the mouse without a second
 * image.
 *
 * <p>Its own class rather than a plain Button because a plain Button draws a vanilla frame and
 * label, and the whole point here is that it does not. Its position is recomputed as it is drawn,
 * following the inventory panel — the panel moves when the recipe book is opened and closed, and a
 * button that kept its first position would be left behind.
 */
@OnlyIn(Dist.CLIENT)
public class RacePropertiesButton extends AbstractButton
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("trinketsandbaubles", "textures/gui/mana_button.png");

    /** The icon's size, and the size of one cell of the texture it is cut from. */
    private static final int SIZE = 10;

    /** The texture is two cells stacked: the plain icon, then the lit one. */
    private static final int TEXTURE_CELL = 32;
    private static final int TEXTURE_HEIGHT = 64;

    /** Where the icon sits inside the inventory panel, in the panel's own coordinates. */
    private static final int OFFSET_X = 28;
    private static final int OFFSET_Y = 66;

    private final InventoryScreen parent;

    public RacePropertiesButton(InventoryScreen parent)
    {
        super(0, 0, SIZE, SIZE, Component.translatable("gui.xat.button.open"));
        this.parent = parent;
        updatePosition();
    }

    /**
     * Follows the inventory panel.
     *
     * <p>Also hidden while the recipe book is open: the panel
     * is a different shape then and the icon would sit on top of the book's own controls.
     */
    private void updatePosition()
    {
        setX(parent.getGuiLeft() + OFFSET_X);
        setY(parent.getGuiTop() + OFFSET_Y);
        visible = !parent.getRecipeBookComponent().isVisible();
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        updatePosition();

        Minecraft minecraft = Minecraft.getInstance();
        boolean hovered = isHoveredOrFocused();

        // The lit half of the texture while hovered, the plain half otherwise.
        //
        // The eleven-argument form, which names the size to draw and the band to take from the
        // texture separately. The shorter form takes the band first and would draw a ten-pixel
        // stretch of the texture rather than a ten-pixel icon out of it.
        graphics.blit(TEXTURE, getX(), getY(), width, height,
                0.0F, hovered ? TEXTURE_CELL : 0.0F,
                TEXTURE_CELL, TEXTURE_CELL, TEXTURE_CELL, TEXTURE_HEIGHT);

        if (hovered)
        {
            // The label below the icon, which is where a player finds out what it does.
            graphics.drawString(minecraft.font, getMessage(), getX() + 5, getY() + height, 0xFFFFFF);
        }
    }

    @Override
    public void onPress()
    {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null)
        {
            minecraft.setScreen(new RaceAppearanceScreen(minecraft.player));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output)
    {
        defaultButtonNarrationText(output);
    }
}