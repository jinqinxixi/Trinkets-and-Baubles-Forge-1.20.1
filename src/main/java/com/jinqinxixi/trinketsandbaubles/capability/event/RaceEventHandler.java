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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
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
    }

    private static <T extends IBaseRaceCapability> void saveCapabilityData(
            Player player,
            Capability<T> capability,
            String key) {
        player.getCapability(capability).ifPresent(cap -> {
            if (cap.isActive()) {
                CompoundTag playerData = player.getPersistentData();
                CompoundTag capData = new CompoundTag();
                capData.putBoolean("WasActive", true);
                capData.putFloat("CurrentMaxMana", ManaData.getMaxMana(player));
                playerData.put(key, capData);
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

        if (!player.level().isClientSide()) {
            scheduleRaceAbilityRestore(player);
        }
    }

    private static void handleDeathClone(Player original, Player player) {
        // 在恢复种族能力之前先清除所有能力
        AbstractRaceCapability.clearAllRaceAbilities(player);

        // 添加延迟处理
        net.minecraft.server.MinecraftServer server = player.level().getServer();
        if (server != null) {
            server.tell(new TickTask(server.getTickCount() + 1, () -> {

                // 恢复种族能力
                restoreRaceCapabilityFromKey(original, player, DWARVES_CAP_KEY, ModCapabilities.DWARVES_CAPABILITY);
                restoreRaceCapabilityFromKey(original, player, ELVES_CAP_KEY, ModCapabilities.ELVES_CAPABILITY);
                restoreRaceCapabilityFromKey(original, player, FAELES_CAP_KEY, ModCapabilities.FAELES_CAPABILITY);
                restoreRaceCapabilityFromKey(original, player, TITAN_CAP_KEY, ModCapabilities.TITAN_CAPABILITY);
                restoreRaceCapabilityFromKey(original, player, GOBLINS_CAP_KEY, ModCapabilities.GOBLINS_CAPABILITY);
                restoreRaceCapabilityFromKey(original, player, FAIRY_CAP_KEY, ModCapabilities.FAIRY_CAPABILITY);
                restoreRaceCapabilityFromKey(original, player, DRAGON_CAP_KEY, ModCapabilities.DRAGON_CAPABILITY);

                restorePlayerMana(player);
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
                if (capData.getBoolean("WasActive")) {
                    cap.setActive(true);
                    if (capData.contains("CurrentMaxMana")) {
                        ManaData.setMaxMana(player, capData.getFloat("CurrentMaxMana"));
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

        Capability<? extends IBaseRaceCapability> capability =
                ModCapabilities.RACE_CAPABILITIES.get(raceName.toLowerCase());
        if (capability != null) {
            player.getCapability(capability).ifPresent(cap -> cap.setActive(true));
            restorePlayerMana(player);
        }

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
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

        refreshRaceCapabilities(event.getEntity());
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
    public static void onEntityMount(EntityMountEvent event) {
        if (event.isMounting() &&
                event.getEntityMounting() instanceof Player player &&
                event.getEntityBeingMounted() instanceof AbstractHorse horse) {

            player.getCapability(ModCapabilities.GOBLINS_CAPABILITY).ifPresent(cap -> {
                if (cap.isActive()) {
                    ((GoblinsCapability)cap).handleMount(horse);
                }
            });
        }
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