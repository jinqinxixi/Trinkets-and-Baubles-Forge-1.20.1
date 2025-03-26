package com.jinqinxixi.trinketsandbaubles.items;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
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
                    .icon(() -> ModItem.MOON_ROSE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        ITEMS.getEntries().forEach(item -> {
                            output.accept(item.get());
                        });
                    })
                    .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}