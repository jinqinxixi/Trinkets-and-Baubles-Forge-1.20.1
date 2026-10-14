package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.capability.attribute.AttributeRegistry;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import com.jinqinxixi.trinketsandbaubles.util.RaceRingUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
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

public class TaurusRingItem extends ModifiableBaubleItem
{
    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers()
    {
        return MODIFIERS;
    }

    public TaurusRingItem(Properties properties)
    {
        super(properties);
    }

    private static boolean isEquipped(LivingEntity entity)
    {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof TaurusRingItem))
                .isPresent();
    }

    private void applyTaurusBuff(LivingEntity entity)
    {
        if (!(entity instanceof ServerPlayer serverPlayer))
        {
            return;
        }
        if (RaceRingUtil.hasMultipleRaceRings(serverPlayer))
        {
            serverPlayer.getCapability(ModCapabilities.TAURUS_CAPABILITY).ifPresent(cap -> {
                if (cap.isActive())
                {
                    cap.setActive(false);
                }
            });
            return;
        }

        serverPlayer.getCapability(ModCapabilities.TAURUS_CAPABILITY).ifPresent(cap -> {
            if (!cap.isActive())
            {
                AbstractRaceCapability.clearAllRaceAbilities(serverPlayer);
                cap.setActive(true);
            }
        });
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack)
    {
        super.curioTick(slotContext, stack);
        LivingEntity entity = slotContext.entity();

        if (entity instanceof ServerPlayer serverPlayer)
        {
            if (RaceRingUtil.hasMultipleRaceRings(serverPlayer))
            {
                serverPlayer.getCapability(ModCapabilities.TAURUS_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive())
                    {
                        cap.setActive(false);
                    }
                });
                return;
            }

            if (isEquipped(entity))
            {
                serverPlayer.getCapability(ModCapabilities.TAURUS_CAPABILITY).ifPresent(cap -> {
                    if (!cap.isActive())
                    {
                        applyTaurusBuff(entity);
                    }
                });
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack)
    {
        super.onEquip(slotContext, prevStack, stack);
        LivingEntity entity = slotContext.entity();

        if (entity instanceof ServerPlayer serverPlayer)
        {
            if (RaceRingUtil.hasMultipleRaceRings(serverPlayer))
            {
                return;
            }
            if (isEquipped(entity))
            {
                applyTaurusBuff(entity);
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack)
    {
        super.onUnequip(slotContext, newStack, stack);
        LivingEntity entity = slotContext.entity();

        if (!isEquipped(entity) && entity instanceof ServerPlayer serverPlayer)
        {
            serverPlayer.getCapability(ModCapabilities.TAURUS_CAPABILITY).ifPresent(cap -> {
                if (cap.isActive())
                {
                    if (serverPlayer.isDeadOrDying())
                    {
                        cap.setActive(false);
                        CompoundTag playerData = serverPlayer.getPersistentData();
                        if (playerData.contains("TaurusCapability"))
                        {
                            playerData.remove("TaurusCapability");
                        }
                    }
                    else
                    {
                        cap.setActive(false);
                        CompoundTag playerData = serverPlayer.getPersistentData();
                        String savedRace = playerData.getString("SavedRace");
                        if (!savedRace.isEmpty())
                        {
                            RaceRingUtil.activateRace(serverPlayer, savedRace);
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack)
    {
        return true;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack)
    {
        return new ICurio.SoundInfo(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag)
    {
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown())
        {
            for (Map.Entry<String, AttributeRegistry.AttributeEntry> entry : AttributeRegistry.getAll().entrySet())
            {
                try
                {
                    String attributeName = entry.getKey();
                    double value = 0;
                    switch (attributeName)
                    {
                        case "MAX_HEALTH" -> value = RaceAttributesConfig.TAURUS.MAX_HEALTH.get();
                        case "FOLLOW_RANGE" -> value = RaceAttributesConfig.TAURUS.FOLLOW_RANGE.get();
                        case "MOVEMENT_SPEED" -> value = RaceAttributesConfig.TAURUS.MOVEMENT_SPEED.get();
                        case "ATTACK_SPEED" -> value = RaceAttributesConfig.TAURUS.ATTACK_SPEED.get();
                        case "ATTACK_DAMAGE" -> value = RaceAttributesConfig.TAURUS.ATTACK_DAMAGE.get();
                        case "SWIM_SPEED" -> value = RaceAttributesConfig.TAURUS.SWIM_SPEED.get();
                        case "FLYING_SPEED" -> value = RaceAttributesConfig.TAURUS.FLYING_SPEED.get();
                        case "ENTITY_GRAVITY" -> value = RaceAttributesConfig.TAURUS.ENTITY_GRAVITY.get();
                        case "BLOCK_REACH" -> value = RaceAttributesConfig.TAURUS.BLOCK_REACH.get();
                        case "ENTITY_REACH" -> value = RaceAttributesConfig.TAURUS.ENTITY_REACH.get();
                        case "NAMETAG_DISTANCE" -> value = RaceAttributesConfig.TAURUS.NAMETAG_DISTANCE.get();
                        case "ARMOR" -> value = RaceAttributesConfig.TAURUS.ARMOR.get();
                        case "ARMOR_TOUGHNESS" -> value = RaceAttributesConfig.TAURUS.ARMOR_TOUGHNESS.get();
                        case "KNOCKBACK_RESISTANCE" -> value = RaceAttributesConfig.TAURUS.KNOCKBACK_RESISTANCE.get();
                        case "ATTACK_KNOCKBACK" -> value = RaceAttributesConfig.TAURUS.ATTACK_KNOCKBACK.get();
                        case "LUCK" -> value = RaceAttributesConfig.TAURUS.LUCK.get();
                        case "STEP_HEIGHT" -> value = RaceAttributesConfig.TAURUS.STEP_HEIGHT.get();
                    }

                    if (value != 0)
                    {
                        AttributeRegistry.AttributeEntry attr = entry.getValue();
                        String displayText;
                        if (attr.isPercentage())
                        {
                            displayText = String.format("%s %s%.1f%%",
                                    Component.translatable(attr.getTranslationKey()).getString(),
                                    value > 0 ? "+" : "",
                                    value * 100);
                        }
                        else
                        {
                            displayText = String.format("%s %s%.2f",
                                    Component.translatable(attr.getTranslationKey()).getString(),
                                    value > 0 ? "+" : "",
                                    value);
                        }
                        tooltip.add(Component.literal(displayText)
                                .withStyle(value > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
                    }
                }
                catch (Exception ignored)
                {
                }
            }
        }
        else
        {
            tooltip.add(Component.translatable("item.trinketsandbaubles.taurus_ring.tooltip11")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.trinketsandbaubles.taurus_ring.tooltip12")
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("item.trinketsandbaubles.taurus_ring.tooltip13",
                            KeyBindings.RACE_ABILITY_KEY.getKey().getDisplayName().getString())
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.trinketsandbaubles.taurus_ring.press_shift")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
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
}