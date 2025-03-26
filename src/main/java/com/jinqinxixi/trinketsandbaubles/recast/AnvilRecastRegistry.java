package com.jinqinxixi.trinketsandbaubles.recast;



import com.jinqinxixi.trinketsandbaubles.items.ModItem;
import net.minecraft.world.item.Item;

public class AnvilRecastRegistry {
    public static void registerAllRecipes() {
        register(ModItem.WEIGHTLESS_STONE.get());
        register(ModItem.INERTIA_NULL_STONE.get());
        register(ModItem.GREATER_INERTIA_STONE.get());
        register(ModItem.GLOW_RING.get());
        register(ModItem.SEA_STONE.get());
        register(ModItem.POLARIZED_STONE.get());
        register(ModItem.DRAGONS_EYE.get());
        register(ModItem.DRAGONS_EYE_FIRE.get());
        register(ModItem.DRAGONS_EYE_ICE.get());
        register(ModItem.DRAGONS_EYE_LIGHTNING.get());
        register(ModItem.WITHER_RING.get());
        register(ModItem.POISON_STONE.get());
        register(ModItem.ENDER_TIARA.get());
        register(ModItem.DAMAGE_SHIELD.get());
        register(ModItem.ARCING_ORB.get());
        register(ModItem.TEDDY_BEAR.get());
        register(ModItem.FAELIS_CLAW.get());
        register(ModItem.FAELIS_RING.get());
        register(ModItem.FAIRIES_RING.get());
        register(ModItem.DWARVES_RING.get());
        register(ModItem.TITAN_RING.get());
        register(ModItem.GOBLINS_RING.get());
        register(ModItem.ELVES_RING.get());
        register(ModItem.DRAGONS_RING.get());




    }

    private static void register(Item item) {
        if (item != null) {
            AnvilRecastHandler.registerRecipe(item, ModItem.GLOWING_INGOT.get(), item);
        }
    }
}
