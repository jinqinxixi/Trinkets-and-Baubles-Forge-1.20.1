package com.jinqinxixi.trinketsandbaubles.traits.elements;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;

/**
 * The ice element.
 *
 * <p>The matchups and both colours are this element's own values. Some of
 * them read oddly — more than one element lists itself among the things it beats, and a pair may be
 * both strong and weak against the same other — and they are left exactly as they were: these are the
 * numbers as shipped, and tidying them up would change how the element plays.
 */
public class IceElement extends Element
{
    public IceElement()
    {
        super("Ice");
    }

    @Override
    public Element[] getStrengths()
    {
        return new Element[] { ModElements.ICE.get() };
    }

    @Override
    public Element[] getWeaknesses()
    {
        return new Element[] { ModElements.FIRE.get() };
    }

    @Override
    public int getPrimaryColor()
    {
        return 1999615;
    }

    @Override
    public int getSecondaryColor()
    {
        return 15132415;
    }
}
