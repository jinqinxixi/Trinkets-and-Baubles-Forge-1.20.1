package com.jinqinxixi.trinketsandbaubles.loot;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Mod.EventBusSubscriber(modid = "trinketsandbaubles")
public class LootTableHandler {
    private static final Logger log = LoggerFactory.getLogger(LootTableHandler.class);

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation tableId = event.getName();
        List<ModConfig.LootEntry> entries = ModConfig.lootConfig.get(tableId);

        if (entries != null && !entries.isEmpty()) {
            LootPool.Builder poolBuilder = LootPool.lootPool()
                    .name("trinketsandbaubles_config_loot");

            for (ModConfig.LootEntry entry : entries) {
                Item item = BuiltInRegistries.ITEM.get(entry.itemId);
                if (item != null) {
                    poolBuilder.add(LootItem.lootTableItem(item)
                                    .setWeight(entry.weight))
                            .setRolls(UniformGenerator.between(entry.minRolls, entry.maxRolls));
                } else {
                    log.error("Invalid item in config: {}", entry.itemId);
                }
            }

            event.getTable().addPool(poolBuilder.build());
            log.debug("Added custom loot to {}", tableId);
        }
    }
}