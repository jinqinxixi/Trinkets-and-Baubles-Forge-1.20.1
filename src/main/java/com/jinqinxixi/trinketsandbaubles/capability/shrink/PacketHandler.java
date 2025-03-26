package com.jinqinxixi.trinketsandbaubles.capability.shrink;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1.1";
    public static SimpleChannel INSTANCE;

    public static void init() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(TrinketsandBaublesMod.MOD_ID, "main1"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;
        INSTANCE.registerMessage(id++,
                PacketSyncShrink.class,
                PacketSyncShrink::encode,
                PacketSyncShrink::decode,
                PacketSyncShrink::handle
        );
    }
}