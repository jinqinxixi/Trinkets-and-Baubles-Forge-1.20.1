package com.jinqinxixi.trinketsandbaubles.modEffects;


import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.items.ModItem;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;

import com.jinqinxixi.trinketsandbaubles.util.RaceScaleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
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
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class FaelesEffect extends MobEffect {
    private static final String MANA_BONUS_TAG = "FaelesManaBonus";
    private static final String ORIGINAL_MANA_TAG = "FaelesOriginalMaxMana";
    private static final String CRYSTAL_BONUS_TAG = "CrystalManaBonus"; // 水晶加成标记
    public FaelesEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFFB6C1); // 浅粉色
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {

        // 使用工具类设置体型缩放
        if (pLivingEntity != null) {
            RaceScaleHelper.setSmoothModelScale(pLivingEntity,
                    ModConfig.FAELES_SCALE_FACTOR.get().floatValue(),20);
        }
        // 基础属性修改
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                "d141ef28-51c6-4b47-8a0d-6946e841c132",
                ModConfig.FAELES_ATTACK_DAMAGE.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                "4520f278-fb8f-4c75-9336-5c3ab7c6134a",
                ModConfig.FAELES_ATTACK_SPEED.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                "dc3b4b8c-a02c-4bd8-82e9-204088927d1f",
                ModConfig.FAELES_MAX_HEALTH.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        this.addAttributeModifier(
                Attributes.ARMOR_TOUGHNESS,
                "8fc5e73c-2cf2-4729-8128-d99f49aa37f2",
                ModConfig.FAELES_ARMOR_TOUGHNESS.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "3b8f4065-5f43-4939-8e6a-a34f2d67c55d",
                ModConfig.FAELES_MOVEMENT_SPEED.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        // 固定值增加的属性
        this.addAttributeModifier(
                Attributes.LUCK,
                "95eb4f0a-dd60-4ada-98c1-2ce5c3d4374c",
                ModConfig.FAELES_LUCK.get(),
                AttributeModifier.Operation.ADDITION
        );

        this.addAttributeModifier(
                ForgeMod.SWIM_SPEED.get(),
                "7a925a64-d1e0-4cb9-8926-dd7848482bb4",
                ModConfig.FAELES_SWIM_SPEED.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        // 空手伤害作为固定值添加
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                "b2461c37-8d2e-4a4d-95ac-d2169c49182a",
                ModConfig.FAELES_UNARMED_DAMAGE.get(),
                AttributeModifier.Operation.ADDITION
        );

        // 跨步高度作为固定值添加
        this.addAttributeModifier(
                ForgeMod.STEP_HEIGHT_ADDITION.get(),
                "e8c9a6f5-4376-4e7b-9a5c-8f2e3d91d7c4",
                ModConfig.FAELES_STEP_HEIGHT.get(),
                AttributeModifier.Operation.ADDITION
        );

        // 交互距离作为百分比修改
        this.addAttributeModifier(
                ForgeMod.BLOCK_REACH.get(),
                "d74f3a1c-89b2-4b3e-bf8e-6d24d8c9517d",
                ModConfig.FAELES_REACH.get(),
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
    public static void onLivingJump(net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && player.hasEffect(ModEffects.FAELES.get())) {
            Vec3 motion = player.getDeltaMovement();
            double multiplier = 1.0 + ModConfig.FAELES_JUMP_BOOST.get();
            player.setDeltaMovement(motion.x, motion.y * multiplier, motion.z);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        MobEffectInstance effect = player.getEffect(ModEffects.FAELES.get());

        // 如果玩家有精灵露效果
        if (effect != null) {
            // 直接移除当前效果
            player.removeEffect(ModEffects.FAELES.get());
            RaceScaleHelper.setModelScale(player,
                    ModConfig.FAELES_SCALE_FACTOR.get().floatValue());
            // 直接应用一个新的永久效果
            player.addEffect(new MobEffectInstance(
                    ModEffects.FAELES.get(),
                    -1, // 永久持续
                    0,  // 0级效果
                    false,
                    false,
                    false
            ));
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
                    double upwardSpeed = ModConfig.FAELES_CLIMB_SPEED.get();
                    player.setDeltaMovement(motion.x, upwardSpeed, motion.z);
                }

                // 重置下落距离以防止摔落伤害
                player.resetFallDistance();

                // 减小水平移动以增加稳定性
                double drag = ModConfig.FAELES_CLIMB_HORIZONTAL_DRAG.get();
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
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Player player) {

            // 检查并设置最大魔力值
            CompoundTag data = player.getPersistentData();
            if (!data.contains(MANA_BONUS_TAG)) {
                int currentMaxMana = ManaData.getMaxMana(player);
                int crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                int permanentDecrease = data.getInt("PermanentManaDecrease");

                int baseMaxMana = currentMaxMana - crystalBonus + permanentDecrease;
                data.putInt(ORIGINAL_MANA_TAG, baseMaxMana);

                // 使用配置的魔力加成值
                int newMaxMana = baseMaxMana - permanentDecrease + crystalBonus + ModConfig.FAELES_MANA_BONUS.get();
                ManaData.setMaxMana(player, newMaxMana);
                data.putBoolean(MANA_BONUS_TAG, true);
            }

            // 护甲减速效果处理
            int nonLeatherArmorCount = 0;
            for (ItemStack armorItem : player.getArmorSlots()) {
                if (!armorItem.isEmpty() && armorItem.getItem() instanceof ArmorItem armor) {
                    if (armor.getMaterial() != ArmorMaterials.LEATHER) {
                        nonLeatherArmorCount++;
                    }
                }
            }

            var movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed != null) {
                // 先清除现有的所有减速效果
                for (int i = 0; i < 4; i++) {
                    UUID armorPenaltyUUID = UUID.fromString("a5923e8d-1c7f-4b6a-b5e9-9d3c7f4a8d2" + i);
                    movementSpeed.removeModifier(armorPenaltyUUID);
                }

                // 为每件非皮革护甲单独添加减速效果
                if (nonLeatherArmorCount > 0) {
                    try {
                        for (int i = 0; i < nonLeatherArmorCount; i++) {
                            UUID armorPenaltyUUID = UUID.fromString("a5923e8d-1c7f-4b6a-b5e9-9d3c7f4a8d2" + i);
                            movementSpeed.addTransientModifier(
                                    new AttributeModifier(
                                            armorPenaltyUUID,
                                            "Armor Speed Penalty " + (i + 1),
                                            ModConfig.FAELES_ARMOR_SPEED_PENALTY.get(),
                                            AttributeModifier.Operation.MULTIPLY_TOTAL
                                    )
                            );
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            // 如果配置允许，处理爬墙
            if (ModConfig.FAELES_WALL_CLIMB.get()) {
                handleWallClimb(player);
            }
        }
    }

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
                if (data.contains(ORIGINAL_MANA_TAG)) {
                    int baseMaxMana = data.getInt(ORIGINAL_MANA_TAG);
                    int crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                    int permanentDecrease = data.getInt("PermanentManaDecrease");

                    // 恢复到基础值，考虑永久减少和水晶加成
                    int restoredMana = baseMaxMana - permanentDecrease + crystalBonus;
                    ManaData.setMaxMana(player, restoredMana);
                }
                // 清理标记
                data.remove(MANA_BONUS_TAG);
                data.remove(ORIGINAL_MANA_TAG);
                data.remove("FaelesEffect");
            }

            // 移除护甲减速效果
            var movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed != null) {
                // 移除所有护甲减速效果
                for (int i = 0; i < 4; i++) {
                    UUID armorPenaltyUUID = UUID.fromString("a5923e8d-1c7f-4b6a-b5e9-9d3c7f4a8d2" + i);
                    movementSpeed.removeModifier(armorPenaltyUUID);
                }
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
            MobEffectInstance effect = player.getEffect(ModEffects.FAELES.get());
            if (effect != null) {
                CompoundTag playerData = player.getPersistentData();
                CompoundTag effectData = new CompoundTag();
                // 只保存一个标记表示玩家死亡时有效果
                effectData.putBoolean("HadEffect", true);

                // 保存所有魔力相关数据
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

                playerData.put("FaelesEffect", effectData);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player player = event.getEntity();
        CompoundTag originalData = original.getPersistentData();

        if (originalData.contains("FaelesEffect")) {
            CompoundTag effectData = originalData.getCompound("FaelesEffect");

            // 检查是否有效果标记
            if (effectData.getBoolean("HadEffect")) {
                // 获取服务器实例
                net.minecraft.server.MinecraftServer server = player.level().getServer();
                if (server != null) {
                    // 延迟1tick后应用效果
                    server.tell(new net.minecraft.server.TickTask(
                            server.getTickCount() + 1,
                            () -> {
                                // 重新应用永久效果
                                player.addEffect(new MobEffectInstance(
                                        ModEffects.FAELES.get(),
                                        -1,    // 永久持续
                                        0,     // 0级效果
                                        false, // ambient
                                        false,  // visible
                                        false   // showIcon
                                ));

                                // 复制所有数据到新玩家
                                CompoundTag newData = player.getPersistentData();

                                // 处理魔力值相关数据
                                if (effectData.contains(ORIGINAL_MANA_TAG)) {
                                    int originalMana = effectData.getInt(ORIGINAL_MANA_TAG);
                                    int crystalBonus = effectData.contains(CRYSTAL_BONUS_TAG) ?
                                            effectData.getInt(CRYSTAL_BONUS_TAG) : 0;
                                    int permanentDecrease = effectData.contains("PermanentManaDecrease") ?
                                            effectData.getInt("PermanentManaDecrease") : 0;

                                    // 保存原始值
                                    newData.putInt(ORIGINAL_MANA_TAG, originalMana);

                                    // 计算并设置正确的魔力值
                                    int correctMana = originalMana - permanentDecrease + crystalBonus +
                                            ModConfig.FAELES_MANA_BONUS.get();
                                    ManaData.setMaxMana(player, correctMana);

                                    // 标记魔力加成已应用
                                    newData.putBoolean(MANA_BONUS_TAG, true);

                                    // 保存其他相关数据
                                    if (effectData.contains(CRYSTAL_BONUS_TAG)) {
                                        newData.putInt(CRYSTAL_BONUS_TAG, crystalBonus);
                                    }
                                    if (effectData.contains("PermanentManaDecrease")) {
                                        newData.putInt("PermanentManaDecrease", permanentDecrease);
                                    }
                                }
                            }
                    ));
                }
            }
        }
    }

    // 处理喝牛奶获得增益效果
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player &&
                player.hasEffect(ModEffects.FAELES.get()) &&
                event.getItem().getItem() == net.minecraft.world.item.Items.MILK_BUCKET) {

            // 添加增益效果
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 3600, 0)); // 3分钟跳跃提升
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 0)); // 3分钟力量
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, 0)); // 3分钟速度
        }
    }
}