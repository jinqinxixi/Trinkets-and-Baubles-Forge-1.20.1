package com.jinqinxixi.trinketsandbaubles.config;


import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

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
    public static final ForgeConfigSpec.IntValue DRAGONS_EYE_SCAN_RANGE;

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

    // 极化之石配置
    public static final ForgeConfigSpec.DoubleValue POLARIZED_STONE_ATTRACTION_RANGE;
    public static final ForgeConfigSpec.DoubleValue POLARIZED_STONE_ATTRACTION_SPEED;
    public static final ForgeConfigSpec.DoubleValue POLARIZED_STONE_DEFLECTION_RANGE;
    public static final ForgeConfigSpec.IntValue POLARIZED_STONE_DEFLECTION_MANA_COST;

    // 荣誉之盾配置
    public static final ForgeConfigSpec.IntValue SHIELD_MAX_DAMAGE_COUNT;
    public static final ForgeConfigSpec.DoubleValue SHIELD_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue SHIELD_EXPLOSION_REDUCTION;

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

    // 龙之效果配置
    public static final ForgeConfigSpec.DoubleValue DRAGON_ATTACK_DAMAGE_BOOST;
    public static final ForgeConfigSpec.DoubleValue DRAGON_MAX_HEALTH_BOOST;
    public static final ForgeConfigSpec.DoubleValue DRAGON_ARMOR_TOUGHNESS;
    public static final ForgeConfigSpec.DoubleValue DRAGON_FLIGHT_SPEED;
    public static final ForgeConfigSpec.IntValue DRAGON_MANA_BONUS;
    public static final ForgeConfigSpec.DoubleValue DRAGON_FLIGHT_MANA_COST;
    public static final ForgeConfigSpec.DoubleValue DRAGON_MANA_CHECK_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue DRAGON_BREATH_BASE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue DRAGON_BREATH_MIN_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue DRAGON_BREATH_DECAY_RATE;
    public static final ForgeConfigSpec.DoubleValue DRAGON_SCALE_FACTOR;

    // 矮人效果配置
    public static final ForgeConfigSpec.DoubleValue DWARVES_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue DWARVES_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue DWARVES_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue DWARVES_ARMOR_TOUGHNESS;
    public static final ForgeConfigSpec.DoubleValue DWARVES_KNOCKBACK_RESISTANCE;
    public static final ForgeConfigSpec.DoubleValue DWARVES_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue DWARVES_SCALE_FACTOR;

    // 精灵效果配置
    public static final ForgeConfigSpec.DoubleValue ELVES_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue ELVES_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ELVES_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue ELVES_FOREST_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue ELVES_FOREST_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.IntValue ELVES_MANA_BONUS;
    public static final ForgeConfigSpec.DoubleValue ELVES_BOW_DAMAGE_BOOST;
    public static final ForgeConfigSpec.DoubleValue ELVES_SCALE_FACTOR;

    // 猫妖效果配置
    public static final ForgeConfigSpec.DoubleValue FAELES_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue FAELES_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue FAELES_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue FAELES_ARMOR_TOUGHNESS;
    public static final ForgeConfigSpec.DoubleValue FAELES_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue FAELES_LUCK;
    public static final ForgeConfigSpec.DoubleValue FAELES_SWIM_SPEED;
    public static final ForgeConfigSpec.DoubleValue FAELES_JUMP_BOOST;
    public static final ForgeConfigSpec.DoubleValue FAELES_UNARMED_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue FAELES_STEP_HEIGHT;
    public static final ForgeConfigSpec.DoubleValue FAELES_REACH;
    public static final ForgeConfigSpec.DoubleValue FAELES_ARMOR_SPEED_PENALTY;
    public static final ForgeConfigSpec.IntValue FAELES_MANA_BONUS;
    public static final ForgeConfigSpec.BooleanValue FAELES_WALL_CLIMB;
    public static final ForgeConfigSpec.DoubleValue FAELES_CLIMB_SPEED;
    public static final ForgeConfigSpec.DoubleValue FAELES_CLIMB_HORIZONTAL_DRAG;
    public static final ForgeConfigSpec.DoubleValue FAELES_SCALE_FACTOR;

    // 精灵露效果配置
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_ARMOR;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_ARMOR_TOUGHNESS;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_SWIM_SPEED;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_REACH;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_STEP_HEIGHT;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_FLIGHT_SPEED;
    public static final ForgeConfigSpec.IntValue FAIRY_DEW_MANA_BONUS;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_JUMP_BOOST;
    public static final ForgeConfigSpec.BooleanValue FAIRY_DEW_WALL_CLIMB;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_CLIMB_SPEED;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_CLIMB_HORIZONTAL_DRAG;
    public static final ForgeConfigSpec.DoubleValue FAIRY_DEW_SCALE_FACTOR;

    // 哥布林效果配置
    public static final ForgeConfigSpec.DoubleValue GOBLIN_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue GOBLIN_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue GOBLIN_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue GOBLIN_LUCK;
    public static final ForgeConfigSpec.DoubleValue GOBLIN_SWIM_SPEED;
    public static final ForgeConfigSpec.DoubleValue GOBLIN_STEP_HEIGHT;
    public static final ForgeConfigSpec.IntValue GOBLIN_MANA_PENALTY;
    public static final ForgeConfigSpec.DoubleValue GOBLIN_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue GOBLIN_SCALE_FACTOR;

    // 泰坦效果配置
    public static final ForgeConfigSpec.DoubleValue TITAN_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue TITAN_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue TITAN_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue TITAN_KNOCKBACK_RESISTANCE;
    public static final ForgeConfigSpec.DoubleValue TITAN_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue TITAN_JUMP_BOOST;
    public static final ForgeConfigSpec.DoubleValue TITAN_STEP_HEIGHT;
    public static final ForgeConfigSpec.DoubleValue TITAN_REACH_DISTANCE;
    public static final ForgeConfigSpec.IntValue TITAN_MANA_MODIFIER;
    public static final ForgeConfigSpec.DoubleValue TITAN_WATER_SINK_SPEED;
    public static final ForgeConfigSpec.DoubleValue TITAN_SCALE_FACTOR;

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
        BUILDER.comment("修饰系统配置").push("modifiers");

        MODIFIER_ENABLED = BUILDER
                .comment("是否启用饰品修饰系统")
                .define("enabled", true);

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
                .defineInRange("mana.defaultMaxMana", 100, 10, 10000);

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
        DRAGONS_EYE_SCAN_RANGE = BUILDER
                .comment("龙眼扫描范围")
                .defineInRange("dragonsEye.scanRange", 12, 4, 64);

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
                .comment("每秒防御消耗魔力")
                .defineInRange("polarizedStone.deflectionManaCost", 8, 1, 5000);

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

        //龙效果
        DRAGON_ATTACK_DAMAGE_BOOST = BUILDER
                .comment("攻击伤害提升百分比")
                .defineInRange("dragon.attackDamageBoost", 0.50D, -1.0D, 50.0D);

        DRAGON_MAX_HEALTH_BOOST = BUILDER
                .comment("最大生命值提升百分比")
                .defineInRange("dragon.maxHealthBoost", 0.25D, -1.0D, 50.0D);

        DRAGON_ARMOR_TOUGHNESS = BUILDER
                .comment("盔甲韧性增加百分比")
                .defineInRange("dragon.armorToughness", 0.5D, -1.0D, 20.0D);

        DRAGON_FLIGHT_SPEED = BUILDER
                .comment("飞行速度倍率")
                .defineInRange("dragon.flightSpeed", 0.3D, -1.0D, 2.0D);

        DRAGON_MANA_BONUS = BUILDER
                .comment("魔力值加成")
                .defineInRange("dragon.manaBonus", 300, 0, 10000);

        DRAGON_FLIGHT_MANA_COST = BUILDER
                .comment("每秒飞行消耗魔力值")
                .defineInRange("dragon.flightManaCost", 5.0D, -1.0D, 100.0D);

        DRAGON_MANA_CHECK_INTERVAL = BUILDER
                .comment("魔力检查间隔(tick)")
                .defineInRange("dragon.manaCheckInterval", 20.0D, 1.0D, 100.0D);

        DRAGON_BREATH_BASE_DAMAGE = BUILDER
                .comment("龙息基础伤害")
                .defineInRange("dragon.BreathBaseDamage", 4.0, 0.0, 100.0);

        DRAGON_BREATH_MIN_DAMAGE = BUILDER
                .comment("龙息最小伤害")
                .defineInRange("dragon.BreathMinDamage", 2.0, 0.0, 100.0);

        DRAGON_BREATH_DECAY_RATE = BUILDER
                .comment("龙息伤害衰减率")
                .defineInRange("dragon.BreathDecayRate", 0.7, 0.0, 1.0);

        DRAGON_SCALE_FACTOR = BUILDER
                .comment("龙族体型缩放 1 为正常体型")
                .defineInRange("dragon.scale", 1.2, 0.1, 10.0);

        // 矮人效果
        DWARVES_ATTACK_SPEED = BUILDER
                .comment("攻击速度修改值(负值降低,正值提升)")
                .defineInRange("dwarves.attackSpeed", -0.25D, -1.0D, 100.0D);

        DWARVES_ATTACK_DAMAGE = BUILDER
                .comment("攻击伤害修改值")
                .defineInRange("dwarves.attackDamage", 0.25D, -1.0D, 200.0D);

        DWARVES_MAX_HEALTH = BUILDER
                .comment("最大生命值修改值")
                .defineInRange("dwarves.maxHealth", -0.30D, -1.0D, 200.0D);

        DWARVES_ARMOR_TOUGHNESS = BUILDER
                .comment("护甲韧性修改值")
                .defineInRange("dwarves.armorToughness", 0.25D, -1.0D, 200.0D);

        DWARVES_KNOCKBACK_RESISTANCE = BUILDER
                .comment("击退抗性修改值")
                .defineInRange("dwarves.knockbackResistance", 0.20D, -1.0D, 1.0D);

        DWARVES_MOVEMENT_SPEED = BUILDER
                .comment("移动速度修改值")
                .defineInRange("dwarves.movementSpeed", -0.25D, -1.0D, 100.0D);

        DWARVES_SCALE_FACTOR = BUILDER
                .comment("矮人族体型缩放 1 为正常体型")
                .defineInRange("dwarves.scale", 0.75D, 0.1D, 10.0D);

        // 精灵效果
        ELVES_ATTACK_SPEED = BUILDER
                .comment("基础攻击速度修改值")
                .defineInRange("elves.attackSpeed", 0.30D, -1.0D, 20.0D);

        ELVES_ATTACK_DAMAGE = BUILDER
                .comment("基础攻击伤害修改值")
                .defineInRange("elves.attackDamage", -0.25D, -1.0D, 20.0D);

        ELVES_MOVEMENT_SPEED = BUILDER
                .comment("基础移动速度修改值")
                .defineInRange("elves.movementSpeed", 0.10D, -1.0D, 20.0D);

        ELVES_FOREST_ATTACK_SPEED = BUILDER
                .comment("森林中额外攻击速度加成")
                .defineInRange("elves.forestAttackSpeed", 0.15D, -1.0D, 10.0D);

        ELVES_FOREST_MOVEMENT_SPEED = BUILDER
                .comment("森林中额外移动速度加成")
                .defineInRange("elves.forestMovementSpeed", 0.15D, -1.0D, 10.0D);

        ELVES_MANA_BONUS = BUILDER
                .comment("精灵效果魔力值增加量")
                .defineInRange("elves.manaBonus", 100, 0, 10000);

        ELVES_BOW_DAMAGE_BOOST = BUILDER
                .comment("潜行时弓箭伤害增加倍率")
                .defineInRange("elves.bowDamageBoost", 1.5D, 1.0D, 50.0D);

        ELVES_SCALE_FACTOR = BUILDER
                .comment("精灵族体型缩放 1 为正常体型")
                .defineInRange("elves.scale", 0.5D, 0.1D, 10.0D);

        // 猫妖效果
        FAELES_ATTACK_DAMAGE = BUILDER
                .comment("基础攻击伤害修改值")
                .defineInRange("faeles.attackDamage", -0.25D, -1.0D, 1.0D);

        FAELES_ATTACK_SPEED = BUILDER
                .comment("攻击速度修改值")
                .defineInRange("faeles.attackSpeed", 0.15D, -1.0D, 2.0D);

        FAELES_MAX_HEALTH = BUILDER
                .comment("最大生命值修改值")
                .defineInRange("faeles.maxHealth", -0.25D, -1.0D, 1.0D);

        FAELES_ARMOR_TOUGHNESS = BUILDER
                .comment("护甲韧性修改值")
                .defineInRange("faeles.armorToughness", -0.15D, -1.0D, 100.0D);

        FAELES_MOVEMENT_SPEED = BUILDER
                .comment("移动速度修改值")
                .defineInRange("faeles.movementSpeed", 0.15D, -1.0D, 200.0D);

        FAELES_LUCK = BUILDER
                .comment("幸运值增加")
                .defineInRange("faeles.luck", 2.0D, -100.0D, 100.0D);

        FAELES_SWIM_SPEED = BUILDER
                .comment("游泳速度修改值")
                .defineInRange("faeles.swimSpeed", 0.30D, -1.0D, 200.0D);

        FAELES_JUMP_BOOST = BUILDER
                .comment("跳跃高度修改值")
                .defineInRange("faeles.jumpStrength", 0.60D, -1.0D, 200.0D);

        FAELES_UNARMED_DAMAGE = BUILDER
                .comment("空手伤害增加值")
                .defineInRange("faeles.unarmedDamage", 6.0D, -100.0D, 2000.0D);

        FAELES_STEP_HEIGHT = BUILDER
                .comment("跨步高度增加值")
                .defineInRange("faeles.stepHeight", 0.4D, 0.0D, 100.0D);

        FAELES_REACH = BUILDER
                .comment("交互距离修改值")
                .defineInRange("faeles.reach", -0.1D, -1.0D, 0.5D);

        FAELES_ARMOR_SPEED_PENALTY = BUILDER
                .comment("每件非皮革护甲的速度惩罚")
                .defineInRange("faeles.armorSpeedPenalty", -0.04D, -1D, 0.0D);

        FAELES_MANA_BONUS = BUILDER
                .comment("魔力值增加量")
                .defineInRange("faeles.manaBonus", 25, 0, 10000);

        FAELES_WALL_CLIMB = BUILDER
                .comment("猫猫效果是否允许攀爬墙壁")
                .define("faeles.wallClimb", true);

        FAELES_CLIMB_SPEED = BUILDER
                .comment("猫猫效果爬墙速度（原版梯子速度约为0.11）")
                .defineInRange("faeles.climbSpeed", 0.11D, 0.05D, 0.3D);

        FAELES_CLIMB_HORIZONTAL_DRAG = BUILDER
                .comment("猫猫效果爬墙时的水平移动阻力")
                .defineInRange("faeles.climbHorizontalDrag", 0.7D, 0.5D, 1.0D);

        FAELES_SCALE_FACTOR = BUILDER
                .comment("猫族体型缩放 1 为正常体型")
                .defineInRange("faeles.scale", 0.85D, 0.1D, 10.0D);


        // 精灵露效果
        FAIRY_DEW_ATTACK_DAMAGE = BUILDER
                .comment("攻击伤害修改值")
                .defineInRange("fairyDew.attackDamage", -0.75D, -1.0D, 100.0D);

        FAIRY_DEW_MAX_HEALTH = BUILDER
                .comment("最大生命值修改值")
                .defineInRange("fairyDew.maxHealth", -0.60D, -1.0D, 100.0D);

        FAIRY_DEW_ARMOR = BUILDER
                .comment("护甲值修改值")
                .defineInRange("fairyDew.armor", -0.50D, -1.0D, 100.0D);

        FAIRY_DEW_ARMOR_TOUGHNESS = BUILDER
                .comment("护甲韧性修改值")
                .defineInRange("fairyDew.armorToughness", -0.25D, -1.0D, 100.0D);

        FAIRY_DEW_MOVEMENT_SPEED = BUILDER
                .comment("移动速度修改值")
                .defineInRange("fairyDew.movementSpeed", -0.25D, -1.0D, 100.0D);

        FAIRY_DEW_SWIM_SPEED = BUILDER
                .comment("游泳速度修改值")
                .defineInRange("fairyDew.swimSpeed", -0.25D, -1.0D, 100.0D);

        FAIRY_DEW_REACH = BUILDER
                .comment("交互距离修改值")
                .defineInRange("fairyDew.reach", -0.35D, -1.0D, 100.0D);

        FAIRY_DEW_STEP_HEIGHT = BUILDER
                .comment("跨步高度增加值")
                .defineInRange("fairyDew.stepHeight", 0.4D, -1.0D, 100.0D);

        FAIRY_DEW_FLIGHT_SPEED = BUILDER
                .comment("飞行速度倍率")
                .defineInRange("fairyDew.flightSpeed", 0.3D, -1.0D, 2.0D);

        FAIRY_DEW_MANA_BONUS = BUILDER
                .comment("魔力值增加量")
                .defineInRange("fairyDew.manaBonus", 400, 0, 20000);

        FAIRY_DEW_JUMP_BOOST = BUILDER
                .comment("跳跃重力修改值")
                .defineInRange("fairyDew.jumpStrength", -0.25D, -1.0D, 200.0D);

        FAIRY_DEW_WALL_CLIMB = BUILDER
                .comment("精灵露效果是否允许攀爬墙壁")
                .define("fairyDew.wallClimb", true);

        FAIRY_DEW_CLIMB_SPEED = BUILDER
                .comment("精灵露效果爬墙速度（原版梯子速度约为0.11）")
                .defineInRange("fairyDew.climbSpeed", 0.11D, 0.05D, 0.3D);

        FAIRY_DEW_CLIMB_HORIZONTAL_DRAG = BUILDER
                .comment("精灵露效果爬墙时的水平移动阻力")
                .defineInRange("fairyDew.climbHorizontalDrag", 0.7D, 0.5D, 1.0D);

        FAIRY_DEW_SCALE_FACTOR = BUILDER
                .comment("仙女族体型缩放 1 为正常体型")
                .defineInRange("fairyDew.scale", 0.25D, 0.1D, 10.0D);

        // 哥布林效果
        GOBLIN_ATTACK_DAMAGE = BUILDER
                .comment("攻击伤害修改值")
                .defineInRange("goblin.attackDamage", -0.50D, -1.0D, 0.0D);

        GOBLIN_MAX_HEALTH = BUILDER
                .comment("最大生命值修改值")
                .defineInRange("goblin.maxHealth", -0.40D, -1.0D, 0.0D);

        GOBLIN_MOVEMENT_SPEED = BUILDER
                .comment("移动速度修改值")
                .defineInRange("goblin.movementSpeed", 0.20D, -1.0D, 1.0D);

        GOBLIN_LUCK = BUILDER
                .comment("幸运值增加")
                .defineInRange("goblin.luck", 1.0D, -100.0D, 100.0D);

        GOBLIN_SWIM_SPEED = BUILDER
                .comment("游泳速度修改值")
                .defineInRange("goblin.swimSpeed", 0.10D, -1.0D, 100.0D);

        GOBLIN_STEP_HEIGHT = BUILDER
                .comment("跨步高度增加值")
                .defineInRange("goblin.stepHeight", 0.4D, -1.0D, 100.0D);

        GOBLIN_MANA_PENALTY = BUILDER
                .comment("魔力值修改量")
                .defineInRange("goblin.manaPenalty", -25, -10000, 10000);

        GOBLIN_DAMAGE_REDUCTION = BUILDER
                .comment("火焰和爆炸伤害减免比例")
                .defineInRange("goblin.damageReduction", 0.75D, 0.0D, 1.0D);

        GOBLIN_SCALE_FACTOR = BUILDER
                .comment("哥布林族体型缩放 1 为正常体型")
                .defineInRange("goblin.scale", 0.5D, 0.1D, 10.0D);

        // 泰坦效果
        TITAN_ATTACK_SPEED = BUILDER
                .comment("攻击速度修改值")
                .defineInRange("titan.attackSpeed", -0.50D, -1.0D, 1.0D);

        TITAN_ATTACK_DAMAGE = BUILDER
                .comment("攻击伤害修改值")
                .defineInRange("titan.attackDamage", 0.50D, -1.0D, 5.0D);

        TITAN_MAX_HEALTH = BUILDER
                .comment("最大生命值修改值")
                .defineInRange("titan.maxHealth", 1.0D, -1.0D, 5.0D);

        TITAN_KNOCKBACK_RESISTANCE = BUILDER
                .comment("击退抗性")
                .defineInRange("titan.knockbackResistance", 1.0D, 0.0D, 1.0D);

        TITAN_MOVEMENT_SPEED = BUILDER
                .comment("移动速度修改值")
                .defineInRange("titan.movementSpeed", 0.05D, -1.0D, 1.0D);

        TITAN_JUMP_BOOST = BUILDER
                .comment("跳跃力量修改值")
                .defineInRange("titan.jumpStrength", 0.75D, -1.0D, 2.0D);

        TITAN_STEP_HEIGHT = BUILDER
                .comment("跨步高度值")
                .defineInRange("titan.stepHeight", 1.4D, -1.0D, 3.0D);

        TITAN_REACH_DISTANCE = BUILDER
                .comment("交互距离修改值")
                .defineInRange("titan.reachDistance", 1.0D, -1.0D, 3.0D);

        TITAN_MANA_MODIFIER = BUILDER
                .comment("魔力值修改量")
                .defineInRange("titan.manaModifier", -50, -10000, 10000);

        TITAN_WATER_SINK_SPEED = BUILDER
                .comment("水中下沉速度")
                .defineInRange("titan.waterSinkSpeed", 0.1D, 0.0D, 1.0D);

        TITAN_SCALE_FACTOR = BUILDER
                .comment("泰坦族体型缩放 1 为正常体型")
                .defineInRange("titan.scale", 3.0D, 0.1D, 10.0D);




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
