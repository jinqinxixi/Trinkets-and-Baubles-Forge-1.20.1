package com.jinqinxixi.trinketsandbaubles.potion;


import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import com.jinqinxixi.trinketsandbaubles.util.RaceEffectUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ElfSapPotion extends Item {
    static final int USE_DURATION = 32;    // 长按时间

    public ElfSapPotion() {
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
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            // 先清除所有现有的种族效果
            RaceEffectUtil.clearAllRaceEffects(player);
            player.addEffect(new MobEffectInstance(
                    ModEffects.ELVES.get(),
                    -1,
                    0,
                    false,
                    false,
                    false
            ));

            // 立即恢复100点魔力
            ManaData.addMana(player, 100f);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
            player.playSound(SoundEvents.GLASS_BREAK, 0.8F, 1.0F);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // 始终显示附魔光效
    }
    // 定义使用动作为拉弓
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
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
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.elf_sap.tooltip"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.elf_sap.tooltip1"));
    }
}
