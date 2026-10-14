package com.jinqinxixi.trinketsandbaubles.entity;

import com.jinqinxixi.trinketsandbaubles.init.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * The damage half of a dragon-breath impact: a short-lived zone that hurts everything inside it
 * except its owner.
 *
 * <p>The lingering cloud draws itself and is its own damage; here the two are split so the
 * vanilla {@link net.minecraft.world.entity.AreaEffectCloud} can stay pure visuals. Vanilla's cloud
 * has no owner-free passthrough for {@code HARM}, so leaving the damage to it would hit the shooter.
 * This entity is invisible and only applies the damage, skipping its owner.
 */
public class BreathAreaEffect extends Entity
{
    private static final int REAPPLY_DELAY = 20;

    @Nullable
    private UUID ownerUUID;
    private float radius = 3.0F;
    private float damage = 6.0F;
    private int duration = 80;

    public BreathAreaEffect(EntityType<?> type, Level level)
    {
        super(type, level);
    }

    public static void spawn(ServerLevel level, Vec3 pos, @Nullable LivingEntity owner,
                             float radius, float damage, int duration)
    {
        BreathAreaEffect area = new BreathAreaEffect(ModEntities.BREATH_AREA.get(), level);
        area.setPos(pos.x, pos.y, pos.z);
        area.ownerUUID = owner != null ? owner.getUUID() : null;
        area.radius = radius;
        area.damage = damage;
        area.duration = duration;
        level.addFreshEntity(area);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.level().isClientSide)
        {
            return;
        }

        if (this.tickCount >= this.duration)
        {
            this.discard();
            return;
        }

        if (this.tickCount % REAPPLY_DELAY == 0)
        {
            LivingEntity owner = this.owner();
            double r = this.radius;
            AABB box = new AABB(
                    this.getX() - r, this.getY() - r, this.getZ() - r,
                    this.getX() + r, this.getY() + r, this.getZ() + r);

            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, box))
            {
                if (target == owner)
                {
                    continue;
                }
                target.hurt(this.damageSource(owner), this.damage);
            }
        }
    }

    @Nullable
    private LivingEntity owner()
    {
        if (this.ownerUUID == null || !(this.level() instanceof ServerLevel serverLevel))
        {
            return null;
        }
        return serverLevel.getEntity(this.ownerUUID) instanceof LivingEntity living ? living : null;
    }

    private DamageSource damageSource(@Nullable LivingEntity owner)
    {
        return owner != null
                ? owner.damageSources().indirectMagic(this, owner)
                : this.damageSources().magic();
    }

    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        if (tag.hasUUID("Owner"))
        {
            this.ownerUUID = tag.getUUID("Owner");
        }
        this.radius = tag.contains("Radius") ? tag.getFloat("Radius") : 3.0F;
        this.damage = tag.contains("Damage") ? tag.getFloat("Damage") : 6.0F;
        this.duration = tag.contains("Duration") ? tag.getInt("Duration") : 80;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        if (this.ownerUUID != null)
        {
            tag.putUUID("Owner", this.ownerUUID);
        }
        tag.putFloat("Radius", this.radius);
        tag.putFloat("Damage", this.damage);
        tag.putInt("Duration", this.duration);
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }
}