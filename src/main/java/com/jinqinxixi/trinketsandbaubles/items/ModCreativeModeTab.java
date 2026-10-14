package com.jinqinxixi.trinketsandbaubles.items;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.traits.elements.Element;
import com.jinqinxixi.trinketsandbaubles.traits.elements.IElementProvider;
import com.jinqinxixi.trinketsandbaubles.traits.elements.ItemElements;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.jinqinxixi.trinketsandbaubles.items.ModItem.ITEMS;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TrinketsandBaublesMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TRINKETSANDBAUBLES_TAB = CREATIVE_MODE_TABS.register("trinketsandbaubles_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.trinketsandbaubles"))
                    .icon(() -> ModItem.DAMAGE_SHIELD.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        ITEMS.getEntries().forEach(item -> {
                            Item value = item.get();
                            output.accept(value);
                            // 带元素变体的物品（龙戒）额外展示各元素版本。
                            if (value instanceof IElementProvider provider) {
                                for (Element element : provider.getSubElements()) {
                                    ItemStack stack = new ItemStack(value);
                                    ItemElements.setPrimary(stack, element);
                                    output.accept(stack);
                                }
                            }
                        });
                    })
                    .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}