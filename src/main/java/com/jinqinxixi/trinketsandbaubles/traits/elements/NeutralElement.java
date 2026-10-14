package com.jinqinxixi.trinketsandbaubles.traits.elements;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;

/**
 * The neutral element.
 *
 * <p>The matchups and both colours are this element's own values. Some of
 * them read oddly — more than one element lists itself among the things it beats, and a pair may be
 * both strong and weak against the same other — and they are left exactly as they were: these are the
 * numbers as shipped, and tidying them up would change how the element plays.
 */
public class NeutralElement extends Element
{
    public NeutralElement()
    {
        super("Neutral");
    }

    @Override
    public int getPrimaryColor()
    {
        return 16777215;
    }

    @Override
    public int getSecondaryColor()
    {
        return 16777215;
    }
}
