package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 装饰饰品：戴上后借某个种族的外貌特征（角、耳、翅、尾）打扮自己，不改变种族、不加能力、不变体型。
 *
 * <p>右键（潜行反向）在 18 种外观间循环切换。当前外观记在物品堆 NBT 的 {@code cosmetic.variant}，
 * 物品名按外观后缀区分，渲染侧按这个值分发到对应的种族特征绘制代码。
 *
 * <p>和其它可修饰饰品一样支持修饰词：随机一个属性加成，铁砧上配发光锭可重铸。
 */
public class CosmeticItem extends ModifiableBaubleItem
{
    private static final String ROOT = "cosmetic";
    private static final String VARIANT = "variant";

    public CosmeticItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public Modifier[] getModifiers()
    {
        return Modifier.values();
    }

    /** 当前外观，读不到 NBT 时为 HUMAN。 */
    public static CosmeticFeature featureOf(ItemStack stack)
    {
        CompoundTag tag = stack.getTagElement(ROOT);
        return tag == null ? CosmeticFeature.HUMAN : CosmeticFeature.byId(tag.getInt(VARIANT));
    }

    private static void setFeature(ItemStack stack, int variant)
    {
        stack.getOrCreateTagElement(ROOT).putInt(VARIANT, variant);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide)
        {
            boolean reverse = player.isShiftKeyDown();
            int count = CosmeticFeature.values().length;
            int next = Math.floorMod(featureOf(stack).ordinal() + (reverse ? -1 : 1), count);
            setFeature(stack, next);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.3F, reverse ? 0.6F : 0.3F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** 名字按外观序号后缀区分（.0~.17），循环切换时物品名跟着变。 */
    @Override
    public String getDescriptionId(ItemStack stack)
    {
        return super.getDescriptionId() + "." + featureOf(stack).ordinal();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("item.trinketsandbaubles.cosmetic.tooltip"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}