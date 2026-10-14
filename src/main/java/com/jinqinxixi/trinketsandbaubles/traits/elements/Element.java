package com.jinqinxixi.trinketsandbaubles.traits.elements;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * One of the forces a race is aligned with.
 *
 * <p>An element is a name, a pair of colours, and what it beats. Nothing here is behaviour: what an
 * element does to a creature that is weak to it is decided by whoever asks, and this only holds the
 * answers those decisions are made from.
 *
 * <p>The colours are what a race's name is drawn in when its element is shown beside it, which is
 * the whole of what a player usually sees of this system — a dragon is a dragon, and the word beside
 * it is coloured by lightning.
 *
 * <p>A registry entry rather than an enum, so that a pack can add one without touching this mod.
 * Each element is a class of its own for the same reason: an element added from outside needs
 * somewhere to override the colours and the matchups, and an enum would have no such place.
 */
public class Element
{
    protected final String name;

    public Element(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    /**
     * What this element is strong against.
     *
     * <p>Empty for most of them, which is not an oversight: several are without a matchup and the
     * ones that have them name only one or two. Filling in a symmetric table here would be inventing
     * rules the mod does not have.
     */
    public Element[] getStrengths()
    {
        return new Element[0];
    }

    /** What this element is weak to. */
    public Element[] getWeaknesses()
    {
        return new Element[0];
    }

    public String getTranslationKey()
    {
        return "xat.element." + name.toLowerCase();
    }

    public Component getDisplayName()
    {
        return Component.translatable(getTranslationKey() + ".name");
    }

    /** The colour the name is drawn in. */
    public int getPrimaryColor()
    {
        return 0xFFFFFF;
    }

    public int getSecondaryColor()
    {
        return 0xFFFFFF;
    }

    public ResourceLocation getRegistryName()
    {
        return ModElements.registry().getKey(this);
    }

    /**
     * Whether this is the absence of an element.
     *
     * <p>Asked rather than compared against null, because "no element" is a registered element here:
     * it has a name and a colour like the rest, and code that wanted to know whether a race had one
     * would otherwise have to know which of the entries is the empty one.
     */
    public boolean isNone()
    {
        return this == ModElements.NEUTRAL.get();
    }
}