package com.jinqinxixi.trinketsandbaubles.init;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.entity.AlphaWolf;
import com.jinqinxixi.trinketsandbaubles.entity.BreathProjectile;
import com.jinqinxixi.trinketsandbaubles.entity.BreathAreaEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TrinketsandBaublesMod.MOD_ID);

    public static final RegistryObject<EntityType<AlphaWolf>> ALPHA_WOLF = ENTITY_TYPES.register(
            "alpha_wolf",
            () -> EntityType.Builder.of(AlphaWolf::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.2F)
                    .clientTrackingRange(10)
                    .build("trinketsandbaubles:alpha_wolf")
    );

    public static final RegistryObject<EntityType<BreathProjectile>> DRAGON_BREATH = ENTITY_TYPES.register(
            "dragon_breath",
            () -> EntityType.Builder.<BreathProjectile>of(BreathProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("trinketsandbaubles:dragon_breath")
    );

    public static final RegistryObject<EntityType<BreathAreaEffect>> BREATH_AREA = ENTITY_TYPES.register(
            "breath_area",
            () -> EntityType.Builder.<BreathAreaEffect>of(BreathAreaEffect::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("trinketsandbaubles:breath_area")
    );

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(ModEntities::onAttributeCreation);
    }

    private static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ALPHA_WOLF.get(), Wolf.createAttributes().build());
    }
}