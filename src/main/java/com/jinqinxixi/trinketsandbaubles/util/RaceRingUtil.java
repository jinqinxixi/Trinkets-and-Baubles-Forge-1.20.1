package com.jinqinxixi.trinketsandbaubles.util;

import com.jinqinxixi.trinketsandbaubles.items.baubles.*;
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
}