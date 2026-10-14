package com.jinqinxixi.trinketsandbaubles.util;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

public class RaceHelper {
    public static final String SAVED_RACE_KEY = "SavedRace";
    public static final String SAVED_DRAGON_ELEMENT_KEY = "SavedDragonElement";

    // 检查玩家当前的种族状态
    public static void checkAndUpdateRaceState(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // 检查是否装备了任何种族戒指 - 如果有戒指，直接返回
        boolean hasAnyRaceRing = RaceRingUtil.hasAnyRaceRing(player);
        if (hasAnyRaceRing) return;

        // 获取当前激活的种族
        String currentRace = getActiveRace(player);

        // 获取之前保存的种族
        CompoundTag playerData = player.getPersistentData();
        String savedRace = playerData.getString(SAVED_RACE_KEY);

        // 只在没有戒指且有种族能力，并且与保存的种族不同时才保存
        if (currentRace != null && !currentRace.equals(savedRace)) {
            saveRace(serverPlayer, currentRace);
        }
    }

    // 获取当前激活的种族
    private static String getActiveRace(Player player) {
        for (Map.Entry<String, Capability<? extends IBaseRaceCapability>> entry :
                ModCapabilities.RACE_CAPABILITIES.entrySet()) {
            String raceName = entry.getKey();
            var optCap = player.getCapability(entry.getValue());
            if (optCap.isPresent() && optCap.resolve().get().isActive()) {
                return raceName;
            }
        }
        return null;
    }

    // 保存当前种族
    private static void saveRace(ServerPlayer player, String raceName) {
        CompoundTag playerData = player.getPersistentData();
        playerData.putString(SAVED_RACE_KEY, raceName);

        // 龙额外记下元素：吃龙宝石变的是哪种元素，戴戒指会覆盖能力里的元素，摘掉后要按这个还原，
        // 否则 buff 图标会停在最后戴的戒指的元素上。
        if ("dragon".equalsIgnoreCase(raceName)) {
            player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability d) {
                    playerData.putString(SAVED_DRAGON_ELEMENT_KEY, d.getDragonElement());
                }
            });
        }
    }

    @Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
    public static class EventHandler {
        // 每tick检查一次玩家状态
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END &&
                    !event.player.level().isClientSide()) {
                checkAndUpdateRaceState(event.player);
            }
        }
    }
}