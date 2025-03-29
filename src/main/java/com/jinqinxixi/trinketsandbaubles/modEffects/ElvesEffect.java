package com.jinqinxixi.trinketsandbaubles.modEffects;


import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.items.ModItem;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.util.RaceScaleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class ElvesEffect extends MobEffect {

    private static final String ORIGINAL_MAX_MANA_KEY = "ElvesOriginalMaxMana";
    private static final String MANA_BONUS_TAG = "ElvesManaBonusApplied";
    private static final String CRYSTAL_BONUS_TAG = "CrystalManaBonus"; // 水晶加成标记



    public ElvesEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x98FB98); // 淡绿色
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        // 使用工具类设置体型缩放
        if (pLivingEntity != null) {
            RaceScaleHelper.setSmoothModelScale(pLivingEntity,
                    ModConfig.ELVES_SCALE_FACTOR.get().floatValue(),20);
        }
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC",
                ModConfig.ELVES_ATTACK_SPEED.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                "55FCED67-E92A-486E-9800-B47F202C4386",
                ModConfig.ELVES_ATTACK_DAMAGE.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "91AEAA56-376B-4498-935B-2F7F68070635",
                ModConfig.ELVES_MOVEMENT_SPEED.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);



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
        MobEffectInstance effect = player.getEffect(ModEffects.ELVES.get());

        // 如果玩家有精灵露效果
        if (effect != null) {
            // 直接移除当前效果
            player.removeEffect(ModEffects.ELVES.get());
            RaceScaleHelper.setModelScale(player,
                    ModConfig.ELVES_SCALE_FACTOR.get().floatValue());
            // 直接应用一个新的永久效果
            player.addEffect(new MobEffectInstance(
                    ModEffects.ELVES.get(),
                    -1, // 永久持续
                    0,  // 0级效果
                    false,
                    false,
                    false
            ));
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

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Player player) {
            CompoundTag data = player.getPersistentData();


            // 处理魔力加成
            if (!data.contains(MANA_BONUS_TAG)) {
                int currentMaxMana = ManaData.getMaxMana(player);
                int crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                int permanentDecrease = data.getInt("PermanentManaDecrease");

                int baseMaxMana = currentMaxMana - crystalBonus + permanentDecrease;
                data.putInt(ORIGINAL_MAX_MANA_KEY, baseMaxMana);

                // 使用配置的魔力加成值
                int newMaxMana = baseMaxMana - permanentDecrease + crystalBonus + ModConfig.ELVES_MANA_BONUS.get();
                ManaData.setMaxMana(player, newMaxMana);
                data.putBoolean(MANA_BONUS_TAG, true);
            }

            // 处理森林增益效果
            boolean isInForestNow = isInForest(player);
            var movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            var attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);

            if (movementSpeed != null && attackSpeed != null) {
                // 移除现有的森林增益效果
                movementSpeed.removeModifier(UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"));
                attackSpeed.removeModifier(UUID.fromString("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC"));

                // 如果在森林中，应用新的增益效果
                if (isInForestNow) {
                    try {
                        // 获取基础属性值
                        double baseMovementSpeed = player.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
                        double baseAttackSpeed = player.getAttributeBaseValue(Attributes.ATTACK_SPEED);

                        // 计算实际的增益值（基础值 * 配置的百分比）
                        double movementSpeedBonus = baseMovementSpeed * ModConfig.ELVES_FOREST_MOVEMENT_SPEED.get();
                        double attackSpeedBonus = baseAttackSpeed * ModConfig.ELVES_FOREST_ATTACK_SPEED.get();

                        // 添加移动速度增益
                        movementSpeed.addTransientModifier(
                                new AttributeModifier(
                                        UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"),
                                        "Forest Movement Speed Bonus",
                                        movementSpeedBonus,
                                        AttributeModifier.Operation.ADDITION
                                )
                        );

                        // 添加攻击速度增益
                        attackSpeed.addTransientModifier(
                                new AttributeModifier(
                                        UUID.fromString("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC"),
                                        "Forest Attack Speed Bonus",
                                        attackSpeedBonus,
                                        AttributeModifier.Operation.ADDITION
                                )
                        );

                        TrinketsandBaublesMod.LOGGER.debug("Applied forest bonuses to player: {}, Movement Speed: +{}%, Attack Speed: +{}%",
                                player.getName().getString(),
                                ModConfig.ELVES_FOREST_MOVEMENT_SPEED.get() * 100,
                                ModConfig.ELVES_FOREST_ATTACK_SPEED.get() * 100);

                    } catch (IllegalArgumentException e) {
                        TrinketsandBaublesMod.LOGGER.error("Error applying forest bonuses: {}", e.getMessage());
                    }
                }
            }
        }
    }

    // 处理效果移除时的魔力值恢复
    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {

        // 使用工具类重置体型
        if (pLivingEntity != null) {
            RaceScaleHelper.setSmoothModelScale(pLivingEntity, 1.0f, 20); // 1秒过渡时间
        }
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

        if (pLivingEntity instanceof Player player) {
            CompoundTag data = player.getPersistentData();
            if (data.contains(MANA_BONUS_TAG)) {
                if (data.contains(ORIGINAL_MAX_MANA_KEY)) {
                    int baseMaxMana = data.getInt(ORIGINAL_MAX_MANA_KEY);
                    int crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                    int permanentDecrease = data.getInt("PermanentManaDecrease");

                    // 恢复到基础值，考虑永久减少和水晶加成
                    int restoredMana = baseMaxMana - permanentDecrease + crystalBonus;
                    ManaData.setMaxMana(player, restoredMana);
                }
                // 清理标记
                data.remove(MANA_BONUS_TAG);
                data.remove(ORIGINAL_MAX_MANA_KEY);
                data.remove("ElvesEffect");
            }

            // 移除森林增益
            var movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            var attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
            if (movementSpeed != null && attackSpeed != null) {
                movementSpeed.removeModifier(UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"));
                attackSpeed.removeModifier(UUID.fromString("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC"));
            }
        }
        // 强制同步玩家属性
        if (pLivingEntity instanceof Player player) {
            // 强制同步生命值
            player.setHealth(player.getHealth());
        }
    }

    // 处理玩家死亡时保存效果数据
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            var effect = player.getEffect(ModEffects.ELVES.get());
            if (effect != null) {
                CompoundTag playerData = player.getPersistentData();
                CompoundTag effectData = new CompoundTag();

                // 保存效果数据
                effectData.putInt("Duration", effect.getDuration());
                effectData.putInt("Amplifier", effect.getAmplifier());

                // 保存当前实际的最大魔力值
                effectData.putInt("CurrentMaxMana", ManaData.getMaxMana(player));

                // 保存所有魔力修改因素
                if (playerData.contains(ORIGINAL_MAX_MANA_KEY)) {
                    effectData.putInt(ORIGINAL_MAX_MANA_KEY,
                            playerData.getInt(ORIGINAL_MAX_MANA_KEY));
                }
                if (playerData.contains(CRYSTAL_BONUS_TAG)) {
                    effectData.putInt(CRYSTAL_BONUS_TAG,
                            playerData.getInt(CRYSTAL_BONUS_TAG));
                }
                if (playerData.contains("PermanentManaDecrease")) {
                    effectData.putInt("PermanentManaDecrease",
                            playerData.getInt("PermanentManaDecrease"));
                }

                playerData.put("ElvesEffect", effectData);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player player = event.getEntity();
        CompoundTag originalData = original.getPersistentData();

        if (originalData.contains("ElvesEffect")) {
            CompoundTag effectData = originalData.getCompound("ElvesEffect");

            // 获取服务器实例以延迟应用效果
            net.minecraft.server.MinecraftServer server = player.level().getServer();
            if (server != null) {
                // 延迟1tick后应用效果
                server.tell(new net.minecraft.server.TickTask(
                        server.getTickCount() + 1,
                        () -> {
                            // 重新应用精灵效果
                            player.addEffect(new MobEffectInstance(
                                    ModEffects.ELVES.get(),
                                    effectData.getInt("Duration"),
                                    effectData.getInt("Amplifier"),
                                    false,
                                    false,  // 改为true使效果可见
                                    false   // 改为true使图标可见
                            ));

                            // 恢复所有魔力相关数据
                            CompoundTag newData = player.getPersistentData();

                            // 保存原始魔力值
                            if (effectData.contains(ORIGINAL_MAX_MANA_KEY)) {
                                newData.putInt(ORIGINAL_MAX_MANA_KEY,
                                        effectData.getInt(ORIGINAL_MAX_MANA_KEY));
                            }

                            // 保存水晶加成
                            if (effectData.contains(CRYSTAL_BONUS_TAG)) {
                                newData.putInt(CRYSTAL_BONUS_TAG,
                                        effectData.getInt(CRYSTAL_BONUS_TAG));
                            }

                            // 保存永久减少值
                            if (effectData.contains("PermanentManaDecrease")) {
                                newData.putInt("PermanentManaDecrease",
                                        effectData.getInt("PermanentManaDecrease"));
                            }

                            // 直接设置为死亡时的实际魔力值
                            if (effectData.contains("CurrentMaxMana")) {
                                ManaData.setMaxMana(player, effectData.getInt("CurrentMaxMana"));
                                newData.putBoolean(MANA_BONUS_TAG, true);
                            }
                        }
                ));
            }
        }
    }

    // 检查是否在森林生物群系
    private boolean isInForest(Player player) {
        return player.level().getBiome(player.blockPosition()).is(BiomeTags.IS_FOREST);
    }

    // 处理弓箭伤害增加
    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof Arrow) {
            if (event.getSource().getEntity() instanceof Player player) {
                if (player.hasEffect(ModEffects.ELVES.get()) && player.isCrouching()) {
                    float newDamage = event.getAmount() * ModConfig.ELVES_BOW_DAMAGE_BOOST.get().floatValue();
                    event.setAmount(newDamage);
                }
            }
        }
    }
}