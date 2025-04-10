package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.api.IDragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.SyncAllDragonStatesMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.SyncDragonBreathMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.PacketDistributor;

public class DragonCapability extends AbstractRaceCapability implements IDragonCapability {
    private boolean flightEnabled = true;
    private boolean nightVisionEnabled = false;
    private boolean dragonBreathActive = false;

    public DragonCapability(Player player) {
        super(player);
        this.scaleFactor = RaceAttributesConfig.DRAGON.DRAGON_SCALE_FACTOR.get().floatValue();
    }

    @Override
    protected void registerAttributeValues() {
        // 基础属性
        registerValue("MAX_HEALTH", RaceAttributesConfig.DRAGON.MAX_HEALTH::get);
        registerValue("FOLLOW_RANGE", RaceAttributesConfig.DRAGON.FOLLOW_RANGE::get);
        registerValue("KNOCKBACK_RESISTANCE", RaceAttributesConfig.DRAGON.KNOCKBACK_RESISTANCE::get);
        registerValue("MOVEMENT_SPEED", RaceAttributesConfig.DRAGON.MOVEMENT_SPEED::get);
        registerValue("FLYING_SPEED", RaceAttributesConfig.DRAGON.FLYING_SPEED::get);

        // 战斗相关
        registerValue("ATTACK_DAMAGE", RaceAttributesConfig.DRAGON.ATTACK_DAMAGE::get);
        registerValue("ATTACK_KNOCKBACK", RaceAttributesConfig.DRAGON.ATTACK_KNOCKBACK::get);
        registerValue("ATTACK_SPEED", RaceAttributesConfig.DRAGON.ATTACK_SPEED::get);

        // 防御相关
        registerValue("ARMOR", RaceAttributesConfig.DRAGON.ARMOR::get);
        registerValue("ARMOR_TOUGHNESS", RaceAttributesConfig.DRAGON.ARMOR_TOUGHNESS::get);

        // 特殊能力
        registerValue("LUCK", RaceAttributesConfig.DRAGON.LUCK::get);

        // Forge添加的属性
        registerValue("SWIM_SPEED", RaceAttributesConfig.DRAGON.SWIM_SPEED::get);
        registerValue("NAMETAG_DISTANCE", RaceAttributesConfig.DRAGON.NAMETAG_DISTANCE::get);
        registerValue("ENTITY_GRAVITY", RaceAttributesConfig.DRAGON.ENTITY_GRAVITY::get);
        registerValue("STEP_HEIGHT", RaceAttributesConfig.DRAGON.STEP_HEIGHT::get);
        registerValue("BLOCK_REACH", RaceAttributesConfig.DRAGON.BLOCK_REACH::get);
        registerValue("ENTITY_REACH", RaceAttributesConfig.DRAGON.ENTITY_REACH::get);
    }

    @Override
    public String getRaceName() {
        return "Dragon";
    }

    @Override
    public String getRaceId() {
        return "dragon";
    }

    @Override
    public float getManaBonus() {
        return RaceAttributesConfig.DRAGON.DRAGON_MANA_BONUS.get().floatValue();
    }

