package com.jinqinxixi.trinketsandbaubles.capability.event;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.BaseRaceCapabilityProvider;
import com.jinqinxixi.trinketsandbaubles.capability.impl.*;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.util.RaceRingUtil;
import com.jinqinxixi.trinketsandbaubles.util.RaceScaleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.jinqinxixi.trinketsandbaubles.util.RaceHelper.SAVED_RACE_KEY;

@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
public class RaceEventHandler {
    private static final String LAST_ACTIVE_RACE_KEY = "LastActiveRaceCapability";
    private static final int RESTORE_DELAY_TICKS = 20;

    // 种族能力键名
    private static final String DWARVES_CAP_KEY = "DwarvesCapability";
    private static final String ELVES_CAP_KEY = "ElvesCapability";
    private static final String FAELES_CAP_KEY = "FaelesCapability";
    private static final String TITAN_CAP_KEY = "TitanCapability";
    private static final String GOBLINS_CAP_KEY = "GoblinsCapability";
    private static final String FAIRY_CAP_KEY = "FairyCapability";
    private static final String DRAGON_CAP_KEY = "DragonCapability";
    private static final String TAURUS_CAP_KEY = "TaurusCapability";

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            registerRaceCapability(event, player, "dwarves", ModCapabilities.DWARVES_CAPABILITY,
                    new DwarvesCapability(player));
            registerRaceCapability(event, player, "elves", ModCapabilities.ELVES_CAPABILITY,
                    new ElvesCapability(player));
            registerRaceCapability(event, player, "faeles", ModCapabilities.FAELES_CAPABILITY,
                    new FaelesCapability(player));
            registerRaceCapability(event, player, "titan", ModCapabilities.TITAN_CAPABILITY,
                    new TitanCapability(player));
            registerRaceCapability(event, player, "goblins", ModCapabilities.GOBLINS_CAPABILITY,
                    new GoblinsCapability(player));
            registerRaceCapability(event, player, "fairy", ModCapabilities.FAIRY_CAPABILITY,
                    new FairyCapability(player));
            registerRaceCapability(event, player, "dragon", ModCapabilities.DRAGON_CAPABILITY,
                    new DragonCapability(player));
            registerRaceCapability(event, player, "taurus", ModCapabilities.TAURUS_CAPABILITY,
                    new TaurusCapability(player));
            registerRaceCapability(event, player, "human", ModCapabilities.HUMAN_CAPABILITY,
                    new HumanCapability(player));
        }
    }

    private static void restorePlayerMana(Player player) {
        ManaData.restorePlayerMana(player);
    }

    private static <T extends IBaseRaceCapability> void registerRaceCapability(
            AttachCapabilitiesEvent<Entity> event,
            Player player,
            String raceName,
            Capability<T> capability,
            T implementation) {
        event.addCapability(
                new ResourceLocation(TrinketsandBaublesMod.MOD_ID, raceName),
                new BaseRaceCapabilityProvider<>(implementation, capability)
        );
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            saveRaceCapabilityState(player);
        }
    }

    private static void saveRaceCapabilityState(Player player) {
        saveCapabilityData(player, ModCapabilities.DWARVES_CAPABILITY, DWARVES_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.ELVES_CAPABILITY, ELVES_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.FAELES_CAPABILITY, FAELES_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.TITAN_CAPABILITY, TITAN_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.GOBLINS_CAPABILITY, GOBLINS_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.FAIRY_CAPABILITY, FAIRY_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.DRAGON_CAPABILITY, DRAGON_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.TAURUS_CAPABILITY, TAURUS_CAP_KEY);
    }

    private static <T extends IBaseRaceCapability> void saveCapabilityData(
            Player player,
            Capability<T> capability,
            String key) {
        player.getCapability(capability).ifPresent(cap -> {
            if (cap.isActive()) {
                CompoundTag playerData = player.getPersistentData();
                // 照 serializeNBT 存全部：active 状态 + 外观颜色 + 魔力。之前只存 WasActive/CurrentMaxMana，
                // 颜色没存，死亡复活后颜色回默认。serializeNBT 本身会带上 saveAppearance（颜色）和
                // saveAdditional（魔力），一起存进来，复活时 deserializeNBT 一并还原。
                playerData.put(key, cap.serializeNBT());
            }
        });
    }


    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        if (!event.isWasDeath()) {
            handleDimensionalClone(original, player);
        } else {
            handleDeathClone(original, player);
        }
    }

    private static void handleDimensionalClone(Player original, Player player) {
        CompoundTag originalData = original.getPersistentData();
        CompoundTag newPlayerData = player.getPersistentData();

        if (originalData.contains(LAST_ACTIVE_RACE_KEY)) {
            newPlayerData.putString(LAST_ACTIVE_RACE_KEY,
                    originalData.getString(LAST_ACTIVE_RACE_KEY));
        }

        // 跨维度时把各种族保存的完整 NBT（含外观颜色）也复制过去，否则 restoreRaceAbility
        // 恢复时读不到颜色，回默认。照死亡路径的做法，颜色跟着种族一起跨维度。
        copyCapabilityNbt(originalData, newPlayerData, DWARVES_CAP_KEY);
        copyCapabilityNbt(originalData, newPlayerData, ELVES_CAP_KEY);
        copyCapabilityNbt(originalData, newPlayerData, FAELES_CAP_KEY);
        copyCapabilityNbt(originalData, newPlayerData, TITAN_CAP_KEY);
        copyCapabilityNbt(originalData, newPlayerData, GOBLINS_CAP_KEY);
        copyCapabilityNbt(originalData, newPlayerData, FAIRY_CAP_KEY);
        copyCapabilityNbt(originalData, newPlayerData, DRAGON_CAP_KEY);
        copyCapabilityNbt(originalData, newPlayerData, TAURUS_CAP_KEY);

        if (!player.level().isClientSide()) {
            scheduleRaceAbilityRestore(player);
        }
    }

    private static void copyCapabilityNbt(CompoundTag from, CompoundTag to, String key) {
        if (from.contains(key)) {
            to.put(key, from.getCompound(key));
        }
    }

    private static void handleDeathClone(Player original, Player player) {
        // 先保存之前存储的种族
        String savedRace = original.getPersistentData().getString(SAVED_RACE_KEY);

        // 清除所有能力
        AbstractRaceCapability.clearAllRaceAbilities(player);

        // 添加延迟处理
        net.minecraft.server.MinecraftServer server = player.level().getServer();
        if (server != null && player instanceof ServerPlayer serverPlayer) {  // 确保是服务器端玩家
            server.tell(new TickTask(server.getTickCount() + 1, () -> {
                // 恢复种族能力
                restoreRaceCapabilityFromKey(original, serverPlayer, DWARVES_CAP_KEY, ModCapabilities.DWARVES_CAPABILITY);
                restoreRaceCapabilityFromKey(original, serverPlayer, ELVES_CAP_KEY, ModCapabilities.ELVES_CAPABILITY);
                restoreRaceCapabilityFromKey(original, serverPlayer, FAELES_CAP_KEY, ModCapabilities.FAELES_CAPABILITY);
                restoreRaceCapabilityFromKey(original, serverPlayer, TITAN_CAP_KEY, ModCapabilities.TITAN_CAPABILITY);
                restoreRaceCapabilityFromKey(original, serverPlayer, GOBLINS_CAP_KEY, ModCapabilities.GOBLINS_CAPABILITY);
                restoreRaceCapabilityFromKey(original, serverPlayer, FAIRY_CAP_KEY, ModCapabilities.FAIRY_CAPABILITY);
                restoreRaceCapabilityFromKey(original, serverPlayer, DRAGON_CAP_KEY, ModCapabilities.DRAGON_CAPABILITY);
                restoreRaceCapabilityFromKey(original, serverPlayer, TAURUS_CAP_KEY, ModCapabilities.TAURUS_CAPABILITY);

                // 如果有之前保存的种族，且现在没有任何种族戒指，则恢复该种族
                if (!savedRace.isEmpty() && !RaceRingUtil.hasAnyRaceRing(serverPlayer)) {
                    RaceRingUtil.activateRace(serverPlayer, savedRace);
                }

                restorePlayerMana(serverPlayer);
            }));
        }
    }

    private static <T extends IBaseRaceCapability> void restoreRaceCapabilityFromKey(
            Player original,
            Player player,
            String capabilityKey,
            Capability<T> capability) {
        CompoundTag originalData = original.getPersistentData();
        if (originalData.contains(capabilityKey)) {
            CompoundTag capData = originalData.getCompound(capabilityKey);
            player.getCapability(capability).ifPresent(cap -> {
                // 照 deserializeNBT 恢复全部：active 状态 + 外观颜色 + 魔力。之前只 setActive(true)，
                // 颜色回默认。deserializeNBT 会读回 saveAppearance 存的颜色和 active 字段。
                if (capData.getBoolean("active")) {
                    cap.deserializeNBT(capData);
                    // deserializeNBT 只设了 isActive 和字段，不触发 setActive 的体型/属性副作用。
                    // 补上：体型（瞬间到位）和属性（+血量等），否则复活后体型回 1.0、属性丢失。
                    if (cap instanceof AbstractRaceCapability race) {
                        race.applyEffects();
                    }
                }
            });
        }
    }

    private static void scheduleRaceAbilityRestore(Player player) {
        player.level().getServer().tell(new TickTask(RESTORE_DELAY_TICKS, () -> {
            restoreRaceAbility(player);
        }));
    }

    private static void restoreRaceAbility(Player player) {
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains(LAST_ACTIVE_RACE_KEY)) return;

        AbstractRaceCapability.clearAllRaceAbilities(player);
        String raceName = playerData.getString(LAST_ACTIVE_RACE_KEY);

        // 种族名 → 保存 NBT 的 key，读完整 NBT（含外观颜色）恢复，而不是只 setActive(true)。
        String capKey = switch (raceName.toLowerCase()) {
            case "dwarves" -> DWARVES_CAP_KEY;
            case "elves" -> ELVES_CAP_KEY;
            case "faeles" -> FAELES_CAP_KEY;
            case "titan" -> TITAN_CAP_KEY;
            case "goblins" -> GOBLINS_CAP_KEY;
            case "fairy" -> FAIRY_CAP_KEY;
            case "dragon" -> DRAGON_CAP_KEY;
            case "taurus" -> TAURUS_CAP_KEY;
            default -> null;
        };

        if (capKey != null && playerData.contains(capKey)) {
            Capability<? extends IBaseRaceCapability> capability =
                    ModCapabilities.RACE_CAPABILITIES.get(raceName.toLowerCase());
            if (capability != null) {
                CompoundTag saved = playerData.getCompound(capKey);
                player.getCapability(capability).ifPresent(cap -> {
                    cap.deserializeNBT(saved);
                    // 补体型/属性副作用（deserializeNBT 只设字段，不触发 setActive 的副作用）。
                    if (cap instanceof AbstractRaceCapability race) {
                        race.applyEffects();
                    }
                });
            }
        }

        restorePlayerMana(player);
        playerData.remove(LAST_ACTIVE_RACE_KEY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof Player player) {
            saveActiveRaceState(player);
            AbstractRaceCapability.clearAllRaceAbilities(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityTravelToDimensionCanceled(EntityTravelToDimensionEvent event) {
        if (event.isCanceled() && event.getEntity() instanceof Player player) {
            if (!player.level().isClientSide()) {
                player.level().getServer().tell(new TickTask(1, () -> {
                    restoreRaceAbility(player);
                }));
            }
        }
    }

    private static void saveActiveRaceState(Player player) {
        player.getCapability(ModCapabilities.DWARVES_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive()) {
                player.getPersistentData().putString(LAST_ACTIVE_RACE_KEY, "dwarves");
            }
        });
        player.getCapability(ModCapabilities.ELVES_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive()) {
                player.getPersistentData().putString(LAST_ACTIVE_RACE_KEY, "elves");
            }
        });
        player.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive()) {
                player.getPersistentData().putString(LAST_ACTIVE_RACE_KEY, "faeles");
            }
        });
        player.getCapability(ModCapabilities.TITAN_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive()) {
                player.getPersistentData().putString(LAST_ACTIVE_RACE_KEY, "titan");
            }
        });
        player.getCapability(ModCapabilities.GOBLINS_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive()) {
                player.getPersistentData().putString(LAST_ACTIVE_RACE_KEY, "goblins");
            }
        });
        player.getCapability(ModCapabilities.FAIRY_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive()) {
                player.getPersistentData().putString(LAST_ACTIVE_RACE_KEY, "fairy");
            }
        });
        player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive()) {
                player.getPersistentData().putString(LAST_ACTIVE_RACE_KEY, "dragon");
            }
        });

        // 存完整 NBT（含外观颜色），跨维度恢复时用 deserializeNBT 还原，否则颜色回默认。
        saveCapabilityData(player, ModCapabilities.DWARVES_CAPABILITY, DWARVES_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.ELVES_CAPABILITY, ELVES_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.FAELES_CAPABILITY, FAELES_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.TITAN_CAPABILITY, TITAN_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.GOBLINS_CAPABILITY, GOBLINS_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.FAIRY_CAPABILITY, FAIRY_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.DRAGON_CAPABILITY, DRAGON_CAP_KEY);
        saveCapabilityData(player, ModCapabilities.TAURUS_CAPABILITY, TAURUS_CAP_KEY);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

        refreshRaceCapabilities(event.getEntity());

        // And tells the client what the race looks like, which the two events below already do and
        // this one did not. Without it a player who set a colour saw it while they were in the world
        // and lost it on the next login: the value was saved on the server the whole time, but the
        // client had never been told it, so it drew the defaults.
        syncRaceCapabilities(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            restoreRaceAbility(player);
            syncRaceCapabilities(player);
            restorePlayerMana(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncRaceCapabilities(event.getEntity());
    }

    private static void syncRaceCapabilities(Player player) {
        player.getCapability(ModCapabilities.DWARVES_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.sync(); });
        player.getCapability(ModCapabilities.ELVES_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.sync(); });
        player.getCapability(ModCapabilities.FAELES_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.sync(); });
        player.getCapability(ModCapabilities.TITAN_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.sync(); });
        player.getCapability(ModCapabilities.GOBLINS_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.sync(); });
        player.getCapability(ModCapabilities.FAIRY_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.sync(); });
        player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.sync(); });
        player.getCapability(ModCapabilities.TAURUS_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.sync(); });
    }

    public static void refreshRaceCapabilities(Player player) {
        // 重新激活需要的种族能力
        ModCapabilities.RACE_CAPABILITIES.forEach((raceName, capability) -> {
            player.getCapability(capability).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap) {
                    if (abstractCap.isActive()) {
                        // 使用统一的方法应用效果
                        abstractCap.applyEffects();
                    }
                }
            });
        });
    }

    // 特殊能力事件处理
    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        handleDwarvesBlockBreak(player, event);
        handleTitanBlockBreak(player, event);
    }

    private static void handleDwarvesBlockBreak(Player player, BlockEvent.BreakEvent event) {
        player.getCapability(ModCapabilities.DWARVES_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive() && !event.getLevel().isClientSide() &&
                    event.getLevel() instanceof ServerLevel serverLevel) {
                cap.onBreakBlock(event.getPos(), event.getState().getBlock(), serverLevel);
            }
        });
    }
    private static void handleTitanBlockBreak(Player player, BlockEvent.BreakEvent event) {
        player.getCapability(ModCapabilities.TITAN_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive() && !event.getLevel().isClientSide() &&
                    event.getLevel() instanceof ServerLevel serverLevel) {
                cap.onBreakBlock(event.getPos(), event.getState().getBlock(), serverLevel);
            }
        });
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 推进体型缩放的平滑过渡。无论是否激活种族都要推进 —— 移除种族后也要从当前值
            // 平滑回 1.0，否则碰撞箱会瞬间弹回正常体型。
            RaceScaleHelper.tick(player);
            updateRaceCapabilities(player);
        }
    }

    private static void updateRaceCapabilities(Player player) {
        player.getCapability(ModCapabilities.DWARVES_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.tick(); });
        player.getCapability(ModCapabilities.ELVES_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.tick(); });
        player.getCapability(ModCapabilities.FAELES_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.tick(); });
        player.getCapability(ModCapabilities.TITAN_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.tick(); });
        player.getCapability(ModCapabilities.GOBLINS_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.tick(); });
        player.getCapability(ModCapabilities.FAIRY_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.tick(); });
        player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.tick(); });
        player.getCapability(ModCapabilities.TAURUS_CAPABILITY)
                .ifPresent(cap -> { if (cap.isActive()) cap.tick(); });
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof Arrow &&
                event.getSource().getEntity() instanceof Player player) {
            player.getCapability(ModCapabilities.ELVES_CAPABILITY).ifPresent(cap -> {
                if (cap.isActive() && player.isCrouching()) {
                    float newDamage = event.getAmount() *
                            RaceAttributesConfig.ELVES.ELVES_BOW_DAMAGE_BOOST.get().floatValue();
                    event.setAmount(newDamage);
                }
            });
                player.getCapability(ModCapabilities.GOBLINS_CAPABILITY).ifPresent(cap -> {
                    DamageSource source = event.getSource();
                    if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) ||
                            source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE) ||
                            source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) {

                        event.setAmount(cap.handleDamage(event.getAmount(), true));
                    }
                });
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {

            // 处理猫妖种族跳跃
            player.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
                if (cap.isActive()) {
                    ((FaelesCapability) cap).onJump();
                }
            });

            // 处理泰坦种族跳跃
            player.getCapability(ModCapabilities.TITAN_CAPABILITY).ifPresent(cap -> {
                if (cap.isActive()) {
                    cap.handleJump();
                }
            });
            // 处理仙女种族跳跃
            player.getCapability(ModCapabilities.FAIRY_CAPABILITY).ifPresent(cap -> {
                if (cap.isActive()) {
                    Vec3 motion = player.getDeltaMovement();
                    double multiplier = 1.0 + RaceAttributesConfig.FAIRY.FAIRY_DEW_JUMP_BOOST.get();
                    player.setDeltaMovement(motion.x, motion.y * multiplier, motion.z);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player &&
                event.getItem().getItem() == Items.MILK_BUCKET) {
            player.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
                if (cap.isActive()) {
                    ((FaelesCapability) cap).onDrinkMilk();
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;

        // 统一处理爬墙逻辑
        handleRaceWallClimb(player);

        // 处理泰坦种族水中下沉
        handleTitanWaterMovement(player);

        // Counts the glide push cooldowns down and holds glides up. The gliding itself is the game's,
        // but the game clears its flag every tick because the player wears no elytra, so it is written
        // back here.
        com.jinqinxixi.trinketsandbaubles.capability.glide.GlideHandler.tickCooldowns();

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            com.jinqinxixi.trinketsandbaubles.capability.glide.GlideHandler.tickGliding(serverPlayer);
        }
    }

    private static void handleTitanWaterMovement(Player player) {
        player.getCapability(ModCapabilities.TITAN_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive()) {
                ((TitanCapability) cap).handleWaterMovement();
            }
        });
    }

    private static void handleRaceWallClimb(Player player) {
        // 处理猫妖种族爬墙
        player.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive() && RaceAttributesConfig.FAELES.FAELES_WALL_CLIMB.get()) {
                ((FaelesCapability) cap).handleWallClimb();
            }
        });

        // 处理精灵种族爬墙
        player.getCapability(ModCapabilities.FAIRY_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive() && RaceAttributesConfig.FAIRY.FAIRY_DEW_WALL_CLIMB.get()) {
                ((FairyCapability) cap).handleWallClimb();
            }
        });
    }
    @SubscribeEvent
    public static void onCreeperTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof Player player &&
                event.getEntity() instanceof Creeper) {
            player.getCapability(ModCapabilities.GOBLINS_CAPABILITY).ifPresent(cap -> {
                if (cap.isActive()) {
                    event.setCanceled(true);
                }
            });
        }
    }
    @SubscribeEvent
    public static void onPlayerGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        Player player = event.getEntity();
        player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive()) {
                ((DragonCapability) cap).handleGameModeChange(event.getNewGameMode());
            }
        });
    }
}