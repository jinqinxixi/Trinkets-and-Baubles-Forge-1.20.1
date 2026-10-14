package com.jinqinxixi.trinketsandbaubles.traits.elements;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;

/**
 * The fire element.
 *
 * <p>The matchups and both colours are this element's own values. Some of
 * them read oddly — more than one element lists itself among the things it beats, and a pair may be
 * both strong and weak against the same other — and they are left exactly as they were: these are the
 * numbers as shipped, and tidying them up would change how the element plays.
 */
public class FireElement extends Element
{
    public FireElement()
    {
        super("Fire");
    }

    @Override
    public Element[] getStrengths()
    {
        return new Element[] { ModElements.FIRE.get() };
    }

    @Override
    public Element[] getWeaknesses()
    {
        return new Element[] { ModElements.ICE.get() };
    }

    @Override
    public int getPrimaryColor()
    {
        return 16711680;
    }

    @Override
    public int getSecondaryColor()
    {
        return 16737832;
    }
}
