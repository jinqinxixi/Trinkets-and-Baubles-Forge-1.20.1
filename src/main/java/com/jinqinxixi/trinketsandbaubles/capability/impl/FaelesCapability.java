package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.api.IFaelesCapability;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class FaelesCapability extends AbstractRaceCapability implements IFaelesCapability {
    private static final UUID ATTACK_DAMAGE_UNARMED_UUID = UUID.fromString("b2461c37-8d2e-4a4d-95ac-d2169c49182a");

    public FaelesCapability(Player player) {
        super(player);
        this.scaleFactor = RaceAttributesConfig.FAELES.FAELES_SCALE_FACTOR.get().floatValue();
    }

    @Override
    protected void registerAttributeValues() {
        // 基础属性
        registerValue("MAX_HEALTH", RaceAttributesConfig.FAELES.MAX_HEALTH::get);
        registerValue("FOLLOW_RANGE", RaceAttributesConfig.FAELES.FOLLOW_RANGE::get);
        registerValue("KNOCKBACK_RESISTANCE", RaceAttributesConfig.FAELES.KNOCKBACK_RESISTANCE::get);
        registerValue("MOVEMENT_SPEED", RaceAttributesConfig.FAELES.MOVEMENT_SPEED::get);
        registerValue("FLYING_SPEED", RaceAttributesConfig.FAELES.FLYING_SPEED::get);

        // 战斗相关
        registerValue("ATTACK_DAMAGE", RaceAttributesConfig.FAELES.ATTACK_DAMAGE::get);
        registerValue("ATTACK_KNOCKBACK", RaceAttributesConfig.FAELES.ATTACK_KNOCKBACK::get);
        registerValue("ATTACK_SPEED", RaceAttributesConfig.FAELES.ATTACK_SPEED::get);

        // 防御相关
        registerValue("ARMOR", RaceAttributesConfig.FAELES.ARMOR::get);
        registerValue("ARMOR_TOUGHNESS", RaceAttributesConfig.FAELES.ARMOR_TOUGHNESS::get);

        // 特殊能力
        registerValue("LUCK", RaceAttributesConfig.FAELES.LUCK::get);

        // Forge添加的属性
        registerValue("SWIM_SPEED", RaceAttributesConfig.FAELES.SWIM_SPEED::get);
        registerValue("NAMETAG_DISTANCE", RaceAttributesConfig.FAELES.NAMETAG_DISTANCE::get);
        registerValue("ENTITY_GRAVITY", RaceAttributesConfig.FAELES.ENTITY_GRAVITY::get);
        registerValue("STEP_HEIGHT", RaceAttributesConfig.FAELES.STEP_HEIGHT::get);
        registerValue("BLOCK_REACH", RaceAttributesConfig.FAELES.BLOCK_REACH::get);
        registerValue("ENTITY_REACH", RaceAttributesConfig.FAELES.ENTITY_REACH::get);

    }

    @Override
    public float getManaBonus() {
        return RaceAttributesConfig.FAELES.FAELES_MANA_BONUS.get().floatValue();
    }

    @Override
    public String getRaceName() {
        return "Faeles";
    }

    @Override
    public String getRaceId() {
        return "faeles";
    }

    @Override
    protected void onTick() {
        if (!isActive) return;

        player.addEffect(new MobEffectInstance(
                ModEffects.FAELES.get(),
                30,
                0,
                false,
                false,
                false
        ));

        // 更新护甲减速效果
        updateArmorPenalties();
        // 更新徒手伤害
        updateUnarmedDamage();
    }
    private void updateUnarmedDamage() {
        // 检查主手和副手是否为空
        boolean isUnarmed = player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty();

        var attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);

        if (attackDamage != null) {
            boolean hasUnarmedModifier = attackDamage.getModifier(ATTACK_DAMAGE_UNARMED_UUID) != null;

            // 只有在完全空手时才给予徒手加成
            if (isUnarmed && !hasUnarmedModifier) {
                addAttributeModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UNARMED_UUID,
                        "Faeles Unarmed Damage",
                        RaceAttributesConfig.FAELES.FAELES_UNARMED_DAMAGE.get(),
                        AttributeModifier.Operation.ADDITION);
            } else if (!isUnarmed && hasUnarmedModifier) {
                removeAttributeModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UNARMED_UUID);
            }
        }
    }

    private void updateArmorPenalties() {
        int nonLeatherArmorCount = 0;
        for (ItemStack armorItem : player.getArmorSlots()) {
            if (!armorItem.isEmpty() && armorItem.getItem() instanceof ArmorItem armor) {
                if (armor.getMaterial() != ArmorMaterials.LEATHER) {
                    nonLeatherArmorCount++;
                }
            }
        }

        var movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            // 清除现有的减速效果
            for (int i = 0; i < 4; i++) {
                UUID armorPenaltyUUID = UUID.fromString("a5923e8d-1c7f-4b6a-b5e9-9d3c7f4a8d2" + i);
                movementSpeed.removeModifier(armorPenaltyUUID);
            }

            // 添加新的减速效果
            if (nonLeatherArmorCount > 0) {
                for (int i = 0; i < nonLeatherArmorCount; i++) {  // 修正这里的变量名
                    UUID armorPenaltyUUID = UUID.fromString("a5923e8d-1c7f-4b6a-b5e9-9d3c7f4a8d2" + i);
                    addAttributeModifier(player, Attributes.MOVEMENT_SPEED, armorPenaltyUUID,
                            "Armor Speed Penalty " + (i + 1),
                            RaceAttributesConfig.FAELES.FAELES_ARMOR_SPEED_PENALTY.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL);
                }
            }
        }
    }

    @Override
    public void removeAttributes() {
        super.removeAttributes();
        var attributes = new net.minecraft.world.entity.ai.attributes.Attribute[] {
                Attributes.ATTACK_DAMAGE  // 只保留徒手伤害的属性
        };
        var uuids = new UUID[] {
                ATTACK_DAMAGE_UNARMED_UUID
        };

        for (int i = 0; i < attributes.length; i++) {
            removeAttributeModifier(player, attributes[i], uuids[i]);
        }

        // 护甲减速效果单独处理
        var movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            for (int i = 0; i < 4; i++) {
                UUID armorPenaltyUUID = UUID.fromString("a5923e8d-1c7f-4b6a-b5e9-9d3c7f4a8d2" + i);
                movementSpeed.removeModifier(armorPenaltyUUID);
            }
        }

        player.setHealth(player.getHealth());
    }

    @Override
    public void forceRemoveAllModifiers() {
        removeAttributes();
    }


    @Override
    public void handleWallClimb() {
        handleWallClimbInternal(
                RaceAttributesConfig.FAELES.FAELES_CLIMB_SPEED.get(),
                RaceAttributesConfig.FAELES.FAELES_CLIMB_HORIZONTAL_DRAG.get()
        );
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        // Faeles 不需要特殊的破坏方块逻辑
    }

    public void onJump() {
        if (!isActive) return;
        Vec3 motion = player.getDeltaMovement();
        double multiplier = 1.0 + RaceAttributesConfig.FAELES.FAELES_JUMP_BOOST.get();
        player.setDeltaMovement(motion.x, motion.y * multiplier, motion.z);
    }

    public void onDrinkMilk() {
        if (!isActive) return;
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 3600, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, 0));
    }

    @Override
    public void validateAndFixAttributes() {
        if (!isActive) return;

        validateAndFixAttribute(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UNARMED_UUID,
                RaceAttributesConfig.FAELES.FAELES_UNARMED_DAMAGE.get(), "Faeles Unarmed Damage",
                AttributeModifier.Operation.ADDITION);

        // 验证护甲减速效果
        updateArmorPenalties();
    }
}