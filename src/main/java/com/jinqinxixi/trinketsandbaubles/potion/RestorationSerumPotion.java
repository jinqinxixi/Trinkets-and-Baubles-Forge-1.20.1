package com.jinqinxixi.trinketsandbaubles.potion;


import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;


import javax.annotation.Nullable;
import java.util.List;

public class RestorationSerumPotion extends Item {
    static final int USE_DURATION = 32;    // 长按时间

    public RestorationSerumPotion() {
        super(new Properties()
                .stacksTo(16)
                .food(new FoodProperties.Builder()
                        .fast()
                        .alwaysEat()
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
            // 使用通用的清除方法移除所有种族能力
            AbstractRaceCapability.clearAllRaceAbilities(player);

            // 添加药水效果
            player.addEffect(new MobEffectInstance(
                    MobEffects.POISON, 400, 1, false, false, false));
            player.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, 600, 1, false, false, false));

            // 只在非创造模式下禁用飞行能力
            if (!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
                player.refreshDimensions();
            }

            // 消耗物品（非创造模式）
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                // 添加空玻璃瓶
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }

            // 播放音效
            player.playSound(SoundEvents.GLASS_BREAK, 0.8F, 1.0F);
        }

        // 返回正确的剩余物品堆栈
        return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
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
        tooltip.add(Component.translatable("item.trinketsandbaubles.restoration_serum.tooltip"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.restoration_serum.tooltip1"));
    }
}
