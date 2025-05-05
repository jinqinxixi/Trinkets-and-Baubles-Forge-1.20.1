package com.jinqinxixi.trinketsandbaubles.util;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.UpdateTargetsMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
public class ScanSystem {
    public static final String TAG_DRAGONS_EYE_TARGETS = "Targets";

    private static final List<Set<Block>> ORE_GROUPS = new ArrayList<>();
    private static final Set<Block> CHEST_BLOCKS = new HashSet<>();
    private static final Map<UUID, PlayerScanState> PLAYER_SCAN_STATES = new HashMap<>();

    private static final List<String> ORE_GROUP_NAMES = Arrays.asList(
            "valuables", // 贵重矿物（钻石、绿宝石、金）
            "common",    // 常见矿物（铁、煤、铜）
            "redstone", // 红石相关
            "all"       // 所有矿物
    );

    public static class PlayerScanState {
        public boolean isTargetMode = false;
        public int oreGroupIndex = -1;
        public ListTag lastTargets = new ListTag();
        public boolean scanEnabled = false;
        public boolean nightVisionEnabled = false;
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PLAYER_SCAN_STATES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        if (event.player instanceof ServerPlayer player) {
            checkAndUpdateScan(player);
            applyEffects(player);
        }
    }

    public static void toggleNightVision(ServerPlayer player) {
        PlayerScanState state = getPlayerState(player);
        state.nightVisionEnabled = !state.nightVisionEnabled;

        player.displayClientMessage(
                Component.translatable("item.dragons_eye.night_vision." +
                        (state.nightVisionEnabled ? "on" : "off")), true);

        if (!state.nightVisionEnabled) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    public static boolean hasNightVision(Player player) {
        return getPlayerState(player).nightVisionEnabled;
    }

    private static void applyEffects(ServerPlayer player) {
        boolean hasDragonCapability = player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                .map(cap -> cap instanceof DragonCapability && cap.isActive())
                .orElse(false);

        boolean hasDragonsEye = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof DragonsEyeItem)
                        .isPresent())
                .orElse(false);

        if (!hasDragonCapability && !hasDragonsEye) {
            return;
        }

        PlayerScanState state = getPlayerState(player);

        if (!player.hasEffect(MobEffects.FIRE_RESISTANCE) ||
                player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration() <= 20) {
            MobEffectInstance fireResistance = new MobEffectInstance(
                    MobEffects.FIRE_RESISTANCE,
                    Integer.MAX_VALUE,
                    0,
                    true,
                    false
            );
            player.addEffect(fireResistance);
        }

