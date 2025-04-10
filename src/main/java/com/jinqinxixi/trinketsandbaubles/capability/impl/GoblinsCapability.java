package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.api.IGoblinsCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.sounds.SoundSource;

public class GoblinsCapability extends AbstractRaceCapability implements IGoblinsCapability {

    public GoblinsCapability(Player player) {
        super(player);
        this.scaleFactor = RaceAttributesConfig.GOBLINS.GOBLIN_SCALE_FACTOR.get().floatValue();
    }

    @Override
    protected void registerAttributeValues() {
        // 基础属性
        registerValue("MAX_HEALTH", RaceAttributesConfig.GOBLINS.MAX_HEALTH::get);
        registerValue("FOLLOW_RANGE", RaceAttributesConfig.GOBLINS.FOLLOW_RANGE::get);
        registerValue("KNOCKBACK_RESISTANCE", RaceAttributesConfig.GOBLINS.KNOCKBACK_RESISTANCE::get);
        registerValue("MOVEMENT_SPEED", RaceAttributesConfig.GOBLINS.MOVEMENT_SPEED::get);
        registerValue("FLYING_SPEED", RaceAttributesConfig.GOBLINS.FLYING_SPEED::get);

        // 战斗相关
        registerValue("ATTACK_DAMAGE", RaceAttributesConfig.GOBLINS.ATTACK_DAMAGE::get);
        registerValue("ATTACK_KNOCKBACK", RaceAttributesConfig.GOBLINS.ATTACK_KNOCKBACK::get);
        registerValue("ATTACK_SPEED", RaceAttributesConfig.GOBLINS.ATTACK_SPEED::get);

        // 防御相关
        registerValue("ARMOR", RaceAttributesConfig.GOBLINS.ARMOR::get);
        registerValue("ARMOR_TOUGHNESS", RaceAttributesConfig.GOBLINS.ARMOR_TOUGHNESS::get);

        // 特殊能力
        registerValue("LUCK", RaceAttributesConfig.GOBLINS.LUCK::get);


        // Forge添加的属性
        registerValue("SWIM_SPEED", RaceAttributesConfig.GOBLINS.SWIM_SPEED::get);
        registerValue("NAMETAG_DISTANCE", RaceAttributesConfig.GOBLINS.NAMETAG_DISTANCE::get);
        registerValue("ENTITY_GRAVITY", RaceAttributesConfig.GOBLINS.ENTITY_GRAVITY::get);
        registerValue("STEP_HEIGHT", RaceAttributesConfig.GOBLINS.STEP_HEIGHT::get);
        registerValue("BLOCK_REACH", RaceAttributesConfig.GOBLINS.BLOCK_REACH::get);
        registerValue("ENTITY_REACH", RaceAttributesConfig.GOBLINS.ENTITY_REACH::get);
    }

    @Override
    public String getRaceName() {
        return "GOBLINS";
    }

    @Override
    public String getRaceId() {
        return "goblins";
    }

    @Override
    public float getManaBonus() {
        return RaceAttributesConfig.GOBLINS.GOBLIN_MANA_PENALTY.get().floatValue();
    }

    @Override
    protected void onTick() {
        if (!isActive) return;

        player.addEffect(new MobEffectInstance(
                ModEffects.GOBLINS.get(),
                30,  // 持续时间1秒，确保效果持续
                0,    // 等级0
                false, // 不显示粒子
                false, // 不显示图标
                false  // 不显示环境效果
        ));

        if (player.getVehicle() instanceof Horse horse) {
            // 给玩家添加BUFF
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false));

            // 给马添加生命恢复效果
            if (horse.isAlive() && !horse.hasEffect(MobEffects.REGENERATION)) {
                horse.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, false));
            }
        }
    }

    public void handleMount(AbstractHorse horse) {
        if (isActive && !horse.level().isClientSide()) {
            // 使用官方方法装备鞍
            horse.equipSaddle(SoundSource.NEUTRAL);

            // 自动驯服并设置主人
            horse.setTamed(true);
            horse.setOwnerUUID(player.getUUID());
        }
    }

    public float handleDamage(float damage, boolean isFireOrExplosion) {
        if (isActive && isFireOrExplosion) {
            return damage * RaceAttributesConfig.GOBLINS.GOBLIN_DAMAGE_REDUCTION.get().floatValue();
        }
        return damage;
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        // 哥布林不需要特殊的破坏方块逻辑
    }
}