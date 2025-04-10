package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.api.IElvesCapability;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import java.util.UUID;

public class ElvesCapability extends AbstractRaceCapability implements IElvesCapability {
    private static final UUID FOREST_MOVEMENT_SPEED_UUID = UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635");
    private static final UUID FOREST_ATTACK_SPEED_UUID = UUID.fromString("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC");

    public ElvesCapability(Player player) {
        super(player);
        this.scaleFactor = RaceAttributesConfig.ELVES.ELVES_SCALE_FACTOR.get().floatValue();
    }

    @Override
    protected void registerAttributeValues() {
        // 基础属性
        registerValue("MAX_HEALTH", RaceAttributesConfig.ELVES.MAX_HEALTH::get);
        registerValue("FOLLOW_RANGE", RaceAttributesConfig.ELVES.FOLLOW_RANGE::get);
        registerValue("KNOCKBACK_RESISTANCE", RaceAttributesConfig.ELVES.KNOCKBACK_RESISTANCE::get);
        registerValue("MOVEMENT_SPEED", RaceAttributesConfig.ELVES.MOVEMENT_SPEED::get);
        registerValue("FLYING_SPEED", RaceAttributesConfig.ELVES.FLYING_SPEED::get);

        // 战斗相关
        registerValue("ATTACK_DAMAGE", RaceAttributesConfig.ELVES.ATTACK_DAMAGE::get);
        registerValue("ATTACK_KNOCKBACK", RaceAttributesConfig.ELVES.ATTACK_KNOCKBACK::get);
        registerValue("ATTACK_SPEED", RaceAttributesConfig.ELVES.ATTACK_SPEED::get);

        // 防御相关
        registerValue("ARMOR", RaceAttributesConfig.ELVES.ARMOR::get);
        registerValue("ARMOR_TOUGHNESS", RaceAttributesConfig.ELVES.ARMOR_TOUGHNESS::get);

        // 特殊能力
        registerValue("LUCK", RaceAttributesConfig.ELVES.LUCK::get);

        // Forge添加的属性
        registerValue("SWIM_SPEED", RaceAttributesConfig.ELVES.SWIM_SPEED::get);
        registerValue("NAMETAG_DISTANCE", RaceAttributesConfig.ELVES.NAMETAG_DISTANCE::get);
        registerValue("ENTITY_GRAVITY", RaceAttributesConfig.ELVES.ENTITY_GRAVITY::get);
        registerValue("STEP_HEIGHT", RaceAttributesConfig.ELVES.STEP_HEIGHT::get);
        registerValue("BLOCK_REACH", RaceAttributesConfig.ELVES.BLOCK_REACH::get);
        registerValue("ENTITY_REACH", RaceAttributesConfig.ELVES.ENTITY_REACH::get);
    }

    @Override
    public float getManaBonus() {
        return RaceAttributesConfig.ELVES.ELVES_MANA_BONUS.get().floatValue();
    }

    @Override
    public String getRaceName() {
        return "Elves";
    }

    @Override
    public String getRaceId() {
        return "elves";
    }

    @Override
    protected void onTick() {
        if (!isActive) return;

        player.addEffect(new MobEffectInstance(
                ModEffects.ELVES.get(),
                30,
                0,
                false,
                false,
                false
        ));

        // 每tick都更新森林加成
        updateForestBonuses();
    }

    private void updateForestBonuses() {
        // 检查是否在森林中
        boolean inForest = isInForest();

        var movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);

        if (movementSpeed != null && attackSpeed != null) {
            boolean hasMovementBonus = movementSpeed.getModifier(FOREST_MOVEMENT_SPEED_UUID) != null;
            boolean hasAttackBonus = attackSpeed.getModifier(FOREST_ATTACK_SPEED_UUID) != null;

            // 在森林中且没有加成时添加加成
            if (inForest && !hasMovementBonus) {
                addAttributeModifier(player, Attributes.MOVEMENT_SPEED, FOREST_MOVEMENT_SPEED_UUID,
                        "Forest Movement Speed Bonus",
                        RaceAttributesConfig.ELVES.ELVES_FOREST_MOVEMENT_SPEED.get(),
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            // 不在森林中但有加成时移除加成
            else if (!inForest && hasMovementBonus) {
                removeAttributeModifier(player, Attributes.MOVEMENT_SPEED, FOREST_MOVEMENT_SPEED_UUID);
            }

            // 攻击速度加成同理
            if (inForest && !hasAttackBonus) {
                addAttributeModifier(player, Attributes.ATTACK_SPEED, FOREST_ATTACK_SPEED_UUID,
                        "Forest Attack Speed Bonus",
                        RaceAttributesConfig.ELVES.ELVES_FOREST_ATTACK_SPEED.get(),
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            else if (!inForest && hasAttackBonus) {
                removeAttributeModifier(player, Attributes.ATTACK_SPEED, FOREST_ATTACK_SPEED_UUID);
            }
        }
    }

    @Override
    public void validateAndFixAttributes() {
        if (!isActive) return;

        // 调用父类的验证
        super.validateAndFixAttributes();

        // 验证森林加成
        updateForestBonuses();
    }

    @Override
    public void removeAttributes() {
        // 先调用父类移除基础属性
        super.removeAttributes();

        // 移除森林加成
        removeAttributeModifier(player, Attributes.MOVEMENT_SPEED, FOREST_MOVEMENT_SPEED_UUID);
        removeAttributeModifier(player, Attributes.ATTACK_SPEED, FOREST_ATTACK_SPEED_UUID);

        player.setHealth(player.getHealth());
    }

    @Override
    public void forceRemoveAllModifiers() {
        removeAttributes();
    }

    @Override
    public boolean isInForest() {
        return player.level().getBiome(player.blockPosition()).is(BiomeTags.IS_FOREST);
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        // 精灵族不需要特殊的破坏方块逻辑
    }
}