package com.jinqinxixi.trinketsandbaubles.capability.mana;

import com.jinqinxixi.trinketsandbaubles.capability.event.RaceEventHandler;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.network.handler.ManaNetworkHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "trinketsandbaubles")
public class ManaData {
    private static final String MOD_ID = "trinketsandbaubles";
    private static final String MANA_KEY = MOD_ID + "_mana";
    private static final String MAX_MANA_KEY = MOD_ID + "_maxMana";
    private static final String LAST_MANA_REGEN_TIME_KEY = MOD_ID + "_lastManaRegenTime";
    private static final String LAST_MANA_CHANGE_TIME_KEY = MOD_ID + "_lastManaChangeTime";

    // 基础魔力操作
    public static float getMana(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getFloat(MANA_KEY);
    }

    public static void setMana(Player player, float mana) {
        if (player == null) return;

        CompoundTag data = player.getPersistentData();
        float maxMana = getMaxMana(player);
        float newMana = Math.max(0f, Math.min(mana, maxMana));
        data.putFloat(MANA_KEY, newMana);

        // 只在服务器端且玩家完全加载时同步
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection != null && !serverPlayer.isRemoved()) {
            syncManaToClient(serverPlayer);
        }
    }

    public static float getMaxMana(Player player) {
        CompoundTag data = player.getPersistentData();
        float mana = data.contains(MAX_MANA_KEY) ?
                data.getFloat(MAX_MANA_KEY) :
                ModConfig.DEFAULT_MAX_MANA.get().floatValue();
        return mana;
    }

    public static void setMaxMana(Player player, float maxMana) {
        if (player == null) return;

        CompoundTag data = player.getPersistentData();
        maxMana = Math.max(0f, maxMana);
        data.putFloat(MAX_MANA_KEY, maxMana);

        validateMana(player);

        // 只在服务器端且玩家完全加载时同步
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection != null && !serverPlayer.isRemoved()) {
            syncManaToClient(serverPlayer);
        }
    }

    public static void modifyMaxMana(Player player, float amount) {
        if (player == null) return;

        float currentMaxMana = getMaxMana(player);
        float newMaxMana = Math.max(0f, currentMaxMana + amount);

        CompoundTag data = player.getPersistentData();
        data.putFloat(MAX_MANA_KEY, newMaxMana);

        validateMana(player);

        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection != null && !serverPlayer.isRemoved()) {
            syncManaToClient(serverPlayer);
        }
    }

    // 魔力消耗和恢复
    public static void consumeMana(Player player, float amount) {
        if (!player.level().isClientSide) {
            CompoundTag data = player.getPersistentData();
            data.putLong(LAST_MANA_CHANGE_TIME_KEY, player.level().getGameTime());
            setMana(player, getMana(player) - amount);
        }
    }

    public static void addMana(Player player, float amount) {
        if (!player.level().isClientSide) {
            setMana(player, getMana(player) + amount);
        }
    }

    public static boolean hasMana(Player player, float amount) {
        return getMana(player) >= amount;
    }

    // 同步和验证
    private static void syncManaToClient(ServerPlayer player) {
        if (player == null || player.connection == null || player.isRemoved()) return;

        if (!player.level().isClientSide()) {
            try {
                ManaNetworkHandler.syncManaToClient(
                        player,
                        getMana(player),
                        getMaxMana(player)
                );
            } catch (Exception e) {
            }
        }
    }

    public static void validateMana(Player player) {
        float current = getMana(player);
        float max = getMaxMana(player);
        if (current > max) {
            setMana(player, max);
        } else if (current < 0) {
            setMana(player, 0);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player == null) return;

        CompoundTag data = player.getPersistentData();

        // 获取新的配置文件值和处理配置变化
        float newConfigValue = ModConfig.DEFAULT_MAX_MANA.get().floatValue();
        if (!data.contains(MAX_MANA_KEY)) {
            // 新玩家：直接使用配置值
            data.putFloat(MAX_MANA_KEY, newConfigValue);
            data.putFloat(MANA_KEY, newConfigValue);
        } else {
            // 老玩家：计算道具加成并保持
            float currentMaxMana = data.getFloat(MAX_MANA_KEY);
            float oldConfigValue = data.contains("config_mana") ?
                    data.getFloat("config_mana") : newConfigValue;

            // 计算道具总加成 = 当前最大魔力 - 旧配置值
            float itemBonus = currentMaxMana - oldConfigValue;

            // 新的最大魔力 = 新配置值 + 道具加成
            float newMaxMana = newConfigValue + itemBonus;
            data.putFloat(MAX_MANA_KEY, newMaxMana);
        }

        // 记录当前配置值，用于下次计算
        data.putFloat("config_mana", newConfigValue);

        // 处理旧版本数据兼容性
        handleLegacyData(data);

        // 确保数据有效
        validateMana(player);

        // 使用更安全的延迟同步机制
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.level().getServer().tell(new TickTask(
                    serverPlayer.level().getServer().getTickCount() + 2,
                    () -> {
                        if (serverPlayer.connection != null && !serverPlayer.isRemoved()) {
                            // 先同步魔力值
                            syncManaToClient(serverPlayer);

                            // 然后刷新种族能力
                            RaceEventHandler.refreshRaceCapabilities(player);

                            // 最后恢复魔力值
                            restorePlayerMana(player);
                        }
                    }
            ));
        }
    }

    public static void restorePlayerMana(Player player) {
        if (!player.level().isClientSide) {
            // 直接获取全局容器中的最大魔力值
            float maxMana = getMaxMana(player);
            setMana(player, maxMana);
        }
    }


    private static void handleLegacyData(CompoundTag data) {
        if (data.contains(MANA_KEY) && data.contains(MAX_MANA_KEY)) {
            if (data.contains(MANA_KEY, CompoundTag.TAG_INT)) {
                float oldMana = data.getInt(MANA_KEY);
                data.putFloat(MANA_KEY, oldMana);
            }
            if (data.contains(MAX_MANA_KEY, CompoundTag.TAG_INT)) {
                float oldMaxMana = data.getInt(MAX_MANA_KEY);
                data.putFloat(MAX_MANA_KEY, oldMaxMana);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            // 重生时重置魔力为最大值
            setMana(player, getMaxMana(player));
            syncManaToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            if (!player.level().isClientSide()) {
                CompoundTag data = player.getPersistentData();
                long currentTime = player.level().getGameTime();
                long lastRegenTime = data.getLong(LAST_MANA_REGEN_TIME_KEY);
                long lastManaChangeTime = data.getLong(LAST_MANA_CHANGE_TIME_KEY);

                // 创造模式快速恢复
                if (player.isCreative()) {
                    float currentMana = getMana(player);
                    float maxMana = getMaxMana(player);
                    if (currentMana < maxMana) {
                        float newMana = Math.min(currentMana + ModConfig.CREATIVE_REGEN_RATE.get().floatValue(), maxMana);
                        setMana(player, newMana);
                    }
                } else {
                    // 生存模式正常恢复
                    if (currentTime - lastRegenTime >= ModConfig.MANA_REGEN_INTERVAL.get() &&
                            currentTime - lastManaChangeTime >= ModConfig.MANA_REGEN_COOLDOWN.get()) {

                        float currentMana = getMana(player);
                        float maxMana = getMaxMana(player);

                        if (currentMana < maxMana) {
                            float newMana = Math.min(currentMana + ModConfig.MANA_REGEN_RATE.get().floatValue(), maxMana);
                            setMana(player, newMana);
                        }

                        data.putLong(LAST_MANA_REGEN_TIME_KEY, currentTime);
                    }
                }
            }
        }
    }

    // 玩家克隆事件（死亡、维度传送等）
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        // 复制魔力相关数据
        CompoundTag originalData = original.getPersistentData();
        CompoundTag newData = player.getPersistentData();

        if (originalData.contains(MAX_MANA_KEY)) {
            newData.putFloat(MAX_MANA_KEY, originalData.getFloat(MAX_MANA_KEY));
        }

        // 如果不是死亡传送，保持当前魔力值
        if (!event.isWasDeath()) {
            if (originalData.contains(MANA_KEY)) {
                newData.putFloat(MANA_KEY, originalData.getFloat(MANA_KEY));
            }
        } else {
            // 死亡重生时重置为最大魔力值
            newData.putFloat(MANA_KEY, getMaxMana(player));
        }

        // 重置计时器
        newData.putLong(LAST_MANA_REGEN_TIME_KEY, player.level().getGameTime());
        newData.putLong(LAST_MANA_CHANGE_TIME_KEY, player.level().getGameTime());

        // 同步到客户端
        if (player instanceof ServerPlayer serverPlayer) {
            syncManaToClient(serverPlayer);
        }
    }
}