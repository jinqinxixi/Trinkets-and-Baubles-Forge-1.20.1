package com.jinqinxixi.trinketsandbaubles.potion;

import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.init.ModElements;
import com.jinqinxixi.trinketsandbaubles.traits.elements.Element;
import com.jinqinxixi.trinketsandbaubles.traits.elements.IElementProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class DragonGemPotion extends Item implements IElementProvider {
    static final int USE_DURATION = 32;    // 长按时间

    /**
     * 龙宝石的元素变体：火、冰、雷。
     *
     * <p>调用时才取注册对象，不写成静态字段：元素注册表在类加载时还没填好，静态初始化里调
     * {@code get()} 会抛「Registry Object not present」。
     */
    @Override
    public Element[] getSubElements() {
        return new Element[]{
                ModElements.FIRE.get(),
                ModElements.ICE.get(),
                ModElements.LIGHTNING.get()
        };
    }

    /**
     * 名字按元素变体区分：{@code dragon_gem.food.fire} / {@code .ice} / {@code .lightning}。
     *
     * <p>物品本身分不出变体，名字要读 NBT 决定。
     */
    @Override
    public String getDescriptionId(ItemStack stack) {
        Element element = getPrimaryElement(stack);
        if (!element.isNone()) {
            String name = element.getName().toLowerCase();
            if ("fire".equals(name) || "ice".equals(name) || "lightning".equals(name)) {
                return super.getDescriptionId() + ".food." + name;
            }
        }
        return super.getDescriptionId();
    }

    public DragonGemPotion() {
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

            // 先清除所有现有的种族效果
            AbstractRaceCapability.clearAllRaceAbilities(player);

            // 激活龙族能力（含元素变体）
            player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof DragonCapability dragonCap) {
                    dragonCap.applyElementFromRing(stack);
                }
                cap.setActive(true);
            });

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

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getEnchantmentValue() {
        return 0; // 附魔等级为0
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.dragon_gem.tooltip"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.dragon_gem.tooltip1"));
    }
}