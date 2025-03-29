package com.jinqinxixi.trinketsandbaubles.capability.mana;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.network.handler.ManaNetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "trinketsandbaubles")
public class ManaData {
    private static final String MANA_KEY = "mana";
    private static final String MAX_MANA_KEY = "maxMana";
    private static final String LAST_MANA_REGEN_TIME_KEY = "lastManaRegenTime";
    private static final String LAST_MANA_CHANGE_TIME_KEY = "lastManaChangeTime";

    // 基础魔力操作
    public static int getMana(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getInt(MANA_KEY);
    }

    public static void setMana(Player player, int mana) {
        CompoundTag data = player.getPersistentData();
        int maxMana = getMaxMana(player);
        int newMana = Math.max(0, Math.min(mana, maxMana));
        data.putInt(MANA_KEY, newMana);

        if (player instanceof ServerPlayer serverPlayer) {
            syncManaToClient(serverPlayer);
        }
    }

    public static int getMaxMana(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.contains(MAX_MANA_KEY) ?
                data.getInt(MAX_MANA_KEY) :
                ModConfig.DEFAULT_MAX_MANA.get();
    }

    public static void setMaxMana(Player player, int maxMana) {
        CompoundTag data = player.getPersistentData();
        maxMana = Math.max(10, maxMana); // 最小值为10
        data.putInt(MAX_MANA_KEY, maxMana);

        validateMana(player);
        if (player instanceof ServerPlayer serverPlayer) {
            syncManaToClient(serverPlayer);
        }
    }

    // 魔力消耗和恢复
    public static void consumeMana(Player player, float amount) {
        if (!player.level().isClientSide) {
            CompoundTag data = player.getPersistentData();
            data.putLong(LAST_MANA_CHANGE_TIME_KEY, player.level().getGameTime());
            setMana(player, (int)(getMana(player) - amount));
        }
    }

    public static void addMana(Player player, float amount) {
        if (!player.level().isClientSide) {
            setMana(player, (int)(getMana(player) + amount));
        }
    }

    public static boolean hasMana(Player player, float amount) {
        return getMana(player) >= amount;
    }

    // 同步和验证
    private static void syncManaToClient(ServerPlayer player) {
        if (!player.level().isClientSide() && !player.isRemoved()) {
            ManaNetworkHandler.syncManaToClient(
                    player,
                    getMana(player),
                    getMaxMana(player)
            );
        }
    }

    public static void validateMana(Player player) {
        int current = getMana(player);
        int max = getMaxMana(player);
        if (current > max) {
            setMana(player, max);
        } else if (current < 0) {
            setMana(player, 0);
        }
    }

    // 事件处理
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        CompoundTag data = player.getPersistentData();

        // 初始化所有数据
        if (!data.contains(MAX_MANA_KEY)) {
            data.putInt(MAX_MANA_KEY, ModConfig.DEFAULT_MAX_MANA.get());
        }
        if (!data.contains(MANA_KEY)) {
            data.putInt(MANA_KEY, getMaxMana(player));
        }
        if (!data.contains(LAST_MANA_REGEN_TIME_KEY)) {
            data.putLong(LAST_MANA_REGEN_TIME_KEY, player.level().getGameTime());
        }
        if (!data.contains(LAST_MANA_CHANGE_TIME_KEY)) {
            data.putLong(LAST_MANA_CHANGE_TIME_KEY, player.level().getGameTime());
        }
        // 确保服务器端数据有效
        validateMana(player);

        // 立即同步到客户端，使用延迟以确保客户端准备就绪
        if (player instanceof ServerPlayer serverPlayer) {
            // 延迟1tick后同步，确保客户端完全加载
            serverPlayer.level().getServer().tell(new net.minecraft.server.TickTask(
                    serverPlayer.level().getServer().getTickCount() + 1,
                    () -> {
                        System.out.println("准备发送初始魔力同步");
                        System.out.println("发送魔力同步：" + getMana(player) + "/" + getMaxMana(player));
                        syncManaToClient(serverPlayer);
                    }
            ));
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
                    int currentMana = getMana(player);
                    int maxMana = getMaxMana(player);
                    if (currentMana < maxMana) {
                        int newMana = Math.min(currentMana + ModConfig.CREATIVE_REGEN_RATE.get(), maxMana);
                        setMana(player, newMana);
                    }
                } else {
                    // 生存模式正常恢复
                    if (currentTime - lastRegenTime >= ModConfig.MANA_REGEN_INTERVAL.get() &&
                            currentTime - lastManaChangeTime >= ModConfig.MANA_REGEN_COOLDOWN.get()) {

                        int currentMana = getMana(player);
                        int maxMana = getMaxMana(player);

                        if (currentMana < maxMana) {
                            int newMana = Math.min(currentMana + ModConfig.MANA_REGEN_RATE.get(), maxMana);
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
            newData.putInt(MAX_MANA_KEY, originalData.getInt(MAX_MANA_KEY));
        }

        // 如果不是死亡传送，保持当前魔力值
        if (!event.isWasDeath()) {
            if (originalData.contains(MANA_KEY)) {
                newData.putInt(MANA_KEY, originalData.getInt(MANA_KEY));
            }
        } else {
            // 死亡重生时重置为最大魔力值
            newData.putInt(MANA_KEY, getMaxMana(player));
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