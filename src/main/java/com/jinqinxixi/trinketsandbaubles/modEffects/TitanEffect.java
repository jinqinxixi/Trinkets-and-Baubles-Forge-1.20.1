package com.jinqinxixi.trinketsandbaubles.modEffects;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


import java.util.ArrayList;
import java.util.List;

import static com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class TitanEffect extends MobEffect {
    private static final String MANA_PENALTY_TAG = "TitanManaPenaltyApplied";
    private static final String ORIGINAL_MANA_TAG = "TitanOriginalMaxMana";
    private static final String CRYSTAL_BONUS_TAG = "CrystalManaBonus"; // 新增：水晶加成标记


    public TitanEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x808080); // 灰色
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        // 使用平滑过渡设置体型缩放
        if (pLivingEntity != null) {
            RaceScaleHelper.setSmoothModelScale(pLivingEntity,
                    ModConfig.TITAN_SCALE_FACTOR.get().floatValue(),
                    20); // 1秒过渡时间
        }
            this.addAttributeModifier(
                    Attributes.ATTACK_SPEED,
                    "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC",
                    ModConfig.TITAN_ATTACK_SPEED.get(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL);

            this.addAttributeModifier(
                    Attributes.ATTACK_DAMAGE,
                    "55FCED67-E92A-486E-9800-B47F202C4386",
                    ModConfig.TITAN_ATTACK_DAMAGE.get(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL);

            this.addAttributeModifier(
                    Attributes.MAX_HEALTH,
                    "2AD3F246-FEE1-4E67-B886-69FD380BB150",
                    ModConfig.TITAN_MAX_HEALTH.get(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL);

            this.addAttributeModifier(
                    Attributes.KNOCKBACK_RESISTANCE,
                    "501E39C3-9F2A-4CCE-9A89-ACD6C7C3546A",
                    ModConfig.TITAN_KNOCKBACK_RESISTANCE.get(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL);

            this.addAttributeModifier(
                    Attributes.MOVEMENT_SPEED,
                    "91AEAA56-376B-4498-935B-2F7F68070635",
                    ModConfig.TITAN_MOVEMENT_SPEED.get(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL);

            this.addAttributeModifier(
                    ForgeMod.BLOCK_REACH.get(),
                    "D7184E46-5B46-4C99-9EA3-7E2987BF4C91",
                    ModConfig.TITAN_REACH_DISTANCE.get(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL);

            this.addAttributeModifier(
                    ForgeMod.STEP_HEIGHT_ADDITION.get(),
                    "8D062387-C3E4-4FD7-B47A-32E54CCB13C6",
                    ModConfig.TITAN_STEP_HEIGHT.get(),
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
        if (entity instanceof Player player && player.hasEffect(ModEffects.TITAN.get())) {
            Vec3 motion = player.getDeltaMovement();
            double multiplier = 1.0 + ModConfig.TITAN_JUMP_BOOST.get();
            player.setDeltaMovement(motion.x, motion.y * multiplier, motion.z);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        MobEffectInstance effect = player.getEffect(ModEffects.TITAN.get());


        if (effect != null) {
            // 直接移除当前效果
            player.removeEffect(ModEffects.TITAN.get());

            // 重新设置体型大小
            RaceScaleHelper.setModelScale(player,
                    ModConfig.TITAN_SCALE_FACTOR.get().floatValue());


            // 直接应用一个新的永久效果
            player.addEffect(new MobEffectInstance(
                    ModEffects.TITAN.get(),
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
            Level level = player.level();

            // 处理魔力值修改
            if (!level.isClientSide) {
                CompoundTag data = player.getPersistentData();
                if (!data.contains(MANA_PENALTY_TAG)) {
                    float currentMaxMana = ManaData.getMaxMana(player);
                    float crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                    float permanentDecrease = data.getInt("PermanentManaDecrease");

                    float baseMaxMana = currentMaxMana - crystalBonus + permanentDecrease;
                    data.putFloat(ORIGINAL_MANA_TAG, baseMaxMana);

                    // 使用配置的魔力修改值
                    float newMaxMana = baseMaxMana - permanentDecrease +
                            ModConfig.TITAN_MANA_MODIFIER.get().floatValue() + crystalBonus;
                    ManaData.setMaxMana(player, Math.max(0, newMaxMana));
                    data.putBoolean(MANA_PENALTY_TAG, true);
                }
            }

            // 处理水中下沉
            if (player.isInWater()) {
                player.setDeltaMovement(player.getDeltaMovement().add(0, -ModConfig.TITAN_WATER_SINK_SPEED.get(), 0));
            }


            // 处理踩坏植物 - 检查玩家碰撞箱范围内的方块
            double width = player.getBbWidth();
            double height = player.getBbHeight();
            BlockPos playerPos = player.blockPosition();

            // 获取玩家碰撞箱范围内的所有方块位置
            for (int x = (int) (-width - 1); x <= width + 1; x++) {
                for (int y = -1; y <= (int) height; y++) {
                    for (int z = (int) (-width - 1); z <= width + 1; z++) {
                        BlockPos checkPos = playerPos.offset(x, y, z);
                        BlockState state = level.getBlockState(checkPos);
                        Block block = state.getBlock();

                        // 检查并破坏植物和耕地
                        if (block instanceof CropBlock ||      // 所有作物的通用类型
                                // 草类植物和蕨类
                                block == Blocks.GRASS ||     // 短草
                                block == Blocks.TALL_GRASS ||      // 高草
                                block == Blocks.FERN ||            // 蕨类
                                block == Blocks.LARGE_FERN ||      // 大型蕨类
                                // 普通花朵
                                block == Blocks.DANDELION ||       // 蒲公英
                                block == Blocks.POPPY ||           // 虞美人
                                block == Blocks.BLUE_ORCHID ||     // 蓝色兰花
                                block == Blocks.ALLIUM ||          // 绒球葱
                                block == Blocks.AZURE_BLUET ||     // 蓝花美耳草
                                block == Blocks.RED_TULIP ||       // 红色郁金香
                                block == Blocks.ORANGE_TULIP ||    // 橙色郁金香
                                block == Blocks.WHITE_TULIP ||     // 白色郁金香
                                block == Blocks.PINK_TULIP ||      // 粉色郁金香
                                block == Blocks.OXEYE_DAISY ||     // 滨菊
                                block == Blocks.CORNFLOWER ||      // 矢车菊
                                block == Blocks.LILY_OF_THE_VALLEY ||// 铃兰
                                // 农作物
                                block == Blocks.WHEAT ||           // 小麦
                                block == Blocks.CARROTS ||         // 胡萝卜
                                block == Blocks.POTATOES ||        // 马铃薯
                                block == Blocks.BEETROOTS ||       // 甜菜根
                                block == Blocks.FARMLAND ||        // 耕地
                                // 甘蔗和竹子
                                block == Blocks.SUGAR_CANE ||      // 甘蔗
                                block == Blocks.BAMBOO ||          // 竹子
                                block == Blocks.BAMBOO_SAPLING ||  // 竹笋
                                // 灌木和浆果
                                block == Blocks.SWEET_BERRY_BUSH || // 甜浆果丛
                                // 洞穴植物
                                block == Blocks.CAVE_VINES ||      // 洞穴藤蔓
                                block == Blocks.CAVE_VINES_PLANT ||// 洞穴藤蔓植物
                                block == Blocks.GLOW_LICHEN ||     // 发光地衣
                                // 藤蔓和水生植物
                                block == Blocks.VINE ||            // 藤蔓
                                block == Blocks.KELP ||            // 海带
                                block == Blocks.KELP_PLANT ||      // 海带植物
                                block == Blocks.SEAGRASS ||        // 海草
                                block == Blocks.TALL_SEAGRASS ||   // 高海草
                                // 树苗
                                block == Blocks.OAK_SAPLING ||     // 橡树树苗
                                block == Blocks.SPRUCE_SAPLING ||  // 云杉树苗
                                block == Blocks.BIRCH_SAPLING ||   // 白桦树苗
                                block == Blocks.JUNGLE_SAPLING ||  // 丛林树苗
                                block == Blocks.ACACIA_SAPLING ||  // 金合欢树苗
                                block == Blocks.DARK_OAK_SAPLING ||// 深色橡树树苗
                                block == Blocks.MANGROVE_PROPAGULE // 红树胎生苗
                        ) {
                            if (block == Blocks.FARMLAND) {
                                // 耕地直接变为泥土，不产生掉落物
                                level.setBlock(checkPos, Blocks.DIRT.defaultBlockState(), 3);
                            } else {
                                // 其他植物正常掉落
                                level.destroyBlock(checkPos, true);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        // 使用工具类重置体型
        if (pLivingEntity != null) {
            RaceScaleHelper.setSmoothModelScale(pLivingEntity, 1.0f, 20); // 1秒过渡时间
        }
        // 先调用父类方法移除所有属性修改器
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

        // 如果是玩家实体
        if (pLivingEntity instanceof Player player) {
            CompoundTag data = player.getPersistentData();
            if (data.contains(MANA_PENALTY_TAG)) {
                if (data.contains(ORIGINAL_MANA_TAG)) {
                    float baseMaxMana = data.getFloat(ORIGINAL_MANA_TAG);
                    float crystalBonus = data.getInt(CRYSTAL_BONUS_TAG);
                    float permanentDecrease = data.getInt("PermanentManaDecrease");

                    // 恢复到基础值，考虑永久减少和水晶加成
                    float restoredMana = baseMaxMana - permanentDecrease + crystalBonus;
                    ManaData.setMaxMana(player, restoredMana);
                }
                // 清理标记
                data.remove(MANA_PENALTY_TAG);
                data.remove(ORIGINAL_MANA_TAG);
                data.remove("TitansEffect");
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
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            MobEffectInstance effect = player.getEffect(ModEffects.TITAN.get());
            if (effect != null) {
                CompoundTag playerData = player.getPersistentData();
                CompoundTag effectData = new CompoundTag();
                effectData.putInt("Duration", effect.getDuration());
                effectData.putInt("Amplifier", effect.getAmplifier());

                // 保存所有魔力相关数据为浮点数
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

                playerData.put("TitansEffect", effectData);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player player = event.getEntity();
        CompoundTag originalData = original.getPersistentData();

        if (originalData.contains("TitansEffect")) {
            CompoundTag effectData = originalData.getCompound("TitansEffect");

            // 获取服务器实例以延迟应用效果
            net.minecraft.server.MinecraftServer server = player.level().getServer();
            if (server != null) {
                // 延迟1tick后应用效果
                server.tell(new net.minecraft.server.TickTask(
                        server.getTickCount() + 1,
                        () -> {
                            // 应用效果
                            player.addEffect(new MobEffectInstance(
                                    ModEffects.TITAN.get(),
                                    effectData.getInt("Duration"),
                                    effectData.getInt("Amplifier"),
                                    false,
                                    false,  // 改为true使效果可见
                                    false   // 改为true使图标可见
                            ));

                            // 如果存在原始魔力值数据，直接设置正确的魔力值
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
                                        ModConfig.TITAN_MANA_MODIFIER.get().floatValue();
                                ManaData.setMaxMana(player, Math.max(0, correctMana));

                                // 标记魔力修改已应用
                                player.getPersistentData().putBoolean(MANA_PENALTY_TAG, true);

                                // 保存其他相关数据
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

    // 处理3x3x3范围挖掘
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasEffect(ModEffects.TITAN.get())) {
            Level level = (Level) event.getLevel();
            if (!level.isClientSide()) {
                // 获取正在破坏的方块的类型
                BlockPos breakPos = event.getPos();
                BlockState breakingState = level.getBlockState(breakPos);
                Block breakingBlock = breakingState.getBlock();

                // 获取3x3x3范围内的所有方块
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos pos = breakPos.offset(x, y, z);
                            if (!pos.equals(breakPos)) {
                                BlockState state = level.getBlockState(pos);
                                // 只破坏与正在挖掘的方块相同类型的方块
                                if (state.getBlock() == breakingBlock && player.hasCorrectToolForDrops(state)) {
                                    level.destroyBlock(pos, true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}