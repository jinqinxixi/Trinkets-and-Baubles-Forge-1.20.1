package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class StoneoftheSeaItem extends ModifiableBaubleItem {

    // 使用固定 UUID
    private static final UUID WATER_SPEED_UUID = UUID.fromString("d7184e46-5b46-4c99-9ea3-7e2987bf4c84");
    private static final boolean TOUGH_AS_NAILS_LOADED = ModList.get().isLoaded("toughasnails");

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    public StoneoftheSeaItem(Properties properties) {
        super(properties);
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof StoneoftheSeaItem))
                .isPresent();
    }

    private void applyFixedAttributes(Player player) {
        applyAttribute(player,
                ForgeMod.SWIM_SPEED.get(),
                WATER_SPEED_UUID,
                "trinketsandbaubles.water_speed",
                4.0D,
                AttributeModifier.Operation.MULTIPLY_BASE);
    }

    private void removeFixedAttributes(Player player) {
        removeAttribute(player, ForgeMod.SWIM_SPEED.get(), WATER_SPEED_UUID);
    }

    private void applyAttribute(Player player,
                                net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                UUID uuid,
                                String name,
                                double value,
                                AttributeModifier.Operation operation) {
        AttributeInstance attrInstance = player.getAttribute(attribute);
        if (attrInstance == null) return;

        AttributeModifier existing = attrInstance.getModifier(uuid);
        if (existing == null) {
            attrInstance.addPermanentModifier(new AttributeModifier(
                    uuid, name, value, operation
            ));
        }
    }

    private void removeAttribute(Player player,
                                 net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                 UUID uuid) {
        AttributeInstance attrInstance = player.getAttribute(attribute);
        if (attrInstance != null) {
            attrInstance.removeModifier(uuid);
        }
    }

    // 处理口渴效果
    private void handleThirst(Player player) {
        if (TOUGH_AS_NAILS_LOADED) {
            ResourceLocation thirstId = new ResourceLocation("toughasnails", "thirst");
            var effect = BuiltInRegistries.MOB_EFFECT.get(thirstId);
            if (effect != null) {
                player.removeEffect(effect);
            }
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (slotContext.entity() instanceof Player player) {
            // 水下呼吸效果
            if (player.isInWater()) {
                player.setAirSupply(player.getMaxAirSupply());
                // 在水中时应用属性
                applyFixedAttributes(player);
            } else {
                // 不在水中时移除属性
                removeFixedAttributes(player);
            }

            // 处理口渴效果
            if (TOUGH_AS_NAILS_LOADED) {
                handleThirst(player);
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        if (slotContext.entity() instanceof Player player && player.isInWater()) {
            applyFixedAttributes(player);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        if (slotContext.entity() instanceof Player player) {
            removeFixedAttributes(player);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.sea_stone.tooltip"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.sea_stone.tooltip1"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.sea_stone.tooltip2"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}