package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.attribute.AttributeRegistry;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import com.jinqinxixi.trinketsandbaubles.util.RaceRingUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class DragonsRingItem extends ModifiableBaubleItem {

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    public DragonsRingItem(Properties properties) {
        super(properties);
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof DragonsRingItem))
                .isPresent();
    }

    private void applyFaelisBuff(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            if (RaceRingUtil.hasMultipleRaceRings(serverPlayer)) {
                serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        cap.setActive(false);
                    }
                });
                return;
            }

            serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                if (!cap.isActive()) {
                    AbstractRaceCapability.clearAllRaceAbilities(serverPlayer);
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
                serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        cap.setActive(false);
                    }
                });
                return;
            }

            if (isEquipped(entity)) {
                serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
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
        if (entity instanceof ServerPlayer serverPlayer &&
                (prevStack.isEmpty() || !hasSameModifier(prevStack, stack))) {

            if (!RaceRingUtil.hasMultipleRaceRings(serverPlayer)) {
                if (isEquipped(entity)) {
                    applyFaelisBuff(entity);
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (entity instanceof ServerPlayer serverPlayer &&
                (newStack.isEmpty() || !hasSameModifier(newStack, stack))) {

            if (!isEquipped(entity)) {
                serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        // 如果玩家已经死亡，不要恢复任何种族
                        if (serverPlayer.isDeadOrDying()) {
                            // 直接设置为非激活状态，不要恢复任何种族
                            cap.setActive(false);
                            // 清除掉保存的死亡状态数据
                            CompoundTag playerData = serverPlayer.getPersistentData();
                            if (playerData.contains("DragonCapability")) {
                                playerData.remove("DragonCapability");
                            }
                        } else {
                            // 正常的取消装备流程
                            cap.setActive(false);
                            CompoundTag playerData = serverPlayer.getPersistentData();
                            String savedRace = playerData.getString("SavedRace");
                            if (!savedRace.isEmpty()) {
                                RaceRingUtil.activateRace(serverPlayer, savedRace);
                            }
                        }
                    }
                });
            }
        }

        super.onUnequip(slotContext, newStack, stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            // 遍历所有属性并显示非零值
            for (Map.Entry<String, AttributeRegistry.AttributeEntry> entry : AttributeRegistry.getAll().entrySet()) {
                try {
                    String attributeName = entry.getKey();
                    // 直接从DRAGON实例获取值
                    double value = 0;
                    switch (attributeName) {
                        case "MAX_HEALTH":
                            value = RaceAttributesConfig.DRAGON.MAX_HEALTH.get();
                            break;
                        case "FOLLOW_RANGE":
                            value = RaceAttributesConfig.DRAGON.FOLLOW_RANGE.get();
                            break;
                        case "MOVEMENT_SPEED":
                            value = RaceAttributesConfig.DRAGON.MOVEMENT_SPEED.get();
                            break;
                        case "ATTACK_SPEED":
                            value = RaceAttributesConfig.DRAGON.ATTACK_SPEED.get();
                            break;
                        case "ATTACK_DAMAGE":
                            value = RaceAttributesConfig.DRAGON.ATTACK_DAMAGE.get();
                            break;
                        case "SWIM_SPEED":
                            value = RaceAttributesConfig.DRAGON.SWIM_SPEED.get();
                            break;
                        case "FLYING_SPEED":
                            value = RaceAttributesConfig.DRAGON.FLYING_SPEED.get();
                            break;
                        case "ENTITY_GRAVITY":
                            value = RaceAttributesConfig.DRAGON.ENTITY_GRAVITY.get();
                            break;
                        case "BLOCK_REACH":
                            value = RaceAttributesConfig.DRAGON.BLOCK_REACH.get();
                            break;
                        case "ENTITY_REACH":
                            value = RaceAttributesConfig.DRAGON.ENTITY_REACH.get();
                            break;
                        case "NAMETAG_DISTANCE":
                            value = RaceAttributesConfig.DRAGON.NAMETAG_DISTANCE.get();
                            break;
                        case "ARMOR":
                            value = RaceAttributesConfig.DRAGON.ARMOR.get();
                            break;
                        case "ARMOR_TOUGHNESS":
                            value = RaceAttributesConfig.DRAGON.ARMOR_TOUGHNESS.get();
                            break;
                        case "KNOCKBACK_RESISTANCE":
                            value = RaceAttributesConfig.DRAGON.KNOCKBACK_RESISTANCE.get();
                            break;
                        case "ATTACK_KNOCKBACK":
                            value = RaceAttributesConfig.DRAGON.ATTACK_KNOCKBACK.get();
                            break;
                        case "LUCK":
                            value = RaceAttributesConfig.DRAGON.LUCK.get();
                            break;
                        case "STEP_HEIGHT":
                            value = RaceAttributesConfig.DRAGON.STEP_HEIGHT.get();
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
            String dragonBreathKeyName = KeyBindings.DRAGON_BREATH_KEY.getKey().getDisplayName().getString();
            String nightVisionKeyName = KeyBindings.DRAGON_NIGHT_VISION_KEY.getKey().getDisplayName().getString();
            String toggleModeKeyName = KeyBindings.TOGGLE_DRAGONS_EYE_MODE.getKey().getDisplayName().getString();
            String flightToggleKeyName = KeyBindings.DRAGON_FLIGHT_TOGGLE_KEY.getKey().getDisplayName().getString();

            // 简短描述
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip11")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip12",
                            dragonBreathKeyName)
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip13")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip14",
                            nightVisionKeyName)
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip15",
                            toggleModeKeyName)
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip16",
                            flightToggleKeyName)
                    .withStyle(ChatFormatting.DARK_AQUA));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.press_shift")
                    .withStyle(ChatFormatting.GRAY));

            // 在详细属性前添加魔力系统信息
            if (level != null && level.isClientSide) {
                Player player = net.minecraft.client.Minecraft.getInstance().player;
                if (player != null) {
                    player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                        if (cap instanceof DragonCapability dragonCap) {
                            // 只在启用了植物魔法的情况下显示魔力信息
                            if (ModConfig.USE_BOTANIA_MANA.get() && net.minecraftforge.fml.ModList.get().isLoaded("botania")) {
                                // 显示魔力系统类型
                                tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.mana_system", "Botania")
                                        .withStyle(ChatFormatting.GOLD));

                                // 通过能力来获取当前魔力值
                                float currentMana = dragonCap.getCurrentMana();
                                tooltip.add(Component.translatable(
                                        "item.trinketsandbaubles.dragons_ring.current_mana",
                                        (int) currentMana
                                ).withStyle(ChatFormatting.AQUA));
                            }
                        }
                    });
                }
            }
        }
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