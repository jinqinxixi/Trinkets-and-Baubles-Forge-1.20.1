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

public class GlowingIngotItem extends Item {
    public GlowingIngotItem() {
        super(new Properties()
                .stacksTo(64)
                .rarity(Rarity.UNCOMMON)
                .fireResistant());
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.glowing_lngot.tooltip")
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.trinketsandbaubles.glowing_lngot.tooltip2")
                .withStyle(ChatFormatting.AQUA));
    }
    @Override
    public int getEnchantmentValue() {
        return 0; // 附魔等级为0
    }

    // 禁止任何形式的附魔（包括铁砧）
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
