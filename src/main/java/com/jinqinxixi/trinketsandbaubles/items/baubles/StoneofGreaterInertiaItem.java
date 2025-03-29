package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.UUID;


@Mod.EventBusSubscriber
public class StoneofGreaterInertiaItem extends ModifiableBaubleItem {

    private static final Modifier[] MODIFIERS = Modifier.values();

    // 固定修饰符专用UUID
    private static final UUID KNOCKBACK_RESIST_UUID = UUID.fromString("d7184e46-5b46-4c99-9ea3-7e2987bf4c81");
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("d7184e46-5b46-4c99-9ea3-7e2987bf4c82");
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString("d7184e46-5b46-4c99-9ea3-7e2987bf4c83");

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    public StoneofGreaterInertiaItem(Properties properties) {
        super(properties);
    }

    @Override
    public void applyModifier(Player player, ItemStack stack) {
        // 先处理随机修饰符
        super.applyModifier(player, stack);

        // 处理固定属性
        applyFixedAttributes(player);
    }

    @Override
    public void removeModifier(Player player, ItemStack stack) {
        // 先移除随机修饰符
        super.removeModifier(player, stack);

        // 仅当没有其他同类型物品时移除固定属性
        if (!hasSameItemEquipped(player)) {
            removeFixedAttributes(player);
        }
    }

    private void applyFixedAttributes(Player player) {
        // 击退抗性
        applyAttribute(player, Attributes.KNOCKBACK_RESISTANCE,
                KNOCKBACK_RESIST_UUID,
                "trinketsandbaubles.knockback_resist",
                ModConfig.GREATER_INERTIA_KNOCKBACK_RESISTANCE.get(),
                AttributeModifier.Operation.ADDITION);

        // 移动速度
        applyAttribute(player, Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_UUID,
                "trinketsandbaubles.movement_speed",
                ModConfig.GREATER_INERTIA_MOVEMENT_SPEED.get(),
                AttributeModifier.Operation.MULTIPLY_BASE);

        // 步高
        applyAttribute(player, ForgeMod.STEP_HEIGHT_ADDITION.get(),
                STEP_HEIGHT_UUID,
                "trinketsandbaubles.step_height",
                ModConfig.GREATER_INERTIA_STEP_HEIGHT.get(),
                AttributeModifier.Operation.ADDITION);
    }

    private void removeFixedAttributes(Player player) {
        removeAttribute(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESIST_UUID);
        removeAttribute(player, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID);
        removeAttribute(player, ForgeMod.STEP_HEIGHT_ADDITION.get(), STEP_HEIGHT_UUID);
    }

    private void applyAttribute(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                UUID uuid, String name, double value, AttributeModifier.Operation operation) {
        AttributeInstance attrInstance = player.getAttribute(attribute);
        if (attrInstance == null) return;

        AttributeModifier existing = attrInstance.getModifier(uuid);
        if (existing == null) {
            attrInstance.addPermanentModifier(new AttributeModifier(
                    uuid, name, value, operation
            ));
        } else if (existing.getAmount() != value) {
            // 配置热更新处理
            attrInstance.removeModifier(uuid);
            attrInstance.addPermanentModifier(new AttributeModifier(
                    uuid, name, value, operation
            ));
        }
    }

    private void removeAttribute(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID uuid) {
        AttributeInstance attrInstance = player.getAttribute(attribute);
        if (attrInstance != null) {
            attrInstance.removeModifier(uuid);
        }
    }

    private boolean hasSameItemEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> {
                    int count = 0;
                    // 遍历所有Curios槽位类型
                    for (String identifier : handler.getCurios().keySet()) {
                        ICurioStacksHandler stackHandler = handler.getCurios().get(identifier);
                        if (stackHandler != null) {
                            // 遍历槽位中的物品
                            for (int i = 0; i < stackHandler.getSlots(); i++) {
                                ItemStack stack = stackHandler.getStacks().getStackInSlot(i);
                                // 检测是否同类物品
                                if (stack.getItem() instanceof StoneofGreaterInertiaItem) {
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

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            boolean hasItem = CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .flatMap(handler -> handler.findFirstCurio(
                            item -> item.getItem() instanceof StoneofGreaterInertiaItem))
                    .isPresent();

            if (hasItem) {
                player.setDeltaMovement(player.getDeltaMovement().add(0,
                        ModConfig.GREATER_INERTIA_JUMP_BOOST.get(), 0));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            boolean hasItem = CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .flatMap(handler -> handler.findFirstCurio(
                            item -> item.getItem() instanceof StoneofGreaterInertiaItem))
                    .isPresent();

            if (hasItem) {
                event.setDamageMultiplier(event.getDamageMultiplier() *
                        ModConfig.GREATER_INERTIA_FALL_REDUCTION.get().floatValue());
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.greater_inertia_stone.tooltip")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.trinketsandbaubles.greater_inertia_stone.tooltip.knockback",
                        String.format("%.1f", ModConfig.GREATER_INERTIA_KNOCKBACK_RESISTANCE.get() * 100))
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.trinketsandbaubles.greater_inertia_stone.tooltip.speed",
                        String.format("%.0f", ModConfig.GREATER_INERTIA_MOVEMENT_SPEED.get() * 100))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.trinketsandbaubles.greater_inertia_stone.tooltip.jump",
                        String.format("%.0f", ModConfig.GREATER_INERTIA_JUMP_BOOST.get() * 250))
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.trinketsandbaubles.greater_inertia_stone.tooltip.fall",
                        String.format("%.0f", (1 - ModConfig.GREATER_INERTIA_FALL_REDUCTION.get()) * 100))
                .withStyle(ChatFormatting.DARK_BLUE));
        tooltip.add(Component.translatable("item.trinketsandbaubles.greater_inertia_stone.tooltip4")
                .withStyle(ChatFormatting.DARK_GREEN));

        super.appendHoverText(stack, level, tooltip, flag);
    }
}