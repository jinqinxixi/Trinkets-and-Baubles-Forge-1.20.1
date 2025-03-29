package com.jinqinxixi.trinketsandbaubles.modEffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "trinketsandbaubles");

    // 注册恢复药剂效果
    public static final RegistryObject<MobEffect> FAIRY_DEW =
            EFFECTS.register("fairy_dew", FairyDewEffect::new);

    public static final RegistryObject<MobEffect> DWARVES =
            EFFECTS.register("dwarves", DwarvesEffect::new);

    public static final RegistryObject<MobEffect> TITAN =
            EFFECTS.register("titan", TitanEffect::new);

    public static final RegistryObject<MobEffect> GOBLIN =
            EFFECTS.register("goblin", GoblinsEffect::new);

    public static final RegistryObject<MobEffect> ELVES =
            EFFECTS.register("elves", ElvesEffect::new);

    public static final RegistryObject<MobEffect> FAELES =
            EFFECTS.register("faeles", FaelesEffect::new);

    public static final RegistryObject<MobEffect> DRAGON =
            EFFECTS.register("dragon", DragonsEffect::new);

    public static final RegistryObject<MobEffect> BLEEDING =
            EFFECTS.register("bleeding", BleedingEffect::new);

    public static final RegistryObject<MobEffect> FIRE_RESISTANCE =
            EFFECTS.register("fire_resistance", FireResistanceEffect::new);

    public static final RegistryObject<MobEffect> ICE_RESISTANCE =
            EFFECTS.register("ice_resistance", IceResistanceEffet::new);

    public static final RegistryObject<MobEffect> LIGHTNING_RESISTANCE =
            EFFECTS.register("lightning_resistance", LightningResistanceEffect::new);
}