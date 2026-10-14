package com.jinqinxixi.trinketsandbaubles.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A column of labels, one per value, where picking one tells a listener about it.
 *
 * <p>Built on the game's own selection list, which brings the scrolling, the highlight and the
 * keyboard movement with it. What is added is that the rows carry a value rather than an index: a
 * caller hands over a list of things and two functions — how to name one, and what to do when one is
 * chosen — and never has to map between rows and positions itself.
 *
 * <p>The labels are translated as they are drawn rather than as they are built, following the
 * original. A label that is a translation key stays one until the moment it is put on screen, which
 * means a language change takes effect without the list being rebuilt.
 */
@OnlyIn(Dist.CLIENT)
public class LabelList<T> extends ObjectSelectionList<LabelList.Row<T>>
{
    private final Consumer<T> onSelect;

    /**
     * The labels, in order.
     *
     * <p>Held beside the rows because the width of the list is computed by whoever builds it, and the
     * game's own list does not offer them back. Without this the caller would have to keep a copy to
     * measure, which is the same list written twice.
     */
    private final List<Component> labels;

    public LabelList(Minecraft minecraft, int left, int top, int bottom, int width, int rowHeight,
                     List<T> values, Function<T, Component> label, Consumer<T> onSelect)
    {
        // Six arguments, in the order the game's own list wants them: the width and height of the
        // list itself, then the top and bottom of the area it scrolls within, then the row height.
        // The decompiled original passes five, which does not match any constructor this version has
        // — a reminder that a decompiler's output is a reading of the bytecode and not the bytecode.
        super(minecraft, width, bottom - top, top, bottom, rowHeight);

        this.onSelect = onSelect;
        this.labels = values.stream().map(label).toList();

        // Placed and trimmed: the list is a panel rather than a full-screen menu, so it has a left
        // edge of its own and no title bar.
        setLeftPos(left);
        setRenderBackground(false);
        setRenderTopAndBottom(false);

        for (T value : values)
        {
            addEntry(new Row<>(this, value, label.apply(value)));
        }
    }

    /**
     * Where a row starts.
     *
     * <p>Two pixels out from the list's own left edge, so that the highlight's left border sits clear
     * of the first character rather than through it. The text is centred inside the row, so this does
     * not move the names — it moves the frame that is drawn around them.
     */
    @Override
    public int getRowLeft()
    {
        return x0;
    }

    /**
     * How wide a row is drawn.
     *
     * <p>The list's whole width less two pixels at either side. The scrollbar no longer runs over the
     * rows — it sits outside the list entirely — so the highlight can span the names at full width, and
     * the two pixels are the frame's own margin around them.
     */
    @Override
    public int getRowWidth()
    {
        return width - 4;
    }

    /**
     * Where the scrollbar sits, which is just outside the list's right edge.
     *
     * <p>Outside rather than inside, because a bar drawn over the rows covers the end of the highlight
     * — which is the whole reason a highlight wide enough for its name kept coming up short. In its own
     * lane beside the list, it touches nothing.
     */
    @Override
    protected int getScrollbarPosition()
    {
        return x1 + 2;
    }

    /** The list's left edge, for a row placing itself against the frame drawn around it. */
    public int getX0()
    {
        return x0;
    }

    /** The list's width, which the frame's own position is worked out from. */
    public int getListWidth()
    {
        return width;
    }

    @Override
    public void setSelected(@Nullable Row<T> row)
    {
        super.setSelected(row);

        if (row != null)
        {
            onSelect.accept(row.value);
        }
    }

    /** One row: a value, and how to draw its name. */
    public static class Row<T> extends ObjectSelectionList.Entry<Row<T>>
    {
        private final LabelList<T> list;
        private final T value;
        private final Component label;

        Row(LabelList<T> list, T value, Component label)
        {
            this.list = list;
            this.value = value;
            this.label = label;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick)
        {
            Minecraft minecraft = Minecraft.getInstance();

            // Drawn whole, not clipped. The list is already as wide as its longest name, so a clip only
            // cuts the last character off that name and adds nothing.
            FormattedCharSequence text = net.minecraft.locale.Language.getInstance()
                    .getVisualOrder(label);

            // Centred inside the highlight, by the highlight's own arithmetic.
            //
            // The game draws the selection frame centred in the list — its left edge is at
            // `x0 + (listWidth - rowWidth) / 2` — and pays no attention to getRowLeft, which is why
            // every earlier attempt to line the name up with the frame failed: the name was placed by
            // one formula and the frame by another. Same formula here, so the name sits in the middle
            // of the frame whatever its width.
            int rowWidth = this.list.getRowWidth();
            int frameLeft = this.list.getX0() + (this.list.getWidth() - rowWidth) / 2;

            int textWidth = minecraft.font.width(text);
            int textLeft = frameLeft + Math.max((rowWidth - textWidth) / 2, 2);

            graphics.drawString(minecraft.font, text, textLeft, top + 2, GOLD, false);
        }

        /** The colour a name is drawn in, matching the gold the mod's tooltips use for a heading. */
        private static final int GOLD = 0xFFAA00;

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if (button == 0)
            {
                list.setSelected(this);
                return true;
            }

            return false;
        }

        @Override
        public Component getNarration()
        {
            return label;
        }
    }
}