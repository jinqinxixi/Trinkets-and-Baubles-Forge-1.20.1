package com.jinqinxixi.trinketsandbaubles.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * A colour, chosen as a hex value or by its channels.
 *
 * <p>Three parts, because three ways of answering the same question are each the right one at a
 * different moment: a hex field for a player who knows the colour they want, three sliders for one
 * who is feeling their way to it, and a reset for one who wants back what the race shipped with.
 *
 * <p>The three are kept in step. Moving a slider writes the hex, typing a hex moves the sliders, and
 * reset moves both — which is why the writes go through one method with a flag saying whether the
 * text field needs updating, rather than each control writing the others itself.
 *
 * <p>Red, green and blue but no alpha. These colours tint a texture rather than replacing it, and an
 * alpha here would be a second and quieter way to set the opacity the drawing code already decides.
 */
@OnlyIn(Dist.CLIENT)
public class ColorPicker
{
    private static final String[] CHANNELS = { "R", "G", "B" };

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    /** What the race uses when the player has chosen nothing, offered by the reset button. */
    private final int defaultColor;

    private final IntConsumer onChange;

    private final EditBox field;
    private final IntSlider[] sliders = new IntSlider[3];
    private final Button reset;

    private int color;

    /** Set while the controls are being written to, so their own callbacks do not write back. */
    private boolean syncing;

    public ColorPicker(Font font, int x, int y, int width, int height,
                       int color, int defaultColor, IntConsumer onChange)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color & 0xFFFFFF;
        this.defaultColor = defaultColor & 0xFFFFFF;
        this.onChange = onChange;

        this.field = new EditBox(font, x + 1, y, width, height, Component.empty());
        this.field.setMaxLength(7);
        this.field.setFilter(text -> text.matches("[#0-9a-fA-F]*"));
        this.field.setValue(toHex(this.color));
        this.field.setResponder(this::onTyped);

        for (int i = 0; i < 3; i++)
        {
            int shift = 16 - i * 8;

            this.sliders[i] = new IntSlider(x, y + height * (i + 1) + 1, width + 22, height,
                    Component.literal(CHANNELS[i] + ": "),
                    0, 255,
                    this.color >> shift & 0xFF,
                    value -> apply(this.color & ~(255 << shift) | value << shift, true));
        }

        this.reset = Button.builder(Component.literal("R"), button -> apply(this.defaultColor, true))
                .bounds(x + width + 2, y, 20, height)
                .build();
    }

    /** Hands every control to whatever is collecting them, in the order they are drawn. */
    public void addTo(Consumer<AbstractWidget> adder)
    {
        adder.accept(this.field);

        for (IntSlider slider : this.sliders)
        {
            adder.accept(slider);
        }

        adder.accept(this.reset);
    }

    /** Ticks the text field, which is what makes its cursor blink. */
    public void tick()
    {
        this.field.tick();
    }

    /**
     * Whether the player is typing in the hex field.
     *
     * <p>Asked by the screen so that a key which would close it — the inventory key, escape — can go
     * to the field instead while a colour is being entered. A half-typed colour is not a reason to
     * throw the screen away.
     */
    public boolean isEditing()
    {
        return this.field.isFocused();
    }

    /**
     * Draws the swatch beside the controls.
     *
     * <p>The chosen colour as a block, with the race's own colour as a small square in its corner —
     * so that a player who has drifted a long way from the default can see where they started
     * without resetting to find out.
     */
    public void render(GuiGraphics graphics)
    {
        int left = this.x + this.width + 3;
        graphics.fill(left, this.y + 1, left + 18, this.y + 19, 0xFF000000 | this.color);
        graphics.fill(left, this.y + 1, left + 4, this.y + 5, 0xFF000000 | this.defaultColor);
    }

    /** Where the next control in the same column should go. */
    public int getNextY()
    {
        return this.y + this.height * 4 + 1;
    }

    private void onTyped(String text)
    {
        if (!this.syncing)
        {
            apply(parse(text), false);
        }
    }

    /**
     * Sets the colour and brings the other controls into line.
     *
     * <p>The guard is what stops the loop: setting the text fires the responder, which would call
     * back into here, which would set the text again. While the writes are being made the callbacks
     * are told to do nothing.
     */
    private void apply(int newColor, boolean updateField)
    {
        this.color = newColor & 0xFFFFFF;
        this.syncing = true;

        if (updateField)
        {
            this.field.setValue(toHex(this.color));
        }

        for (int i = 0; i < 3; i++)
        {
            this.sliders[i].setValue(this.color >> (16 - i * 8) & 0xFF);
        }

        this.syncing = false;
        this.onChange.accept(this.color);
    }

    private static String toHex(int color)
    {
        String hex = Integer.toHexString(color & 0xFFFFFF);
        return "#" + "000000".substring(hex.length()) + hex;
    }

    /** Reads a hex string, treating anything unreadable as black rather than refusing it. */
    private static int parse(String text)
    {
        String hex = text.replace("#", "");

        if (hex.isEmpty())
        {
            return 0;
        }

        try
        {
            return Integer.parseInt(hex, 16) & 0xFFFFFF;
        }
        catch (NumberFormatException e)
        {
            // Half-typed input is ordinary while typing. Black is what the field shows in that
            // moment and what the player is about to replace.
            return 0;
        }
    }
}