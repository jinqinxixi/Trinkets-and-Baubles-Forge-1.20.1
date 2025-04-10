package com.jinqinxixi.trinketsandbaubles.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RaceAttributesConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // 矮人族属性配置
    public static class DwarvesAttributes {
        public final ForgeConfigSpec.DoubleValue MAX_HEALTH;
        public final ForgeConfigSpec.DoubleValue FOLLOW_RANGE;
        public final ForgeConfigSpec.DoubleValue KNOCKBACK_RESISTANCE;
        public final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED;
        public final ForgeConfigSpec.DoubleValue FLYING_SPEED;
        public final ForgeConfigSpec.DoubleValue ATTACK_DAMAGE;
        public final ForgeConfigSpec.DoubleValue ATTACK_KNOCKBACK;
        public final ForgeConfigSpec.DoubleValue ATTACK_SPEED;
        public final ForgeConfigSpec.DoubleValue ARMOR;
        public final ForgeConfigSpec.DoubleValue ARMOR_TOUGHNESS;
        public final ForgeConfigSpec.DoubleValue LUCK;
        public final ForgeConfigSpec.DoubleValue SWIM_SPEED;
        public final ForgeConfigSpec.DoubleValue NAMETAG_DISTANCE;
        public final ForgeConfigSpec.DoubleValue ENTITY_GRAVITY;
        public final ForgeConfigSpec.DoubleValue STEP_HEIGHT;
        public final ForgeConfigSpec.DoubleValue BLOCK_REACH;
        public final ForgeConfigSpec.DoubleValue ENTITY_REACH;

        public final ForgeConfigSpec.DoubleValue DWARVES_SCALE_FACTOR;
        public final ForgeConfigSpec.DoubleValue DWARVES_MANA_BONUS;

        public DwarvesAttributes(ForgeConfigSpec.Builder builder) {
            builder.comment("矮人族属性配置").push("dwarves");

            MAX_HEALTH = builder.comment("生命值倍率")
                    .defineInRange("max_health", -0.30, -10.0, 10.0);

            FOLLOW_RANGE = builder.comment("跟随范围倍率")
                    .defineInRange("follow_range", 0.0, -10.0, 10.0);

            KNOCKBACK_RESISTANCE = builder.comment("击退抗性")
                    .defineInRange("knockback_resistance", 0.20, -10.0, 1.0);

            MOVEMENT_SPEED = builder.comment("移动速度倍率")
                    .defineInRange("movement_speed", 0.25, -10.0, 10.0);

            FLYING_SPEED = builder.comment("飞行速度倍率")
                    .defineInRange("flying_speed", 0.0, -10.0, 10.0);

            ATTACK_DAMAGE = builder.comment("攻击伤害倍率")
                    .defineInRange("attack_damage", 0.25, -10.0, 10.0);

            ATTACK_KNOCKBACK = builder.comment("攻击击退力度")
                    .defineInRange("attack_knockback", 0.0, -10.0, 10.0);

            ATTACK_SPEED = builder.comment("攻击速度倍率")
                    .defineInRange("attack_speed", -0.25, -10.0, 10.0);

            ARMOR = builder.comment("护甲值加成")
                    .defineInRange("armor", 0.0, -10.0, 50.0);

            ARMOR_TOUGHNESS = builder.comment("护甲韧性加成")
                    .defineInRange("armor_toughness", 0.25, -10.0, 50.0);

            LUCK = builder.comment("幸运值加成")
                    .defineInRange("luck", 1.0, -10.0, 10.0);

            SWIM_SPEED = builder.comment("游泳速度倍率")
                    .defineInRange("swim_speed", 0.0, -10.0, 10.0);

            NAMETAG_DISTANCE = builder.comment("名称标签显示距离倍率")
                    .defineInRange("nametag_distance", 0.0, -10.0, 10.0);

            ENTITY_GRAVITY = builder.comment("重力影响倍率")
                    .defineInRange("entity_gravity", 0.0, -10.0, 10.0);

            STEP_HEIGHT = builder.comment("步高增加值")
                    .defineInRange("step_height", 0.0, -10.0, 10.0);

            BLOCK_REACH = builder.comment("方块交互距离倍率")
                    .defineInRange("block_reach", 0.0, -10.0, 10.0);

            ENTITY_REACH = builder.comment("实体交互距离倍率")
                    .defineInRange("entity_reach", 0.0, -10.0, 10.0);

            DWARVES_SCALE_FACTOR = BUILDER.comment("矮人族体型缩放 1 为正常体型")
                    .defineInRange("scale", 0.75D, 0.1D, 10.0D);

            DWARVES_MANA_BONUS = BUILDER.comment("矮人效果魔力值增加量")
                    .defineInRange("manaBonus", 0.0, 0, 10000);

            builder.pop();
        }
    }

    // 龙族属性配置
    public static class DragonAttributes {
        public final ForgeConfigSpec.DoubleValue MAX_HEALTH;
        public final ForgeConfigSpec.DoubleValue FOLLOW_RANGE;
        public final ForgeConfigSpec.DoubleValue KNOCKBACK_RESISTANCE;
        public final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED;
        public final ForgeConfigSpec.DoubleValue FLYING_SPEED;
        public final ForgeConfigSpec.DoubleValue ATTACK_DAMAGE;
        public final ForgeConfigSpec.DoubleValue ATTACK_KNOCKBACK;
        public final ForgeConfigSpec.DoubleValue ATTACK_SPEED;
        public final ForgeConfigSpec.DoubleValue ARMOR;
        public final ForgeConfigSpec.DoubleValue ARMOR_TOUGHNESS;
        public final ForgeConfigSpec.DoubleValue LUCK;
        public final ForgeConfigSpec.DoubleValue SWIM_SPEED;
        public final ForgeConfigSpec.DoubleValue NAMETAG_DISTANCE;
        public final ForgeConfigSpec.DoubleValue ENTITY_GRAVITY;
        public final ForgeConfigSpec.DoubleValue STEP_HEIGHT;
        public final ForgeConfigSpec.DoubleValue BLOCK_REACH;
        public final ForgeConfigSpec.DoubleValue ENTITY_REACH;

        public final ForgeConfigSpec.DoubleValue DRAGON_FLIGHT_SPEED;
        public final ForgeConfigSpec.DoubleValue DRAGON_MANA_BONUS;
        public final ForgeConfigSpec.DoubleValue DRAGON_FLIGHT_MANA_COST;
        public final ForgeConfigSpec.DoubleValue DRAGON_MANA_CHECK_INTERVAL;
        public final ForgeConfigSpec.DoubleValue DRAGON_BREATH_MANA_COST;
        public final ForgeConfigSpec.DoubleValue DRAGON_BREATH_BASE_DAMAGE;
        public final ForgeConfigSpec.DoubleValue DRAGON_BREATH_MIN_DAMAGE;
        public final ForgeConfigSpec.DoubleValue DRAGON_BREATH_DECAY_RATE;
        public final ForgeConfigSpec.DoubleValue DRAGON_SCALE_FACTOR;

        public DragonAttributes(ForgeConfigSpec.Builder builder) {
            builder.comment("龙族属性配置").push("dragon");

            MAX_HEALTH = builder.comment("生命值倍率")
                    .defineInRange("max_health", 0.25, -10.0, 10.0);

            FOLLOW_RANGE = builder.comment("跟随范围倍率")
                    .defineInRange("follow_range", 0.0, -10.0, 10.0);

            KNOCKBACK_RESISTANCE = builder.comment("击退抗性")
                    .defineInRange("knockback_resistance", 0.0, -10.0, 1.0);

            MOVEMENT_SPEED = builder.comment("移动速度倍率")
                    .defineInRange("movement_speed", 0.0, -10.0, 10.0);

            FLYING_SPEED = builder.comment("飞行速度倍率")
                    .defineInRange("flying_speed", 0.0, -10.0, 10.0);

            ATTACK_DAMAGE = builder.comment("攻击伤害倍率")
                    .defineInRange("attack_damage", 0.5, -10.0, 10.0);

            ATTACK_KNOCKBACK = builder.comment("攻击击退力度")
                    .defineInRange("attack_knockback", 0.0, -10.0, 10.0);

            ATTACK_SPEED = builder.comment("攻击速度倍率")
                    .defineInRange("attack_speed", 0.0, -10.0, 10.0);

            ARMOR = builder.comment("护甲值加成")
                    .defineInRange("armor", 0.0, -10.0, 50.0);

            ARMOR_TOUGHNESS = builder.comment("护甲韧性加成")
                    .defineInRange("armor_toughness", 0.5, -10.0, 50.0);

            LUCK = builder.comment("幸运值加成")
                    .defineInRange("luck", 0.0, -10.0, 10.0);

            SWIM_SPEED = builder.comment("游泳速度倍率")
                    .defineInRange("swim_speed", 0.0, -10.0, 10.0);

            NAMETAG_DISTANCE = builder.comment("名称标签显示距离倍率")
                    .defineInRange("nametag_distance", 0.0, -10.0, 10.0);

            ENTITY_GRAVITY = builder.comment("重力影响倍率")
                    .defineInRange("entity_gravity", 0.0, -10.0, 10.0);

            STEP_HEIGHT = builder.comment("步高增加值")
                    .defineInRange("step_height", 0.0, -10.0, 10.0);

            BLOCK_REACH = builder.comment("方块交互距离倍率")
                    .defineInRange("block_reach", 0.0, -10.0, 10.0);

            ENTITY_REACH = builder.comment("实体交互距离倍率")
                    .defineInRange("entity_reach", 0.0, -10.0, 10.0);

            DRAGON_FLIGHT_SPEED = BUILDER.comment("飞行速度倍率")
                    .defineInRange("flightSpeed", 0.3D, -1.0D, 2.0D);

            DRAGON_MANA_BONUS = BUILDER.comment("魔力值加成")
                    .defineInRange("manaBonus", 300.0, 0, 10000);

            DRAGON_FLIGHT_MANA_COST = BUILDER.comment("每秒飞行消耗魔力值")
                    .defineInRange("flightManaCost", 5.0D, 0.0D, 1000.0D);

            DRAGON_MANA_CHECK_INTERVAL = BUILDER.comment("魔力检查间隔(tick)")
                    .defineInRange("manaCheckInterval", 20.0D, 1.0D, 200.0D);

            DRAGON_BREATH_MANA_COST = BUILDER.comment("每秒龙息消耗魔力")
                    .defineInRange("breathManaCost", 5.0D, 0.0D, 5000.0D);

            DRAGON_BREATH_BASE_DAMAGE = BUILDER.comment("龙息基础伤害")
                    .defineInRange("BreathBaseDamage", 4.0, 0.0, 100.0);

            DRAGON_BREATH_MIN_DAMAGE = BUILDER.comment("龙息最小伤害")
                    .defineInRange("BreathMinDamage", 2.0, 0.0, 100.0);

            DRAGON_BREATH_DECAY_RATE = BUILDER.comment("龙息伤害衰减率")
                    .defineInRange("BreathDecayRate", 0.7, 0.0, 1.0);

            DRAGON_SCALE_FACTOR = BUILDER.comment("龙族体型缩放 1 为正常体型")
                    .defineInRange("scale", 1.2, 0.1, 10.0);

            builder.pop();
        }
    }

    // 精灵族属性配置
    public static class ElvesAttributes {
        public final ForgeConfigSpec.DoubleValue MAX_HEALTH;
        public final ForgeConfigSpec.DoubleValue FOLLOW_RANGE;
        public final ForgeConfigSpec.DoubleValue KNOCKBACK_RESISTANCE;
        public final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED;
        public final ForgeConfigSpec.DoubleValue FLYING_SPEED;
        public final ForgeConfigSpec.DoubleValue ATTACK_DAMAGE;
        public final ForgeConfigSpec.DoubleValue ATTACK_KNOCKBACK;
        public final ForgeConfigSpec.DoubleValue ATTACK_SPEED;
        public final ForgeConfigSpec.DoubleValue ARMOR;
        public final ForgeConfigSpec.DoubleValue ARMOR_TOUGHNESS;
        public final ForgeConfigSpec.DoubleValue LUCK;
        public final ForgeConfigSpec.DoubleValue SWIM_SPEED;
        public final ForgeConfigSpec.DoubleValue NAMETAG_DISTANCE;
        public final ForgeConfigSpec.DoubleValue ENTITY_GRAVITY;
        public final ForgeConfigSpec.DoubleValue STEP_HEIGHT;
        public final ForgeConfigSpec.DoubleValue BLOCK_REACH;
        public final ForgeConfigSpec.DoubleValue ENTITY_REACH;

        public  final ForgeConfigSpec.DoubleValue ELVES_FOREST_ATTACK_SPEED;
        public  final ForgeConfigSpec.DoubleValue ELVES_FOREST_MOVEMENT_SPEED;
        public  final ForgeConfigSpec.DoubleValue ELVES_MANA_BONUS;
        public  final ForgeConfigSpec.DoubleValue ELVES_BOW_DAMAGE_BOOST;
        public  final ForgeConfigSpec.DoubleValue ELVES_SCALE_FACTOR;

        public ElvesAttributes(ForgeConfigSpec.Builder builder) {
            builder.comment("精灵族属性配置").push("elves");

            MAX_HEALTH = builder.comment("生命值倍率")
                    .defineInRange("max_health", 0.0, -10.0, 10.0);

            FOLLOW_RANGE = builder.comment("跟随范围倍率")
                    .defineInRange("follow_range", 0.0, -10.0, 10.0);

            KNOCKBACK_RESISTANCE = builder.comment("击退抗性")
                    .defineInRange("knockback_resistance", 0.0, -10.0, 1.0);

            MOVEMENT_SPEED = builder.comment("移动速度倍率")
                    .defineInRange("movement_speed", 0.1, -10.0, 10.0);

            FLYING_SPEED = builder.comment("飞行速度倍率")
                    .defineInRange("flying_speed", 0.0, -10.0, 10.0);

            ATTACK_DAMAGE = builder.comment("攻击伤害倍率")
                    .defineInRange("attack_damage", -0.25, -10.0, 10.0);

            ATTACK_KNOCKBACK = builder.comment("攻击击退力度")
                    .defineInRange("attack_knockback", 0.0, -10.0, 10.0);

            ATTACK_SPEED = builder.comment("攻击速度倍率")
                    .defineInRange("attack_speed", 0.3, -10.0, 10.0);

            ARMOR = builder.comment("护甲值加成")
                    .defineInRange("armor", 0.0, -10.0, 50.0);

            ARMOR_TOUGHNESS = builder.comment("护甲韧性加成")
                    .defineInRange("armor_toughness", 0.0, -10.0, 50.0);

            LUCK = builder.comment("幸运值加成")
                    .defineInRange("luck", 0.0, -10.0, 10.0);

            SWIM_SPEED = builder.comment("游泳速度倍率")
                    .defineInRange("swim_speed", 0.0, -10.0, 10.0);

            NAMETAG_DISTANCE = builder.comment("名称标签显示距离倍率")
                    .defineInRange("nametag_distance", 0.0, -10.0, 10.0);

            ENTITY_GRAVITY = builder.comment("重力影响倍率")
                    .defineInRange("entity_gravity", 0.0, -10.0, 10.0);

            STEP_HEIGHT = builder.comment("步高增加值")
                    .defineInRange("step_height", 0.0, -10.0, 10.0);

            BLOCK_REACH = builder.comment("方块交互距离倍率")
                    .defineInRange("block_reach", 0.0, -10.0, 10.0);

            ENTITY_REACH = builder.comment("实体交互距离倍率")
                    .defineInRange("entity_reach", 0.0, -10.0, 10.0);

            ELVES_FOREST_ATTACK_SPEED = BUILDER.comment("森林中额外攻击速度加成")
                    .defineInRange("forestAttackSpeed", 0.15D, -1.0D, 10.0D);

            ELVES_FOREST_MOVEMENT_SPEED = BUILDER.comment("森林中额外移动速度加成")
                    .defineInRange("forestMovementSpeed", 0.15D, -1.0D, 10.0D);

            ELVES_MANA_BONUS = BUILDER.comment("精灵效果魔力值增加量")
                    .defineInRange("manaBonus", 100.0, 0, 10000);

            ELVES_BOW_DAMAGE_BOOST = BUILDER.comment("潜行时弓箭伤害增加倍率")
                    .defineInRange("bowDamageBoost", 1.5D, 1.0D, 50.0D);

            ELVES_SCALE_FACTOR = BUILDER.comment("精灵族体型缩放 1 为正常体型")
                    .defineInRange("scale", 1.0D, 0.1D, 10.0D);

            builder.pop();
        }
    }

    // 猫族属性配置
    public static class FaelesAttributes {
        public final ForgeConfigSpec.DoubleValue MAX_HEALTH;
        public final ForgeConfigSpec.DoubleValue FOLLOW_RANGE;
        public final ForgeConfigSpec.DoubleValue KNOCKBACK_RESISTANCE;
        public final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED;
        public final ForgeConfigSpec.DoubleValue FLYING_SPEED;
        public final ForgeConfigSpec.DoubleValue ATTACK_DAMAGE;
        public final ForgeConfigSpec.DoubleValue ATTACK_KNOCKBACK;
        public final ForgeConfigSpec.DoubleValue ATTACK_SPEED;
        public final ForgeConfigSpec.DoubleValue ARMOR;
        public final ForgeConfigSpec.DoubleValue ARMOR_TOUGHNESS;
        public final ForgeConfigSpec.DoubleValue LUCK;
        public final ForgeConfigSpec.DoubleValue SWIM_SPEED;
        public final ForgeConfigSpec.DoubleValue NAMETAG_DISTANCE;
        public final ForgeConfigSpec.DoubleValue ENTITY_GRAVITY;
        public final ForgeConfigSpec.DoubleValue STEP_HEIGHT;
        public final ForgeConfigSpec.DoubleValue BLOCK_REACH;
        public final ForgeConfigSpec.DoubleValue ENTITY_REACH;

        public  final ForgeConfigSpec.DoubleValue FAELES_UNARMED_DAMAGE;
        public  final ForgeConfigSpec.DoubleValue FAELES_ARMOR_SPEED_PENALTY;
        public  final ForgeConfigSpec.DoubleValue FAELES_MANA_BONUS;
        public  final ForgeConfigSpec.BooleanValue FAELES_WALL_CLIMB;
        public  final ForgeConfigSpec.DoubleValue FAELES_CLIMB_SPEED;
        public  final ForgeConfigSpec.DoubleValue FAELES_CLIMB_HORIZONTAL_DRAG;
        public  final ForgeConfigSpec.DoubleValue FAELES_SCALE_FACTOR;
        public  final ForgeConfigSpec.DoubleValue FAELES_JUMP_BOOST;

        public FaelesAttributes(ForgeConfigSpec.Builder builder) {
            builder.comment("猫族属性配置").push("faeles");

            MAX_HEALTH = builder.comment("生命值倍率")
                    .defineInRange("max_health", -0.25, -10.0, 10.0);

            FOLLOW_RANGE = builder.comment("跟随范围倍率")
                    .defineInRange("follow_range", 0.0, -10.0, 10.0);

            KNOCKBACK_RESISTANCE = builder.comment("击退抗性")
                    .defineInRange("knockback_resistance", 0.0, -10.0, 1.0);

            MOVEMENT_SPEED = builder.comment("移动速度倍率")
                    .defineInRange("movement_speed", 0.15, -10.0, 10.0);

            FLYING_SPEED = builder.comment("飞行速度倍率")
                    .defineInRange("flying_speed", 0.0, -10.0, 10.0);

            ATTACK_DAMAGE = builder.comment("攻击伤害倍率")
                    .defineInRange("attack_damage", -0.25, -10.0, 10.0);

            ATTACK_KNOCKBACK = builder.comment("攻击击退力度")
                    .defineInRange("attack_knockback", 0.0, -10.0, 10.0);

            ATTACK_SPEED = builder.comment("攻击速度倍率")
                    .defineInRange("attack_speed", 0.15, -10.0, 10.0);

            ARMOR = builder.comment("护甲值加成")
                    .defineInRange("armor", 0.0, -10.0, 50.0);

            ARMOR_TOUGHNESS = builder.comment("护甲韧性加成")
                    .defineInRange("armor_toughness", -0.15, -10.0, 50.0);

            LUCK = builder.comment("幸运值加成")
                    .defineInRange("luck", 2.0, -10.0, 10.0);

            SWIM_SPEED = builder.comment("游泳速度倍率")
                    .defineInRange("swim_speed", 0.3, -10.0, 10.0);

            NAMETAG_DISTANCE = builder.comment("名称标签显示距离倍率")
                    .defineInRange("nametag_distance", 0.0, -10.0, 10.0);

            ENTITY_GRAVITY = builder.comment("重力影响倍率")
                    .defineInRange("entity_gravity", 0.0, -10.0, 10.0);

            STEP_HEIGHT = builder.comment("步高增加值")
                    .defineInRange("step_height", 0.6, -10.0, 10.0);

            BLOCK_REACH = builder.comment("方块交互距离倍率")
                    .defineInRange("block_reach", -0.1, -10.0, 10.0);

            ENTITY_REACH = builder.comment("实体交互距离倍率")
                    .defineInRange("entity_reach", 0.0, -10.0, 10.0);

            FAELES_UNARMED_DAMAGE = BUILDER.comment("空手伤害增加值")
                    .defineInRange("unarmedDamage", 6.0D, -100.0D, 2000.0D);

            FAELES_ARMOR_SPEED_PENALTY = BUILDER.comment("每件非皮革护甲的速度惩罚")
                    .defineInRange("armorSpeedPenalty", -0.04D, -1D, 0.0D);

            FAELES_MANA_BONUS = BUILDER.comment("魔力值增加量")
                    .defineInRange("manaBonus", 25.0, 0, 10000);

            FAELES_JUMP_BOOST = BUILDER.comment("跳跃高度修改值")
                    .defineInRange("jumpStrength", 0.60D, -1.0D, 200.0D);

            FAELES_WALL_CLIMB = BUILDER.comment("猫猫效果是否允许攀爬墙壁")
                    .define("fwallClimb", true);

            FAELES_CLIMB_SPEED = BUILDER.comment("猫猫效果爬墙速度（原版梯子速度约为0.11）")
                    .defineInRange("climbSpeed", 0.11D, 0.05D, 0.3D);

            FAELES_CLIMB_HORIZONTAL_DRAG = BUILDER.comment("猫猫效果爬墙时的水平移动阻力")
                    .defineInRange("climbHorizontalDrag", 0.7D, 0.5D, 1.0D);

            FAELES_SCALE_FACTOR = BUILDER.comment("猫族体型缩放 1 为正常体型")
                    .defineInRange("scale", 0.85D, 0.1D, 10.0D);


            builder.pop();
        }
    }
    // 仙女族属性配置
    public static class FairyAttributes {
        public final ForgeConfigSpec.DoubleValue MAX_HEALTH;
        public final ForgeConfigSpec.DoubleValue FOLLOW_RANGE;
        public final ForgeConfigSpec.DoubleValue KNOCKBACK_RESISTANCE;
        public final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED;
        public final ForgeConfigSpec.DoubleValue FLYING_SPEED;
        public final ForgeConfigSpec.DoubleValue ATTACK_DAMAGE;
        public final ForgeConfigSpec.DoubleValue ATTACK_KNOCKBACK;
        public final ForgeConfigSpec.DoubleValue ATTACK_SPEED;
        public final ForgeConfigSpec.DoubleValue ARMOR;
        public final ForgeConfigSpec.DoubleValue ARMOR_TOUGHNESS;
        public final ForgeConfigSpec.DoubleValue LUCK;
        public final ForgeConfigSpec.DoubleValue SWIM_SPEED;
        public final ForgeConfigSpec.DoubleValue NAMETAG_DISTANCE;
        public final ForgeConfigSpec.DoubleValue ENTITY_GRAVITY;
        public final ForgeConfigSpec.DoubleValue STEP_HEIGHT;
        public final ForgeConfigSpec.DoubleValue BLOCK_REACH;
        public final ForgeConfigSpec.DoubleValue ENTITY_REACH;

        public  final ForgeConfigSpec.DoubleValue FAIRY_DEW_FLIGHT_SPEED;
        public  final ForgeConfigSpec.DoubleValue FAIRY_DEW_MANA_BONUS;
        public  final ForgeConfigSpec.DoubleValue FAIRY_DEW_JUMP_BOOST;
        public  final ForgeConfigSpec.BooleanValue FAIRY_DEW_WALL_CLIMB;
        public  final ForgeConfigSpec.DoubleValue FAIRY_DEW_CLIMB_SPEED;
        public  final ForgeConfigSpec.DoubleValue FAIRY_DEW_CLIMB_HORIZONTAL_DRAG;
        public  final ForgeConfigSpec.DoubleValue FAIRY_DEW_SCALE_FACTOR;

        public FairyAttributes(ForgeConfigSpec.Builder builder) {
            builder.comment("仙女族属性配置").push("fairy");

            MAX_HEALTH = builder.comment("生命值倍率")
                    .defineInRange("max_health", -0.6, -10.0, 10.0);

            FOLLOW_RANGE = builder.comment("跟随范围倍率")
                    .defineInRange("follow_range", 0.0, -10.0, 10.0);

            KNOCKBACK_RESISTANCE = builder.comment("击退抗性")
                    .defineInRange("knockback_resistance", 0.0, -10.0, 1.0);

            MOVEMENT_SPEED = builder.comment("移动速度倍率")
                    .defineInRange("movement_speed", -0.25, -10.0, 10.0);

            FLYING_SPEED = builder.comment("飞行速度倍率")
                    .defineInRange("flying_speed", 0.0, -10.0, 10.0);

            ATTACK_DAMAGE = builder.comment("攻击伤害倍率")
                    .defineInRange("attack_damage", -0.75, -10.0, 10.0);

            ATTACK_KNOCKBACK = builder.comment("攻击击退力度")
                    .defineInRange("attack_knockback", 0.0, -10.0, 10.0);

            ATTACK_SPEED = builder.comment("攻击速度倍率")
                    .defineInRange("attack_speed", 0.0, -10.0, 10.0);

            ARMOR = builder.comment("护甲值加成")
                    .defineInRange("armor", -0.5, -10.0, 50.0);

            ARMOR_TOUGHNESS = builder.comment("护甲韧性加成")
                    .defineInRange("armor_toughness", -0.25, -10.0, 50.0);

            LUCK = builder.comment("幸运值加成")
                    .defineInRange("luck", 0.0, -10.0, 10.0);

            SWIM_SPEED = builder.comment("游泳速度倍率")
                    .defineInRange("swim_speed", -0.25, -10.0, 10.0);

            NAMETAG_DISTANCE = builder.comment("名称标签显示距离倍率")
                    .defineInRange("nametag_distance", 0.0, -10.0, 10.0);

            ENTITY_GRAVITY = builder.comment("重力影响倍率")
                    .defineInRange("entity_gravity", 0.0, -10.0, 10.0);

            STEP_HEIGHT = builder.comment("步高增加值")
                    .defineInRange("step_height", -0.35, -10.0, 10.0);

            BLOCK_REACH = builder.comment("方块交互距离倍率")
                    .defineInRange("block_reach", -0.35, -10.0, 10.0);

            ENTITY_REACH = builder.comment("实体交互距离倍率")
                    .defineInRange("entity_reach", 0.0, -10.0, 10.0);

            FAIRY_DEW_FLIGHT_SPEED = BUILDER
                    .comment("飞行速度倍率")
                    .defineInRange("flightSpeed", 0.3D, -1.0D, 2.0D);

            FAIRY_DEW_MANA_BONUS = BUILDER.comment("魔力值增加量")
                    .defineInRange("manaBonus", 400.0, 0, 20000);

            FAIRY_DEW_JUMP_BOOST = BUILDER.comment("跳跃重力修改值")
                    .defineInRange("jumpStrength", -0.25D, -1.0D, 200.0D);

            FAIRY_DEW_WALL_CLIMB = BUILDER.comment("仙女效果是否允许攀爬墙壁")
                    .define("wallClimb", true);

            FAIRY_DEW_CLIMB_SPEED = BUILDER.comment("仙女效果爬墙速度（原版梯子速度约为0.11）")
                    .defineInRange("climbSpeed", 0.11D, 0.05D, 0.3D);

            FAIRY_DEW_CLIMB_HORIZONTAL_DRAG = BUILDER.comment("仙女效果爬墙时的水平移动阻力")
                    .defineInRange("climbHorizontalDrag", 0.7D, 0.5D, 1.0D);

            FAIRY_DEW_SCALE_FACTOR = BUILDER.comment("仙女族体型缩放 1 为正常体型")
                    .defineInRange("scale", 0.25D, 0.1D, 10.0D);


            builder.pop();
        }
    }

    // 哥布林族属性配置
    public static class GoblinsAttributes {
        public final ForgeConfigSpec.DoubleValue MAX_HEALTH;
        public final ForgeConfigSpec.DoubleValue FOLLOW_RANGE;
        public final ForgeConfigSpec.DoubleValue KNOCKBACK_RESISTANCE;
        public final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED;
        public final ForgeConfigSpec.DoubleValue FLYING_SPEED;
        public final ForgeConfigSpec.DoubleValue ATTACK_DAMAGE;
        public final ForgeConfigSpec.DoubleValue ATTACK_KNOCKBACK;
        public final ForgeConfigSpec.DoubleValue ATTACK_SPEED;
        public final ForgeConfigSpec.DoubleValue ARMOR;
        public final ForgeConfigSpec.DoubleValue ARMOR_TOUGHNESS;
        public final ForgeConfigSpec.DoubleValue LUCK;
        public final ForgeConfigSpec.DoubleValue SWIM_SPEED;
        public final ForgeConfigSpec.DoubleValue NAMETAG_DISTANCE;
        public final ForgeConfigSpec.DoubleValue ENTITY_GRAVITY;
        public final ForgeConfigSpec.DoubleValue STEP_HEIGHT;
        public final ForgeConfigSpec.DoubleValue BLOCK_REACH;
        public final ForgeConfigSpec.DoubleValue ENTITY_REACH;

        public  final ForgeConfigSpec.DoubleValue GOBLIN_MANA_PENALTY;
        public  final ForgeConfigSpec.DoubleValue GOBLIN_DAMAGE_REDUCTION;
        public  final ForgeConfigSpec.DoubleValue GOBLIN_SCALE_FACTOR;

        public GoblinsAttributes(ForgeConfigSpec.Builder builder) {
            builder.comment("哥布林族属性配置").push("goblins");

            MAX_HEALTH = builder.comment("生命值倍率")
                    .defineInRange("max_health", -0.4, -10.0, 10.0);

            FOLLOW_RANGE = builder.comment("跟随范围倍率")
                    .defineInRange("follow_range", 0.0, -10.0, 10.0);

            KNOCKBACK_RESISTANCE = builder.comment("击退抗性")
                    .defineInRange("knockback_resistance", 0.0, -10.0, 1.0);

            MOVEMENT_SPEED = builder.comment("移动速度倍率")
                    .defineInRange("movement_speed", 0.2, -10.0, 10.0);

            FLYING_SPEED = builder.comment("飞行速度倍率")
                    .defineInRange("flying_speed", 0.0, -10.0, 10.0);

            ATTACK_DAMAGE = builder.comment("攻击伤害倍率")
                    .defineInRange("attack_damage", -0.5, -10.0, 10.0);

            ATTACK_KNOCKBACK = builder.comment("攻击击退力度")
                    .defineInRange("attack_knockback", 0.0, -10.0, 10.0);

            ATTACK_SPEED = builder.comment("攻击速度倍率")
                    .defineInRange("attack_speed", 0.0, -10.0, 10.0);

            ARMOR = builder.comment("护甲值加成")
                    .defineInRange("armor", 0.0, -10.0, 50.0);

            ARMOR_TOUGHNESS = builder.comment("护甲韧性加成")
                    .defineInRange("armor_toughness", 0.0, -10.0, 50.0);

            LUCK = builder.comment("幸运值加成")
                    .defineInRange("luck", 1.0, -10.0, 10.0);

            SWIM_SPEED = builder.comment("游泳速度倍率")
                    .defineInRange("swim_speed", 0.1, -10.0, 10.0);

            NAMETAG_DISTANCE = builder.comment("名称标签显示距离倍率")
                    .defineInRange("nametag_distance", 0.0, -10.0, 10.0);

            ENTITY_GRAVITY = builder.comment("重力影响倍率")
                    .defineInRange("entity_gravity", 0.0, -10.0, 10.0);

            STEP_HEIGHT = builder.comment("步高增加值")
                    .defineInRange("step_height", 0.0, -10.0, 10.0);

            BLOCK_REACH = builder.comment("方块交互距离倍率")
                    .defineInRange("block_reach", 0.0, -10.0, 10.0);

            ENTITY_REACH = builder.comment("实体交互距离倍率")
                    .defineInRange("entity_reach", 0.0, -10.0, 10.0);

            GOBLIN_MANA_PENALTY = BUILDER.comment("魔力值修改量")
                    .defineInRange("manaPenalty", -25.0, -10000, 10000);

            GOBLIN_DAMAGE_REDUCTION = BUILDER.comment("火焰和爆炸伤害减免比例")
                    .defineInRange("damageReduction", 0.75D, 0.0D, 1.0D);

            GOBLIN_SCALE_FACTOR = BUILDER.comment("哥布林族体型缩放 1 为正常体型")
                    .defineInRange("scale", 0.5D, 0.1D, 10.0D);


            builder.pop();
        }
    }

    // 泰坦族属性配置
    public static class TitanAttributes {
        public final ForgeConfigSpec.DoubleValue MAX_HEALTH;
        public final ForgeConfigSpec.DoubleValue FOLLOW_RANGE;
        public final ForgeConfigSpec.DoubleValue KNOCKBACK_RESISTANCE;
        public final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED;
        public final ForgeConfigSpec.DoubleValue FLYING_SPEED;
        public final ForgeConfigSpec.DoubleValue ATTACK_DAMAGE;
        public final ForgeConfigSpec.DoubleValue ATTACK_KNOCKBACK;
        public final ForgeConfigSpec.DoubleValue ATTACK_SPEED;
        public final ForgeConfigSpec.DoubleValue ARMOR;
        public final ForgeConfigSpec.DoubleValue ARMOR_TOUGHNESS;
        public final ForgeConfigSpec.DoubleValue LUCK;
        public final ForgeConfigSpec.DoubleValue SWIM_SPEED;
        public final ForgeConfigSpec.DoubleValue NAMETAG_DISTANCE;
        public final ForgeConfigSpec.DoubleValue ENTITY_GRAVITY;
        public final ForgeConfigSpec.DoubleValue STEP_HEIGHT;
        public final ForgeConfigSpec.DoubleValue BLOCK_REACH;
        public final ForgeConfigSpec.DoubleValue ENTITY_REACH;

        public  final ForgeConfigSpec.DoubleValue TITAN_JUMP_BOOST;
        public  final ForgeConfigSpec.DoubleValue TITAN_MANA_MODIFIER;
        public  final ForgeConfigSpec.DoubleValue TITAN_WATER_SINK_SPEED;
        public  final ForgeConfigSpec.DoubleValue TITAN_SCALE_FACTOR;

        public TitanAttributes(ForgeConfigSpec.Builder builder) {
            builder.comment("泰坦族属性配置").push("titan");

            MAX_HEALTH = builder.comment("生命值倍率")
                    .defineInRange("max_health", 1.0, -10.0, 10.0);

            FOLLOW_RANGE = builder.comment("跟随范围倍率")
                    .defineInRange("follow_range", 0.0, -10.0, 10.0);

            KNOCKBACK_RESISTANCE = builder.comment("击退抗性")
                    .defineInRange("knockback_resistance", 1.0, -10.0, 1.0);

            MOVEMENT_SPEED = builder.comment("移动速度倍率")
                    .defineInRange("movement_speed", 0.0, -10.0, 10.0);

            FLYING_SPEED = builder.comment("飞行速度倍率")
                    .defineInRange("flying_speed", 0.0, -10.0, 10.0);

            ATTACK_DAMAGE = builder.comment("攻击伤害倍率")
                    .defineInRange("attack_damage", 0.5, -10.0, 10.0);

            ATTACK_KNOCKBACK = builder.comment("攻击击退力度")
                    .defineInRange("attack_knockback", 0.0, -10.0, 10.0);

            ATTACK_SPEED = builder.comment("攻击速度倍率")
                    .defineInRange("attack_speed", -0.5, -10.0, 10.0);

            ARMOR = builder.comment("护甲值加成")
                    .defineInRange("armor", 0.0, -10.0, 50.0);

            ARMOR_TOUGHNESS = builder.comment("护甲韧性加成")
                    .defineInRange("armor_toughness", 0.0, -10.0, 50.0);

            LUCK = builder.comment("幸运值加成")
                    .defineInRange("luck", 0.0, -10.0, 10.0);

            SWIM_SPEED = builder.comment("游泳速度倍率")
                    .defineInRange("swim_speed", 0.0, -10.0, 10.0);

            NAMETAG_DISTANCE = builder.comment("名称标签显示距离倍率")
                    .defineInRange("nametag_distance", 0.0, -10.0, 10.0);

            ENTITY_GRAVITY = builder.comment("重力影响倍率")
                    .defineInRange("entity_gravity", 0.0, -10.0, 10.0);

            STEP_HEIGHT = builder.comment("步高增加值")
                    .defineInRange("step_height", 1.4, -10.0, 10.0);

            BLOCK_REACH = builder.comment("方块交互距离倍率")
                    .defineInRange("block_reach", 1.0, -10.0, 10.0);

            ENTITY_REACH = builder.comment("实体交互距离倍率")
                    .defineInRange("entity_reach", 0.0, -10.0, 10.0);

            TITAN_JUMP_BOOST = BUILDER.comment("跳跃力量修改值")
                    .defineInRange("jumpStrength", 0.75D, -1.0D, 2.0D);

            TITAN_MANA_MODIFIER = BUILDER.comment("魔力值修改量")
                    .defineInRange("manaModifier", -50.0, -10000, 10000);

            TITAN_WATER_SINK_SPEED = BUILDER.comment("水中下沉速度")
                    .defineInRange("waterSinkSpeed", 0.1D, 0.0D, 1.0D);

            TITAN_SCALE_FACTOR = BUILDER.comment("泰坦族体型缩放 1 为正常体型")
                    .defineInRange("scale", 3.0D, 0.1D, 10.0D);

            builder.pop();
        }
    }

    // 为每个种族创建配置实例
    public static final DwarvesAttributes DWARVES;
    public static final DragonAttributes DRAGON;
    public static final ElvesAttributes ELVES;
    public static final FaelesAttributes FAELES;
    public static final FairyAttributes FAIRY;
    public static final GoblinsAttributes GOBLINS;
    public static final TitanAttributes TITAN;

    static {
        BUILDER.comment("种族属性配置系统").push("races");

        // 初始化每个种族的属性配置
        DWARVES = new DwarvesAttributes(BUILDER);
        DRAGON = new DragonAttributes(BUILDER);
        ELVES = new ElvesAttributes(BUILDER);
        FAELES = new FaelesAttributes(BUILDER);
        FAIRY = new FairyAttributes(BUILDER);
        GOBLINS = new GoblinsAttributes(BUILDER);
        TITAN = new TitanAttributes(BUILDER);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}