package com.jinqinxixi.trinketsandbaubles.potion;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ManaCandyPotion extends Item {
    static final int USE_DURATION = 32;    // 长按时间

    public ManaCandyPotion() {
        super(new Properties()
                .stacksTo(16)
                .food(new FoodProperties.Builder()
                        .alwaysEat()
                        .fast()
                        .nutrition(0)
                        .saturationMod(0)
                        .build())
                .craftRemainder(Items.GLASS_BOTTLE)
                .rarity(Rarity.UNCOMMON)
                .fireResistant());
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            // 使用配置值来恢复魔力
            ManaData.addMana(player, ModConfig.MANA_CANDY_RESTORE.get().floatValue());

            // 如果不在创造模式，消耗一个物品
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }
    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    // 始终显示附魔光效
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    // 附魔等级为0
    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    // 禁止任何形式的附魔（包括铁砧）
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.mana_candy.tooltip",
                        String.format("%.1f",ModConfig.MANA_CANDY_RESTORE.get().floatValue()))
                .withStyle(ChatFormatting.AQUA));
    }
}