package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.api.ITitanCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TitanCapability extends AbstractRaceCapability implements ITitanCapability {

    public TitanCapability(Player player) {
        super(player);
        this.scaleFactor = RaceAttributesConfig.TITAN.TITAN_SCALE_FACTOR.get().floatValue();
    }

    @Override
    protected void registerAttributeValues() {
        // 基础属性
        registerValue("MAX_HEALTH", RaceAttributesConfig.TITAN.MAX_HEALTH::get);
        registerValue("FOLLOW_RANGE", RaceAttributesConfig.TITAN.FOLLOW_RANGE::get);
        registerValue("KNOCKBACK_RESISTANCE", RaceAttributesConfig.TITAN.KNOCKBACK_RESISTANCE::get);
        registerValue("MOVEMENT_SPEED", RaceAttributesConfig.TITAN.MOVEMENT_SPEED::get);
        registerValue("FLYING_SPEED", RaceAttributesConfig.TITAN.FLYING_SPEED::get);

        // 战斗相关
        registerValue("ATTACK_DAMAGE", RaceAttributesConfig.TITAN.ATTACK_DAMAGE::get);
        registerValue("ATTACK_KNOCKBACK", RaceAttributesConfig.TITAN.ATTACK_KNOCKBACK::get);
        registerValue("ATTACK_SPEED", RaceAttributesConfig.TITAN.ATTACK_SPEED::get);

        // 防御相关
        registerValue("ARMOR", RaceAttributesConfig.TITAN.ARMOR::get);
        registerValue("ARMOR_TOUGHNESS", RaceAttributesConfig.TITAN.ARMOR_TOUGHNESS::get);

        // 特殊能力
        registerValue("LUCK", RaceAttributesConfig.TITAN.LUCK::get);


        // Forge添加的属性
        registerValue("SWIM_SPEED", RaceAttributesConfig.TITAN.SWIM_SPEED::get);
        registerValue("NAMETAG_DISTANCE", RaceAttributesConfig.TITAN.NAMETAG_DISTANCE::get);
        registerValue("ENTITY_GRAVITY", RaceAttributesConfig.TITAN.ENTITY_GRAVITY::get);
        registerValue("STEP_HEIGHT", RaceAttributesConfig.TITAN.STEP_HEIGHT::get);
        registerValue("BLOCK_REACH", RaceAttributesConfig.TITAN.BLOCK_REACH::get);
        registerValue("ENTITY_REACH", RaceAttributesConfig.TITAN.ENTITY_REACH::get);
    }

    @Override
    public String getRaceId() {
        return "titan";
    }

    @Override
    public String getRaceName() {
        return "TITAN";
    }

    @Override
    public float getManaBonus() {
        return RaceAttributesConfig.TITAN.TITAN_MANA_MODIFIER.get().floatValue();
    }

    @Override
    protected void onTick() {
        if (!isActive) return;

        player.addEffect(new MobEffectInstance(
                ModEffects.TITAN.get(),
                30,  // 持续时间1秒，确保效果持续
                0,    // 等级0
                false, // 不显示粒子
                false, // 不显示图标
                false  // 不显示环境效果
        ));

        if (!player.level().isClientSide) {
            handleWaterMovement();
            handlePlantBreaking();
        }
    }

    public void handleJump() {
        if (isActive) {
            Vec3 motion = player.getDeltaMovement();
            double multiplier = 1.0 + RaceAttributesConfig.TITAN.TITAN_JUMP_BOOST.get();
            player.setDeltaMovement(motion.x, motion.y * multiplier, motion.z);
        }
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        if (isActive && !level.isClientSide()) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos checkPos = pos.offset(x, y, z);
                        if (!checkPos.equals(pos)) {
                            BlockState state = level.getBlockState(checkPos);
                            if (state.getBlock() == block && player.hasCorrectToolForDrops(state)) {
                                level.destroyBlock(checkPos, true);
                            }
                        }
                    }
                }
            }
        }
    }

    public void handleWaterMovement() {
        if (player.isInWater()) {
            player.setDeltaMovement(player.getDeltaMovement().add(
                    0, -RaceAttributesConfig.TITAN.TITAN_WATER_SINK_SPEED.get(), 0));
        }
    }

    public void handlePlantBreaking() {
        double width = player.getBbWidth();
        double height = player.getBbHeight();
        BlockPos playerPos = player.blockPosition();

        for (int x = (int) (-width - 1); x <= width + 1; x++) {
            for (int y = -1; y <= (int) height; y++) {
                for (int z = (int) (-width - 1); z <= width + 1; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    handlePlantAtPosition(checkPos);
                }
            }
        }
    }

    private void handlePlantAtPosition(BlockPos pos) {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (isBreakablePlant(block)) {
            if (block == Blocks.FARMLAND) {
                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            } else {
                level.destroyBlock(pos, true);
            }
        }
    }

    private boolean isBreakablePlant(Block block) {
        return block instanceof CropBlock ||
                block == Blocks.GRASS ||
                block == Blocks.TALL_GRASS ||
                block == Blocks.FERN ||
                block == Blocks.LARGE_FERN ||
                block == Blocks.DANDELION ||
                block == Blocks.POPPY ||
                block == Blocks.BLUE_ORCHID ||
                block == Blocks.ALLIUM ||
                block == Blocks.AZURE_BLUET ||
                block == Blocks.RED_TULIP ||
                block == Blocks.ORANGE_TULIP ||
                block == Blocks.WHITE_TULIP ||
                block == Blocks.PINK_TULIP ||
                block == Blocks.OXEYE_DAISY ||
                block == Blocks.CORNFLOWER ||
                block == Blocks.LILY_OF_THE_VALLEY ||
                block == Blocks.WHEAT ||
                block == Blocks.CARROTS ||
                block == Blocks.POTATOES ||
                block == Blocks.BEETROOTS ||
                block == Blocks.FARMLAND ||
                block == Blocks.SUGAR_CANE ||
                block == Blocks.BAMBOO ||
                block == Blocks.BAMBOO_SAPLING ||
                block == Blocks.SWEET_BERRY_BUSH ||
                block == Blocks.CAVE_VINES ||
                block == Blocks.CAVE_VINES_PLANT ||
                block == Blocks.GLOW_LICHEN ||
                block == Blocks.VINE ||
                block == Blocks.KELP ||
                block == Blocks.KELP_PLANT ||
                block == Blocks.SEAGRASS ||
                block == Blocks.TALL_SEAGRASS ||
                block == Blocks.OAK_SAPLING ||
                block == Blocks.SPRUCE_SAPLING ||
                block == Blocks.BIRCH_SAPLING ||
                block == Blocks.JUNGLE_SAPLING ||
                block == Blocks.ACACIA_SAPLING ||
                block == Blocks.DARK_OAK_SAPLING ||
                block == Blocks.MANGROVE_PROPAGULE;
    }
}