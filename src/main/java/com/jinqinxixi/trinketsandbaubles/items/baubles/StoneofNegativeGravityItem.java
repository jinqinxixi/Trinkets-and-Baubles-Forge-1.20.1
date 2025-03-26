package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public class StoneofNegativeGravityItem extends ModifiableBaubleItem {
    private static final float FLIGHT_SPEED_MULTIPLIER = 0.3f; // 飞行速度倍率

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    public StoneofNegativeGravityItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        // 如果是玩家且物品在主手或副手中
        if (entity instanceof Player player && isSelected) {
            // 给予漂浮效果1级，持续时间5秒（100刻），无粒子效果
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0, false, false));
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (slotContext.entity() instanceof Player player) {
            // 检查玩家是否在地面上
            if (player.onGround()) {
                // 玩家着陆时关闭飞行
                if (player.getAbilities().flying) {
                    player.getAbilities().flying = false;
                    player.getAbilities().mayfly = false;
                    player.onUpdateAbilities();
                }
            } else {
                // 玩家离开地面时自动启用飞行
                if (!player.getAbilities().flying) {
                    player.getAbilities().mayfly = true;
                    player.getAbilities().flying = true;
                    // 设置飞行速度为创造模式的50%
                    player.getAbilities().setFlyingSpeed(0.05f * FLIGHT_SPEED_MULTIPLIER);
                    player.onUpdateAbilities();
                }
            }

            // 如果玩家正在飞行，确保保持正确的速度
            if (player.getAbilities().flying) {
                float baseFlightSpeed = 0.05f;
                player.getAbilities().setFlyingSpeed(baseFlightSpeed * FLIGHT_SPEED_MULTIPLIER);
                player.onUpdateAbilities();
            }
        }
    }
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
    }
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        if (slotContext.entity() instanceof Player player) {
            // 取下饰品时关闭飞行
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            // 恢复原版飞行速度
            player.getAbilities().setFlyingSpeed(0.05f);
            player.onUpdateAbilities();
        }
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return !isEquipped(slotContext.entity());
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof StoneofNegativeGravityItem))
                .isPresent();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.weightless_stone.tooltip"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.weightless_stone.tooltip1"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.weightless_stone.tooltip2"));
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