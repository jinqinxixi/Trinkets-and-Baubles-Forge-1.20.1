package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import com.jinqinxixi.trinketsandbaubles.util.RaceEffectUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.*;

public class DragonsRingItem extends ModifiableBaubleItem {

    public static final String TAG_TARGET_MODE = "TargetMode";          // 目标模式
    public static final String TAG_ORE_GROUP_INDEX = "OreGroupIndex";   // 矿物组索引
    public static final String TAG_DRAGONS_EYE_TARGETS = "Targets";     // 目标列表

    private static final List<Set<Block>> ORE_GROUPS = new ArrayList<>();
    private static final Set<Block> CHEST_BLOCKS = new HashSet<>();
    private static final List<String> ORE_GROUP_NAMES = Arrays.asList(
            "valuables", // 贵重矿物
            "common",    // 常见矿物
            "redstone", // 红石相关
            "all"       // 所有矿物
    );

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    public DragonsRingItem(Properties properties) {
        super(properties);
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof DragonsRingItem))
                .isPresent();
    }

    private void applyFaelisBuff(LivingEntity entity) {
        // 只在服务器端处理
        if (entity instanceof ServerPlayer serverPlayer) {
            // 检查是否装备了多个种族戒指
            if (RaceEffectUtil.hasMultipleRaceRings(serverPlayer)) {
                // 如果有多个种族戒指，移除效果
                serverPlayer.removeEffect(ModEffects.DRAGON.get());
                return;
            }

            // 检查玩家是否已经有效果
            if (!serverPlayer.hasEffect(ModEffects.DRAGON.get())) {
                // 先清除所有种族效果
                RaceEffectUtil.clearAllRaceEffects(serverPlayer);

                // 然后应用新的效果
                entity.addEffect(new MobEffectInstance(
                        ModEffects.DRAGON.get(),
                        Integer.MAX_VALUE,
                        0,
                        false,
                        false,
                        false
                ));
            }
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        LivingEntity entity = slotContext.entity();

        if (entity instanceof ServerPlayer serverPlayer) {
            // 处理种族效果
            if (RaceEffectUtil.hasMultipleRaceRings(serverPlayer)) {
                serverPlayer.removeEffect(ModEffects.DRAGON.get());
                return;
            }

            // 更新探测目标
            if (entity.tickCount % 5 == 0) {
                updateTargets(serverPlayer, stack);
            }

            // 应用种族效果
            if (isEquipped(entity) && !serverPlayer.hasEffect(ModEffects.DRAGON.get())) {
                applyFaelisBuff(entity);
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        // 先调用父类的onEquip方法，它会处理修饰符的比较和应用
        super.onEquip(slotContext, prevStack, stack);

        // 如果实体是玩家且物品真的改变了才应用种族效果
        LivingEntity entity = slotContext.entity();
        if (entity instanceof ServerPlayer serverPlayer &&
                (prevStack.isEmpty() || !hasSameModifier(prevStack, stack))) {

            // 检查是否有多个种族戒指
            if (!RaceEffectUtil.hasMultipleRaceRings(serverPlayer)) {
                // 应用种族效果
                if (isEquipped(entity)) {
                    applyFaelisBuff(entity);
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        // 先处理种族效果的移除
        if (entity instanceof ServerPlayer serverPlayer &&
                (newStack.isEmpty() || !hasSameModifier(newStack, stack))) {

            // 只有当没有其他相同戒指装备时才移除效果
            if (!isEquipped(entity)) {
                serverPlayer.removeEffect(ModEffects.DRAGON.get());
            }
        }

        // 然后调用父类的onUnequip方法，它会处理修饰符的移除
        super.onUnequip(slotContext, newStack, stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            // 详细信息
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip1", ModConfig.DRAGON_MAX_HEALTH_BOOST.get() * 100)
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip2", ModConfig.DRAGON_ATTACK_DAMAGE_BOOST.get() * 100)
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip3", ModConfig.DRAGON_ARMOR_TOUGHNESS.get() * 100)
                    .withStyle(ChatFormatting.BLUE));

        } else {
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip11")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip12")
                    .withStyle(ChatFormatting.DARK_AQUA));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip13")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip14")
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip15")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.press_shift")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
    @Override
    public int getEnchantmentValue() {
        return 0; // 附魔等级为0
    }

    // 禁止任何形式的附魔（包括铁砧）
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    static {
        // 初始化贵重矿物组
        Set<Block> valuableOres = new HashSet<>(Arrays.asList(
                Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
                Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
                Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
                Blocks.ANCIENT_DEBRIS
        ));

        // 初始化常见矿物组
        Set<Block> commonOres = new HashSet<>(Arrays.asList(
                Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
                Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
                Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
                Blocks.NETHER_QUARTZ_ORE, Blocks.NETHER_GOLD_ORE
        ));

        // 初始化红石相关矿物组
        Set<Block> redstoneOres = new HashSet<>(Arrays.asList(
                Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
                Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE
        ));

        // 创建所有矿物的集合
        Set<Block> allOres = new HashSet<>();
        allOres.addAll(valuableOres);
        allOres.addAll(commonOres);
        allOres.addAll(redstoneOres);

        // 添加到组列表中
        ORE_GROUPS.add(valuableOres);
        ORE_GROUPS.add(commonOres);
        ORE_GROUPS.add(redstoneOres);
        ORE_GROUPS.add(allOres);

        // 初始化箱子类方块
        BuiltInRegistries.BLOCK.stream()
                .filter(b -> b instanceof ShulkerBoxBlock ||
                        BuiltInRegistries.BLOCK.getKey(b).getPath().contains("chest") ||
                        BuiltInRegistries.BLOCK.getKey(b).getPath().contains("barrel"))
                .forEach(CHEST_BLOCKS::add);
    }
    public static void handleModeToggle(ServerPlayer player, ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        boolean isChestMode = nbt.getBoolean(TAG_TARGET_MODE);
        int currentGroupIndex = nbt.getInt(TAG_ORE_GROUP_INDEX);

        if (isChestMode) {
            // 关闭箱子模式
            nbt.putBoolean(TAG_TARGET_MODE, false);
            nbt.putInt(TAG_ORE_GROUP_INDEX, -1);
            sendStatusMessage(player, "item.dragons_eye.mode.off");
        } else {
            if (currentGroupIndex == -1) {
                // 启动第一个矿物组
                nbt.putInt(TAG_ORE_GROUP_INDEX, 0);
                sendStatusMessage(player, "item.dragons_eye.mode.ore." + ORE_GROUP_NAMES.get(0));
            } else {
                // 循环切换模式
                currentGroupIndex = (currentGroupIndex + 1) % (ORE_GROUPS.size() + 1);
                if (currentGroupIndex == ORE_GROUPS.size()) {
                    nbt.putBoolean(TAG_TARGET_MODE, true);
                    sendStatusMessage(player, "item.dragons_eye.mode.chest");
                } else if (currentGroupIndex == 0) {
                    nbt.putBoolean(TAG_TARGET_MODE, false);
                    nbt.putInt(TAG_ORE_GROUP_INDEX, -1);
                    sendStatusMessage(player, "item.dragons_eye.mode.off");
                } else {
                    nbt.putInt(TAG_ORE_GROUP_INDEX, currentGroupIndex);
                    sendStatusMessage(player, "item.dragons_eye.mode.ore." + ORE_GROUP_NAMES.get(currentGroupIndex));
                }
            }
        }

        updateTargets(player, stack);
    }

    private static void sendStatusMessage(ServerPlayer player, String translationKey) {
        player.displayClientMessage(Component.translatable(translationKey), true);
    }
    public static void updateTargets(ServerPlayer player, ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        boolean scanChests = nbt.getBoolean(TAG_TARGET_MODE);
        int groupIndex = nbt.getInt(TAG_ORE_GROUP_INDEX);

        if (groupIndex == -1 && !scanChests) {
            // 存储空列表
            nbt.put(TAG_DRAGONS_EYE_TARGETS, new ListTag());
            return;
        }

        Set<Block> targetBlocks = scanChests ? CHEST_BLOCKS : ORE_GROUPS.get(groupIndex);
        int scanRange = ModConfig.DRAGONS_EYE_SCAN_RANGE.get();

        // 创建存储位置的NBT列表
        ListTag targetsList = new ListTag();
        BlockPos.betweenClosedStream(
                        player.blockPosition().offset(-scanRange, -scanRange, -scanRange),
                        player.blockPosition().offset(scanRange, scanRange, scanRange))
                .filter(pos -> targetBlocks.contains(player.level().getBlockState(pos).getBlock()))
                .forEach(pos -> {
                    CompoundTag posTag = new CompoundTag();
                    posTag.putInt("X", pos.getX());
                    posTag.putInt("Y", pos.getY());
                    posTag.putInt("Z", pos.getZ());
                    targetsList.add(posTag);
                });

        nbt.put(TAG_DRAGONS_EYE_TARGETS, targetsList);
    }
    public static int[] getColorForGroup(int groupIndex, boolean isChestMode) {
        if (groupIndex == -1 && !isChestMode) {
            return new int[]{128, 128, 128}; // 灰色表示关闭状态
        }

        if (isChestMode) {
            return new int[]{255, 0, 255}; // 箱子模式 - 紫色
        }

        switch (groupIndex) {
            case 0: // 贵重矿物
                return new int[]{255, 255, 0}; // 黄色
            case 1: // 常见矿物
                return new int[]{0, 255, 255}; // 青色
            case 2: // 红石相关
                return new int[]{255, 0, 0}; // 红色
            case 3: // 所有矿物
                return new int[]{0, 255, 0}; // 绿色
            default:
                return new int[]{255, 255, 255}; // 白色
        }
    }
}