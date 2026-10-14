package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.api.ITaurusCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * 金牛族能力实现。
 *
 * <p>坦克型种族，属性为：+50% 血量、+0.5 击退抗性、+20% 护甲、
 * +30% 护甲韧性、+20% 攻击、-10% 移速、-15% 游速，体型 1.2。特有「踏击」——朝视线方向冲刺，
 * 撞到的敌人受伤害并被击退。
 */
public class TaurusCapability extends AbstractRaceCapability implements ITaurusCapability
{
    public TaurusCapability(Player player)
    {
        super(player);
        this.scaleFactor = RaceAttributesConfig.TAURUS.TAURUS_SCALE_FACTOR.get().floatValue();
    }

    @Override
    protected void registerAttributeValues()
    {
        registerValue("MAX_HEALTH", RaceAttributesConfig.TAURUS.MAX_HEALTH::get);
        registerValue("FOLLOW_RANGE", RaceAttributesConfig.TAURUS.FOLLOW_RANGE::get);
        registerValue("KNOCKBACK_RESISTANCE", RaceAttributesConfig.TAURUS.KNOCKBACK_RESISTANCE::get);
        registerValue("MOVEMENT_SPEED", RaceAttributesConfig.TAURUS.MOVEMENT_SPEED::get);
        registerValue("FLYING_SPEED", RaceAttributesConfig.TAURUS.FLYING_SPEED::get);
        registerValue("ATTACK_DAMAGE", RaceAttributesConfig.TAURUS.ATTACK_DAMAGE::get);
        registerValue("ATTACK_KNOCKBACK", RaceAttributesConfig.TAURUS.ATTACK_KNOCKBACK::get);
        registerValue("ATTACK_SPEED", RaceAttributesConfig.TAURUS.ATTACK_SPEED::get);
        registerValue("ARMOR", RaceAttributesConfig.TAURUS.ARMOR::get);
        registerValue("ARMOR_TOUGHNESS", RaceAttributesConfig.TAURUS.ARMOR_TOUGHNESS::get);
        registerValue("LUCK", RaceAttributesConfig.TAURUS.LUCK::get);
        registerValue("SWIM_SPEED", RaceAttributesConfig.TAURUS.SWIM_SPEED::get);
        registerValue("NAMETAG_DISTANCE", RaceAttributesConfig.TAURUS.NAMETAG_DISTANCE::get);
        registerValue("ENTITY_GRAVITY", RaceAttributesConfig.TAURUS.ENTITY_GRAVITY::get);
        registerValue("STEP_HEIGHT", RaceAttributesConfig.TAURUS.STEP_HEIGHT::get);
        registerValue("BLOCK_REACH", RaceAttributesConfig.TAURUS.BLOCK_REACH::get);
        registerValue("ENTITY_REACH", RaceAttributesConfig.TAURUS.ENTITY_REACH::get);
    }

    @Override
    public String getRaceId()
    {
        return "taurus";
    }

    @Override
    public String getRaceName()
    {
        return "TAURUS";
    }

    @Override
    public float getManaBonus()
    {
        return RaceAttributesConfig.TAURUS.TAURUS_MANA_BONUS.get().floatValue();
    }

    @Override
    protected void onTick()
    {
        if (!isActive)
        {
            return;
        }

        player.addEffect(new MobEffectInstance(
                ModEffects.TAURUS.get(),
                30, 0, false, false, false
        ));
    }

