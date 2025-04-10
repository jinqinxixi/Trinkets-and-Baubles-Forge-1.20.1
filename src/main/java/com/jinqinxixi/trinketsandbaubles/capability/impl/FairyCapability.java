package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.api.IFairyCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.SyncAllDragonStatesMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.PacketDistributor;

public class FairyCapability extends AbstractRaceCapability implements IFairyCapability {

    private boolean flightEnabled = true;

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

        updateFlightAbility();
    }

    @Override
    public void setActive(boolean active) {
        if (this.isActive == active) return;

        if (!active) {
            // 只对生存模式玩家禁用飞行
            if (!player.isCreative() && !player.isSpectator()) {
                disableFairyFlight();
            }
        } else {
            updateFlightAbility();
        }

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
        if (!isActive || !flightEnabled) {
            return; // 如果能力未激活或飞行被禁用，直接跳过
        }

        // 只有生存模式玩家需要我们的飞行逻辑
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = true;
            // 只对生存模式玩家修改飞行速度
            player.getAbilities().setFlyingSpeed(0.05f * RaceAttributesConfig.FAIRY.FAIRY_DEW_FLIGHT_SPEED.get().floatValue());
            player.onUpdateAbilities();
        } else if (flightEnabled) {
            // 对于创造和旁观模式，只给予飞行权限，不修改速度
            player.getAbilities().mayfly = true;
            // 保持原版速度
            player.getAbilities().setFlyingSpeed(0.05f);
            player.onUpdateAbilities();
        }
    }

    @Override
    public boolean isFlightEnabled() {
        return flightEnabled;
    }

    @Override
    public void toggleFlight() {
        if (!isActive) return;

        flightEnabled = !flightEnabled;

        if (!flightEnabled) {
            // 如果禁用飞行，只对生存模式玩家执行飞行关闭逻辑
            if (!player.isCreative() && !player.isSpectator()) {
                disableFairyFlight();
            }
        } else {
            // 启用飞行时，根据游戏模式更新飞行能力
            updateFlightAbility();
        }

        // 发送飞行状态反馈消息
        if (player instanceof ServerPlayer serverPlayer) {
            Component message = Component.translatable(
                    flightEnabled ?
                            "message.trinketsandbaubles.dragon.flight.enabled" :
                            "message.trinketsandbaubles.dragon.flight.disabled"
            ).withStyle(flightEnabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);
            serverPlayer.displayClientMessage(message, true);
        }

        sync();
    }


    private void disableFairyFlight() {
        // 只对生存模式玩家禁用飞行
        if (!player.isCreative() && !player.isSpectator() && (player.getAbilities().mayfly || player.getAbilities().flying)) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(0.05f); // 恢复默认飞行速度
            player.onUpdateAbilities();
        }
    }


    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        // 精灵族不需要特殊的破坏方块逻辑
    }

    @Override
    public void sync() {
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                    new SyncAllDragonStatesMessage(flightEnabled, false, false, serverPlayer.getId())
            );
        }
        super.sync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("FlightEnabled", flightEnabled);
    }

    @Override
    protected void loadAdditional(CompoundTag tag) {
        super.loadAdditional(tag);
        flightEnabled = tag.contains("FlightEnabled") ? tag.getBoolean("FlightEnabled") : true;
    }
}