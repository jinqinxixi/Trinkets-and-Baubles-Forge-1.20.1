package com.jinqinxixi.trinketsandbaubles.client.gui.widget;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.util.function.IntConsumer;

/**
 * A slider that reports whole numbers.
 *
 * <p>Vanilla's slider is a fraction and the callers here want an index — which shape, which
 * variant — so the value is rounded back to an integer on the way out. The step is one, so the
 * rounding is exact and the slider stops at every value it can report.
 *
 * <p>Disabled when there is only one value to choose. A slider that can only sit in one place reads
 * as broken; greyed out with the value shown, it reads as "there is nothing to choose here", which
 * is the truth.
 */
@OnlyIn(Dist.CLIENT)
public class IntSlider extends ForgeSlider
{
    private final IntConsumer onChange;

    public IntSlider(int x, int y, int width, int height, Component prefix,
                     int min, int max, int value, IntConsumer onChange)
    {
        super(x, y, width, height, prefix, Component.empty(),
                min,
                // An upper bound equal to the lower one would give the slider nothing to travel
                // across; one step keeps it valid while the disabled flag says it cannot be used.
                Math.max(max, min + 1),
                Math.max(min, Math.min(value, max)),
                1.0D,
                0,
                true);

        this.onChange = onChange;
        this.active = max > min;
    }

    /** Called when the slider is released, which is where a change is reported. */
    @Override
    protected void applyValue()
    {
        onChange.accept(getValueInt());
    }
}