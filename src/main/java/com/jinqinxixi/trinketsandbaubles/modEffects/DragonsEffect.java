package com.jinqinxixi.trinketsandbaubles.modeffects;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.items.ModItem;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.util.RaceScaleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import static com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class DragonsEffect extends MobEffect {

    private static final String BONUS_TAG = "DragonManaBonus";
    private static final String ORIGINAL_MANA_TAG = "DragonOriginalMaxMana";
    private static final String CRYSTAL_BONUS_TAG = "CrystalManaBonus"; //记录水晶增加的魔力值

    public DragonsEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF4500);
    }
/*
    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        if (pLivingEntity != null) {
            RaceScaleHelper.setSmoothModelScale(pLivingEntity,
                    ModConfig.DRAGON_SCALE_FACTOR.get().floatValue(), 20);
        }

        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                "d141ef28-51c6-4b47-8a0d-6946e841c132",
                ModConfig.DRAGON_ATTACK_DAMAGE_BOOST.get(),
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                "dc3b4b8c-a02c-4bd8-82e9-204088927d1f",
                ModConfig.DRAGON_MAX_HEALTH_BOOST.get(),
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        this.addAttributeModifier(
                Attributes.ARMOR_TOUGHNESS,
                "8fc5e73c-2cf2-4729-8128-d99f49aa37f2",
                ModConfig.DRAGON_ARMOR_TOUGHNESS.get(),
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

        if (pLivingEntity instanceof Player player) {
            if (!player.isCreative()) {
                player.getAbilities().setFlyingSpeed(0.05f * ModConfig.DRAGON_FLIGHT_SPEED.get().floatValue());
                player.onUpdateAbilities();
            }
            player.setHealth(player.getHealth());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        initializeFlightEnabled(player);
        MobEffectInstance effect = player.getEffect(ModEffects.DRAGON.get());

        if (effect != null) {
            player.removeEffect(ModEffects.DRAGON.get());
            RaceScaleHelper.setModelScale(player,
                    ModConfig.DRAGON_SCALE_FACTOR.get().floatValue());
            player.addEffect(new MobEffectInstance(
                    ModEffects.DRAGON.get(),
                    -1,
                    0,
                    false,
                    false,
                    false
            ));
        }
    }

    private static void initializeFlightEnabled(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains("DragonFlightEnabled")) {
            // 添加检查：只有拥有龙效果的玩家才能获得飞行能力
            if (player.hasEffect(ModEffects.DRAGON.get())) {
                data.putBoolean("DragonFlightEnabled", true);
                if (!player.isCreative()) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
            } else {
                // 如果没有龙效果，确保飞行被禁用
                data.putBoolean("DragonFlightEnabled", false);
                if (!player.isCreative()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.getAbilities().setFlyingSpeed(0.05f);
                    player.onUpdateAbilities();
                }
            }
        } else {
            // 即使已经有标记，也要检查是否应该有飞行能力
            boolean shouldHaveFlight = player.hasEffect(ModEffects.DRAGON.get());
            if (!shouldHaveFlight && !player.isCreative()) {
                data.putBoolean("DragonFlightEnabled", false);
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            float currentMana = ManaData.getMana(player);
            boolean flightEnabled = player.getPersistentData().getBoolean("DragonFlightEnabled");

            if (flightEnabled) {
                if (!player.isCreative() && !player.isSpectator()) {
                    if (player.tickCount % ModConfig.DRAGON_MANA_CHECK_INTERVAL.get() == 0) {
                        float manaCost = ModConfig.DRAGON_FLIGHT_MANA_COST.get().floatValue();
                        boolean hasEnoughMana = currentMana >= manaCost;
                        boolean shouldUpdateAbilities = false;

                        if (player.getAbilities().flying) {
                            if (hasEnoughMana) {
                                ManaData.consumeMana(player, manaCost);
                            } else {
                                player.getAbilities().flying = false;
                                player.getAbilities().mayfly = false;
                                shouldUpdateAbilities = true;

                                if (player instanceof ServerPlayer serverPlayer) {
                                    serverPlayer.displayClientMessage(
                                            Component.translatable("message.trinketsandbaubles.dragon.no_mana")
                                                    .withStyle(ChatFormatting.RED),
                                            true
                                    );
                                }
                            }
                        }

                        if (player.getAbilities().mayfly != hasEnoughMana) {
                            player.getAbilities().mayfly = hasEnoughMana;
                            shouldUpdateAbilities = true;
                        }

                        if (shouldUpdateAbilities) {
                            player.onUpdateAbilities();
                        }
                    }

                    if (!player.isCreative() && player.getAbilities().flying) {
                        player.getAbilities().setFlyingSpeed(0.05f * ModConfig.DRAGON_FLIGHT_SPEED.get().floatValue());
                    }
                }
            }

            player.refreshDimensions();

            CompoundTag data = player.getPersistentData();
            if (!data.contains(BONUS_TAG)) {
                float currentMaxMana = ManaData.getMaxMana(player);
                float crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                float permanentDecrease = data.getInt("PermanentManaDecrease");

                float baseMaxMana = currentMaxMana - crystalBonus + permanentDecrease;
                data.putFloat(ORIGINAL_MANA_TAG, baseMaxMana);

                float newMaxMana = baseMaxMana - permanentDecrease + crystalBonus +
                        ModConfig.DRAGON_MANA_BONUS.get().floatValue();
                ManaData.setMaxMana(player, newMaxMana);
                data.putBoolean(BONUS_TAG, true);
            }

            player.addEffect(new MobEffectInstance(
                    MobEffects.FIRE_RESISTANCE,
                    100,
                    0,
                    false,
                    false
            ));

            boolean nightVisionEnabled = player.getPersistentData().getBoolean("DragonNightVisionEnabled");
            if (nightVisionEnabled) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        400,
                        0,
                        false,
                        false
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        Player player = event.getEntity();

        if (player.hasEffect(ModEffects.DRAGON.get())) {
            if (event.getNewGameMode() != GameType.SURVIVAL) {
                player.getAbilities().setFlyingSpeed(0.05f);
            } else {
                player.getAbilities().setFlyingSpeed(0.05f * ModConfig.DRAGON_FLIGHT_SPEED.get().floatValue());
            }
            player.onUpdateAbilities();
        }
    }

    public static void toggleFlight(ServerPlayer player) {
        boolean isEnabled = player.getPersistentData().getBoolean("DragonFlightEnabled");

        if (!player.isCreative()) {
            player.getAbilities().mayfly = isEnabled;
            if (!isEnabled) {
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
            } else {
                player.getAbilities().setFlyingSpeed(0.05f * ModConfig.DRAGON_FLIGHT_SPEED.get().floatValue());
            }
            player.onUpdateAbilities();
        }

        Component message = Component.translatable(
                isEnabled ?
                        "message.trinketsandbaubles.dragon.flight.enabled" :
                        "message.trinketsandbaubles.dragon.flight.disabled"
        ).withStyle(isEnabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);

        player.displayClientMessage(message, true);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            MobEffectInstance effect = player.getEffect(ModEffects.DRAGON.get());
            if (effect != null) {
                CompoundTag playerData = player.getPersistentData();
                playerData.putBoolean("HasDragonEffect", true);

                CompoundTag effectData = new CompoundTag();
                effectData.putBoolean("DragonFlightEnabled",
                        playerData.getBoolean("DragonFlightEnabled"));
                effectData.putBoolean("DragonNightVisionEnabled",
                        playerData.getBoolean("DragonNightVisionEnabled"));
                effectData.putFloat("CurrentMaxMana", ManaData.getMaxMana(player));

                if (playerData.contains(ORIGINAL_MANA_TAG)) {
                    effectData.putFloat(ORIGINAL_MANA_TAG, playerData.getFloat(ORIGINAL_MANA_TAG));
                }
                if (playerData.contains(CRYSTAL_BONUS_TAG)) {
                    effectData.putInt(CRYSTAL_BONUS_TAG, playerData.getInt(CRYSTAL_BONUS_TAG));
                }
                if (playerData.contains("PermanentManaDecrease")) {
                    effectData.putInt("PermanentManaDecrease",
                            playerData.getInt("PermanentManaDecrease"));
                }

                playerData.put("DragonEffectData", effectData);

                TrinketsandBaublesMod.LOGGER.info("Dragon effect saved for player: {} (Death)",
                        player.getName().getString());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player player = event.getEntity();
        CompoundTag originalData = original.getPersistentData();

        player.getPersistentData().putBoolean("DragonFlightEnabled",
                originalData.getBoolean("DragonFlightEnabled"));

        if (originalData.getBoolean("HasDragonEffect")) {
            player.getPersistentData().putBoolean("HasDragonEffect", true);

            if (originalData.contains("DragonEffectData")) {
                CompoundTag effectData = originalData.getCompound("DragonEffectData");
                player.getPersistentData().put("DragonEffectData", effectData.copy());

                TrinketsandBaublesMod.LOGGER.info("Dragon effect data copied for player: {} (Clone)",
                        player.getName().getString());

                if (!player.level().isClientSide && player.level().getServer() != null) {
                    player.level().getServer().tell(new net.minecraft.server.TickTask(
                            player.level().getServer().getTickCount() + 2,
                            () -> {
                                player.addEffect(new MobEffectInstance(
                                        ModEffects.DRAGON.get(),
                                        -1,
                                        0,
                                        false,
                                        false,
                                        false
                                ));

                                CompoundTag newData = player.getPersistentData();

                                boolean nightVision = effectData.getBoolean("DragonNightVisionEnabled");
                                newData.putBoolean("DragonNightVisionEnabled", nightVision);

                                if (effectData.contains(ORIGINAL_MANA_TAG)) {
                                    newData.putFloat(ORIGINAL_MANA_TAG, effectData.getFloat(ORIGINAL_MANA_TAG));
                                }
                                if (effectData.contains(CRYSTAL_BONUS_TAG)) {
                                    newData.putInt(CRYSTAL_BONUS_TAG, effectData.getInt(CRYSTAL_BONUS_TAG));
                                }
                                if (effectData.contains("PermanentManaDecrease")) {
                                    newData.putInt("PermanentManaDecrease",
                                            effectData.getInt("PermanentManaDecrease"));
                                }

                                if (effectData.contains("CurrentMaxMana")) {
                                    ManaData.setMaxMana(player, effectData.getFloat("CurrentMaxMana"));
                                    newData.putBoolean(BONUS_TAG, true);
                                }

                                player.refreshDimensions();

                                TrinketsandBaublesMod.LOGGER.info("Dragon effect reapplied for player: {} (Clone)",
                                        player.getName().getString());
                            }
                    ));
                }
            }
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        if (pLivingEntity != null) {
            RaceScaleHelper.setSmoothModelScale(pLivingEntity, 1.0f, 20);
        }
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

        if (pLivingEntity instanceof Player player) {
            CompoundTag data = player.getPersistentData();
            boolean flightEnabled = data.getBoolean("DragonFlightEnabled");

            if (data.contains(BONUS_TAG)) {
                if (data.contains(ORIGINAL_MANA_TAG)) {
                    float baseMaxMana = data.getFloat(ORIGINAL_MANA_TAG);
                    float crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                    float permanentDecrease = data.getInt("PermanentManaDecrease");

                    float restoredMana = baseMaxMana - permanentDecrease + crystalBonus;
                    ManaData.setMaxMana(player, Math.max(0, restoredMana));
                }
                data.remove(BONUS_TAG);
                data.remove(ORIGINAL_MANA_TAG);
            }

            player.getAbilities().setFlyingSpeed(0.05f);

            if (!player.isCreative()) {
                player.getAbilities().mayfly = flightEnabled;
                if (!flightEnabled) {
                    player.getAbilities().flying = false;
                }
            }
            player.onUpdateAbilities();
            player.setHealth(player.getHealth());
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        List<ItemStack> items = new ArrayList<>();
        items.add(new ItemStack(ModItem.RESTORATION_SERUM.get()));
        return items;
    }

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        boolean isBreathing = player.getPersistentData().getBoolean("DragonBreathActive");

        if (!isBreathing) {
            return;
        }

        if (!player.level().isClientSide && player.hasEffect(ModEffects.DRAGON.get())) {
            Level level = player.level();
            if (player.getRandom().nextFloat() < 0.4f) {
                level.playSound(
                        null,
                        player,
                        SoundEvents.BLAZE_SHOOT,
                        SoundSource.PLAYERS,
                        0.3f,
                        0.7f + player.getRandom().nextFloat() * 0.3f
                );
            }

            if (player.getRandom().nextFloat() < 0.4f) {
                level.playSound(
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
    */
}