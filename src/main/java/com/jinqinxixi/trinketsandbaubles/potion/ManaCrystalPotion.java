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

public class ManaCrystalPotion extends Item {
    static final int USE_DURATION = 32;    // 长按时间
    static final float MANA_RESTORE = 10000.0f;   // 恢复的魔力值

    public ManaCrystalPotion() {
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
            CompoundTag data = player.getPersistentData();

            // 获取当前的永久减少值
            float permanentDecrease = data.getFloat("PermanentManaDecrease");

            // 记录水晶增加的魔力值
            float crystalBonus = data.getFloat("CrystalManaBonus");
            crystalBonus += ModConfig.MANA_CRYSTAL_MAX_INCREASE.get().floatValue();  // 使用配置浮点值
            data.putFloat("CrystalManaBonus", crystalBonus);

            // 增加最大魔力值 (考虑永久减少值)
            float currentMaxMana = ManaData.getMaxMana(player);
            ManaData.setMaxMana(player, currentMaxMana +
                    ModConfig.MANA_CRYSTAL_MAX_INCREASE.get().floatValue());  // 使用配置浮点值

            // 恢复魔力值
            ManaData.addMana(player, MANA_RESTORE);

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
        tooltip.add(Component.translatable("item.trinketsandbaubles.mana_crystal.tooltip",
                        String.format("%.1f", ModConfig.MANA_CRYSTAL_MAX_INCREASE.get().floatValue()))
                .withStyle(ChatFormatting.AQUA));
    }
}