package com.jinqinxixi.trinketsandbaubles.config;


import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;


import java.util.*;

@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    private static final String BAUBLES_REFORKED_MOD_ID = "baublesreforked";
    private static boolean baublesReforkedLoaded;
    //修饰系统
    public static final ForgeConfigSpec.BooleanValue MODIFIER_ENABLED;

    // 铁砧重铸系统配置
    public static final ForgeConfigSpec.IntValue ANVIL_RECAST_EXP_COST;
    public static final ForgeConfigSpec.IntValue ANVIL_RECAST_MATERIAL_COST;

    // 魔力系统配置
    public static final ForgeConfigSpec.IntValue DEFAULT_MAX_MANA;
    public static final ForgeConfigSpec.IntValue MANA_REGEN_COOLDOWN;
    public static final ForgeConfigSpec.IntValue MANA_REGEN_RATE;
    public static final ForgeConfigSpec.IntValue MANA_REGEN_INTERVAL;
    public static final ForgeConfigSpec.IntValue CREATIVE_REGEN_RATE;
    public static final ForgeConfigSpec.BooleanValue USE_IRONS_SPELLS_MANA;
    public static final ForgeConfigSpec.BooleanValue USE_BOTANIA_MANA;

    // 魔法球配置
    public static final ForgeConfigSpec.DoubleValue SPEED_BOOST;
    public static final ForgeConfigSpec.DoubleValue CHARGE_RATE;
    public static final ForgeConfigSpec.DoubleValue MIN_CHARGE;
    public static final ForgeConfigSpec.DoubleValue MAX_CHARGE;
    public static final ForgeConfigSpec.DoubleValue DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue DASH_DISTANCE;
    public static final ForgeConfigSpec.IntValue DASH_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue DASH_MANA_COST;
    public static final ForgeConfigSpec.DoubleValue DASH_JUMP_BOOST;

    // 龙眼扫描范围配置
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> VALUABLE_ORES;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> COMMON_ORES;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> REDSTONE_ORES;
    public static ForgeConfigSpec.IntValue MAX_RENDER_BLOCKS;
    public static ForgeConfigSpec.IntValue RENDER_RANGE;


    // 末影王冠配置
    public static final ForgeConfigSpec.DoubleValue ENDERMAN_FOLLOW_RANGE;
    public static final ForgeConfigSpec.DoubleValue DAMAGE_IMMUNITY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue TELEPORT_RANGE;
    public static final ForgeConfigSpec.BooleanValue WATER_DAMAGE_ENABLED;

    // 猫爪配置
    public static final ForgeConfigSpec.DoubleValue FAELIS_CLAW_DAMAGE_BOOST;
    public static final ForgeConfigSpec.DoubleValue FAELIS_CLAW_BLEED_CHANCE;
    public static final ForgeConfigSpec.IntValue FAELIS_CLAW_BLEED_DURATION;
    public static final ForgeConfigSpec.DoubleValue FAELIS_CLAW_NORMAL_DURATION_MULTIPLIER;

    // 魔力糖果配置
    public static final ForgeConfigSpec.IntValue MANA_CANDY_RESTORE;

    // 魔力水晶配置
    public static final ForgeConfigSpec.IntValue MANA_CRYSTAL_MAX_INCREASE;

    // 魔力试剂配置
    public static final ForgeConfigSpec.IntValue MANA_REAGENT_MAX_DECREASE;

    // 毒石配置
    public static final ForgeConfigSpec.DoubleValue POISON_STONE_CHANCE;
    public static final ForgeConfigSpec.IntValue POISON_STONE_DURATION;
    public static final ForgeConfigSpec.IntValue POISON_STONE_AMPLIFIER;
    public static final ForgeConfigSpec.DoubleValue POISON_STONE_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue POISON_STONE_ACTIVATE_DAMAGE_EVENT;
    // 极化之石配置
    public static final ForgeConfigSpec.DoubleValue POLARIZED_STONE_ATTRACTION_RANGE;
    public static final ForgeConfigSpec.DoubleValue POLARIZED_STONE_ATTRACTION_SPEED;
    public static final ForgeConfigSpec.DoubleValue POLARIZED_STONE_DEFLECTION_RANGE;
    public static final ForgeConfigSpec.DoubleValue POLARIZED_STONE_DEFLECTION_MANA_COST;

    // 荣誉之盾配置
    public static final ForgeConfigSpec.IntValue SHIELD_MAX_DAMAGE_COUNT;
    public static final ForgeConfigSpec.DoubleValue SHIELD_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue SHIELD_EXPLOSION_REDUCTION;
    public static ForgeConfigSpec.BooleanValue ENABLE_DAMAGE_SHIELD_EFFECT;
    public static ForgeConfigSpec.DoubleValue DAMAGE_SHIELD_TRIGGER_CHANCE;

    // 大惯性之石配置
    public static final ForgeConfigSpec.DoubleValue GREATER_INERTIA_KNOCKBACK_RESISTANCE;
    public static final ForgeConfigSpec.DoubleValue GREATER_INERTIA_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue GREATER_INERTIA_JUMP_BOOST;
    public static final ForgeConfigSpec.DoubleValue GREATER_INERTIA_FALL_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue GREATER_INERTIA_STEP_HEIGHT;

    // 凋零戒指配置
    public static final ForgeConfigSpec.DoubleValue WITHER_RING_CHANCE;
    public static final ForgeConfigSpec.IntValue WITHER_RING_DURATION;
    public static final ForgeConfigSpec.IntValue WITHER_RING_AMPLIFIER;

    // === 战利品配置项 ===
    // ===================== 战利品表配置项 =====================
    public static final ForgeConfigSpec.ConfigValue<String> DESERT_PYRAMID_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> JUNGLE_TEMPLE_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> ABANDONED_MINESHAFT_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> STRONGHOLD_LIBRARY_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> WOODLAND_MANSION_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_TEMPLE_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> SHIPWRECK_TREASURE_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> PILLAGER_OUTPOST_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> BURIED_TREASURE_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> UNDERWATER_RUIN_BIG_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> UNDERWATER_RUIN_SMALL_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> IGLOO_CHEST_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> BASTION_TREASURE_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> BASTION_BRIDGE_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> BASTION_HOUSING_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> BASTION_OTHER_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> RUINED_PORTAL_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> END_CITY_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> ANCIENT_CITY_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> SIMPLE_DUNGEON_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> STRONGHOLD_CORRIDOR_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> FOSSIL_DINOSAUR_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> FOSSIL_MAMMAL_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> SHIPWRECK_SUPPLY_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> WOODLAND_CARTOGRAPHY_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> STRONGHOLD_CROSSING_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> SHIPWRECK_MAP_LOOT;


    // === 村庄职业配置 ===
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_ARMORER_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_BUTCHER_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_CARTOGRAPHER_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_PLAINS_HOUSE_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_FISHER_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_FLETCHER_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_TANNERY_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_LIBRARY_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_MASON_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_SHEPHERD_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_TOOLSMITH_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_WEAPONSMITH_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_DESERT_HOUSE_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_SNOWY_HOUSE_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_SAVANNA_HOUSE_LOOT;
    public static final ForgeConfigSpec.ConfigValue<String> VILLAGE_TAIGA_HOUSE_LOOT;


    static {
        baublesReforkedLoaded = ModList.get().isLoaded(BAUBLES_REFORKED_MOD_ID);

        BUILDER.comment("修饰系统配置").push("modifiers");

        MODIFIER_ENABLED = BUILDER
                .comment(
                        "是否启用饰品修饰系统",
                        "注意：如果检测到安装了 Baubles Reforked 模组，此选项默认为 false",
                        "你仍然可以手动启用此选项，但不建议同时使用两个模组的修饰符系统"
                )
                .define("enabled", !baublesReforkedLoaded);

        BUILDER.pop();

        BUILDER.comment("铁砧重铸系统配置").push("anvilRecast");

        ANVIL_RECAST_EXP_COST = BUILDER
                .comment("重铸所需经验等级")
                .defineInRange("expCost", 3, 1, 100);

        ANVIL_RECAST_MATERIAL_COST = BUILDER
                .comment("重铸消耗的材料数量")
                .defineInRange("materialCost", 1, 1, 64);

        BUILDER.pop();

        // 魔力系统
        DEFAULT_MAX_MANA = BUILDER
                .comment("默认最大魔力值")
                .defineInRange("mana.defaultMaxMana", 100, 10, 100000);

        MANA_REGEN_COOLDOWN = BUILDER
                .comment("魔力恢复冷却时间（以tick为单位，20 tick = 1秒）")
                .defineInRange("mana.manaRegenCooldown", 20, 0, 20000);

        MANA_REGEN_RATE = BUILDER
                .comment("每次恢复的魔力量")
                .defineInRange("mana.manaRegenRate", 3, 1, 10000);

        MANA_REGEN_INTERVAL = BUILDER
                .comment("魔力恢复间隔（以tick为单位）")
                .defineInRange("mana.manaRegenInterval", 20, 1, 20000);

        CREATIVE_REGEN_RATE = BUILDER
                .comment("创造模式每tick恢复的魔力量")
                .defineInRange("mana.creativeRegenRate", 5, 1, 10000);

        USE_IRONS_SPELLS_MANA = BUILDER
                .comment("若已安装 Iron's Spells 模组，是否使用其魔力系统")
                .define("mana.useIronsSpellsMana", false);

        USE_BOTANIA_MANA = BUILDER
                .comment("若已安装 Botania 模组，是否使用其魔力系统")
                .define("mana.useBotaniaMana", false);

        // 魔法球
        SPEED_BOOST = BUILDER
                .comment("移动速度增益倍率")
                .defineInRange("arcingOrb.speedBoost", 0.25D, 0.0D, 1000.0D);

        CHARGE_RATE = BUILDER
                .comment("每tick的充能速率和魔力消耗")
                .defineInRange("arcingOrb.chargeRate", 0.5D, 0.1D, 5000.0D);

        MIN_CHARGE = BUILDER
                .comment("最小充能需求")
                .defineInRange("arcingOrb.minCharge", 5.0D, 1.0D, 5000.0D);

        MAX_CHARGE = BUILDER
                .comment("最大充能值")
                .defineInRange("arcingOrb.maxCharge", 100.0D, 10.0D, 10000.0D);

        DAMAGE_MULTIPLIER = BUILDER
                .comment("伤害系数")
                .defineInRange("arcingOrb.damageMultiplier", 0.133D, 0.01D, 100.0D);

        DASH_DISTANCE = BUILDER
                .comment("闪避技能的位移距离")
                .defineInRange("arcingOrb.dashDistance", 5.0D, 1.0D, 2000.0D);

        DASH_COOLDOWN = BUILDER
                .comment("闪避技能冷却时间（单位：tick）")
                .defineInRange("arcingOrb.dashCooldown", 20, 1, 20000);

        DASH_MANA_COST = BUILDER
                .comment("闪避技能消耗的魔力值")
                .defineInRange("arcingOrb.dashManaCost", 10.0D, 1.0D, 10000.0D);

        DASH_JUMP_BOOST = BUILDER
                .comment("跳跃时闪避的额外推力系数")
                .defineInRange("arcingOrb.dashJumpBoost", 0.3D, 0.0D, 1.0D);

        // 巨龙之眼
        MAX_RENDER_BLOCKS = BUILDER
                .comment("最大渲染方块数量\n" +
                        "更大的数值可能会影响性能\n" +
                        "范围: 50-10000")
                .defineInRange("dragonsEye.max_render_blocks", 200, 50, 10000);

        RENDER_RANGE = BUILDER
                .comment("渲染范围（以方块为单位）\n" +
                        "这个值会被平方来计算实际范围\n" +
                        "默认值12表示12*12的范围\n" +
                        "范围: 1-128")
                .defineInRange("dragonsEye.render_range", 12, 1, 128);

        VALUABLE_ORES = BUILDER
                .comment("贵重矿物组 - 在这里添加要探测的额外贵重矿石\n" +
                        "添加方式示例：\n" +
                        "[\"minecraft:ancient_debris\", \"create:zinc_ore\", \"thermal:silver_ore\"]\n" +
                        "或者每个矿石单独一行：\n" +
                        "[\n" +
                        "    \"minecraft:ancient_debris\",\n" +
                        "    \"create:zinc_ore\",\n" +
                        "    \"thermal:silver_ore\"\n" +
                        "]")
                .defineList("dragonsEye.valuable.ores",
                        Arrays.asList(),
                        entry -> entry instanceof String);

        COMMON_ORES = BUILDER
                .comment("常见矿物组 - 在这里添加要探测的额外普通矿石\n" +
                        "添加方式示例：\n" +
                        "[\"create:copper_ore\", \"thermal:tin_ore\", \"mekanism:osmium_ore\"]\n" +
                        "注意：每个矿石ID之间需要用逗号分隔，最后一个不需要加逗号")
                .defineList("dragonsEye.common.ores",
                        Arrays.asList(),
                        entry -> entry instanceof String);

        REDSTONE_ORES = BUILDER
                .comment("红石矿物组 - 在这里添加要探测的额外红石类矿石\n" +
                        "添加方式示例：\n" +
                        "[\"thermal:cinnabar_ore\", \"projectred:electrotine_ore\"]\n" +
                        "注意：矿石ID格式必须是 模组id:方块id")
                .defineList("dragonsEye.redstone.ores",
                        Arrays.asList(),
                        entry -> entry instanceof String);

        // 末影王冠
        ENDERMAN_FOLLOW_RANGE = BUILDER
                .comment("末影人跟随范围")
                .defineInRange("enderCrown.followRange", 16.0D, 8.0D, 32.0D);

        DAMAGE_IMMUNITY_CHANCE = BUILDER
                .comment("伤害免疫触发概率")
                .defineInRange("enderCrown.immunityChance", 0.02D, 0.0D, 1.0D);

        TELEPORT_RANGE = BUILDER
                .comment("传送范围")
                .defineInRange("enderCrown.teleportRange", 32.0D, 8.0D, 64.0D);

        WATER_DAMAGE_ENABLED = BUILDER
                .comment("是否启用水伤害")
                .define("enderCrown.waterDamageEnabled", false);

        // 猫爪
        FAELIS_CLAW_DAMAGE_BOOST = BUILDER
                .comment("攻击力增益百分比")
                .defineInRange("faelisClaw.damageBoost", 0.25D, 0.0D, 100.0D);

        FAELIS_CLAW_BLEED_CHANCE = BUILDER
                .comment("流血效果触发概率")
                .defineInRange("faelisClaw.bleedChance", 0.25D, 0.0D, 1.0D);

        FAELIS_CLAW_BLEED_DURATION = BUILDER
                .comment("流血效果持续时间(tick)")
                .defineInRange("faelisClaw.bleedDuration", 300, 20, 12000);

        FAELIS_CLAW_NORMAL_DURATION_MULTIPLIER = BUILDER
                .comment("普通状态下流血持续时间倍率")
                .defineInRange("faelisClaw.normalDurationMultiplier", 1.0D/3.0D, 0.1D, 100.0D);

        // 魔力糖果
        MANA_CANDY_RESTORE = BUILDER
                .comment("魔力糖果恢复的魔力值")
                .defineInRange("manaCandy.restore", 50, 1, 20000);

        // 魔力水晶
        MANA_CRYSTAL_MAX_INCREASE = BUILDER
                .comment("魔力水晶增加的魔力上限值")
                .defineInRange("manaCrystal.maxIncrease", 10, 1, 50000);

        // 魔力试剂
        MANA_REAGENT_MAX_DECREASE = BUILDER
                .comment("魔力试剂减少的魔力上限值")
                .defineInRange("manaReagent.maxDecrease", 10, 1, 50000);

        // 毒石
        POISON_STONE_CHANCE = BUILDER
                .comment("毒石触发中毒效果的概率")
                .defineInRange("poisonStone.chance", 0.2D, 0.0D, 1.0D);

        POISON_STONE_DURATION = BUILDER
                .comment("中毒效果持续时间(tick)")
                .defineInRange("poisonStone.duration", 60, 20, 40000);

        POISON_STONE_AMPLIFIER = BUILDER
                .comment("中毒效果等级")
                .defineInRange("poisonStone.amplifier", 0, 0, 255);

        POISON_STONE_DAMAGE_MULTIPLIER = BUILDER
                .comment("对中毒目标的伤害倍率")
                .defineInRange("poisonStone.damageMultiplier", 2.0D, 1.0D, 500.0D);

        POISON_STONE_ACTIVATE_DAMAGE_EVENT = BUILDER
                .comment("是否启用毒石的伤害加成效果")
                .define("poisonStone.activateDamageEvent", true);

        // 极化之石
        POLARIZED_STONE_ATTRACTION_RANGE = BUILDER
                .comment("物品吸引范围")
                .defineInRange("polarizedStone.attractionRange", 12.0D, 4.0D, 64.0D);

        POLARIZED_STONE_ATTRACTION_SPEED = BUILDER
                .comment("物品吸引速度")
                .defineInRange("polarizedStone.attractionSpeed", 0.5D, 0.1D, 10.0D);

        POLARIZED_STONE_DEFLECTION_RANGE = BUILDER
                .comment("弹射物防御范围")
                .defineInRange("polarizedStone.deflectionRange", 2.0D, 1.0D, 10.0D);

        POLARIZED_STONE_DEFLECTION_MANA_COST = BUILDER
                .comment("每秒防御消耗魔力/秒")
                .defineInRange("polarizedStone.deflectionManaCost", 5.0D, 0.0D, 5000.0D);

        // 荣誉之盾
        SHIELD_MAX_DAMAGE_COUNT = BUILDER
                .comment("完全免伤所需的伤害计数")
                .defineInRange("shield.maxDamageCount", 3, 1, 100);

        SHIELD_DAMAGE_REDUCTION = BUILDER
                .comment("基础伤害减免百分比")
                .defineInRange("shield.damageReduction", 0.2D, 0.0D, 1.0D);

        SHIELD_EXPLOSION_REDUCTION = BUILDER
                .comment("爆炸伤害减免系数")
                .defineInRange("shield.explosionReduction", 0.25D, 0.0D, 1.0D);

        ENABLE_DAMAGE_SHIELD_EFFECT = BUILDER
                .comment("是否启用伤害护盾效果\n" +
                        "需要安装前置模组 First Aid 才能生效\n" +
                        "true - 启用\n" +
                        "false - 禁用\n" +
                        "注意：此功能需要安装 First Aid 模组才能正常工作，如果没安装可以忽略此配置")
                .define("shield.enable_effect", true);

        DAMAGE_SHIELD_TRIGGER_CHANCE = BUILDER
                .comment("伤害护盾触发概率\n" +
                        "需要安装前置模组 First Aid 才能生效\n" +
                        "范围: 0.0 到 1.0\n" +
                        "例如: 0.1 表示 10% 的触发概率\n" +
                        "注意：此功能需要安装 First Aid 模组才能正常工作，如果没安装可以忽略此配置")
                .defineInRange("shield.trigger_chance", 0.1, 0.0, 1.0);


        // 大惯性之石
        GREATER_INERTIA_KNOCKBACK_RESISTANCE = BUILDER
                .comment("击退抗性增加值")
                .defineInRange("greaterInertiaStone.knockbackResistance", 0.4D, 0.0D, 1.0D);

        GREATER_INERTIA_MOVEMENT_SPEED = BUILDER
                .comment("移动速度增加百分比")
                .defineInRange("greaterInertiaStone.movementSpeed", 1.5D, -1.0D, 30.0D);

        GREATER_INERTIA_JUMP_BOOST = BUILDER
                .comment("跳跃高度提升值")
                .defineInRange("greaterInertiaStone.jumpBoost", 0.4D, 0.0D, 100.0D);

        GREATER_INERTIA_FALL_REDUCTION = BUILDER
                .comment("摔落伤害减免系数(0.25表示减免75%)")
                .defineInRange("greaterInertiaStone.fallReduction", 0.25D, 0.0D, 1.0D);

        GREATER_INERTIA_STEP_HEIGHT = BUILDER
                .comment("跨步高度增加值")
                .defineInRange("greaterInertiaStone.stepHeight", 0.4D, -1.0D, 10.0D);

        // 凋零戒指
        WITHER_RING_CHANCE = BUILDER
                .comment("凋零效果触发概率")
                .defineInRange("witherRing.chance", 0.2D, 0.0D, 1.0D);

        WITHER_RING_DURATION = BUILDER
                .comment("凋零效果持续时间(tick)")
                .defineInRange("witherRing.duration", 100, 20, 6000);

        WITHER_RING_AMPLIFIER = BUILDER
                .comment("凋零效果等级")
                .defineInRange("witherRing.amplifier", 0, 0, 4);


        BUILDER.comment("结构战利品配置\n配置格式：物品ID,权重,最小数量,最大数量;多个物品用分号分隔").push("Loot Settings");

        // 主世界结构
        DESERT_PYRAMID_LOOT = BUILDER
                .comment("沙漠神殿战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("desertPyramidLoot","trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");
        JUNGLE_TEMPLE_LOOT = BUILDER
                .comment("丛林神庙战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("jungleTempleLoot","trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");
                        ABANDONED_MINESHAFT_LOOT = BUILDER
                .comment("废弃矿井战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("abandonedMineshaftLoot","trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");
        STRONGHOLD_LIBRARY_LOOT = BUILDER
                .comment("要塞图书馆战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("strongholdLibraryLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        WOODLAND_MANSION_LOOT = BUILDER
                .comment("林地府邸战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("woodlandMansionLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        VILLAGE_TEMPLE_LOOT = BUILDER
                .comment("村庄教堂战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageTempleLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        SHIPWRECK_TREASURE_LOOT = BUILDER
                .comment("沉船宝藏配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("shipwreckTreasureLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        PILLAGER_OUTPOST_LOOT = BUILDER
                .comment("掠夺者前哨站配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("pillagerOutpostLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        BURIED_TREASURE_LOOT = BUILDER
                .comment("埋藏宝藏配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("buriedTreasureLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        UNDERWATER_RUIN_BIG_LOOT = BUILDER
                .comment("大型水下废墟配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("underwaterRuinBigLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        UNDERWATER_RUIN_SMALL_LOOT = BUILDER
                .comment("小型水下废墟配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("underwaterRuinSmallLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        IGLOO_CHEST_LOOT = BUILDER
                .comment("雪屋地下室配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("iglooChestLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        BASTION_TREASURE_LOOT = BUILDER
                .comment("堡垒遗迹-宝藏室配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("bastionTreasureLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        BASTION_BRIDGE_LOOT = BUILDER
                .comment("堡垒遗迹-桥梁配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("bastionBridgeLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        BASTION_HOUSING_LOOT = BUILDER
                .comment("堡垒遗迹-居住区配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("bastionHousingLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        BASTION_OTHER_LOOT = BUILDER
                .comment("堡垒遗迹-杂项配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("bastionOtherLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        RUINED_PORTAL_LOOT = BUILDER
                .comment("废弃传送门配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("ruinedPortalLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        END_CITY_LOOT = BUILDER
                .comment("末地城战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("endCityLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        ANCIENT_CITY_LOOT = BUILDER
                .comment("远古城市配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("ancientCityLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        SIMPLE_DUNGEON_LOOT = BUILDER
                .comment("小型地牢配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("simpleDungeonLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        STRONGHOLD_CORRIDOR_LOOT = BUILDER
                .comment("要塞走廊配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("strongholdCorridorLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        FOSSIL_DINOSAUR_LOOT = BUILDER
                .comment("恐龙化石配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("fossilDinosaurLoot", "");

        FOSSIL_MAMMAL_LOOT = BUILDER
                .comment("哺乳动物化石配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("fossilMammalLoot", "");

        SHIPWRECK_SUPPLY_LOOT = BUILDER
                .comment("沉船补给配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("shipwreckSupplyLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        WOODLAND_CARTOGRAPHY_LOOT = BUILDER
                .comment("林地制图室配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("woodlandCartographyLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        STRONGHOLD_CROSSING_LOOT = BUILDER
                .comment("要塞十字厅配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("strongholdCrossingLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");


        SHIPWRECK_MAP_LOOT = BUILDER
                .comment("沉船地图配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("shipwreckMapLoot", "trinketsandbaubles:glowing_powder,10,1,2;trinketsandbaubles:glowing_ingot,1,1,1");

        // 村庄职业配置
        VILLAGE_ARMORER_LOOT = BUILDER
                .comment("盔甲匠战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageArmorerLoot", "");

        VILLAGE_BUTCHER_LOOT = BUILDER
                .comment("屠夫战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageButcherLoot", "");

        VILLAGE_CARTOGRAPHER_LOOT = BUILDER
                .comment("制图师战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageCartographerLoot", "");

        VILLAGE_PLAINS_HOUSE_LOOT = BUILDER
                .comment("平原村庄房屋配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villagePlainsHouseLoot", "");

        VILLAGE_FISHER_LOOT = BUILDER
                .comment("渔夫战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageFisherLoot", "");

        VILLAGE_FLETCHER_LOOT = BUILDER
                .comment("制箭师战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageFletcherLoot", "");

        VILLAGE_TANNERY_LOOT = BUILDER
                .comment("制革厂战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageTanneryLoot", "");

        VILLAGE_LIBRARY_LOOT = BUILDER
                .comment("村庄图书馆配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageLibraryLoot", "");

        VILLAGE_MASON_LOOT = BUILDER
                .comment("石匠战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageMasonLoot", "");

        VILLAGE_SHEPHERD_LOOT = BUILDER
                .comment("牧羊人战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageShepherdLoot", "");

        VILLAGE_TOOLSMITH_LOOT = BUILDER
                .comment("工具匠战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageToolsmithLoot", "");

        VILLAGE_WEAPONSMITH_LOOT = BUILDER
                .comment("武器匠战利品配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageWeaponsmithLoot", "");

        VILLAGE_DESERT_HOUSE_LOOT = BUILDER
                .comment("沙漠村庄房屋配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageDesertHouseLoot", "");

        VILLAGE_SNOWY_HOUSE_LOOT = BUILDER
                .comment("雪原村庄房屋配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageSnowyHouseLoot", "");

        VILLAGE_SAVANNA_HOUSE_LOOT = BUILDER
                .comment("热带草原村庄房屋配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageSavannaHouseLoot", "");

        VILLAGE_TAIGA_HOUSE_LOOT = BUILDER
                .comment("针叶林村庄房屋配置\n格式：物品ID,权重,最小数量,最大数量（用分号分隔）")
                .define("villageTaigaHouseLoot", "");


        SPEC = BUILDER.build();
    }


    // 辅助方法：统一创建配置项
    private ForgeConfigSpec.ConfigValue<String> defineLoot(
            ForgeConfigSpec.Builder builder,
            String configKey,
            String structureId,
            String comment,
            String defaultValue
    ) {
        return builder.comment(comment + "\n格式：物品ID,权重,最小数量,最大数量（多个条目用分号分隔）\n结构ID：" + structureId)
                .define(configKey + "Loot", defaultValue);
    }

    // ===================== 战利品加载逻辑 =====================
    private static final Map<ResourceLocation, ForgeConfigSpec.ConfigValue<String>> LOOT_MAPPING = new HashMap<>();

    static {
        // 初始化映射表
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/desert_pyramid"), ModConfig.DESERT_PYRAMID_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/jungle_temple"), ModConfig.JUNGLE_TEMPLE_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/abandoned_mineshaft"), ModConfig.ABANDONED_MINESHAFT_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/stronghold_library"), ModConfig.STRONGHOLD_LIBRARY_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/woodland_mansion"), ModConfig.WOODLAND_MANSION_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_temple"), ModConfig.VILLAGE_TEMPLE_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/shipwreck_treasure"), ModConfig.SHIPWRECK_TREASURE_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/pillager_outpost"), ModConfig.PILLAGER_OUTPOST_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/buried_treasure"), ModConfig.BURIED_TREASURE_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/underwater_ruin_big"), ModConfig.UNDERWATER_RUIN_BIG_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/underwater_ruin_small"), ModConfig.UNDERWATER_RUIN_SMALL_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/igloo_chest"), ModConfig.IGLOO_CHEST_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/bastion_treasure"), ModConfig.BASTION_TREASURE_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/bastion_bridge"), ModConfig.BASTION_BRIDGE_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/bastion_housing"), ModConfig.BASTION_HOUSING_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/bastion_other"), ModConfig.BASTION_OTHER_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/ruined_portal"), ModConfig.RUINED_PORTAL_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/end_city_treasure"), ModConfig.END_CITY_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/ancient_city"), ModConfig.ANCIENT_CITY_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/simple_dungeon"), ModConfig.SIMPLE_DUNGEON_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/stronghold_corridor"), ModConfig.STRONGHOLD_CORRIDOR_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/fossil_dinosaur"), ModConfig.FOSSIL_DINOSAUR_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/fossil_mammal"), ModConfig.FOSSIL_MAMMAL_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/shipwreck_supply"), ModConfig.SHIPWRECK_SUPPLY_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/woodland_mansion_cartography"), ModConfig.WOODLAND_CARTOGRAPHY_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/stronghold_crossing"), ModConfig.STRONGHOLD_CROSSING_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/shipwreck_map"), ModConfig.SHIPWRECK_MAP_LOOT);
        // 村庄职业
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_armorer"), ModConfig.VILLAGE_ARMORER_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_butcher"), ModConfig.VILLAGE_BUTCHER_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_cartographer"), ModConfig.VILLAGE_CARTOGRAPHER_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_plains_house"), ModConfig.VILLAGE_PLAINS_HOUSE_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_fisher"), ModConfig.VILLAGE_FISHER_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_fletcher"), ModConfig.VILLAGE_FLETCHER_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_tannery"), ModConfig.VILLAGE_TANNERY_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_library"), ModConfig.VILLAGE_LIBRARY_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_mason"), ModConfig.VILLAGE_MASON_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_shepherd"), ModConfig.VILLAGE_SHEPHERD_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_toolsmith"), ModConfig.VILLAGE_TOOLSMITH_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_weaponsmith"), ModConfig.VILLAGE_WEAPONSMITH_LOOT);

        // 村庄房屋
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_desert_house"), ModConfig.VILLAGE_DESERT_HOUSE_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_snowy_house"), ModConfig.VILLAGE_SNOWY_HOUSE_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_savanna_house"), ModConfig.VILLAGE_SAVANNA_HOUSE_LOOT);
        LOOT_MAPPING.put(new ResourceLocation("minecraft:chests/village/village_taiga_house"), ModConfig.VILLAGE_TAIGA_HOUSE_LOOT);


    }

    public static void loadLootConfig() {
        lootConfig.clear();
        LOOT_MAPPING.forEach((tableId, config) -> {
            String value = config.get();
            if (value.isEmpty()) return;

            List<LootEntry> entries = parseLootEntries(value);
            if (!entries.isEmpty()) {
                lootConfig.put(tableId, entries);
            }
        });
    }

    public static Map<ResourceLocation, List<LootEntry>> lootConfig = new HashMap<>();

    // === LootEntry 内部类 ===
    public static class LootEntry {
        public final ResourceLocation itemId;
        public final int weight;
        public final int minRolls;
        public final int maxRolls;

        public LootEntry(ResourceLocation itemId, int weight, int minRolls, int maxRolls) {
            this.itemId = itemId;
            this.weight = weight;
            this.minRolls = minRolls;
            this.maxRolls = maxRolls;
        }
    }

    private static List<LootEntry> parseLootEntries(String configValue) {
        List<LootEntry> entries = new ArrayList<>();
        for (String entry : configValue.split(";")) {
            String[] parts = entry.split(",");
            if (parts.length != 4) {
                TrinketsandBaublesMod.LOGGER.warn("Invalid loot entry format: {}", entry);
                continue;
            }

            try {
                ResourceLocation itemId = new ResourceLocation(parts[0].trim());
                entries.add(new LootEntry(
                        itemId,
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()),
                        Integer.parseInt(parts[3].trim())
                ));
            } catch (Exception e) {
                TrinketsandBaublesMod.LOGGER.error("Invalid loot entry: {}", entry, e);
            }
        }
        return entries;
    }



    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        if (configEvent.getConfig().getModId().equals(TrinketsandBaublesMod.MOD_ID)) {
            TrinketsandBaublesMod.LOGGER.info("Loaded Trinkets and Baubles config file {}",
                    configEvent.getConfig().getFileName());
            loadLootConfig();
        }
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        if (configEvent.getConfig().getModId().equals(TrinketsandBaublesMod.MOD_ID)) {
            TrinketsandBaublesMod.LOGGER.debug("Trinkets and Baubles config reloaded!");
            loadLootConfig();
        }
    }
    public static boolean isModifierEnabled() {
        return MODIFIER_ENABLED.get();
    }
    public static int getAnvilRecastExpCost() {
        return ANVIL_RECAST_EXP_COST.get();
    }

    public static int getAnvilRecastMaterialCost() {
        return ANVIL_RECAST_MATERIAL_COST.get();
    }
}
