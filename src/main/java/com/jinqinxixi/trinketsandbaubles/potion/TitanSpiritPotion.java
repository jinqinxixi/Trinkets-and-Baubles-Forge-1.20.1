package com.jinqinxixi.trinketsandbaubles.potion;

import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class TitanSpiritPotion extends Item {
    static final int USE_DURATION = 32;    // 长按时间

    public TitanSpiritPotion() {
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
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {

            // 清除所有现有的种族能力
            AbstractRaceCapability.clearAllRaceAbilities(player);

            // 激活泰坦能力
            player.getCapability(ModCapabilities.TITAN_CAPABILITY).ifPresent(cap -> {
                cap.setActive(true);
            });

            // 立即恢复100点魔力
            ManaData.addMana(player, 100f);

            // 处理物品消耗
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }

            // 播放音效
            player.playSound(SoundEvents.GLASS_BREAK, 0.8F, 1.0F);
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

    // 定义使用动作为喝水
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
        tooltip.add(Component.translatable("item.trinketsandbaubles.titan_spirit.tooltip"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.titan_spirit.tooltip1"));
    }
}