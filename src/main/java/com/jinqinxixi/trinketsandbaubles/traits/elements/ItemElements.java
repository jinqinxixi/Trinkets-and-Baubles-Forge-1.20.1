package com.jinqinxixi.trinketsandbaubles.traits.elements;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Reads and writes an item's element off its stack.
 *
 * <p>The element is a registry id written into the stack's own tag, so a dragon ring that is fire
 * and one that is lightning are the same item with different NBT — nothing needs a second item, and
 * the element survives being dropped and picked up again because it rides the stack.
 */
public final class ItemElements
{
    private static final String ELEMENTS_TAG = "Elements";
    private static final String PRIMARY_TAG = "primary";

    private ItemElements()
    {
    }

    public static Element getPrimary(ItemStack stack, Element fallback)
    {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(ELEMENTS_TAG, CompoundTag.TAG_COMPOUND))
        {
            String id = tag.getCompound(ELEMENTS_TAG).getString(PRIMARY_TAG);
            if (!id.isEmpty())
            {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                Element element = rl == null ? null : ModElements.registry().getValue(rl);
                if (element != null)
                {
                    return element;
                }
            }
        }
        return fallback;
    }

    public static void setPrimary(ItemStack stack, Element element)
    {
        ResourceLocation id = ModElements.registry().getKey(element);
        if (id != null)
        {
            stack.getOrCreateTagElement(ELEMENTS_TAG).putString(PRIMARY_TAG, id.toString());
        }
    }
}