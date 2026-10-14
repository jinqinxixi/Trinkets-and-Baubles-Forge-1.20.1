package com.jinqinxixi.trinketsandbaubles.entity;

import com.jinqinxixi.trinketsandbaubles.mixin.LivingEntityAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * A wolf a goblin can ride.
 *
 * <p>Built by replacing a tame wolf the player is looking at: the wolf is stored, this takes its
 * place, and the player rides it. On dismount the stored wolf is restored, so the player's pet is
 * not lost. The wolf is taller, hits harder, shares the rider's buffs, and can lunge forward in an
 * attack while mounted.
 */
public class AlphaWolf extends Wolf
{
    private static final String STORED_WOLF_TAG = "trinketsandbaubles.wolf.stored";
    private static final UUID ATTACK_BONUS_UUID = UUID.fromString("76c436ad-d830-48ff-8b3c-fa3bcc1891c2");
    private static final int RETALIATION_COOLDOWN_TICKS = 40;
    private static final double RETALIATION_RANGE_SQUARED = 25.0;
    private static final float RETALIATION_DAMAGE = 9.0F;

    @Nullable
    private CompoundTag storedWolf;
    private int retaliationCooldown;

    /** 上一帧骑手是否按着跳跃键，用于检测按下沿（避免持续按住连续跳）。 */
    @Unique
    private boolean trinketsandbaubles$wasJumping;

    public AlphaWolf(EntityType<? extends Wolf> type, Level level)
    {
        super(type, level);
        this.setMaxUpStep(1.0F);
        AttributeInstance attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null && attack.getModifier(ATTACK_BONUS_UUID) == null)
        {
            attack.addPermanentModifier(new AttributeModifier(
                    ATTACK_BONUS_UUID, "trinketsandbaubles.alpha_wolf.attack", 4.0,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    /**
     * No goals: this wolf is a mount, not a hunter.
     */
    @Override
    protected void registerGoals()
    {
    }

    @Override
    public void tick()
    {
        super.tick();
        if (!this.level().isClientSide)
        {
            if (this.retaliationCooldown > 0)
            {
                this.retaliationCooldown--;
            }

            LivingEntity driver = this.getControllingPassenger();
            if (driver != null)
            {
                this.shareBuffs(driver);
            }

            // 下骑后主人不再骑在背上（乘客里没有主人），就销毁这个替身狼，由 remove() 里的
            // restoreStoredWolf() 把原狼（带 AI）恢复出来。
            if (this.tickCount > 1 && !this.getPassengers().contains(this.getOwner()))
            {
                this.discard();
            }
        }
    }

    private void shareBuffs(LivingEntity driver)
    {
        if (!driver.hasEffect(MobEffects.DAMAGE_BOOST))
        {
            driver.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0, false, false));
        }

        if (!driver.hasEffect(MobEffects.REGENERATION))
        {
            driver.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false));
        }

