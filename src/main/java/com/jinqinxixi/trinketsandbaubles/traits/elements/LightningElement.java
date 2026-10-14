package com.jinqinxixi.trinketsandbaubles.traits.elements;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;

/**
 * The lightning element.
 *
 * <p>The matchups and both colours are this element's own values. Some of
 * them read oddly — more than one element lists itself among the things it beats, and a pair may be
 * both strong and weak against the same other — and they are left exactly as they were: these are the
 * numbers as shipped, and tidying them up would change how the element plays.
 */
public class LightningElement extends Element
{
    public LightningElement()
    {
        super("Lightning");
    }

    @Override
    public Element[] getStrengths()
    {
        return new Element[] { ModElements.WATER.get() };
    }

    @Override
    public int getPrimaryColor()
    {
        return 4915275;
    }

    @Override
    public int getSecondaryColor()
    {
        return 14381019;
    }
}
