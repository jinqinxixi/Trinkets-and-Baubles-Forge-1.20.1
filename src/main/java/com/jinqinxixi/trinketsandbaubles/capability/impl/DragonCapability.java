package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.PacketDistributor;

public class DragonCapability extends AbstractRaceCapability implements IDragonCapability {
    // 魔力系统接口
    private interface ManaSystem {
        float getMana(Player player, ItemStack stack);
        void consumeMana(Player player, float amount, ItemStack stack);
        boolean hasMana(Player player, float amount, ItemStack stack);
    }
    public float getCurrentMana() {
        return getManaSystem().getMana(player, BotaniaManaSystem.DUMMY_RECEIVER);
    }

    private class IronsSpellsManaSystem implements ManaSystem {
        @Override
        public float getMana(Player player, ItemStack stack) {
            return io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player).getMana();
        }

        @Override
        public void consumeMana(Player player, float amount, ItemStack stack) {
            // 对于铁魔法，确保消耗量向上取整，最小为1
            float actualAmount = Math.max(1.0f, (float) Math.ceil(amount));
            io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player).addMana(-actualAmount);
        }

        @Override
        public boolean hasMana(Player player, float amount, ItemStack stack) {
            // 对于铁魔法，确保检查量向上取整，最小为1
            float actualAmount = Math.max(1.0f, (float) Math.ceil(amount));
            return getMana(player, stack) >= actualAmount;
        }
    }


    private class InternalManaSystem implements ManaSystem {
        @Override
        public float getMana(Player player, ItemStack stack) {
            return ManaData.getMana(player);
        }

        @Override
        public void consumeMana(Player player, float amount, ItemStack stack) {
            ManaData.consumeMana(player, amount);
        }

        @Override
        public boolean hasMana(Player player, float amount, ItemStack stack) {
            return ManaData.hasMana(player, amount);
        }
    }

    private class BotaniaManaSystem implements ManaSystem {
        private static final ItemStack DUMMY_RECEIVER = new ItemStack(net.minecraft.world.item.Items.STICK); // 创建一个虚拟接收者

        @Override
        public float getMana(Player player, ItemStack stack) {
            var handler = vazkii.botania.api.mana.ManaItemHandler.instance();
            return handler.requestMana(DUMMY_RECEIVER, player, Integer.MAX_VALUE, false);
        }

        @Override
        public void consumeMana(Player player, float amount, ItemStack stack) {
            var handler = vazkii.botania.api.mana.ManaItemHandler.instance();
            handler.requestMana(DUMMY_RECEIVER, player, (int)amount, true);
        }

        @Override
        public boolean hasMana(Player player, float amount, ItemStack stack) {
            var handler = vazkii.botania.api.mana.ManaItemHandler.instance();
            return handler.requestMana(DUMMY_RECEIVER, player, (int)amount, false) >= amount;
        }
    }

    private boolean flightEnabled = true;
    public boolean dragonBreathActive = false;

    public DragonCapability(Player player) {
        super(player);
        this.scaleFactor = RaceAttributesConfig.DRAGON.DRAGON_SCALE_FACTOR.get().floatValue();
    }

    private ManaSystem getManaSystem() {
        if (shouldUseIronsSpellsMana()) {
            return new IronsSpellsManaSystem();
        }
        if (shouldUseBotaniaMana()) {
            return new BotaniaManaSystem();
        }
        return new InternalManaSystem();
    }

    private boolean shouldUseBotaniaMana() {
        return net.minecraftforge.fml.ModList.get().isLoaded("botania") && ModConfig.USE_BOTANIA_MANA.get();
    }

    private boolean shouldUseIronsSpellsMana() {
        try {
            Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData");
            return ModConfig.USE_IRONS_SPELLS_MANA.get();
        } catch (ClassNotFoundException e) {
            return false;
        }
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

        updateFlightAbility();

        player.addEffect(new MobEffectInstance(
                ModEffects.DRAGON.get(),
                30,
                0,
                false,
                false,
                false
        ));


        // 处理飞行魔力消耗
        if (flightEnabled && !player.isCreative() && !player.isSpectator() && player.getAbilities().flying) {
            float manaCost = RaceAttributesConfig.DRAGON.DRAGON_FLIGHT_MANA_COST.get().floatValue();
            ManaSystem manaSystem = getManaSystem();

            // 根据魔力系统类型使用不同的消耗逻辑
            boolean hasSufficientMana = false;
            if (manaSystem instanceof BotaniaManaSystem) {
                // 植物魔法使用间隔检查
                if (tickCounter % RaceAttributesConfig.DRAGON.DRAGON_MANA_CHECK_INTERVAL.get() == 0) {
                    if (manaSystem.hasMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER)) {
                        manaSystem.consumeMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER);
                        hasSufficientMana = true;
                    }
                } else {
                    // 在间隔期间也要检查是否有足够的魔力
                    hasSufficientMana = manaSystem.hasMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER);
                }
            } else {
                // 其他魔力系统每tick检查
                float tickCost = Math.max(1.0f, manaCost / 20f); // 确保每tick至少消耗1点魔力
                if (manaSystem instanceof IronsSpellsManaSystem) {
                    // 铁魔法系统特殊处理：向上取整确保最小消耗为1
                    tickCost = (float) Math.ceil(tickCost);
                }
                if (manaSystem.hasMana(player, tickCost, BotaniaManaSystem.DUMMY_RECEIVER)) {
                    manaSystem.consumeMana(player, tickCost, BotaniaManaSystem.DUMMY_RECEIVER);
                    hasSufficientMana = true;
                }
            }

            // 如果没有足够的魔力，禁用飞行并显示消息
            if (!hasSufficientMana) {
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

    @Override
    public void toggleFlight() {
        if (!isActive) return;

        flightEnabled = !flightEnabled;

        if (!flightEnabled && !player.isCreative()) {
            disableDragonFlight();
        }

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

    @Override
    public void toggleDragonBreath() {
        TrinketsandBaublesMod.LOGGER.info("toggleDragonBreath called. Current state: {}, Using Iron's Spells: {}",
                dragonBreathActive, shouldUseIronsSpellsMana());


        boolean shouldActivate = !dragonBreathActive;

        if (shouldActivate) {
            ManaSystem manaSystem = getManaSystem();
            float manaCost = RaceAttributesConfig.DRAGON.DRAGON_BREATH_MANA_COST.get().floatValue();

            if (!manaSystem.hasMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER)) {
                TrinketsandBaublesMod.LOGGER.info("Not enough mana to activate dragon breath");
                return;
            }
        }

        // 明确设置状态而不是切换
        setDragonBreathActive(shouldActivate);
    }

    // 添加新方法
    public boolean setDragonBreathActive(boolean active) {
        // 如果要激活龙息，先检查魔力
        if (active && !dragonBreathActive) {
            ManaSystem manaSystem = getManaSystem();
            float manaCost = RaceAttributesConfig.DRAGON.DRAGON_BREATH_MANA_COST.get().floatValue();

            if (!manaSystem.hasMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER)) {
                TrinketsandBaublesMod.LOGGER.info("Not enough mana to activate dragon breath");

                // 添加玩家提示消息
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.trinketsandbaubles.dragon.no_mana_breath")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
                return false;
            }
        }

        if (dragonBreathActive != active) {
            dragonBreathActive = active;

            if (!player.level().isClientSide) {
                TrinketsandBaublesMod.LOGGER.info("Dragon breath state set to: {}", dragonBreathActive);

                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                            new SyncDragonBreathMessage(dragonBreathActive, serverPlayer.getId())
                    );
                }
            }
        }
        return true;
    }

    @Override
    public void setActive(boolean active) {
        if (this.isActive == active) return;

        if (!active) {
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                        new SyncAllDragonStatesMessage(
                                false,  // flightEnabled
                                false,  // dragonBreathActive
                                serverPlayer.getId()
                        )
                );
            }

            super.setActive(false);
            this.isActive = false;

            if (!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
            }
        } else {
            super.setActive(true);
            this.isActive = true;
            updateFlightAbility();
        }
    }

    @Override
    public void sync() {
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                    new SyncAllDragonStatesMessage(flightEnabled, dragonBreathActive, serverPlayer.getId())
            );
        }
        super.sync();
    }

    @Override
    public boolean isFlightEnabled() {
        return flightEnabled;
    }

    @Override
    public boolean isDragonBreathActive() {
        return dragonBreathActive;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("FlightEnabled", flightEnabled);
        tag.putBoolean("DragonBreathActive", dragonBreathActive);
    }

    @Override
    protected void loadAdditional(CompoundTag tag) {
        super.loadAdditional(tag);
        flightEnabled = tag.contains("FlightEnabled") ? tag.getBoolean("FlightEnabled") : true;
        dragonBreathActive = tag.contains("DragonBreathActive") ? tag.getBoolean("DragonBreathActive") : false;
    }

    private void disableDragonFlight() {
        if (!player.isCreative() && (player.getAbilities().mayfly || player.getAbilities().flying)) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(0.05f);
            player.onUpdateAbilities();
        }
    }

    public void handleGameModeChange(GameType newGameMode) {
        if (!player.isCreative() && !player.isSpectator()) {
            if (newGameMode != GameType.SURVIVAL) {
                player.getAbilities().setFlyingSpeed(0.05f);
            } else {
                player.getAbilities().setFlyingSpeed(0.05f * RaceAttributesConfig.DRAGON.DRAGON_FLIGHT_SPEED.get().floatValue());
            }
            player.onUpdateAbilities();
        }
    }

    @Override
    public void updateFlightAbility() {
        if (!isActive || !flightEnabled) {
            return;
        }

        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = true;
            player.getAbilities().setFlyingSpeed(0.05f * RaceAttributesConfig.DRAGON.DRAGON_FLIGHT_SPEED.get().floatValue());
            player.onUpdateAbilities();
        }
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        // 龙族不需要特殊的破坏方块逻辑
    }
}