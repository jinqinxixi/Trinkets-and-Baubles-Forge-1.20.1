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

/**
 * 金牛族变身食物。
 *
 * <p>食用后变成金牛（坦克型种族），与戒指等效但为永久形式。
 */
public class TaurusFoodPotion extends Item
{
    static final int USE_DURATION = 32;

    public TaurusFoodPotion()
    {
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
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity)
    {
        if (!level.isClientSide && entity instanceof ServerPlayer player)
        {
            AbstractRaceCapability.clearAllRaceAbilities(player);

            player.getCapability(ModCapabilities.TAURUS_CAPABILITY).ifPresent(cap -> cap.setActive(true));

            ManaData.addMana(player, 100f);

            if (!player.getAbilities().instabuild)
            {
                stack.shrink(1);
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }

            player.playSound(SoundEvents.GLASS_BREAK, 0.8F, 1.0F);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return USE_DURATION;
    }

    @Override
    public boolean isFoil(ItemStack stack)
    {
        return true;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.DRINK;
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("item.trinketsandbaubles.taurus_food.tooltip"));
    }
}