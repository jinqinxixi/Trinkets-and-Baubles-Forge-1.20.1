package com.jinqinxixi.trinketsandbaubles.capability.registry;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.api.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {
    // 种族能力注册表
    public static final Map<String, Capability<? extends IBaseRaceCapability>> RACE_CAPABILITIES = new HashMap<>();

    public static final Capability<IDwarvesCapability> DWARVES_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<IDwarvesCapability>(){});

    public static final Capability<IDragonCapability> DRAGON_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<IDragonCapability>(){});

    public static final Capability<IElvesCapability> ELVES_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<IElvesCapability>(){});

    public static final Capability<IFaelesCapability> FAELES_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<IFaelesCapability>(){});

    public static final Capability<IFairyCapability> FAIRY_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<IFairyCapability>(){});

    public static final Capability<IGoblinsCapability> GOBLINS_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<IGoblinsCapability>(){});

    public static final Capability<ITitanCapability> TITAN_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<ITitanCapability>(){});

    // 静态初始化块，注册所有种族能力
    static {
        RACE_CAPABILITIES.put("dwarves", DWARVES_CAPABILITY);
        RACE_CAPABILITIES.put("dragon", DRAGON_CAPABILITY);
        RACE_CAPABILITIES.put("elves", ELVES_CAPABILITY);
        RACE_CAPABILITIES.put("faeles", FAELES_CAPABILITY);
        RACE_CAPABILITIES.put("fairy", FAIRY_CAPABILITY);
        RACE_CAPABILITIES.put("goblins", GOBLINS_CAPABILITY);
        RACE_CAPABILITIES.put("titan", TITAN_CAPABILITY);
    }
}