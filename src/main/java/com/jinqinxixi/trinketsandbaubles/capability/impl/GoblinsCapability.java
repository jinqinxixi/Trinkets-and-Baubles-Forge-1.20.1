package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.api.IGoblinsCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.entity.AlphaWolf;
import com.jinqinxixi.trinketsandbaubles.init.ModEntities;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;

public class GoblinsCapability extends AbstractRaceCapability implements IGoblinsCapability {

    private static final double MOUNT_REACH = 5.0;
    private int wolfAttackCooldown = 0;

    public GoblinsCapability(Player player) {
        super(player);
        this.scaleFactor = RaceAttributesConfig.GOBLINS.GOBLIN_SCALE_FACTOR.get().floatValue();
    }

    @Override
    protected void registerAttributeValues() {
        registerValue("MAX_HEALTH", RaceAttributesConfig.GOBLINS.MAX_HEALTH::get);
        registerValue("FOLLOW_RANGE", RaceAttributesConfig.GOBLINS.FOLLOW_RANGE::get);
        registerValue("KNOCKBACK_RESISTANCE", RaceAttributesConfig.GOBLINS.KNOCKBACK_RESISTANCE::get);
        registerValue("MOVEMENT_SPEED", RaceAttributesConfig.GOBLINS.MOVEMENT_SPEED::get);
        registerValue("FLYING_SPEED", RaceAttributesConfig.GOBLINS.FLYING_SPEED::get);
        registerValue("ATTACK_DAMAGE", RaceAttributesConfig.GOBLINS.ATTACK_DAMAGE::get);
        registerValue("ATTACK_KNOCKBACK", RaceAttributesConfig.GOBLINS.ATTACK_KNOCKBACK::get);
        registerValue("ATTACK_SPEED", RaceAttributesConfig.GOBLINS.ATTACK_SPEED::get);
        registerValue("ARMOR", RaceAttributesConfig.GOBLINS.ARMOR::get);
        registerValue("ARMOR_TOUGHNESS", RaceAttributesConfig.GOBLINS.ARMOR_TOUGHNESS::get);
        registerValue("LUCK", RaceAttributesConfig.GOBLINS.LUCK::get);
        registerValue("SWIM_SPEED", RaceAttributesConfig.GOBLINS.SWIM_SPEED::get);
        registerValue("NAMETAG_DISTANCE", RaceAttributesConfig.GOBLINS.NAMETAG_DISTANCE::get);
        registerValue("ENTITY_GRAVITY", RaceAttributesConfig.GOBLINS.ENTITY_GRAVITY::get);
        registerValue("STEP_HEIGHT", RaceAttributesConfig.GOBLINS.STEP_HEIGHT::get);
        registerValue("BLOCK_REACH", RaceAttributesConfig.GOBLINS.BLOCK_REACH::get);
        registerValue("ENTITY_REACH", RaceAttributesConfig.GOBLINS.ENTITY_REACH::get);
    }

    @Override
    public String getRaceName() {
        return "GOBLINS";
    }

    @Override
    public String getRaceId() {
        return "goblins";
    }

    @Override
    public float getManaBonus() {
        return RaceAttributesConfig.GOBLINS.GOBLIN_MANA_PENALTY.get().floatValue();
    }

    @Override
    protected void onTick() {
        if (!isActive) return;

        player.addEffect(new MobEffectInstance(
                ModEffects.GOBLINS.get(),
                30, 0, false, false, false
        ));

        if (wolfAttackCooldown > 0) {
            wolfAttackCooldown--;
        }
    }

    /**
     * 按键处理：骑在 AlphaWolf 上时冲锋攻击；否则对准驯服狼骑上去。
     */
    @Override
    public void handleWolfRiderKey() {
        if (!isActive || player.level().isClientSide) {
            return;
        }

        // 已骑在 AlphaWolf 上：冲锋攻击
        if (player.getVehicle() instanceof AlphaWolf wolf) {
            if (wolfAttackCooldown <= 0) {
                wolf.mountedAttack(player, MOUNT_REACH);
                wolfAttackCooldown = 40;
            }
            return;
        }

        // 未骑乘：对准驯服狼骑上去
        if (!player.isPassenger()) {
            raytraceWolf().ifPresent(this::mountWolf);
        }
    }

    /**
     * 把驯服狼替换成可骑乘的 AlphaWolf 并骑上去。
     */
    @Override
    public boolean mountWolf(Wolf wolf) {
        if (wolf == null || wolf.level().isClientSide) {
            return false;
        }

        if (!wolf.isTame() || !wolf.isOwnedBy(player) || wolf.isBaby() || !wolf.isAlive()
                || player.isPassenger()) {
            return false;
        }

        AlphaWolf alpha = ModEntities.ALPHA_WOLF.get().create(wolf.level());
        if (alpha == null) {
            return false;
        }

        alpha.setCustomName(wolf.getCustomName());
        alpha.moveTo(wolf.getX(), wolf.getY(), wolf.getZ(), wolf.getYRot(), 0.0F);
        alpha.tame(player);
        alpha.setHealth(wolf.getHealth());

        if (!wolf.level().addFreshEntity(alpha)) {
            return false;
        }

        if (player.startRiding(alpha)) {
            alpha.storeOldWolf(wolf);
            wolf.discard();
            return true;
        } else {
            alpha.discard();
            return false;
        }
    }

    /**
     * 找玩家视线对准的驯服狼。
     */
    private Optional<Wolf> raytraceWolf() {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        for (double d = 0.5; d <= MOUNT_REACH; d += 0.5) {
            Vec3 point = eye.add(look.x * d, look.y * d, look.z * d);
            AABB box = new AABB(point.x - 0.5, point.y - 0.5, point.z - 0.5,
                    point.x + 0.5, point.y + 0.5, point.z + 0.5);

            Optional<Wolf> found = player.level().getEntitiesOfClass(Wolf.class, box,
                            w -> w.isTame() && w.isOwnedBy(player))
                    .stream()
                    .min(Comparator.comparingDouble(w -> w.distanceToSqr(player)));
            if (found.isPresent()) {
                return found;
            }
        }

        return Optional.empty();
    }

    public float handleDamage(float damage, boolean isFireOrExplosion) {
        if (isActive && isFireOrExplosion) {
            return damage * RaceAttributesConfig.GOBLINS.GOBLIN_DAMAGE_REDUCTION.get().floatValue();
        }
        return damage;
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        // 哥布林不需要特殊的破坏方块逻辑
    }

    @Override
    protected int defaultPrimaryColor() {
        return 0x648664;
    }

    @Override
    protected int defaultSecondaryColor() {
        return 0x2F3F2F;
    }

    @Override
    protected int defaultAuxColor() {
        return 0x648664;
    }
}