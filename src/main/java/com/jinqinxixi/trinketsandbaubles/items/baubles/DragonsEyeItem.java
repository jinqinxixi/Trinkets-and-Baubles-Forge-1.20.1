package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.DragonsEyeToggleMessage;
import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.config.Config;

import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.UpdateEffectsMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.UpdateTargetsMessage;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.*;

public class DragonsEyeItem extends ModifiableBaubleItem {

    public static final String TAG_TARGET_MODE = "TargetMode";          // 目标模式
    public static final String TAG_ORE_GROUP_INDEX = "OreGroupIndex";   // 矿物组索引
    public static final String TAG_NIGHT_VISION_MODE = "NightVision";   // 夜视模式
    public static final String TAG_IS_INITIALIZED = "IsInitialized";    // 初始化标记
    public static final String TAG_DRAGONS_EYE_TARGETS = "Targets";     // 目标列表


    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }
    // 定义不同的矿石组
    private static final List<Set<Block>> ORE_GROUPS = new ArrayList<>();
    private static final Set<Block> CHEST_BLOCKS = new HashSet<>();

    // 定义矿石组的名称
    private static final List<String> ORE_GROUP_NAMES = Arrays.asList(
            "valuables", // 贵重矿物（钻石、绿宝石、金）
            "common",    // 常见矿物（铁、煤、铜）
            "redstone", // 红石相关
            "all"       // 所有矿物
    );

    static {
        // 初始化贵重矿物组
        Set<Block> valuableOres = new HashSet<>(Arrays.asList(
                Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
                Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
                Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
                Blocks.ANCIENT_DEBRIS // 添加远古残骸
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

    public DragonsEyeItem(Properties properties) {
        super(properties);
    }

    public static class ClientEvents {
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onKeyInput(InputEvent.Key event) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.findFirstCurio(stack -> stack.getItem() instanceof DragonsEyeItem)
                        .ifPresent(curio -> {
                            if (KeyBindings.TOGGLE_DRAGONS_EYE_MODE.consumeClick()) {
                                // 使用新版API发送数据包
                                NetworkHandler.INSTANCE.sendToServer(new DragonsEyeToggleMessage(0));
                            }
                            if (KeyBindings.TOGGLE_DRAGONS_EYE_VISION.consumeClick()) {
                                NetworkHandler.INSTANCE.sendToServer(new DragonsEyeToggleMessage(1));
                            }
                        });
            });
        }
    }
    // 处理模式切换
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
    // 处理夜视切换
    public static void handleVisionToggle(ServerPlayer player, ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        boolean newVision = !nbt.getBoolean(TAG_NIGHT_VISION_MODE);
        nbt.putBoolean(TAG_NIGHT_VISION_MODE, newVision);
        sendStatusMessage(player, "item.dragons_eye.night_vision." + (newVision ? "on" : "off"));

        if (!newVision) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }

        // 确保修饰符效果保持生效
        if (stack.getItem() instanceof DragonsEyeItem item) {
            item.applyModifier(player, stack);
        }
    }

    private static void sendStatusMessage(ServerPlayer player, String translationKey) {
        player.displayClientMessage(Component.translatable(translationKey), true);
    }


    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class DragonEyeEvents {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            // 只在POST阶段处理
            if (event.phase != TickEvent.Phase.END) return;

            Player player = event.player;
            if (player.level().isClientSide) return;

            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.findFirstCurio(stack -> stack.getItem() instanceof DragonsEyeItem)
                        .ifPresent(curio -> {
                            ItemStack eye = curio.stack();
                            // 应用效果
                            applyEffects(player, eye);

                            // 每 5tick 更新一次目标
                            if (player.tickCount % 5 == 0 && player instanceof ServerPlayer serverPlayer) {
                                updateTargets(serverPlayer, eye);
                            }
                        });
            });
        }

        private static void applyEffects(Player player, ItemStack stack) {
            if (player.level().isClientSide) return;  // 确保在服务器端执行

            // 防火效果
            if (!player.hasEffect(MobEffects.FIRE_RESISTANCE) ||
                    player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration() <= 20) {
                MobEffectInstance fireResistance = new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        Integer.MAX_VALUE,
                        0,
                        true,
                        false);
                player.addEffect(fireResistance);
            }

            // 夜视效果
            CompoundTag nbt = stack.getOrCreateTag();
            if (nbt.getBoolean(TAG_NIGHT_VISION_MODE)) {
                if (!player.hasEffect(MobEffects.NIGHT_VISION) ||
                        player.getEffect(MobEffects.NIGHT_VISION).getDuration() <= 20) {
                    MobEffectInstance nightVision = new MobEffectInstance(
                            MobEffects.NIGHT_VISION,
                            Integer.MAX_VALUE,
                            0,
                            true,
                            false);
                    player.addEffect(nightVision);

                    // 同步到客户端
                    if (player instanceof ServerPlayer) {
                        NetworkHandler.sendToClient(new UpdateEffectsMessage(nightVision), (ServerPlayer) player);
                    }
                }
            } else {
                // 如果夜视模式关闭，移除效果
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        // 调用父类的onEquip方法，它会处理修饰符的比较和应用
        super.onEquip(slotContext, prevStack, stack);

        // 只处理首次装备的初始化
        if (!stack.getOrCreateTag().getBoolean(TAG_IS_INITIALIZED)) {
            stack.getOrCreateTag().putBoolean(TAG_IS_INITIALIZED, true);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        // 调用父类的onUnequip方法，它会处理修饰符的移除
        super.onUnequip(slotContext, newStack, stack);

        // 只处理效果移除
        if (slotContext.entity() instanceof Player player) {
            // 移除抗火效果
            player.removeEffect(MobEffects.FIRE_RESISTANCE);

            // 只有在夜视模式开启时才移除夜视效果
            CompoundTag nbt = stack.getOrCreateTag();
            if (nbt.getBoolean(TAG_NIGHT_VISION_MODE)) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }

            // 清空目标列表
            nbt.put(TAG_DRAGONS_EYE_TARGETS, new ListTag());
        }
    }

    // 更新目标方块位置
    public static void updateTargets(ServerPlayer player, ItemStack stack) {
        if (player.level().isClientSide) return;

        CompoundTag nbt = stack.getOrCreateTag();
        boolean scanChests = nbt.getBoolean(TAG_TARGET_MODE);
        int groupIndex = nbt.getInt(TAG_ORE_GROUP_INDEX);

        ListTag targetsList = new ListTag();

        if (!(groupIndex == -1 && !scanChests)) {
            Set<Block> targetBlocks = scanChests ? CHEST_BLOCKS : ORE_GROUPS.get(groupIndex);
            int scanRange = Config.DRAGONS_EYE_SCAN_RANGE.get();

            if (targetBlocks != null && !targetBlocks.isEmpty()) {
                BlockPos.betweenClosedStream(
                                player.blockPosition().offset(-scanRange, -scanRange, -scanRange),
                                player.blockPosition().offset(scanRange, scanRange, scanRange))
                        .filter(pos -> {
                            try {
                                return targetBlocks.contains(player.level().getBlockState(pos).getBlock());
                            } catch (Exception e) {
                                TrinketsandBaublesMod.LOGGER.error("Error checking block at {}: {}", pos, e.getMessage());
                                return false;
                            }
                        })
                        .forEach(pos -> {
                            CompoundTag posTag = new CompoundTag();
                            posTag.putInt("X", pos.getX());
                            posTag.putInt("Y", pos.getY());
                            posTag.putInt("Z", pos.getZ());
                            targetsList.add(posTag);
                        });
            }
        }

        // 更新服务端 NBT
        nbt.put(TAG_DRAGONS_EYE_TARGETS, targetsList);

        // 发送更新到客户端
        NetworkHandler.sendToClient(new UpdateTargetsMessage(targetsList), player);
    }


    // 获取当前组的颜色
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
    @Override
    public int getEnchantmentValue() {
        return 0; // 附魔等级为0
    }

    // 禁止任何形式的附魔（包括铁砧）
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_eye.tooltip1"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_eye.tooltip2"));
        tooltip.add(Component.translatable("item.dragons_eye.tooltip3",
                        Config.DRAGONS_EYE_SCAN_RANGE.get()) // 使用配置值
                .withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}