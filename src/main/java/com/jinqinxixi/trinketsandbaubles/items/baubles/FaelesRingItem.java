package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.capability.attribute.AttributeRegistry;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import com.jinqinxixi.trinketsandbaubles.util.RaceRingUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class FaelesRingItem extends ModifiableBaubleItem {

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }
    public FaelesRingItem(Properties properties) {
        super(properties);
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof FaelesRingItem))
                .isPresent();
    }

    private void applyFaelisBuff(LivingEntity entity) {
        // 只在服务器端处理
        if (entity instanceof ServerPlayer serverPlayer) {
            // 检查是否装备了多个种族戒指
            if (RaceRingUtil.hasMultipleRaceRings(serverPlayer)) {
                // 如果有多个种族戒指，停用猫妖能力
                serverPlayer.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        cap.setActive(false);
                    }
                });
                return;
            }

            // 激活猫妖能力
            serverPlayer.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
                if (!cap.isActive()) {
                    // 先清除所有种族能力
                    AbstractRaceCapability.clearAllRaceAbilities(serverPlayer);
                    // 然后激活猫妖能力
                    cap.setActive(true);
                }
            });
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        LivingEntity entity = slotContext.entity();

        if (entity instanceof ServerPlayer serverPlayer) {
            if (RaceRingUtil.hasMultipleRaceRings(serverPlayer)) {
                // 如果检测到多个种族戒指，停用能力
                serverPlayer.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        cap.setActive(false);
                    }
                });
                return;
            }

            // 只在装备且能力未激活时激活能力
            if (isEquipped(entity)) {
                serverPlayer.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
                    if (!cap.isActive()) {
                        applyFaelisBuff(entity);
                    }
                });
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        LivingEntity entity = slotContext.entity();

        if (entity instanceof ServerPlayer serverPlayer) {
            if (RaceRingUtil.hasMultipleRaceRings(serverPlayer)) {
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

        // 只有当没有其他相同戒指装备时才停用能力
        if (!isEquipped(entity) && entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
                if (cap.isActive()) {
                    cap.setActive(false);
                }
            });
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
            // 遍历所有属性并显示非零值
            for (Map.Entry<String, AttributeRegistry.AttributeEntry> entry : AttributeRegistry.getAll().entrySet()) {
                try {
                    String attributeName = entry.getKey();
                    // 直接从FAELIS实例获取值
                    double value = 0;
                    switch (attributeName) {
                        case "MAX_HEALTH":
                            value = RaceAttributesConfig.FAELES.MAX_HEALTH.get();
                            break;
                        case "FOLLOW_RANGE":
                            value = RaceAttributesConfig.FAELES.FOLLOW_RANGE.get();
                            break;
                        case "MOVEMENT_SPEED":
                            value = RaceAttributesConfig.FAELES.MOVEMENT_SPEED.get();
                            break;
                        case "ATTACK_SPEED":
                            value = RaceAttributesConfig.FAELES.ATTACK_SPEED.get();
                            break;
                        case "ATTACK_DAMAGE":
                            value = RaceAttributesConfig.FAELES.ATTACK_DAMAGE.get();
                            break;
                        case "SWIM_SPEED":
                            value = RaceAttributesConfig.FAELES.SWIM_SPEED.get();
                            break;
                        case "FLYING_SPEED":
                            value = RaceAttributesConfig.FAELES.FLYING_SPEED.get();
                            break;
                        case "ENTITY_GRAVITY":
                            value = RaceAttributesConfig.FAELES.ENTITY_GRAVITY.get();
                            break;
                        case "BLOCK_REACH":
                            value = RaceAttributesConfig.FAELES.BLOCK_REACH.get();
                            break;
                        case "ENTITY_REACH":
                            value = RaceAttributesConfig.FAELES.ENTITY_REACH.get();
                            break;
                        case "NAMETAG_DISTANCE":
                            value = RaceAttributesConfig.FAELES.NAMETAG_DISTANCE.get();
                            break;
                        case "ARMOR":
                            value = RaceAttributesConfig.FAELES.ARMOR.get();
                            break;
                        case "ARMOR_TOUGHNESS":
                            value = RaceAttributesConfig.FAELES.ARMOR_TOUGHNESS.get();
                            break;
                        case "KNOCKBACK_RESISTANCE":
                            value = RaceAttributesConfig.FAELES.KNOCKBACK_RESISTANCE.get();
                            break;
                        case "ATTACK_KNOCKBACK":
                            value = RaceAttributesConfig.FAELES.ATTACK_KNOCKBACK.get();
                            break;
                        case "LUCK":
                            value = RaceAttributesConfig.FAELES.LUCK.get();
                            break;
                        case "STEP_HEIGHT":
                            value = RaceAttributesConfig.FAELES.STEP_HEIGHT.get();
                            break;
                    }

                    // 如果值不为0，添加到描述中
                    if (value != 0) {
                        AttributeRegistry.AttributeEntry attr = entry.getValue();
                        String displayText;

                        if (attr.isPercentage()) {
                            // 百分比属性，保留2位小数
                            displayText = String.format("%s %s%.1f%%",
                                    Component.translatable(attr.getTranslationKey()).getString(),
                                    value > 0 ? "+" : "",
                                    value * 100);
                        } else {
                            // 固定值属性，保留2位小数
                            displayText = String.format("%s %s%.2f",
                                    Component.translatable(attr.getTranslationKey()).getString(),
                                    value > 0 ? "+" : "",
                                    value);
                        }

                        tooltip.add(Component.literal(displayText)
                                .withStyle(value > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
                    }
                } catch (Exception e) {
                }
            }
        } else {
            // 简短描述
            tooltip.add(Component.translatable("item.trinketsandbaubles.faelis_ring.tooltip11")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.trinketsandbaubles.faelis_ring.tooltip12")
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("item.trinketsandbaubles.faelis_ring.tooltip13")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.trinketsandbaubles.faelis_ring.tooltip14")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.trinketsandbaubles.faelis_ring.press_shift")
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