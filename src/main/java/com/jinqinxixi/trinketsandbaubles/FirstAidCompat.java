package com.jinqinxixi.trinketsandbaubles;

import net.minecraftforge.fml.ModList;

public class FirstAidCompat {
    private static final String FIRSTAID_MODID = "firstaid";

    /**
     * 检查FirstAid mod是否已加载
     */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(FIRSTAID_MODID);
    }
}