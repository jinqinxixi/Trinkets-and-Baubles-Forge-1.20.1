package com.jinqinxixi.trinketsandbaubles.entity;

import com.jinqinxixi.trinketsandbaubles.client.particles.ClientEffects;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.DistExecutor;

/**
 * A fired dragon-breath ball, kept light on purpose.
 *
 * <p>This projectile flies, damages and ignites what it hits, and on ground impact spawns a lingering
 * cloud of damage. One simplification — the ground cloud is the game's own {@link AreaEffectCloud}
 * (the lingering-potion cloud) rather than a bespoke area framework, so the impact reads exactly as a
 * lingering potion while the code stays small.
 */
public class BreathProjectile extends Projectile
{
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(BreathProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_ELEMENT =
            SynchedEntityData.defineId(BreathProjectile.class, EntityDataSerializers.STRING);
    private static final int DEFAULT_COLOR = 0xFF6600;

    /** 龙息的三种元素。 */
    public static final String ELEMENT_FIRE = "fire";
    public static final String ELEMENT_ICE = "ice";
    public static final String ELEMENT_LIGHTNING = "lightning";

    private static final float GRAVITY = 0.03F;
    private static final float AIR_DRAG = 0.99F;
    private static final float WATER_DRAG = 0.8F;
    private static final int MAX_LIFETIME = 200;
    private static final float IMPACT_CLOUD_RADIUS = 3.0F;
    private static final int IMPACT_CLOUD_DURATION = 80;
    private static final float IMPACT_DAMAGE = 6.0F;

    private boolean impactHandled;

    public BreathProjectile(EntityType<? extends BreathProjectile> type, Level level)
    {
        super(type, level);
    }

    public BreathProjectile(Level level, LivingEntity shooter, int color)
    {
        this(level, shooter, color, ELEMENT_FIRE);
    }

    public BreathProjectile(Level level, LivingEntity shooter, int color, String element)
    {
        super(com.jinqinxixi.trinketsandbaubles.init.ModEntities.DRAGON_BREATH.get(), level);
        this.setOwner(shooter);
        this.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), shooter.getYRot(), shooter.getXRot());
        this.setColor(color);
        this.setElement(element);
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(DATA_COLOR, DEFAULT_COLOR);
        this.entityData.define(DATA_ELEMENT, ELEMENT_FIRE);
    }

    public void setColor(int color)
    {
        this.entityData.set(DATA_COLOR, color);
    }

    public int getColor()
    {
        return this.entityData.get(DATA_COLOR);
    }

    public void setElement(String element)
    {
        this.entityData.set(DATA_ELEMENT, element == null ? ELEMENT_FIRE : element);
    }

    public String getElement()
    {
        return this.entityData.get(DATA_ELEMENT);
    }

    public boolean isIce()
    {
        return ELEMENT_ICE.equals(getElement());
    }

    public boolean isLightning()
    {
        return ELEMENT_LIGHTNING.equals(getElement());
    }

    @Override
    public void tick()
    {
        Entity owner = this.getOwner();
        if (!this.level().isClientSide && owner != null && !owner.isAlive())
        {
            this.discard();
            return;
        }

        super.tick();

        // Client: a wisp trails the ball while it flies. Drawn only after the first couple of ticks so
        // it does not flicker right in the shooter's face.
        if (this.level().isClientSide && this.tickCount >= 3)
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientEffects.breathWisp(this.getX(), this.getY() + 0.5, this.getZ(), this.getColor()));
        }

        HitResult hit = this.traceImpact();
        if (hit.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hit))
        {
            this.onHit(hit);
            if (this.isRemoved())
            {
                return;
            }
        }

        // Move, apply drag and gravity.
        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
        this.updateRotation();

        float drag = this.isInWater() ? WATER_DRAG : AIR_DRAG;
        Vec3 next = motion.scale(drag);
        if (!this.isNoGravity())
        {
            next = next.add(0.0, -GRAVITY, 0.0);
        }
        this.setDeltaMovement(next);

        if (this.tickCount >= MAX_LIFETIME)
        {
            this.discard();
        }
    }

    private HitResult traceImpact()
    {
        Vec3 start = this.position();
        Vec3 end = start.add(this.getDeltaMovement());
        HitResult block = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        Vec3 limit = block.getType() != HitResult.Type.MISS ? block.getLocation() : end;
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(this.level(), this, start, limit,
                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity);
        return entityHit != null ? entityHit : block;
    }

    @Override
    protected boolean canHitEntity(Entity target)
    {
        return super.canHitEntity(target)
                && target != this.getOwner()
                && !(target instanceof BreathProjectile)
                && !(target instanceof BreathAreaEffect)
                && !(target instanceof AreaEffectCloud);
    }

    @Override
    protected void onHitEntity(EntityHitResult result)
    {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        if (!this.level().isClientSide && target instanceof LivingEntity living)
        {
            living.hurt(this.damageSource(), IMPACT_DAMAGE);
            // 元素差异：雷点燃 1 秒，冰减速，火点燃 5 秒。
            if (isLightning())
            {
                living.setSecondsOnFire(1);
            }
            else if (isIce())
            {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, true, true));
            }
            else
            {
                living.setSecondsOnFire(5);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result)
    {
        super.onHitBlock(result);
        if (!this.level().isClientSide && !this.impactHandled)
        {
            this.impactHandled = true;
            this.spawnImpactCloud(result.getLocation());
            this.discard();
        }
    }

    /**
     * The ground impact: a vanilla lingering-potion cloud for the visuals, plus an invisible damage
     * zone that skips the shooter. Vanilla's cloud cannot spare its owner for {@code HARM}, so the
     * damage lives in {@link BreathAreaEffect} and the cloud is only there to look right.
     */
    private void spawnImpactCloud(Vec3 impact)
    {
        if (!(this.level() instanceof ServerLevel level))
        {
            return;
        }

        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;

        AreaEffectCloud cloud = new AreaEffectCloud(level, impact.x, impact.y, impact.z);
        cloud.setOwner(owner);
        cloud.setRadius(IMPACT_CLOUD_RADIUS);
        cloud.setDuration(IMPACT_CLOUD_DURATION);
        cloud.setRadiusPerTick(-0.03F);
        cloud.setFixedColor(this.getColor());
        cloud.setWaitTime(0);
        level.addFreshEntity(cloud);

        BreathAreaEffect.spawn(level, impact, owner, IMPACT_CLOUD_RADIUS, IMPACT_DAMAGE, IMPACT_CLOUD_DURATION);
    }

    private DamageSource damageSource()
    {
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity living)
        {
            return this.damageSources().mobAttack(living);
        }
        return this.damageSources().thrown(this, owner);
    }

    @Override
    public boolean isNoGravity()
    {
        return false;
    }
}