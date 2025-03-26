package com.jinqinxixi.trinketsandbaubles.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    // 注册月亮玫瑰方块
    public static final RegistryObject<Block> MOON_ROSE = BLOCKS.register("moon_rose",
            () -> new MoonRoseBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .lightLevel(state -> 15)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .sound(SoundType.GRASS)
                    .strength(0.0f)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}