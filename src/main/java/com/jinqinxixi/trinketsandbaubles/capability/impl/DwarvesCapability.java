package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.api.IDwarvesCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

public class DwarvesCapability extends AbstractRaceCapability implements IDwarvesCapability {

    public DwarvesCapability(Player player) {
        super(player);
        this.scaleFactor = RaceAttributesConfig.DWARVES.DWARVES_SCALE_FACTOR.get().floatValue();

    }

    @Override
    protected void registerAttributeValues() {
        // 基础属性
        registerValue("MAX_HEALTH", RaceAttributesConfig.DWARVES.MAX_HEALTH::get);
        registerValue("FOLLOW_RANGE", RaceAttributesConfig.DWARVES.FOLLOW_RANGE::get);
        registerValue("KNOCKBACK_RESISTANCE", RaceAttributesConfig.DWARVES.KNOCKBACK_RESISTANCE::get);
        registerValue("MOVEMENT_SPEED", RaceAttributesConfig.DWARVES.MOVEMENT_SPEED::get);
        registerValue("FLYING_SPEED", RaceAttributesConfig.DWARVES.FLYING_SPEED::get);

        // 战斗相关
        registerValue("ATTACK_DAMAGE", RaceAttributesConfig.DWARVES.ATTACK_DAMAGE::get);
        registerValue("ATTACK_KNOCKBACK", RaceAttributesConfig.DWARVES.ATTACK_KNOCKBACK::get);
        registerValue("ATTACK_SPEED", RaceAttributesConfig.DWARVES.ATTACK_SPEED::get);

        // 防御相关
        registerValue("ARMOR", RaceAttributesConfig.DWARVES.ARMOR::get);
        registerValue("ARMOR_TOUGHNESS", RaceAttributesConfig.DWARVES.ARMOR_TOUGHNESS::get);

        // 特殊能力
        registerValue("LUCK", RaceAttributesConfig.DWARVES.LUCK::get);


        // Forge添加的属性
        registerValue("SWIM_SPEED", RaceAttributesConfig.DWARVES.SWIM_SPEED::get);
        registerValue("NAMETAG_DISTANCE", RaceAttributesConfig.DWARVES.NAMETAG_DISTANCE::get);
        registerValue("ENTITY_GRAVITY", RaceAttributesConfig.DWARVES.ENTITY_GRAVITY::get);
        registerValue("STEP_HEIGHT", RaceAttributesConfig.DWARVES.STEP_HEIGHT::get);
        registerValue("BLOCK_REACH", RaceAttributesConfig.DWARVES.BLOCK_REACH::get);
        registerValue("ENTITY_REACH", RaceAttributesConfig.DWARVES.ENTITY_REACH::get);
    }

    @Override
    public float getManaBonus() {
        return RaceAttributesConfig.DWARVES.DWARVES_MANA_BONUS.get().floatValue();
    }

    @Override
    public String getRaceName() {
        return "Dwarves";
    }

    @Override
    public String getRaceId() {
        return "dwarves";
    }

    @Override
    protected void onTick() {
        if (!isActive) return;

        player.addEffect(new MobEffectInstance(
                ModEffects.DWARVES.get(),
                30,  // 持续时间1秒，确保效果持续
                0,    // 等级0
                false, // 不显示粒子
                false, // 不显示图标
                false  // 不显示环境效果
        ));
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        if (!isActive) return;

        if (block.defaultBlockState().is(BlockTags.STONE_ORE_REPLACEABLES) ||
                block.defaultBlockState().is(BlockTags.DEEPSLATE_ORE_REPLACEABLES) ||
                block == Blocks.END_STONE) {
            spawnExperienceOrb(level, pos, 1);
        } else if (block.defaultBlockState().is(Tags.Blocks.ORES)) {
            int extraXp = player.getRandom().nextInt(3);
            if (extraXp > 0) {
                spawnExperienceOrb(level, pos, extraXp);
            }
        }
    }

    private void spawnExperienceOrb(ServerLevel level, BlockPos pos, int xp) {
        level.addFreshEntity(new ExperienceOrb(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, xp));
    }

    @Override
    public void forceRemoveAllModifiers() {
        removeAttributes();
    }
}