        if (state.nightVisionEnabled) {
            if (!player.hasEffect(MobEffects.NIGHT_VISION) ||
                    player.getEffect(MobEffects.NIGHT_VISION).getDuration() <= 20) {
                MobEffectInstance nightVision = new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        Integer.MAX_VALUE,
                        0,
                        true,
                        false
                );
                player.addEffect(nightVision);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player != null && KeyBindings.TOGGLE_DRAGONS_EYE_MODE.consumeClick()) {
            NetworkHandler.INSTANCE.sendToServer(new UpdateTargetsMessage());
        }
    }

    public static void initializeOreGroups() {
        ORE_GROUPS.clear();

        Set<Block> valuableOres = new HashSet<>(Arrays.asList(
                Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
                Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
                Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
                Blocks.ANCIENT_DEBRIS
        ));
        addConfiguredBlocks(ModConfig.VALUABLE_ORES.get(), valuableOres);
        ORE_GROUPS.add(valuableOres);

        Set<Block> commonOres = new HashSet<>(Arrays.asList(
                Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
                Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
                Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
                Blocks.NETHER_QUARTZ_ORE, Blocks.NETHER_GOLD_ORE
        ));
        addConfiguredBlocks(ModConfig.COMMON_ORES.get(), commonOres);
        ORE_GROUPS.add(commonOres);

        Set<Block> redstoneOres = new HashSet<>(Arrays.asList(
                Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
                Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE
        ));
        addConfiguredBlocks(ModConfig.REDSTONE_ORES.get(), redstoneOres);
        ORE_GROUPS.add(redstoneOres);

        Set<Block> allOres = new HashSet<>();
        allOres.addAll(valuableOres);
        allOres.addAll(commonOres);
        allOres.addAll(redstoneOres);
        ORE_GROUPS.add(allOres);

        CHEST_BLOCKS.clear();
        BuiltInRegistries.BLOCK.stream()
                .filter(b -> b instanceof ShulkerBoxBlock ||
                        BuiltInRegistries.BLOCK.getKey(b).getPath().contains("chest") ||
                        BuiltInRegistries.BLOCK.getKey(b).getPath().contains("barrel"))
                .forEach(CHEST_BLOCKS::add);
    }

    private static void addConfiguredBlocks(List<? extends String> configList, Set<Block> blockSet) {
        for (String blockId : configList) {
            try {
                ResourceLocation resourceLocation = new ResourceLocation(blockId);
                Block block = BuiltInRegistries.BLOCK.get(resourceLocation);
                if (block != Blocks.AIR) {
                    blockSet.add(block);
                } else {
                    TrinketsandBaublesMod.LOGGER.warn("Could not find block with id: " + blockId);
                }
            } catch (Exception e) {
                TrinketsandBaublesMod.LOGGER.error("Error adding configured block: " + blockId, e);
            }
        }
    }

    private static void checkAndUpdateScan(ServerPlayer player) {
        PlayerScanState state = getPlayerState(player);
        boolean canScanNow = canPlayerScan(player);

        if (state.scanEnabled != canScanNow) {
            state.scanEnabled = canScanNow;
            if (!canScanNow) {
                clearTargets(player);
                return;
            }
        }

        if (canScanNow && state.oreGroupIndex != -1 || state.isTargetMode) {
            updateTargets(player);
        }
    }

    public static boolean canPlayerScan(Player player) {
        boolean hasDragonCapability = player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                .map(cap -> cap instanceof DragonCapability && cap.isActive())
                .orElse(false);

        boolean hasDragonsEye = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof DragonsEyeItem)
                        .isPresent())
                .orElse(false);

        return hasDragonCapability || hasDragonsEye;
    }

    public static void handleScanToggleRequest(ServerPlayer player) {
        if (!canPlayerScan(player)) return;

        PlayerScanState state = getPlayerState(player);
        toggleScanMode(player, state);
    }

    public static PlayerScanState getPlayerState(Player player) {
        return PLAYER_SCAN_STATES.computeIfAbsent(player.getUUID(), k -> new PlayerScanState());
    }

    private static void toggleScanMode(ServerPlayer player, PlayerScanState state) {
        if (state.isTargetMode) {
            state.isTargetMode = false;
            state.oreGroupIndex = -1;
        } else {
            if (state.oreGroupIndex == -1) {
                state.oreGroupIndex = 0;
            } else {
                state.oreGroupIndex = (state.oreGroupIndex + 1) % (ORE_GROUPS.size() + 1);
                if (state.oreGroupIndex == ORE_GROUPS.size()) {
                    state.isTargetMode = true;
                } else if (state.oreGroupIndex == 0) {
                    state.isTargetMode = false;
                    state.oreGroupIndex = -1;
                }
            }
        }

        sendStatusMessage(player, state);
        updateTargets(player);
    }

    private static void updateTargets(ServerPlayer player) {
        if (player.level().isClientSide) return;

        PlayerScanState state = getPlayerState(player);
        ListTag targets = scanForTargets(player, state);
        state.lastTargets = targets;

        NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new UpdateTargetsMessage(targets, state.isTargetMode, state.oreGroupIndex)
        );
    }

    private static ListTag scanForTargets(ServerPlayer player, PlayerScanState state) {
        if (player.level().isClientSide) return new ListTag();

        ListTag targetsList = new ListTag();
        if (state.oreGroupIndex == -1 && !state.isTargetMode) return targetsList;

        Set<Block> targetBlocks = state.isTargetMode ? CHEST_BLOCKS :
                (state.oreGroupIndex >= 0 && state.oreGroupIndex < ORE_GROUPS.size() ?
                        ORE_GROUPS.get(state.oreGroupIndex) : null);

        if (targetBlocks != null && !targetBlocks.isEmpty()) {
            int scanRange = ModConfig.RENDER_RANGE.get();
            BlockPos.betweenClosedStream(
                            player.blockPosition().offset(-scanRange, -scanRange, -scanRange),
                            player.blockPosition().offset(scanRange, scanRange, scanRange))
                    .filter(pos -> {
                        try {
                            return targetBlocks.contains(player.level().getBlockState(pos).getBlock());
                        } catch (Exception e) {
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
        return targetsList;
    }

    private static void clearTargets(ServerPlayer player) {
        PlayerScanState state = getPlayerState(player);
        state.lastTargets = new ListTag();
        state.isTargetMode = false;
        state.oreGroupIndex = -1;
        state.nightVisionEnabled = false;
        player.removeEffect(MobEffects.NIGHT_VISION);
        checkAndClearEffects(player);
        NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new UpdateTargetsMessage(new ListTag(), false, -1)
        );
    }

    private static void checkAndClearEffects(ServerPlayer player) {
        boolean hasDragonCapability = player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                .map(cap -> cap instanceof DragonCapability && cap.isActive())
                .orElse(false);

        boolean hasDragonsEye = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof DragonsEyeItem)
                        .isPresent())
                .orElse(false);

        if (!hasDragonCapability && !hasDragonsEye) {
            player.removeEffect(MobEffects.FIRE_RESISTANCE);
        }
    }

    private static void sendStatusMessage(ServerPlayer player, PlayerScanState state) {
        String key;
        if (state.oreGroupIndex == -1 && !state.isTargetMode) {
            key = "item.dragons_eye.mode.off";
        } else if (state.isTargetMode) {
            key = "item.dragons_eye.mode.chest";
        } else {
            key = "item.dragons_eye.mode.ore." + ORE_GROUP_NAMES.get(state.oreGroupIndex);
        }
        player.displayClientMessage(Component.translatable(key), true);
    }

    public static int[] getColorForGroup(int groupIndex, boolean isChestMode) {
        if (groupIndex == -1 && !isChestMode) {
            return new int[]{128, 128, 128}; // 灰色
        }
        if (isChestMode) {
            return new int[]{255, 0, 255}; // 紫色
        }
        switch (groupIndex) {
            case 0: return new int[]{255, 255, 0}; // 黄色
            case 1: return new int[]{0, 255, 255}; // 青色
            case 2: return new int[]{255, 0, 0};   // 红色
            case 3: return new int[]{0, 255, 0};   // 绿色
            default: return new int[]{255, 255, 255}; // 白色
        }
    }

    public static boolean isTargetMode(Player player) {
        return getPlayerState(player).isTargetMode;
    }

    public static int getOreGroupIndex(Player player) {
        return getPlayerState(player).oreGroupIndex;
    }

    public static ListTag getCurrentTargets(Player player) {
        return getPlayerState(player).lastTargets;
    }
}