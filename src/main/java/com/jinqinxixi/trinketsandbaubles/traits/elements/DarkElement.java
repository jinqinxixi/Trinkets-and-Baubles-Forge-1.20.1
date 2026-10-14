package com.jinqinxixi.trinketsandbaubles.traits.elements;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;

/**
 * The dark element.
 *
 * <p>The matchups and both colours are this element's own values. Some of
 * them read oddly — more than one element lists itself among the things it beats, and a pair may be
 * both strong and weak against the same other — and they are left exactly as they were: these are the
 * numbers as shipped, and tidying them up would change how the element plays.
 */
public class DarkElement extends Element
{
    public DarkElement()
    {
        super("Dark");
    }

    @Override
    public Element[] getStrengths()
    {
        return new Element[] { ModElements.LIGHT.get() };
    }

    @Override
    public Element[] getWeaknesses()
    {
        return new Element[] { ModElements.LIGHT.get() };
    }

    @Override
    public int getPrimaryColor()
    {
        return 3289650;
    }

    @Override
    public int getSecondaryColor()
    {
        return 1644825;
    }
}
