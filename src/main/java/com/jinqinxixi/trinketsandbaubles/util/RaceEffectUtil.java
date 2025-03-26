package com.jinqinxixi.trinketsandbaubles.util;

import com.jinqinxixi.trinketsandbaubles.items.baubles.*;
import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RaceEffectUtil {
    private static final List<RegistryObject<MobEffect>> RACE_EFFECTS = List.of(
            ModEffects.FAIRY_DEW,
            ModEffects.DWARVES,
            ModEffects.TITAN,
            ModEffects.GOBLIN,
            ModEffects.ELVES,
            ModEffects.FAELES,
            ModEffects.DRAGON
    );

    // 清除所有种族效果
    public static void clearAllRaceEffects(Player player) {
        RACE_EFFECTS.forEach(effect -> {
            if (player.hasEffect(effect.get())) {
                player.removeEffect(effect.get());
            }
        });
    }

    // 检查是否有任何种族效果
    public static boolean hasAnyRaceEffect(Player player) {
        return RACE_EFFECTS.stream()
                .map(RegistryObject::get)
                .anyMatch(player::hasEffect);
    }

    // 获取当前的种族效果（如果有的话）
    public static MobEffect getCurrentRaceEffect(Player player) {
        return RACE_EFFECTS.stream()
                .map(RegistryObject::get)
                .filter(player::hasEffect)
                .findFirst()
                .orElse(null);
    }

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
        return item instanceof FaelisRingItem ||
                item instanceof FairiesRingItem ||
                item instanceof DwarvesRingItem ||
                item instanceof TitanRingItem ||
                item instanceof GoblinsRingItem ||
                item instanceof ElvesRingItem ||
                item instanceof DragonsRingItem;
    }
}