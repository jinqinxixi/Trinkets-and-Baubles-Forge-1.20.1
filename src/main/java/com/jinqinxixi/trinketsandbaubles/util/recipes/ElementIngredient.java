package com.jinqinxixi.trinketsandbaubles.util.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

import com.jinqinxixi.trinketsandbaubles.init.ModElements;
import com.jinqinxixi.trinketsandbaubles.traits.elements.Element;
import com.jinqinxixi.trinketsandbaubles.traits.elements.ItemElements;

/**
 * 配方里「匹配某个元素的物品」的 ingredient。
 *
 * <p>龙戒/龙宝石的火、冰、雷变体是同一种物品、不同元素 NBT，普通 ingredient 分不开它们。这个
 * ingredient 按「物品 + 元素」匹配，让龙元素的变体配方能指定原料必须是火/冰/雷的龙宝石或龙眼。
 */
public class ElementIngredient extends AbstractIngredient
{
    public static final ResourceLocation ID = new ResourceLocation("trinketsandbaubles", "element");
    private final Item item;
    private final ResourceLocation element;

    public ElementIngredient(Item item, ResourceLocation element)
    {
        super(Stream.of(new ItemValue(displayStack(item, element))));
        this.item = item;
        this.element = element;
    }

    private static ItemStack displayStack(Item item, ResourceLocation element)
    {
        ItemStack stack = new ItemStack(item);
        Element resolved = ModElements.registry().getValue(element);
        if (resolved != null && !resolved.isNone())
        {
            ItemElements.setPrimary(stack, resolved);
        }
        return stack;
    }

    @Override
    public boolean test(@Nullable ItemStack input)
    {
        if (input != null && input.is(item))
        {
            ResourceLocation actual = ModElements.registry().getKey(
                    ItemElements.getPrimary(input, ModElements.NEUTRAL.get()));
            return element.equals(actual);
        }
        return false;
    }

    @Override
    public boolean isSimple()
    {
        return false;
    }

    @Nonnull
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer()
    {
        return ElementIngredient.Serializer.INSTANCE;
    }

    @Nonnull
    @Override
    public JsonElement toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty("type", ID.toString());
        json.addProperty("item", String.valueOf(ForgeRegistries.ITEMS.getKey(item)));
        json.addProperty("element", element.toString());
        return json;
    }

    public static class Serializer implements IIngredientSerializer<ElementIngredient>
    {
        public static final ElementIngredient.Serializer INSTANCE = new ElementIngredient.Serializer();

        @Nonnull
        @Override
        public ElementIngredient parse(@Nonnull JsonObject json)
        {
            Item item = CraftingHelper.getItem(GsonHelper.getAsString(json, "item"), true);
            ResourceLocation element = ResourceLocation.tryParse(GsonHelper.getAsString(json, "element"));
            if (element == null)
            {
                throw new JsonSyntaxException("Invalid element id in " + json);
            }
            return new ElementIngredient(item, element);
        }

        @Nonnull
        @Override
        public ElementIngredient parse(FriendlyByteBuf buffer)
        {
            return new ElementIngredient(buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS), buffer.readResourceLocation());
        }

        @Override
        public void write(FriendlyByteBuf buffer, ElementIngredient ingredient)
        {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, ingredient.item);
            buffer.writeResourceLocation(ingredient.element);
        }
    }
}