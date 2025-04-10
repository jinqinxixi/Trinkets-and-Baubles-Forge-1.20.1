package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.api.IFairyCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public class FairyCapability extends AbstractRaceCapability implements IFairyCapability {

    public FairyCapability(Player player) {
        super(player);
        this.scaleFactor = RaceAttributesConfig.FAIRY.FAIRY_DEW_SCALE_FACTOR.get().floatValue();
    }

    @Override
    protected void registerAttributeValues() {
        // 基础属性
        registerValue("MAX_HEALTH", RaceAttributesConfig.FAIRY.MAX_HEALTH::get);
        registerValue("FOLLOW_RANGE", RaceAttributesConfig.FAIRY.FOLLOW_RANGE::get);
        registerValue("KNOCKBACK_RESISTANCE", RaceAttributesConfig.FAIRY.KNOCKBACK_RESISTANCE::get);
        registerValue("MOVEMENT_SPEED", RaceAttributesConfig.FAIRY.MOVEMENT_SPEED::get);
        registerValue("FLYING_SPEED", RaceAttributesConfig.FAIRY.FLYING_SPEED::get);

        // 战斗相关
        registerValue("ATTACK_DAMAGE", RaceAttributesConfig.FAIRY.ATTACK_DAMAGE::get);
        registerValue("ATTACK_KNOCKBACK", RaceAttributesConfig.FAIRY.ATTACK_KNOCKBACK::get);
        registerValue("ATTACK_SPEED", RaceAttributesConfig.FAIRY.ATTACK_SPEED::get);

        // 防御相关
        registerValue("ARMOR", RaceAttributesConfig.FAIRY.ARMOR::get);
        registerValue("ARMOR_TOUGHNESS", RaceAttributesConfig.FAIRY.ARMOR_TOUGHNESS::get);

        // 特殊能力
        registerValue("LUCK", RaceAttributesConfig.FAIRY.LUCK::get);

        // Forge添加的属性
        registerValue("SWIM_SPEED", RaceAttributesConfig.FAIRY.SWIM_SPEED::get);
        registerValue("NAMETAG_DISTANCE", RaceAttributesConfig.FAIRY.NAMETAG_DISTANCE::get);
        registerValue("ENTITY_GRAVITY", RaceAttributesConfig.FAIRY.ENTITY_GRAVITY::get);
        registerValue("STEP_HEIGHT", RaceAttributesConfig.FAIRY.STEP_HEIGHT::get);
        registerValue("BLOCK_REACH", RaceAttributesConfig.FAIRY.BLOCK_REACH::get);
        registerValue("ENTITY_REACH", RaceAttributesConfig.FAIRY.ENTITY_REACH::get);
    }

    @Override
    public String getRaceName() {
        return "FAIRY";
    }

    @Override
    public String getRaceId() {
        return "fairy";
    }

    @Override
    public float getManaBonus() {
        return RaceAttributesConfig.FAIRY.FAIRY_DEW_MANA_BONUS.get().floatValue();
    }

    @Override
    protected void onTick() {
        if (!isActive) return;

        player.addEffect(new MobEffectInstance(
                ModEffects.FAIRY_DEW.get(),
                30,  // 持续时间1秒，确保效果持续
                0,    // 等级0
                false, // 不显示粒子
                false, // 不显示图标
                false  // 不显示环境效果
        ));

        // 每5个tick执行一次飞行能力更新
        if (tickCounter % 5 == 0) {
            updateFlightAbility();
        }
    }

    @Override
    public void setActive(boolean active) {
        if (this.isActive == active) return;

        if (!active) {
            if (!player.isCreative()) {
                // 禁用飞行
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
            }
        }

        // 调用父类的 setActive
        super.setActive(active);
    }

    @Override
    public void handleWallClimb() {
        if (!isActive) return;

        double climbSpeed = RaceAttributesConfig.FAIRY.FAIRY_DEW_CLIMB_SPEED.get();
        double horizontalDrag = RaceAttributesConfig.FAIRY.FAIRY_DEW_CLIMB_HORIZONTAL_DRAG.get();

        handleWallClimbInternal(climbSpeed, horizontalDrag);
    }

    @Override
    public void updateFlightAbility() {
        if (!player.isCreative() && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.getAbilities().setFlyingSpeed(0.05f * RaceAttributesConfig.FAIRY.FAIRY_DEW_FLIGHT_SPEED.get().floatValue());
            player.onUpdateAbilities();
        }
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        // 精灵族不需要特殊的破坏方块逻辑
    }
}