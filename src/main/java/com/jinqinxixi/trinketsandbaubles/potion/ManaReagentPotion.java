package com.jinqinxixi.trinketsandbaubles.potion;

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
    static final String PERMANENT_MANA_DECREASE = "PermanentManaDecrease";

    public ManaReagentPotion() {
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
        if (livingEntity instanceof Player player) {
            if (!level.isClientSide) {
                CompoundTag data = player.getPersistentData();

                // 记录永久性减少，使用浮点数
                float permanentDecrease = data.getFloat(PERMANENT_MANA_DECREASE);
                data.putFloat(PERMANENT_MANA_DECREASE,
                        permanentDecrease + ModConfig.MANA_REAGENT_MAX_DECREASE.get().floatValue());

                // 获取当前的水晶加成
                float crystalBonus = data.getFloat("CrystalManaBonus");

                // 更新当前魔力值 (保持水晶加成不变)
                float currentMaxMana = ManaData.getMaxMana(player);
                ManaData.setMaxMana(player,
                        currentMaxMana - ModConfig.MANA_REAGENT_MAX_DECREASE.get().floatValue());
            }

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
        // 使用 String.format 格式化显示浮点数值
        tooltip.add(Component.translatable("item.trinketsandbaubles.mana_reagent.tooltip",
                        String.format("%.1f", ModConfig.MANA_REAGENT_MAX_DECREASE.get().floatValue()))
                .withStyle(ChatFormatting.RED));
    }
}