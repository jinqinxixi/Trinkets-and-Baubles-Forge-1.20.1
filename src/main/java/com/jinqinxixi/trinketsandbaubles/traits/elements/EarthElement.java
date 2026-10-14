package com.jinqinxixi.trinketsandbaubles.traits.elements;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;

/**
 * The earth element.
 *
 * <p>The matchups and both colours are this element's own values. Some of
 * them read oddly — more than one element lists itself among the things it beats, and a pair may be
 * both strong and weak against the same other — and they are left exactly as they were: these are the
 * numbers as shipped, and tidying them up would change how the element plays.
 */
public class EarthElement extends Element
{
    public EarthElement()
    {
        super("Earth");
    }

    @Override
    public Element[] getStrengths()
    {
        return new Element[] { ModElements.LIGHTNING.get() };
    }

    @Override
    public int getPrimaryColor()
    {
        return 6765824;
    }

    @Override
    public int getSecondaryColor()
    {
        return 5277000;
    }
}