    /**
     * 踏击（冲撞）：沿视线方向冲刺，路径上撞到的敌人受伤并被击退。
     *
     * <p>{@code charge} 为蓄力比例 0~1，curve = charge²；伤害 =
     * 1 + (maxDamage-1)*curve；击退 = 0.6/0.3 * curve。命中范围是「当前碰撞箱 + 本帧冲刺位移」再扩
     * 0.5，即只撞到正前方紧邻的敌人。
     */
    @Override
    public void handleStampede(float charge)
    {
        if (!isActive || player.level().isClientSide)
        {
            return;
        }

        double clamped = Math.max(0.0, Math.min(1.0, charge));
        double curve = clamped * clamped;

        // 最低蓄力门槛：蓄力不足 10% 不冲刺。
        if (clamped < 0.1)
        {
            return;
        }

        // 魔力消耗：蓄力越久花得越多（cost * charge）。魔力不足则不冲刺。
        float manaCost = RaceAttributesConfig.TAURUS.TAURUS_STAMPEDE_MANA_COST.get().floatValue() * (float) clamped;
        if (!ManaData.hasMana(player, manaCost))
        {
            return;
        }
        ManaData.consumeMana(player, manaCost);

        double lookX = player.getLookAngle().x;
        double lookZ = player.getLookAngle().z;
        double horizLen = Math.sqrt(lookX * lookX + lookZ * lookZ);
        double hx, hz;
        if (horizLen < 1.0E-4)
        {
            hx = 1.0;
            hz = 0.0;
        }
        else
        {
            hx = lookX / horizLen;
            hz = lookZ / horizLen;
        }

        double minVel = RaceAttributesConfig.TAURUS.TAURUS_STAMPEDE_VELOCITY_MIN.get();
        double maxVel = RaceAttributesConfig.TAURUS.TAURUS_STAMPEDE_VELOCITY_MAX.get();
        // 空中冲刺速度打折（落地 ×1.0，空中 ×0.25）。
        double groundMultiplier = player.onGround() ? 1.0 : 0.25;
        double velocity = (minVel + (maxVel - minVel) * curve) * groundMultiplier;
        double dx = hx * velocity;
        double dz = hz * velocity;

        // 命中范围：地碰撞箱 union 位移后碰撞箱，再扩 0.5。
        AABB swept = player.getBoundingBox()
                .minmax(player.getBoundingBox().move(dx, 0.0, dz))
                .inflate(0.5);

        boolean pvp = player.getServer() != null && player.getServer().isPvpAllowed();
        float maxDamage = RaceAttributesConfig.TAURUS.TAURUS_STAMPEDE_DAMAGE.get().floatValue();
        float damage = (float) (1.0 + (maxDamage - 1.0) * curve);
        Set<Entity> hit = new HashSet<>();

        for (Entity target : player.level().getEntities(player, swept))
        {
            if (target == player || target == player.getVehicle())
            {
                continue;
            }
            if (!canHit(player, target))
            {
                continue;
            }
            if (target instanceof Player && !pvp)
            {
                continue;
            }
            if (!hit.add(target))
            {
                continue;
            }
            target.hurt(player.damageSources().playerAttack(player), damage);
            // 击退方向：用完整视线方向归一化（含俯仰），不是水平方向。
            Vec3 knock = player.getLookAngle().normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(
                    knock.x * (0.6 * curve), 0.3 * curve, knock.z * (0.6 * curve)));
            target.hasImpulse = true;
        }
    }

    /**
     * 目标是否可被踏击命中：存活、同世界、非挂墙实体，且从玩家眼睛到目标中心的
     * 视线不被方块遮挡。
     */
    private static boolean canHit(LivingEntity attacker, Entity target)
    {
        if (!target.isAlive() || target.level() != attacker.level() || target instanceof HangingEntity)
        {
            return false;
        }
        Vec3 start = attacker.getEyePosition();
        Vec3 end = target.getBoundingBox().getCenter();
        return attacker.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, attacker)).getType() == HitResult.Type.MISS;
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level)
    {
        // 金牛无破坏方块的种族特效。
    }

    @Override
    protected int defaultPrimaryColor()
    {
        return 0x141234;
    }

    @Override
    protected int defaultSecondaryColor()
    {
        return 0x2C2C2C;
    }

    @Override
    protected int defaultAuxColor()
    {
        return 0xFFE77C;
    }
}