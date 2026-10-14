package com.jinqinxixi.trinketsandbaubles.client.gui.widget;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ScrollPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * A scrolling block of text.
 *
 * <p>Built on Forge's own scroll panel, which supplies the scrollbar and the dragging; what is added
 * here is the wrapping and the drawing. A list of components is broken to the panel's width once, at
 * construction, and the pieces are drawn from then on — wrapping is not cheap and the text does not
 * change while the screen is open.
 *
 * <p>An optional icon is drawn above the text, which is what that feature is for: the abilities screen
 * has none, and the field exists because the same panel is used elsewhere with one.
 */
@OnlyIn(Dist.CLIENT)
public class TextPanel extends ScrollPanel
{
    /** One line of text, and what one line of the panel is worth. */
    private static final int LINE_HEIGHT = 10;

    /** How much of the panel's own width is kept clear for the scrollbar and the margin. */
    private static final int TEXT_MARGIN = 12;

    private final List<FormattedCharSequence> lines = new ArrayList<>();

    /** Kept because the panel's own font is private to the parent. */
    private final net.minecraft.client.gui.Font font;

    public TextPanel(Minecraft minecraft, int width, int top, int bottom, int left, List<Component> text)
    {
        super(minecraft, Math.max(width, 16), Math.max(bottom - top, 16), top, left);

        this.font = minecraft.font;

        // Wrapped to the panel's width rather than drawn and clipped. A line that ran off the edge
        // would be invisible text — the panel scrolls vertically and not horizontally.
        for (Component line : text)
        {
            if (line.getString().isEmpty())
            {
                // A blank line is a blank line, not a wrap of nothing.
                lines.add(FormattedCharSequence.EMPTY);
            }
            else
            {
                lines.addAll(minecraft.font.split(line, Math.max(width - TEXT_MARGIN, 8)));
            }
        }
    }

    @Override
    protected int getContentHeight()
    {
        // At least as tall as the panel, so that a short list does not leave a gap at the bottom that
        // the scrollbar thinks is scrollable.
        return Math.max(lines.size() * LINE_HEIGHT + 8, bottom - top - 8);
    }

    @Override
    protected void drawPanel(GuiGraphics graphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY)
    {
        int y = relativeY;

        for (FormattedCharSequence line : lines)
        {
            graphics.drawString(font, line, left + 4, y, 0xFFFFFF, true);
            y += LINE_HEIGHT;
        }
    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput output)
    {
        // Nothing to narrate: this panel shows a race's figures, which a screen reader reaches
        // through the screen that owns it.
    }
}