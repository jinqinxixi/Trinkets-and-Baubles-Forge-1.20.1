package com.jinqinxixi.trinketsandbaubles.traits.elements;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;

/**
 * The air element.
 *
 * <p>The matchups and both colours are this element's own values. Some of
 * them read oddly — more than one element lists itself among the things it beats, and a pair may be
 * both strong and weak against the same other — and they are left exactly as they were: these are the
 * numbers as shipped, and tidying them up would change how the element plays.
 */
public class AirElement extends Element
{
    public AirElement()
    {
        super("Air");
    }

    @Override
    public Element[] getStrengths()
    {
        return new Element[] { ModElements.POISON.get() };
    }

    @Override
    public Element[] getWeaknesses()
    {
        return new Element[] { ModElements.EARTH.get() };
    }

    @Override
    public int getPrimaryColor()
    {
        return 10610356;
    }

    @Override
    public int getSecondaryColor()
    {
        return 13816530;
    }
}