    @Override
    protected void onTick() {
        if (!isActive) return;

        player.addEffect(new MobEffectInstance(
                ModEffects.DRAGON.get(),
                30,
                0,
                false,
                false,
                false
        ));

        // 确保飞行状态正确
        if (tickCounter % 20 == 0) { // 每秒检查一次
            updateFlightAbility();
        }

        // 处理飞行魔力消耗
        if (flightEnabled && !player.isCreative() && player.getAbilities().flying) {
            if (tickCounter % RaceAttributesConfig.DRAGON.DRAGON_MANA_CHECK_INTERVAL.get() == 0) {
                float manaCost = RaceAttributesConfig.DRAGON.DRAGON_FLIGHT_MANA_COST.get().floatValue();
                if (ManaData.hasMana(player, manaCost)) {
                    ManaData.consumeMana(player, manaCost);
                } else {
                    player.getAbilities().flying = false;
                    player.getAbilities().mayfly = false;
                    player.onUpdateAbilities();

                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.displayClientMessage(
                                Component.translatable("message.trinketsandbaubles.dragon.no_mana")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                }
            }
        }

        // 添加火焰抗性
        player.addEffect(new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE,
                100,
                0,
                false,
                false
        ));

        // 处理夜视能力
        if (nightVisionEnabled) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    400,
                    0,
                    false,
                    false
            ));
        }

        // 处理龙息音效
        if (dragonBreathActive && !player.level().isClientSide) {
            if (player.getRandom().nextFloat() < 0.4f) {
                player.level().playSound(
                        null,
                        player,
                        SoundEvents.BLAZE_SHOOT,
                        SoundSource.PLAYERS,
                        0.3f,
                        0.7f + player.getRandom().nextFloat() * 0.3f
                );
            }

            if (player.getRandom().nextFloat() < 0.4f) {
                player.level().playSound(
                        null,
                        player,
                        SoundEvents.FIRE_AMBIENT,
                        SoundSource.PLAYERS,
                        0.2f,
                        0.8f + player.getRandom().nextFloat() * 0.2f
                );
            }
        }
    }

    public void handleGameModeChange(GameType newGameMode) {
        if (!player.isCreative()) {
            if (newGameMode != GameType.SURVIVAL) {
                player.getAbilities().setFlyingSpeed(0.05f);
            } else {
                player.getAbilities().setFlyingSpeed(0.05f * RaceAttributesConfig.DRAGON.DRAGON_FLIGHT_SPEED.get().floatValue());
            }
            player.onUpdateAbilities();
        }
    }

    @Override
    public void toggleFlight() {
        if (!isActive) return; // 如果能力未激活，直接返回

        flightEnabled = !flightEnabled;
        updateFlightAbility();

        // 发送反馈消息
        if (player instanceof ServerPlayer serverPlayer) {
            Component message = Component.translatable(
                    flightEnabled ?
                            "message.trinketsandbaubles.dragon.flight.enabled" :
                            "message.trinketsandbaubles.dragon.flight.disabled"
            ).withStyle(flightEnabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);
            serverPlayer.displayClientMessage(message, true);
        }
        sync(); // 确保同步状态
    }

    @Override
    public void toggleNightVision() {
        nightVisionEnabled = !nightVisionEnabled;

        // 发送反馈消息
        if (player instanceof ServerPlayer serverPlayer) {
            Component message = Component.translatable(
                    nightVisionEnabled ?
                            "message.trinketsandbaubles.dragon.night_vision.enabled" :
                            "message.trinketsandbaubles.dragon.night_vision.disabled"
            ).withStyle(nightVisionEnabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);
            serverPlayer.displayClientMessage(message, true);
        }

        if (!nightVisionEnabled) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }

        sync();
    }

    @Override
    public void toggleDragonBreath() {
        dragonBreathActive = !dragonBreathActive;
        // 同步到客户端
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                    new SyncDragonBreathMessage(dragonBreathActive, serverPlayer.getId())
            );
        }
    }
    @Override
    public void setActive(boolean active) {
        if (this.isActive == active) return;

        if (!active) {
            // 先移除夜视效果
            if (nightVisionEnabled) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }

            // 同步到客户端
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                        new SyncAllDragonStatesMessage(false, false, false, serverPlayer.getId())
                );
            }

            // 调用父类的清理逻辑 - 清除属性
            super.setActive(false);

            // 最后再设置状态和处理飞行能力
            this.isActive = false;
            if (!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
            }
        } else {
            // 激活能力时的顺序
            super.setActive(true);
            this.isActive = true;
            // 确保飞行状态正确
            updateFlightAbility();
        }
    }

    @Override
    public void sync() {
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                    new SyncAllDragonStatesMessage(flightEnabled, nightVisionEnabled, dragonBreathActive, serverPlayer.getId())
            );
        }
        super.sync();
    }

    @Override
    public void updateFlightAbility() {
        // 首先检查能力是否激活
        if (!isActive) {
            if (!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
            }
            return;
        }

        if (!player.isCreative()) {
            // 根据飞行开关状态设置飞行能力
            player.getAbilities().mayfly = flightEnabled;
            if (!flightEnabled) {
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
            } else {
                player.getAbilities().setFlyingSpeed(0.05f * RaceAttributesConfig.DRAGON.DRAGON_FLIGHT_SPEED.get().floatValue());
            }
            player.onUpdateAbilities();
        }
    }

    @Override
    public boolean isFlightEnabled() {
        return flightEnabled;
    }

    @Override
    public boolean isNightVisionEnabled() {
        return nightVisionEnabled;
    }

    @Override
    public boolean isDragonBreathActive() {
        return dragonBreathActive;
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        // 龙族不需要特殊的破坏方块逻辑
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("FlightEnabled", flightEnabled);
        tag.putBoolean("NightVisionEnabled", nightVisionEnabled);
        tag.putBoolean("DragonBreathActive", dragonBreathActive);
    }

    @Override
    protected void loadAdditional(CompoundTag tag) {
        super.loadAdditional(tag);
        flightEnabled = tag.contains("FlightEnabled") ? tag.getBoolean("FlightEnabled") : true;
        nightVisionEnabled = tag.contains("NightVisionEnabled") ? tag.getBoolean("NightVisionEnabled") : false;
        dragonBreathActive = tag.contains("DragonBreathActive") ? tag.getBoolean("DragonBreathActive") : false;
    }
}