package com.jinqinxixi.trinketsandbaubles.capability.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 属性注册表类,集中管理所有属性的UUID和操作类型
 */
public class AttributeRegistry {
    public static final class AttributeEntry {
        final Attribute attribute;
        final UUID uuid;
        final AttributeModifier.Operation operation;
        final boolean isPercentage;
        final String translationKey;

        public AttributeEntry(Attribute attribute, UUID uuid,
                              AttributeModifier.Operation operation,
                              boolean isPercentage,
                              String translationKey) {
            this.attribute = attribute;
            this.uuid = uuid;
            this.operation = operation;
            this.isPercentage = isPercentage;
            this.translationKey = translationKey;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public UUID getUuid() {
            return uuid;
        }

        public AttributeModifier.Operation getOperation() {
            return operation;
        }

        public boolean isPercentage() {
            return isPercentage;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }

    private static final Map<String, AttributeEntry> ATTRIBUTES = new HashMap<>();

    static {
        // ====== 百分比修改的属性 (使用 MULTIPLY_TOTAL) ======
        // 最大生命值
        register("MAX_HEALTH", Attributes.MAX_HEALTH,
                UUID.fromString("dc3b4b8c-a02c-4bd8-82e9-204088927d1f"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.max_health");

        // 跟随范围
        register("FOLLOW_RANGE", Attributes.FOLLOW_RANGE,
                UUID.fromString("1d85e342-5786-4c72-b01d-8c65525702b2"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.follow_range");

        // 移动速度
        register("MOVEMENT_SPEED", Attributes.MOVEMENT_SPEED,
                UUID.fromString("3b8f4065-5f43-4939-8e6a-a34f2d67c55d"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.movement_speed");

        // 攻击速度
        register("ATTACK_SPEED", Attributes.ATTACK_SPEED,
                UUID.fromString("4520f278-fb8f-4c75-9336-5c3ab7c6134a"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.attack_speed");

        // 攻击伤害
        register("ATTACK_DAMAGE", Attributes.ATTACK_DAMAGE,
                UUID.fromString("d141ef28-51c6-4b47-8a0d-6946e841c132"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.attack_damage");

        // 游泳速度
        register("SWIM_SPEED", ForgeMod.SWIM_SPEED.get(),
                UUID.fromString("7a925a64-d1e0-4cb9-8926-dd7848482bb4"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.swim_speed");

        // 飞行速度
        register("FLYING_SPEED", Attributes.FLYING_SPEED,
                UUID.fromString("b2c5e342-5786-4c72-b01d-8c65525702b2"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.flying_speed");

        // 实体重力
        register("ENTITY_GRAVITY", ForgeMod.ENTITY_GRAVITY.get(),
                UUID.fromString("1d85e342-5786-4c72-b01d-8c65525702b2"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.entity_gravity");

        // 方块交互距离
        register("BLOCK_REACH", ForgeMod.BLOCK_REACH.get(),
                UUID.fromString("d74f3a1c-89b2-4b3e-bf8e-6d24d8c9517d"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.block_reach");

        // 实体交互距离
        register("ENTITY_REACH", ForgeMod.ENTITY_REACH.get(),
                UUID.fromString("b2c5e342-5786-4c72-b01d-8c65525702b2"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.entity_reach");

        // 名称标签显示距离
        register("NAMETAG_DISTANCE", ForgeMod.NAMETAG_DISTANCE.get(),
                UUID.fromString("9cd3e438-8c5c-4ab2-9845-9a1e3c4a2242"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.nametag_distance");

        // 护甲值
        register("ARMOR", Attributes.ARMOR,
                UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.armor");

        // 护甲韧性
        register("ARMOR_TOUGHNESS", Attributes.ARMOR_TOUGHNESS,
                UUID.fromString("8fc5e73c-2cf2-4729-8128-d99f49aa37f2"),
                AttributeModifier.Operation.MULTIPLY_TOTAL, true,
                "attribute.trinketsandbaubles.armor_toughness");

        // ====== 固定数值修改的属性 (使用 ADDITION) ======
        // 击退抗性
        register("KNOCKBACK_RESISTANCE", Attributes.KNOCKBACK_RESISTANCE,
                UUID.fromString("95eb4f0a-dd60-4ada-98c1-2ce5c3d4374c"),
                AttributeModifier.Operation.ADDITION, true,
                "attribute.trinketsandbaubles.knockback_resistance");

        // 攻击击退
        register("ATTACK_KNOCKBACK", Attributes.ATTACK_KNOCKBACK,
                UUID.fromString("c73e3438-8c5c-4ab2-9845-9a1e3c4a2242"),
                AttributeModifier.Operation.ADDITION, false,
                "attribute.trinketsandbaubles.attack_knockback");

        // 幸运值
        register("LUCK", Attributes.LUCK,
                UUID.fromString("4f76ff37-dd94-4dd8-95e3-3d447fafc4b1"),
                AttributeModifier.Operation.ADDITION, false,
                "attribute.trinketsandbaubles.luck");

        // 步高增加
        register("STEP_HEIGHT", ForgeMod.STEP_HEIGHT_ADDITION.get(),
                UUID.fromString("e8c9a6f5-4376-4e7b-9a5c-8f2e3d91d7c4"),
                AttributeModifier.Operation.ADDITION, false,
                "attribute.trinketsandbaubles.step_height");
    }

    private static void register(String name, Attribute attribute, UUID uuid,
                                 AttributeModifier.Operation operation,
                                 boolean isPercentage, String translationKey) {
        ATTRIBUTES.put(name, new AttributeEntry(attribute, uuid, operation, isPercentage, translationKey));
    }

    public static AttributeEntry get(String name) {
        return ATTRIBUTES.get(name);
    }

    public static Map<String, AttributeEntry> getAll() {
        return ATTRIBUTES;
    }
}