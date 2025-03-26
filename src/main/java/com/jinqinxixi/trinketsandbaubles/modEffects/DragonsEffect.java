    package com.jinqinxixi.trinketsandbaubles.modEffects;

    import com.jinqinxixi.trinketsandbaubles.config.Config;
    import com.jinqinxixi.trinketsandbaubles.items.ModItem;
    import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
    import com.jinqinxixi.trinketsandbaubles.capability.shrink.ModCapabilities;
    import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
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

        @Override
        public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
            // 攻击伤害
            this.addAttributeModifier(
                    Attributes.ATTACK_DAMAGE,
                    "d141ef28-51c6-4b47-8a0d-6946e841c132",
                    Config.DRAGON_ATTACK_DAMAGE_BOOST.get(),
                    AttributeModifier.Operation.MULTIPLY_BASE
            );

            // 最大生命值
            this.addAttributeModifier(
                    Attributes.MAX_HEALTH,
                    "dc3b4b8c-a02c-4bd8-82e9-204088927d1f",
                    Config.DRAGON_MAX_HEALTH_BOOST.get(),
                    AttributeModifier.Operation.MULTIPLY_BASE
            );

            // 护甲韧性
            this.addAttributeModifier(
                    Attributes.ARMOR_TOUGHNESS,
                    "8fc5e73c-2cf2-4729-8128-d99f49aa37f2",
                    Config.DRAGON_ARMOR_TOUGHNESS.get(),
                    AttributeModifier.Operation.MULTIPLY_BASE
            );

            super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

            // 强制同步玩家属性
            if (pLivingEntity instanceof Player player) {
                // 强制同步生命值
                player.setHealth(player.getHealth());
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            MobEffectInstance effect = player.getEffect(ModEffects.DRAGON.get());

            // 如果玩家有精灵露效果
            if (effect != null) {
                // 直接移除当前效果
                player.removeEffect(ModEffects.DRAGON.get());

                // 直接应用一个新的永久效果
                player.addEffect(new MobEffectInstance(
                        ModEffects.DRAGON.get(),
                        -1, // 永久持续
                        0,  // 0级效果
                        false,
                        false,
                        false
                ));
            }
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof Player player) {
                float currentMana = ManaData.getMana(player);

                // 计算每tick消耗的魔力
                float manaCostPerTick = Config.DRAGON_FLIGHT_MANA_COST.get().floatValue() /
                        Config.DRAGON_MANA_CHECK_INTERVAL.get().floatValue();

                if (!player.isCreative()) {
                    boolean hasEnoughMana = currentMana >= manaCostPerTick;
                    boolean shouldUpdateAbilities = false;

                    if (player.getAbilities().flying) {
                        if (hasEnoughMana) {
                            ManaData.consumeMana(player, manaCostPerTick);
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

                    if (player.getAbilities().flying) {
                        player.getAbilities().setFlyingSpeed(0.05f * Config.DRAGON_FLIGHT_SPEED.get().floatValue());
                    }
                }

                player.refreshDimensions();

                CompoundTag data = player.getPersistentData();
                if (!data.contains(BONUS_TAG)) {
                    int currentMaxMana = ManaData.getMaxMana(player);
                    int crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                    int permanentDecrease = data.getInt("PermanentManaDecrease");

                    int baseMaxMana = currentMaxMana - crystalBonus + permanentDecrease;
                    data.putInt(ORIGINAL_MANA_TAG, baseMaxMana);

                    // 使用配置的魔力加成值
                    int newMaxMana = baseMaxMana - permanentDecrease + crystalBonus + Config.DRAGON_MANA_BONUS.get();
                    ManaData.setMaxMana(player, newMaxMana);
                    data.putBoolean(BONUS_TAG, true);
                }

                // 给予防火效果
                player.addEffect(new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        100,
                        0,
                        false,
                        false
                ));

                // 检查夜视状态并应用效果
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
                // 处理缩放效果
                entity.getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap -> {
                    if (!cap.isShrunk()) {
                        float scaleFactor = Config.DRAGON_SCALE_FACTOR.get().floatValue();
                        TrinketsandBaublesMod.LOGGER.debug("Applying shrink effect to player: {}, setting scale to: {}",
                                player.getName().getString(), scaleFactor);
                        cap.setScale(scaleFactor);
                        cap.shrink(entity);
                    }
                });
            }
        }

        @SubscribeEvent
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (event.getEntity() instanceof Player player) {
                MobEffectInstance effect = player.getEffect(ModEffects.DRAGON.get());
                if (effect != null) {
                    CompoundTag playerData = player.getPersistentData();

                    // 直接在根级别设置标记，而不是在嵌套的 CompoundTag 中
                    playerData.putBoolean("HasDragonEffect", true);

                    // 保存关键数据
                    CompoundTag effectData = new CompoundTag();
                    effectData.putBoolean("DragonNightVisionEnabled",
                            playerData.getBoolean("DragonNightVisionEnabled"));
                    effectData.putInt("CurrentMaxMana", ManaData.getMaxMana(player));

                    // 保存魔力相关数据
                    if (playerData.contains(ORIGINAL_MANA_TAG)) {
                        effectData.putInt(ORIGINAL_MANA_TAG, playerData.getInt(ORIGINAL_MANA_TAG));
                    }
                    if (playerData.contains(CRYSTAL_BONUS_TAG)) {
                        effectData.putInt(CRYSTAL_BONUS_TAG, playerData.getInt(CRYSTAL_BONUS_TAG));
                    }
                    if (playerData.contains("PermanentManaDecrease")) {
                        effectData.putInt("PermanentManaDecrease",
                                playerData.getInt("PermanentManaDecrease"));
                    }

                    // 保存效果数据
                    playerData.put("DragonEffectData", effectData);

                    // 添加日志以便调试
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

            // 检查直接标记
            if (originalData.getBoolean("HasDragonEffect")) {
                // 复制标记到新玩家
                player.getPersistentData().putBoolean("HasDragonEffect", true);

                // 如果有效果数据，则复制它
                if (originalData.contains("DragonEffectData")) {
                    CompoundTag effectData = originalData.getCompound("DragonEffectData");
                    player.getPersistentData().put("DragonEffectData", effectData.copy());

                    // 添加日志
                    TrinketsandBaublesMod.LOGGER.info("Dragon effect data copied for player: {} (Clone)",
                            player.getName().getString());

                    // 确保在服务器端运行
                    if (!player.level().isClientSide && player.level().getServer() != null) {
                        player.level().getServer().tell(new net.minecraft.server.TickTask(
                                player.level().getServer().getTickCount() + 2,
                                () -> {
                                    // 应用效果
                                    player.addEffect(new MobEffectInstance(
                                            ModEffects.DRAGON.get(),
                                            -1,
                                            0,
                                            false,
                                            false,
                                            false
                                    ));

                                    CompoundTag newData = player.getPersistentData();

                                    // 恢复夜视状态
                                    boolean nightVision = effectData.getBoolean("DragonNightVisionEnabled");
                                    newData.putBoolean("DragonNightVisionEnabled", nightVision);

                                    // 恢复魔力数据
                                    if (effectData.contains(ORIGINAL_MANA_TAG)) {
                                        newData.putInt(ORIGINAL_MANA_TAG, effectData.getInt(ORIGINAL_MANA_TAG));
                                    }
                                    if (effectData.contains(CRYSTAL_BONUS_TAG)) {
                                        newData.putInt(CRYSTAL_BONUS_TAG, effectData.getInt(CRYSTAL_BONUS_TAG));
                                    }
                                    if (effectData.contains("PermanentManaDecrease")) {
                                        newData.putInt("PermanentManaDecrease",
                                                effectData.getInt("PermanentManaDecrease"));
                                    }

                                    // 设置魔力值
                                    if (effectData.contains("CurrentMaxMana")) {
                                        ManaData.setMaxMana(player, effectData.getInt("CurrentMaxMana"));
                                        newData.putBoolean(BONUS_TAG, true);
                                    }

                                    // 强制刷新
                                    player.refreshDimensions();

                                    // 添加日志
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
            super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

            if (pLivingEntity instanceof Player player) {
                CompoundTag data = player.getPersistentData();
                if (data.contains(BONUS_TAG)) {
                    if (data.contains(ORIGINAL_MANA_TAG)) {
                        int baseMaxMana = data.getInt(ORIGINAL_MANA_TAG);
                        int crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                        int permanentDecrease = data.getInt("PermanentManaDecrease");

                        // 恢复到基础值，但保持水晶加成和永久减少的效果
                        int restoredMana = baseMaxMana - permanentDecrease + crystalBonus;
                        ManaData.setMaxMana(player, Math.max(0, restoredMana));
                    }
                    // 移除龙之效果相关标记
                    data.remove(BONUS_TAG);
                    data.remove(ORIGINAL_MANA_TAG);
                }

                // 恢复默认飞行速度
                player.getAbilities().setFlyingSpeed(0.05f);

                // 如果不是创造模式，移除飞行能力
                if (!player.isCreative()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                }
                // 确保更新能力
                player.onUpdateAbilities();
            }
            // 处理缩放效果的移除
            pLivingEntity.getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap -> {
                if (cap.isShrunk()) {
                    TrinketsandBaublesMod.LOGGER.debug("De-shrinking entity: {}, current scale was: {}",
                            pLivingEntity.getName().getString(), cap.scale());
                    cap.deShrink(pLivingEntity);
                }
            });
            // 强制同步玩家属性
            if (pLivingEntity instanceof Player player) {
                // 强制同步生命值
                player.setHealth(player.getHealth());
            }
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public List<ItemStack> getCurativeItems() {
            // 创建一个新的物品列表
            List<ItemStack> items = new ArrayList<>();
            // 只添加恢复药剂作为治疗物品
            items.add(new ItemStack(ModItem.RESTORATION_SERUM.get()));
            return items;
        }

        @SubscribeEvent
        public static void onEntityTick(LivingEvent.LivingTickEvent event) {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            // 检查是否正在喷火
            boolean isBreathing = player.getPersistentData().getBoolean("DragonBreathActive");

            if (!isBreathing) {
                return;
            }

            // 确保在服务端且玩家有龙效果
            if (!player.level().isClientSide && player.hasEffect(ModEffects.DRAGON.get())) {
                Level level = player.level();
                // 只保留声音效果
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
    }