package com.jinqinxixi.trinketsandbaubles.items;



import com.jinqinxixi.trinketsandbaubles.block.ModBlocks;
import com.jinqinxixi.trinketsandbaubles.items.baubles.*;
import com.jinqinxixi.trinketsandbaubles.potion.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID;

public class ModItem {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final List<RegistryObject<? extends Item>> CURIO_ITEMS = new ArrayList<>();

    // 基础属性模板（所有饰品共有）
    private static Item.Properties baseCurioProps() {
        return new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON)
                .fireResistant();
    }

    // 饰品注册方法
    private static <T extends Item> RegistryObject<T> registerCurio(
            String name,
            UnaryOperator<Item.Properties> propsAdjuster,
            ItemConstructor<T> constructor
    ) {
        // 应用基础属性 + 自定义调整
        Item.Properties finalProps = propsAdjuster.apply(baseCurioProps());

        RegistryObject<T> registryObject = ITEMS.register(name, () -> constructor.create(finalProps));
        CURIO_ITEMS.add(registryObject);
        return registryObject;
    }

    // 简化版（无特殊属性时使用）
    private static <T extends Item> RegistryObject<T> registerCurio(
            String name,
            ItemConstructor<T> constructor
    ) {
        return registerCurio(name, UnaryOperator.identity(), constructor);
    }


    public static final RegistryObject<Item> WEIGHTLESS_STONE = registerCurio(
            "weightless_stone",
            StoneofNegativeGravityItem::new
    );

    public static final RegistryObject<Item> INERTIA_NULL_STONE = registerCurio(
            "inertia_null_stone",
            StoneofInertiaNullItem::new
    );

    public static final RegistryObject<Item> GREATER_INERTIA_STONE = registerCurio(
            "greater_inertia_stone",
            StoneofGreaterInertiaItem::new
    );

    public static final RegistryObject<Item> FAELIS_CLAW = registerCurio(
            "faelis_claw",
            FaelisClawItem::new
    );

    public static final RegistryObject<Item> GLOW_RING = registerCurio(
            "glow_ring",
           RingofEnchantedEyesItem::new
    );

    public static final RegistryObject<Item> SEA_STONE = registerCurio(
            "sea_stone",
           StoneoftheSeaItem::new
    );

    public static final RegistryObject<Item> POISON_STONE = registerCurio(
            "poison_stone",
            PoisonStoneItem::new
    );

    public static final RegistryObject<Item> WITHER_RING = registerCurio(
            "wither_ring",
            WitherRingItem::new
    );

    public static final RegistryObject<Item> DAMAGE_SHIELD = registerCurio(
            "damage_shield",
            ShieldofHonorItem::new
    );

    public static final RegistryObject<Item> TEDDY_BEAR = registerCurio(
            "teddy_bear",
            TeddyBear::new
    );

    public static final RegistryObject<Item> DRAGONS_EYE = registerCurio(
            "dragons_eye",
            DragonsEyeItem::new
    );

    public static final RegistryObject<Item> DRAGONS_EYE_FIRE = registerCurio(
            "dragons_eye_fire",
            DragonsEyeFireItem::new
    );

    public static final RegistryObject<Item> DRAGONS_EYE_ICE = registerCurio(
            "dragons_eye_ice",
            DragonsEyeIceItem::new
    );

    public static final RegistryObject<Item> DRAGONS_EYE_LIGHTNING = registerCurio(
            "dragons_eye_lightning",
            DragonsEyeLightningItem::new
    );

    public static final RegistryObject<Item> POLARIZED_STONE = registerCurio(
            "polarized_stone",
            PolarizedStoneItem::new
    );

    public static final RegistryObject<Item> ENDER_TIARA = registerCurio(
            "ender_tiara",
            EnderQueensCrownItem::new
    );

    public static final RegistryObject<Item> ARCING_ORB = registerCurio(
            "arcing_orb",
            ArcingOrbItem::new
    );

    public static final RegistryObject<Item> DRAGONS_RING = registerCurio(
            "dragons_ring",
            DragonsRingItem::new
    );

    public static final RegistryObject<Item> DWARVES_RING = registerCurio(
            "dwarves_ring",
            DwarvesRingItem::new
    );

    public static final RegistryObject<Item> ELVES_RING = registerCurio(
            "elves_ring",
            ElvesRingItem::new
    );

    public static final RegistryObject<Item> FAELIS_RING = registerCurio(
            "faelis_ring",
            FaelesRingItem::new
    );

    public static final RegistryObject<Item> FAIRIES_RING = registerCurio(
            "fairies_ring",
            FairiesRingItem::new
    );

    public static final RegistryObject<Item> GOBLINS_RING = registerCurio(
            "goblins_ring",
            GoblinsRingItem::new
    );

    public static final RegistryObject<Item> TITAN_RING = registerCurio(
            "titan_ring",
            TitanRingItem::new
    );

    public static final RegistryObject<Item> GLOWING_POWDER = ITEMS.register(
            "glowing_powder",
            GlowingPowderItem::new
    );

    public static final RegistryObject<Item> GLOWING_INGOT = ITEMS.register(
            "glowing_ingot",
            GlowingIngotItem::new
    );

    public static final RegistryObject<Item> GLOWING_GEM = ITEMS.register(
            "glowing_gem",
            GlowingGemItem::new
    );
    public static final RegistryObject<Item> MOON_ROSE = ITEMS.register(
            "moon_rose",
            () -> new BlockItem(ModBlocks.MOON_ROSE.get(), new Item.Properties()
                    .stacksTo(64)
                    .fireResistant()
                    .rarity(Rarity.UNCOMMON))
    );

    public static final RegistryObject<Item> RESTORATION_SERUM = ITEMS.register(
            "restoration_serum",
            RestorationSerumPotion::new
    );

    public static final RegistryObject<Item> MANA_CANDY = ITEMS.register(
            "mana_candy",
            ManaCandyPotion::new
    );

    public static final RegistryObject<Item> MANA_CRYSTAL = ITEMS.register(
            "mana_crystal",
            ManaCrystalPotion::new
    );

    public static final RegistryObject<Item> MANA_REAGENT = ITEMS.register(
            "mana_reagent",
            ManaReagentPotion::new
    );

    public static final RegistryObject<Item> FAIRY_DEW = ITEMS.register(
            "fairy_dew",
            FairyDewPotion::new
    );

    public static final RegistryObject<Item> DWARF_STOUT = ITEMS.register(
            "dwarf_stout",
            DwarfStoutPotion::new
    );

    public static final RegistryObject<Item> TITAN_SPIRIT = ITEMS.register(
            "titan_spirit",
            TitanSpiritPotion::new
    );

    public static final RegistryObject<Item> GOBLIN_SOUP = ITEMS.register(
            "goblin_soup",
            GoblinSoupPotion::new
    );

    public static final RegistryObject<Item> ELF_SAP = ITEMS.register(
            "elf_sap",
            ElfSapPotion::new
    );

    public static final RegistryObject<Item> FAELIS_FOOD = ITEMS.register(
            "faelis_food",
            FaelisNipPotion::new
    );

    public static final RegistryObject<Item> DRAGON_GEM = ITEMS.register(
            "dragon_gem",
            DragonGemPotion::new
    );











    public static List<RegistryObject<? extends Item>> getCurioItems() {
        return Collections.unmodifiableList(CURIO_ITEMS);
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    // 辅助接口（确保类型安全）
    @FunctionalInterface
    private interface ItemConstructor<T extends Item> {
        T create(Item.Properties properties);
    }
}