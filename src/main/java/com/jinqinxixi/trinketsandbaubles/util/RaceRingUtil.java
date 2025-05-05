package com.jinqinxixi.trinketsandbaubles.util;

import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.items.baubles.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicInteger;

public class RaceRingUtil {

    public static boolean hasMultipleRaceRings(Player player) {
        AtomicInteger raceRingCount = new AtomicInteger(0);

        // 使用正确的 Curios API 方法获取饰品栏
        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            // 遍历所有饰品槽
            inventory.getCurios().forEach((identifier, slotInventory) -> {
                // 检查每个槽位中的物品
                for (int i = 0; i < slotInventory.getSlots(); i++) {
                    if (isRaceRing(slotInventory.getStacks().getStackInSlot(i).getItem())) {
                        raceRingCount.incrementAndGet();
                    }
                }
            });
        });

        return raceRingCount.get() > 1;
    }

    // 辅助方法：检查物品是否是种族戒指
    private static boolean isRaceRing(net.minecraft.world.item.Item item) {
        return item instanceof FaelesRingItem ||
                item instanceof FairiesRingItem ||
                item instanceof DwarvesRingItem ||
                item instanceof TitanRingItem ||
                item instanceof GoblinsRingItem ||
                item instanceof ElvesRingItem ||
                item instanceof DragonsRingItem;
    }
    public static boolean hasAnyRaceRing(Player player) {
        AtomicInteger raceRingCount = new AtomicInteger(0);

        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            inventory.getCurios().forEach((identifier, slotInventory) -> {
                for (int i = 0; i < slotInventory.getSlots(); i++) {
                    if (isRaceRing(slotInventory.getStacks().getStackInSlot(i).getItem())) {
                        raceRingCount.incrementAndGet();
                        break; // 找到一个就可以退出了
                    }
                }
            });
        });

        return raceRingCount.get() > 0; // 只要有任何一个种族戒指就返回true
    }
    public static void activateRace(ServerPlayer player, String raceName) {
        if (raceName == null || raceName.isEmpty()) return;

        switch (raceName.toLowerCase()) {
            case "dwarves":
                player.getCapability(ModCapabilities.DWARVES_CAPABILITY)
                        .ifPresent(capability -> capability.setActive(true));
                break;
            case "dragon":
                player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                        .ifPresent(capability -> capability.setActive(true));
                break;
            case "elves":
                player.getCapability(ModCapabilities.ELVES_CAPABILITY)
                        .ifPresent(capability -> capability.setActive(true));
                break;
            case "faeles":
                player.getCapability(ModCapabilities.FAELES_CAPABILITY)
                        .ifPresent(capability -> capability.setActive(true));
                break;
            case "fairy":
                player.getCapability(ModCapabilities.FAIRY_CAPABILITY)
                        .ifPresent(capability -> capability.setActive(true));
                break;
            case "goblins":
                player.getCapability(ModCapabilities.GOBLINS_CAPABILITY)
                        .ifPresent(capability -> capability.setActive(true));
                break;
            case "titan":
                player.getCapability(ModCapabilities.TITAN_CAPABILITY)
                        .ifPresent(capability -> capability.setActive(true));
                break;
        }
    }
}