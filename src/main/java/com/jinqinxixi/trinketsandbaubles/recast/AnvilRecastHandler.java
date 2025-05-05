package com.jinqinxixi.trinketsandbaubles.recast;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class AnvilRecastHandler {
    private static final Map<Item, RecastRecipe> RECIPES = new HashMap<>();

    private static int getExpCost() {
        return ModConfig.getAnvilRecastExpCost();
    }

    private static int getMaterialCost() {
        return ModConfig.getAnvilRecastMaterialCost();
    }

    public static void registerRecipe(Item baseItem, Item tokenItem, Item resultItem) {
        RECIPES.put(baseItem, new RecastRecipe(tokenItem, resultItem));
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        // 首先检查修饰系统是否启用
        if (!ModConfig.isModifierEnabled()) {
            return; // 如果修饰系统未启用，直接返回
        }

        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (!left.isEmpty() && !right.isEmpty()) {
            RecastRecipe recipe = RECIPES.get(left.getItem());
            if (recipe != null && recipe.matches(right)) {
                // 创建输出物品的副本
                ItemStack output = left.copy();

                // 重置修饰符标签
                CompoundTag tag = output.getTag();
                if (tag != null) {
                    // 移除已初始化标记
                    if (tag.contains(ModifiableBaubleItem.INITIALIZED_TAG)) {
                        tag.remove(ModifiableBaubleItem.INITIALIZED_TAG);
                    }
                    // 移除修饰符数据
                    if (tag.contains(ModifiableBaubleItem.MODIFIER_TAG)) {
                        tag.remove(ModifiableBaubleItem.MODIFIER_TAG);
                    }
                }

                event.setOutput(output);
                event.setCost(getExpCost());
                event.setMaterialCost(getMaterialCost());
            }
        }
    }

    public static class RecastRecipe {
        private final Item tokenItem;
        private final Item resultItem;

        public RecastRecipe(Item tokenItem, Item resultItem) {
            this.tokenItem = tokenItem;
            this.resultItem = resultItem;
        }

        public boolean matches(ItemStack rightStack) {
            return rightStack.getItem() == tokenItem;
        }
    }
}