package com.jinqinxixi.trinketsandbaubles.items.baubles;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SparkPowderItem extends Item
{
    public SparkPowderItem()
    {
        super(new Properties()
                .stacksTo(64)
                .rarity(Rarity.UNCOMMON)
                .fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("item.trinketsandbaubles.spark_powder.tooltip")
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public int getEnchantmentValue()
    {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }
}