        if (!this.hasEffect(MobEffects.REGENERATION))
        {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false));
        }

        MobEffectInstance fireResistance = driver.getEffect(MobEffects.FIRE_RESISTANCE);
        if (fireResistance != null)
        {
            this.addEffect(new MobEffectInstance(fireResistance));
        }

        MobEffectInstance invisibility = driver.getEffect(MobEffects.INVISIBILITY);
        if (invisibility != null)
        {
            this.addEffect(new MobEffectInstance(invisibility));
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason)
    {
        if (!this.level().isClientSide && reason.shouldDestroy())
        {
            this.restoreStoredWolf();
        }

        super.remove(reason);
    }

    private void restoreStoredWolf()
    {
        if (this.hasStoredWolf() && this.level() instanceof ServerLevel level)
        {
            EntityType.create(this.storedWolf, level).ifPresent(wolf ->
            {
                wolf.moveTo(this.getX(), this.getY() + 1.1, this.getZ(), this.getYRot(), 0.0F);
                if (level.addFreshEntity(wolf))
                {
                    this.storedWolf = null;
                }
            });
        }
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger()
    {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player player, Vec3 input)
    {
        super.tickRidden(player, input);

        // 跳跃：检测骑手的跳跃键按下沿，落地时触发一次跳跃。这里补回 —— 力度取 getJumpPower
        // （默认 0.42，可 override 调大）。
        boolean riderJumping = ((LivingEntityAccessor) player).isJumping();
        if (riderJumping && !this.trinketsandbaubles$wasJumping && this.onGround())
        {
            double jumpPower = this.getJumpPower();
            this.setDeltaMovement(this.getDeltaMovement().x, jumpPower, this.getDeltaMovement().z);
        }
        this.trinketsandbaubles$wasJumping = riderJumping;

        this.setRot(player.getYRot(), player.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        this.setSprinting(player.isSprinting());
        if (this.onGround())
        {
            this.setJumping(false);
        }
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 input)
    {
        float forward = player.zza <= 0.0F ? player.zza * 0.25F : player.zza;
        return new Vec3(player.xxa * 0.5F, 0.0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player)
    {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public double getPassengersRidingOffset()
    {
        return this.getBbHeight();
    }

    @Override
    public boolean dismountsUnderwater()
    {
        return false;
    }

    @Override
    protected float getJumpPower()
    {
        return 0.7F * this.getBlockJumpFactor() + this.getJumpBoostPower();
    }

    @Override
    public boolean canBreatheUnderwater()
    {
        return true;
    }

    /**
     * The mount's lunge attack: hits everything in a line in front of the rider, hardest on the first.
     */
    public void mountedAttack(Player player, double maxDistance)
    {
        Vec3 start = this.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(maxDistance));
        AABB search = new AABB(start, end).inflate(1.0);
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : this.level().getEntities(
                this, search, e -> e instanceof LivingEntity && e != player))
        {
            if (entity.getBoundingBox().inflate(0.3).clip(start, end).isPresent()
                    && this.wantsToAttack((LivingEntity) entity, player))
            {
                targets.add((LivingEntity) entity);
            }
        }

        targets.sort(Comparator.comparingDouble(target ->
                target.position().subtract(start).dot(look)));
        float baseDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean hit = false;

        for (int i = 0; i < targets.size(); i++)
        {
            float multiplier = i == 0 ? 1.0F : (i == 1 ? 0.5F : 0.2F);
            hit = this.doHurtTargetInner(targets.get(i), baseDamage * multiplier, player) || hit;
        }

        double dx = end.x - this.getX();
        double dz = end.z - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        Vec3 motion = this.getDeltaMovement();
        if (distance >= 1.0E-4)
        {
            motion = motion.add(dx / distance * 4.0 * 0.4 + motion.x * 0.6, 0.0,
                    dz / distance * 4.0 * 0.4 + motion.z * 0.6);
        }

        if (hit)
        {
            motion = new Vec3(motion.x, 0.42, motion.z);
        }

        this.setDeltaMovement(motion);
        this.hurtMarked = true;
        this.swing(InteractionHand.MAIN_HAND);
    }

    private boolean doHurtTargetInner(LivingEntity target, float damage, Player rider)
    {
        boolean hit = target.hurt(this.damageSources().mobAttack(this), damage);
        if (hit)
        {
            this.doEnchantDamageEffects(this, target);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        Entity attacker = source.getEntity();
        boolean fromRider = this.isVehicle() && attacker != null && this.hasPassenger(attacker);
        boolean damaged = !fromRider && super.hurt(source, amount);
        if (damaged && !this.level().isClientSide)
        {
            this.retaliate(attacker);
        }
        return damaged;
    }

    private void retaliate(@Nullable Entity attacker)
    {
        if (this.retaliationCooldown <= 0 && attacker instanceof LivingEntity target)
        {
            LivingEntity owner = this.getOwner();
            if (owner != null && this.wantsToAttack(target, owner) && this.hasLineOfSight(target)
                    && !(this.distanceToSqr(target) > 25.0))
            {
                this.retaliationCooldown = RETALIATION_COOLDOWN_TICKS;
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                Vec3 motion = this.getDeltaMovement();
                if (distance >= 1.0E-4)
                {
                    motion = motion.add(dx / distance, 0.0, dz / distance);
                }

                if (this.onGround())
                {
                    motion = new Vec3(motion.x, 0.32, motion.z);
                }

                this.setDeltaMovement(motion);
                target.hurt(this.damageSources().mobAttack(this), RETALIATION_DAMAGE);
            }
        }
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner)
    {
        if (!(target instanceof net.minecraft.world.entity.monster.Creeper)
                && !(target instanceof net.minecraft.world.entity.monster.Ghast))
        {
            if (target instanceof Wolf wolf && wolf.isTame() && wolf.getOwner() == owner)
            {
                return false;
            }

            if (target instanceof Player targetPlayer && owner instanceof Player ownerPlayer
                    && !ownerPlayer.canHarmPlayer(targetPlayer))
            {
                return false;
            }

            if (target instanceof net.minecraft.world.entity.animal.horse.AbstractHorse horse
                    && horse.isTamed())
            {
                return false;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound()
    {
        return null;
    }

    @Override
    protected boolean shouldDropLoot()
    {
        return false;
    }

    @Override
    public boolean canMate(Animal other)
    {
        return false;
    }

    @Override
    @Nullable
    public Wolf getBreedOffspring(ServerLevel level, AgeableMob other)
    {
        return null;
    }

    @Override
    public boolean isBaby()
    {
        return false;
    }

    public void storeOldWolf(Wolf wolf)
    {
        CompoundTag tag = new CompoundTag();
        if (wolf.saveAsPassenger(tag))
        {
            this.storedWolf = tag;
        }
    }

    public boolean hasStoredWolf()
    {
        return this.storedWolf != null && this.storedWolf.contains("id", 8);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        if (this.hasStoredWolf())
        {
            tag.put(STORED_WOLF_TAG, this.storedWolf);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        this.storedWolf = tag.contains(STORED_WOLF_TAG, 10) ? tag.getCompound(STORED_WOLF_TAG) : null;
    }
}