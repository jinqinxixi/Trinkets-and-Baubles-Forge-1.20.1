package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.config.Config;
import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import com.jinqinxixi.trinketsandbaubles.util.RaceEffectUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.List;

public class FairiesRingItem extends ModifiableBaubleItem {

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }
    public FairiesRingItem(Properties properties) {
        super(properties);
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof FairiesRingItem))
                .isPresent();
    }

    private void applyFaelisBuff(LivingEntity entity) {
        // 只在服务器端处理
        if (entity instanceof ServerPlayer serverPlayer) {
            // 检查是否装备了多个种族戒指
            if (RaceEffectUtil.hasMultipleRaceRings(serverPlayer)) {
                // 如果有多个种族戒指，移除效果
                serverPlayer.removeEffect(ModEffects.FAIRY_DEW.get());
                return;
            }

            // 检查玩家是否已经有效果
            if (!serverPlayer.hasEffect(ModEffects.FAIRY_DEW.get())) {
                // 先清除所有种族效果
                RaceEffectUtil.clearAllRaceEffects(serverPlayer);

                // 然后应用新的效果
                entity.addEffect(new MobEffectInstance(
                        ModEffects.FAIRY_DEW.get(),
                        Integer.MAX_VALUE,
                        0,
                        false,
                        false,
                        false
                ));
            }
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        LivingEntity entity = slotContext.entity();

        if (entity instanceof ServerPlayer serverPlayer) {
            if (RaceEffectUtil.hasMultipleRaceRings(serverPlayer)) {
                // 如果检测到多个种族戒指，移除效果
                serverPlayer.removeEffect(ModEffects.FAIRY_DEW.get());
                return;
            }

            // 只在装备且没有效果时给予效果
            if (isEquipped(entity) && !serverPlayer.hasEffect(ModEffects.FAIRY_DEW.get())) {
                applyFaelisBuff(entity);
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        LivingEntity entity = slotContext.entity();

        if (entity instanceof ServerPlayer serverPlayer) {
            if (RaceEffectUtil.hasMultipleRaceRings(serverPlayer)) {
                return;
            }

            if (isEquipped(entity)) {
                applyFaelisBuff(entity);
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        LivingEntity entity = slotContext.entity();

        // 只有当没有其他相同戒指装备时才移除效果
        if (!isEquipped(entity) && entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.removeEffect(ModEffects.FAIRY_DEW.get());
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            // 详细信息
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip1", Config.FAIRY_DEW_MAX_HEALTH.get() * 100)
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip2", Config.FAIRY_DEW_MOVEMENT_SPEED.get() * 100)
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip3", Config.FAIRY_DEW_ATTACK_DAMAGE.get() * 100)
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip4", Config.FAIRY_DEW_ARMOR.get() * 100)
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip5", Config.FAIRY_DEW_ARMOR_TOUGHNESS.get() * 100)
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip6", Config.FAIRY_DEW_REACH.get() * 100)
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip7", Config.FAIRY_DEW_SWIM_SPEED.get() * 100)
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip8", Config.FAIRY_DEW_JUMP_BOOST.get() * 100)
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip9")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip11")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.tooltip12")
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("item.trinketsandbaubles.fairies_ring.press_shift")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
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
}