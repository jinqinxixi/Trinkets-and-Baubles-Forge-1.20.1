package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class StoneofInertiaNullItem extends ModifiableBaubleItem {
    /**
     * ModifiableBaubleItem 的修饰符数组
     */
    private static final Modifier[] MODIFIERS = Modifier.values();

    /**
     * 击退抗性属性的UUID
     */
    private static final UUID KNOCKBACK_UUID = UUID.fromString("d7184e46-5b46-4c99-9ea3-7e2987bf4c84");

    /**
     * 获取修饰符数组
     */
    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    /**
     * 构造方法
     */
    public StoneofInertiaNullItem(Properties properties) {
        super(properties);
    }

    /**
     * 应用修饰符
     */
    @Override
    public void applyModifier(Player player, ItemStack stack) {
        // 先处理随机修饰符
        super.applyModifier(player, stack);

        // 处理固定属性
        if (!player.level().isClientSide) {
            applyKnockbackResistance(player);
        }
    }

    /**
     * 移除修饰符
     */
    @Override
    public void removeModifier(Player player, ItemStack stack) {
        // 先移除随机修饰符
        super.removeModifier(player, stack);

        // 仅当没有其他同类型物品时移除固定属性
        if (!player.level().isClientSide && !hasSameItemEquipped(player)) {
            removeKnockbackResistance(player);
        }
    }

    /**
     * 装备时触发
     */
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        if (slotContext.entity() instanceof Player player) {
            applyModifier(player, stack);
        }
    }

    /**
     * 卸下时触发
     */
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        if (slotContext.entity() instanceof Player player) {
            removeModifier(player, stack);
        }
    }

    /**
     * 每tick触发
     */
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (slotContext.entity() instanceof Player player && !player.level().isClientSide) {
            AttributeInstance attr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (attr != null && attr.getModifier(KNOCKBACK_UUID) == null) {
                applyKnockbackResistance(player);
            }
        }
    }

    /**
     * 应用击退抗性
     */
    private void applyKnockbackResistance(Player player) {
        AttributeInstance attr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (attr != null) {
            // 移除可能存在的旧修饰符
            if (attr.getModifier(KNOCKBACK_UUID) != null) {
                attr.removeModifier(KNOCKBACK_UUID);
            }
            // 添加新的修饰符
            AttributeModifier modifier = new AttributeModifier(
                    KNOCKBACK_UUID,
                    "trinketsandbaubles.knockback_resistance",
                    1.0D,
                    AttributeModifier.Operation.ADDITION
            );
            attr.addPermanentModifier(modifier);
            TrinketsandBaublesMod.LOGGER.debug("Added knockback resistance to {}, new value: {}",
                    player.getName().getString(), attr.getValue());
        }
    }

    /**
     * 移除击退抗性
     */
    private void removeKnockbackResistance(Player player) {
        AttributeInstance attr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (attr != null) {
            attr.removeModifier(KNOCKBACK_UUID);
            TrinketsandBaublesMod.LOGGER.debug("Removed knockback resistance from {}, new value: {}",
                    player.getName().getString(), attr.getValue());
        }
    }

    /**
     * 检查是否装备了多个同类物品
     */
    private boolean hasSameItemEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> {
                    int count = 0;
                    for (String identifier : handler.getCurios().keySet()) {
                        ICurioStacksHandler stackHandler = handler.getCurios().get(identifier);
                        if (stackHandler != null) {
                            for (int i = 0; i < stackHandler.getSlots(); i++) {
                                ItemStack stack = stackHandler.getStacks().getStackInSlot(i);
                                if (stack.getItem() instanceof StoneofInertiaNullItem) {
                                    count++;
                                    if (count >= 2) return true;
                                }
                            }
                        }
                    }
                    return count >= 2;
                })
                .orElse(false);
    }

    /**
     * 处理摔落事件
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            boolean hasItem = CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .flatMap(handler -> handler.findFirstCurio(
                            stack -> stack.getItem() instanceof StoneofInertiaNullItem))
                    .isPresent();

            if (hasItem) {
                event.setDamageMultiplier(0f);
                event.setCanceled(true);
            }
        }
    }

    /**
     * 处理攻击事件
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            boolean hasItem = CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .flatMap(handler -> handler.findFirstCurio(
                            stack -> stack.getItem() instanceof StoneofInertiaNullItem))
                    .isPresent();

            if (hasItem && event.getSource().is(DamageTypes.FLY_INTO_WALL)) {
                event.setCanceled(true);
                TrinketsandBaublesMod.LOGGER.debug("Cancelled fly into wall damage for {}",
                        player.getName().getString());
            }
        }
    }

    /**
     * 添加物品提示信息
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.inertia_null_stone.tooltip"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.inertia_null_stone.tooltip1"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.inertia_null_stone.tooltip2"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.inertia_null_stone.tooltip3"));
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