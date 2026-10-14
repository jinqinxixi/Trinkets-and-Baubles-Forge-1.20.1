package com.jinqinxixi.trinketsandbaubles.modeffects;

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

    public static final RegistryObject<MobEffect> GOBLINS =
            EFFECTS.register("goblins", GoblinsEffect::new);

    public static final RegistryObject<MobEffect> ELVES =
            EFFECTS.register("elves", ElvesEffect::new);

    public static final RegistryObject<MobEffect> FAELES =
            EFFECTS.register("faeles", FaelesEffect::new);

    public static final RegistryObject<MobEffect> DRAGON =
            EFFECTS.register("dragon", DragonsEffect::new);

    /**
     * 火/冰/雷三种元素的龙效果：逻辑与「dragon」完全相同（同一个类），只是 buff 图标和颜色不同
     * ——火红、冰蓝、雷紫，而基础的「dragon」是黑灰色龙头。玩家从 buff 栏就能认出变的是
     * 哪种龙。变身状态由 {@code DragonsEffect.dragonEffectOn} 按玩家身上实际的元素效果判断。
     */
    public static final RegistryObject<MobEffect> FIRE_DRAGON =
            EFFECTS.register("fire_dragon", () -> new DragonsEffect(0xFF4500));

    public static final RegistryObject<MobEffect> ICE_DRAGON =
            EFFECTS.register("ice_dragon", () -> new DragonsEffect(0x4FC3F7));

    public static final RegistryObject<MobEffect> LIGHTNING_DRAGON =
            EFFECTS.register("lightning_dragon", () -> new DragonsEffect(0xB388FF));

    public static final RegistryObject<MobEffect> TAURUS =
            EFFECTS.register("taurus", TaurusEffect::new);

    public static final RegistryObject<MobEffect> BLEEDING =
            EFFECTS.register("bleeding", BleedingEffect::new);

    public static final RegistryObject<MobEffect> FIRE_RESISTANCE =
            EFFECTS.register("fire_resistance", FireResistanceEffect::new);

    public static final RegistryObject<MobEffect> ICE_RESISTANCE =
            EFFECTS.register("ice_resistance", IceResistanceEffet::new);

    public static final RegistryObject<MobEffect> LIGHTNING_RESISTANCE =
            EFFECTS.register("lightning_resistance", LightningResistanceEffect::new);
}