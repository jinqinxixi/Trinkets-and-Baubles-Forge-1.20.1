package com.jinqinxixi.trinketsandbaubles.potion;

import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ManaReagentPotion extends Item {
    static final int USE_DURATION = 32;    // 长按时间

    public ManaReagentPotion() {
        super(new Properties()
                .stacksTo(16)
                .food(new FoodProperties.Builder()
                        .alwaysEat()
                        .fast()
                        .nutrition(0)
                        .saturationMod(0)
                        .build())
                .rarity(Rarity.UNCOMMON)
                .fireResistant());
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof Player player && !level.isClientSide) {


            // 直接修改全局魔力值
            float currentMaxMana = ManaData.getMaxMana(player);
            float decreaseAmount = ModConfig.MANA_REAGENT_MAX_DECREASE.get().floatValue();
            float newMaxMana = Math.max(10f, currentMaxMana - decreaseAmount); // 确保不低于10


            ManaData.setMaxMana(player, newMaxMana);

            // 消耗物品
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

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.mana_reagent.tooltip",
                        String.format("%.1f", ModConfig.MANA_REAGENT_MAX_DECREASE.get().floatValue()))
                .withStyle(ChatFormatting.RED));
    }
}