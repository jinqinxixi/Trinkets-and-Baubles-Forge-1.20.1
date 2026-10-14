package com.jinqinxixi.trinketsandbaubles.traits.elements;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;
import net.minecraft.world.item.ItemStack;

/**
 * An item that can carry an element, and that may ship several element variants.
 *
 * <p>The element is read off the stack's own NBT: a fire dragon ring and a lightning one are the
 * same item with different NBT, so nothing needs a second registration and the element survives the
 * stack being dropped and picked up again.
 */
public interface IElementProvider
{
    default Element getPrimaryElement()
    {
        return ModElements.NEUTRAL.get();
    }

    default Element[] getSubElements()
    {
        return ModElements.EMPTY;
    }

    default Element getPrimaryElement(ItemStack stack)
    {
        return ItemElements.getPrimary(stack, getPrimaryElement());
    }
}