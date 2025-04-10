package com.jinqinxixi.trinketsandbaubles.modeffects;


import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.items.ModItem;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.util.RaceScaleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import static com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class FairyDewEffect extends MobEffect {
    private static final String BONUS_TAG = "FairyDewManaBonus";
    private static final String ORIGINAL_MANA_TAG = "OriginalMaxMana";
    private static final String CRYSTAL_BONUS_TAG = "CrystalManaBonus";

    public FairyDewEffect() {
        super(MobEffectCategory.NEUTRAL, 0x00FFFFFF);
    }
/*
    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        // 使用工具类设置体型缩放
        if (pLivingEntity != null) {
            RaceScaleHelper.setSmoothModelScale(pLivingEntity,
                    ModConfig.FAIRY_DEW_SCALE_FACTOR.get().floatValue(),20);
        }

        // 在这里添加属性修改器，而不是在构造函数中
        this.addAttributeModifier(Attributes.MAX_HEALTH,
                "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC",
                ModConfig.FAIRY_DEW_MAX_HEALTH.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                "55FCED67-E92A-486E-9800-B47F202C4386",
                ModConfig.FAIRY_DEW_ATTACK_DAMAGE.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        this.addAttributeModifier(Attributes.ARMOR,
                "2AD3F246-FEE1-4E67-B886-69FD380BB150",
                ModConfig.FAIRY_DEW_ARMOR.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS,
                "501E39C3-9F2A-4CCE-9A89-ACD6C7C3546A",
                ModConfig.FAIRY_DEW_ARMOR_TOUGHNESS.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "91AEAA56-376B-4498-935B-2F7F68070635",
                ModConfig.FAIRY_DEW_MOVEMENT_SPEED.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        this.addAttributeModifier(ForgeMod.SWIM_SPEED.get(),
                "606E2F94-D4C5-4B50-B89F-A023A0F3C102",
                ModConfig.FAIRY_DEW_SWIM_SPEED.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        this.addAttributeModifier(ForgeMod.BLOCK_REACH.get(),
                "D7184E46-5B46-4C99-9EA3-7E2987BF4C91",
                ModConfig.FAIRY_DEW_REACH.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        this.addAttributeModifier(ForgeMod.STEP_HEIGHT_ADDITION.get(),
                "8D062387-C3E4-4FD7-B47A-32E54CCB13C6",
                ModConfig.FAIRY_DEW_STEP_HEIGHT.get(),
                AttributeModifier.Operation.ADDITION);



        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

        // 强制同步玩家属性
        if (pLivingEntity instanceof Player player) {
            // 强制同步生命值
            player.setHealth(player.getHealth());
        }
    }

    @SubscribeEvent
    public static void onLivingJump(net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && player.hasEffect(ModEffects.FAIRY_DEW.get())) {
            Vec3 motion = player.getDeltaMovement();
            double multiplier = 1.0 + ModConfig.FAIRY_DEW_JUMP_BOOST.get();
            player.setDeltaMovement(motion.x, motion.y * multiplier, motion.z);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        MobEffectInstance effect = player.getEffect(ModEffects.FAIRY_DEW.get());

        // 如果玩家有精灵露效果
        if (effect != null) {
            // 直接移除当前效果
            player.removeEffect(ModEffects.FAIRY_DEW.get());
            RaceScaleHelper.setModelScale(player,
                    ModConfig.FAIRY_DEW_SCALE_FACTOR.get().floatValue());
            // 直接应用一个新的永久效果
            player.addEffect(new MobEffectInstance(
                    ModEffects.FAIRY_DEW.get(),
                    -1, // 永久持续
                    0,  // 0级效果
                    false,
                    false,
                    false
            ));
        }
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Player player) {
            // 飞行能力代码
            if (!player.isCreative() && !player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.getAbilities().setFlyingSpeed(0.05f * ModConfig.FAIRY_DEW_FLIGHT_SPEED.get().floatValue());
                player.onUpdateAbilities();
            }

            // 处理魔力值
            CompoundTag data = player.getPersistentData();
            if (!data.contains(BONUS_TAG)) {
                float currentMaxMana = ManaData.getMaxMana(player);
                float crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                float permanentDecrease = data.getInt("PermanentManaDecrease");

                float baseMaxMana = currentMaxMana - crystalBonus + permanentDecrease;
                data.putFloat(ORIGINAL_MANA_TAG, baseMaxMana);

                float newMaxMana = baseMaxMana - permanentDecrease + crystalBonus +
                        ModConfig.FAIRY_DEW_MANA_BONUS.get().floatValue();
                ManaData.setMaxMana(player, newMaxMana);
                data.putBoolean(BONUS_TAG, true);
            }

            // 爬墙代码
            if (ModConfig.FAIRY_DEW_WALL_CLIMB.get()) {
                handleWallClimb(player);
            }
        }
    }

    private void handleWallClimb(Player player) {
        // 检查玩家是否在墙上且按着W键
        if (!player.onGround() && !player.isInWater() && !player.isInLava() && player.zza > 0) {
            if (isPlayerTouchingWall(player)) {
                Vec3 motion = player.getDeltaMovement();

                // 检查玩家是否在潜行
                if (player.isShiftKeyDown()) {
                    // 如果玩家正在潜行，将垂直速度设为0，保持在当前位置
                    player.setDeltaMovement(motion.x, 0, motion.z);
                } else {
                    // 正常爬墙
                    double upwardSpeed = ModConfig.FAIRY_DEW_CLIMB_SPEED.get();
                    player.setDeltaMovement(motion.x, upwardSpeed, motion.z);
                }

                // 重置下落距离以防止摔落伤害
                player.resetFallDistance();

                // 减小水平移动以增加稳定性
                double drag = ModConfig.FAIRY_DEW_CLIMB_HORIZONTAL_DRAG.get();
                player.setDeltaMovement(player.getDeltaMovement().multiply(drag, 1.0, drag));
            }
        } else {
            // 如果玩家贴着墙但没有按W键，检查是否在潜行
            if (isPlayerTouchingWall(player) && player.isShiftKeyDown()) {
                // 如果玩家在潜行，保持在当前位置
                player.setDeltaMovement(player.getDeltaMovement().multiply(0.7, 0, 0.7));
                player.resetFallDistance();
            }
        }
    }

    private boolean isPlayerTouchingWall(Player player) {
        AABB boundingBox = player.getBoundingBox();
        // 扩大检测范围
        AABB checkBox = boundingBox.inflate(0.15, 0, 0.15);
        Level level = player.level();

        int minX = (int) Math.floor(checkBox.minX);
        int maxX = (int) Math.ceil(checkBox.maxX);
        int minY = (int) Math.floor(boundingBox.minY);
        int maxY = (int) Math.ceil(boundingBox.maxY);
        int minZ = (int) Math.floor(checkBox.minZ);
        int maxZ = (int) Math.ceil(checkBox.maxZ);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // 检查玩家周围的所有方向
        double[] checkPoints = {
                player.getX() - 0.3, player.getX() + 0.3, // X轴方向
                player.getZ() - 0.3, player.getZ() + 0.3  // Z轴方向
        };

        for (int y = minY; y <= maxY; y++) {
            // 检查X轴方向
            pos.set(checkPoints[0], y, player.getZ());
            if (isValidWall(level, pos)) return true;

            pos.set(checkPoints[1], y, player.getZ());
            if (isValidWall(level, pos)) return true;

            // 检查Z轴方向
            pos.set(player.getX(), y, checkPoints[2]);
            if (isValidWall(level, pos)) return true;

            pos.set(player.getX(), y, checkPoints[3]);
            if (isValidWall(level, pos)) return true;
        }

        return false;
    }

    // 辅助方法：检查指定位置是否是有效的墙
    private boolean isValidWall(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (!blockState.isAir() && blockState.isSolid() &&
                !(blockState.getBlock() instanceof LadderBlock) &&
                !(blockState.getBlock() instanceof VineBlock)) {
            VoxelShape shape = blockState.getCollisionShape(level, pos);
            return !shape.isEmpty();
        }
        return false;
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
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {

        // 使用工具类重置体型
        if (pLivingEntity != null) {
            RaceScaleHelper.setSmoothModelScale(pLivingEntity, 1.0f, 20);
        }
        // 先移除所有属性修改器（调用父类的方法）
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

        // 如果是玩家实体
        if (pLivingEntity instanceof Player player) {
            // 飞行能力代码保持不变
            if (!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
            }

            // 处理魔力值恢复，使用浮点数
            CompoundTag data = player.getPersistentData();
            if (data.contains(BONUS_TAG)) {
                if (data.contains(ORIGINAL_MANA_TAG)) {
                    float baseMaxMana = data.getFloat(ORIGINAL_MANA_TAG);
                    float crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                    float permanentDecrease = data.getInt("PermanentManaDecrease");

                    float restoredMana = baseMaxMana - permanentDecrease + crystalBonus;
                    ManaData.setMaxMana(player, restoredMana);
                }
                data.remove(BONUS_TAG);
                data.remove(ORIGINAL_MANA_TAG);
            }
        }
        TrinketsandBaublesMod.LOGGER.debug("Removing FairyDewEffect from entity: {}",
                pLivingEntity.getName().getString());
        // 强制同步玩家属性
        if (pLivingEntity instanceof Player player) {
            // 强制同步生命值
            player.setHealth(player.getHealth());
        }
    }


    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            MobEffectInstance effect = player.getEffect(ModEffects.FAIRY_DEW.get());
            if (effect != null) {
                CompoundTag playerData = player.getPersistentData();
                CompoundTag effectData = new CompoundTag();
                effectData.putBoolean("HadEffect", true);

                // 保存魔力相关数据为浮点数
                if (playerData.contains(ORIGINAL_MANA_TAG)) {
                    effectData.putFloat(ORIGINAL_MANA_TAG,
                            playerData.getFloat(ORIGINAL_MANA_TAG));
                }
                if (playerData.contains(CRYSTAL_BONUS_TAG)) {
                    effectData.putInt(CRYSTAL_BONUS_TAG,
                            playerData.getInt(CRYSTAL_BONUS_TAG));
                }
                if (playerData.contains("PermanentManaDecrease")) {
                    effectData.putInt("PermanentManaDecrease",
                            playerData.getInt("PermanentManaDecrease"));
                }

                playerData.put("FairyDewEffect", effectData);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player player = event.getEntity();
        CompoundTag originalData = original.getPersistentData();

        if (originalData.contains("FairyDewEffect")) {
            CompoundTag effectData = originalData.getCompound("FairyDewEffect");

            // 检查是否有效果标记
            if (effectData.getBoolean("HadEffect")) {
                // 获取服务器实例以延迟应用效果
                net.minecraft.server.MinecraftServer server = player.level().getServer();
                if (server != null) {
                    // 延迟1tick后应用效果
                    server.tell(new net.minecraft.server.TickTask(
                            server.getTickCount() + 1,
                            () -> {
                                // 应用新的永久效果
                                player.addEffect(new MobEffectInstance(
                                        ModEffects.FAIRY_DEW.get(),
                                        -1,    // 永久持续
                                        0,     // 0级效果
                                        false, // ambient
                                        false,  // visible
                                        false   // showIcon
                                ));

                                // 魔力值相关数据处理
                                if (effectData.contains(ORIGINAL_MANA_TAG)) {
                                    float originalMana = effectData.getFloat(ORIGINAL_MANA_TAG);
                                    float crystalBonus = effectData.contains(CRYSTAL_BONUS_TAG) ?
                                            effectData.getInt(CRYSTAL_BONUS_TAG) : 0;
                                    float permanentDecrease = effectData.contains("PermanentManaDecrease") ?
                                            effectData.getInt("PermanentManaDecrease") : 0;

                                    // 保存原始值
                                    player.getPersistentData().putFloat(ORIGINAL_MANA_TAG, originalMana);

                                    // 计算并设置正确的魔力值
                                    float correctMana = originalMana - permanentDecrease + crystalBonus +
                                            ModConfig.FAIRY_DEW_MANA_BONUS.get().floatValue();
                                    ManaData.setMaxMana(player, correctMana);

                                    // 标记魔力加成已应用
                                    player.getPersistentData().putBoolean(BONUS_TAG, true);

                                    // 保存其他相关数据，转换为整数
                                    if (effectData.contains(CRYSTAL_BONUS_TAG)) {
                                        player.getPersistentData().putInt(CRYSTAL_BONUS_TAG, (int)crystalBonus);
                                    }
                                    if (effectData.contains("PermanentManaDecrease")) {
                                        player.getPersistentData().putInt("PermanentManaDecrease", (int)permanentDecrease);
                                    }
                                }
                            }
                    ));
                }
            }
        }
    }*/
}