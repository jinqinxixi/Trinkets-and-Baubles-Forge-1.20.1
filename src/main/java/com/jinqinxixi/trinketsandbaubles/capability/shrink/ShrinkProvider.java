package com.jinqinxixi.trinketsandbaubles.capability.shrink;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;

public class ShrinkProvider implements IShrinkProvider {
    private final LivingEntity livingEntity;
    private boolean isShrunk = false;
    private EntityDimensions defaultEntitySize;
    private float defaultEyeHeight;
    private float scale = 1F;


    // 修改构造函数
    public ShrinkProvider(LivingEntity entity) {
        this.livingEntity = entity;

        // 新增玩家类型检查
        if (entity instanceof Player) {
            this.defaultEntitySize = entity.getDimensions(entity.getPose());
            this.defaultEyeHeight = entity.getEyeHeight();
        } else {
            // 对非玩家实体使用安全默认值
            this.defaultEntitySize = EntityDimensions.scalable(0.6F, 1.8F);
            this.defaultEyeHeight = 1.62F;
        }
    }


    @Override
    public boolean isShrunk() {
        return isShrunk;
    }

    @Override
    public void setShrunk(boolean shrunk) {
        if (this.isShrunk != shrunk) {
            this.isShrunk = shrunk;
            sync(livingEntity);
        }
    }

    @Override
    public void shrink(@Nonnull LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player) || livingEntity.isRemoved()) {
            return;
        }
        if (!isShrunk()) {
            setShrunk(true);
            // 强制更新维度
            livingEntity.refreshDimensions();

            // 强制更新碰撞箱
            EntityDimensions newDimensions = livingEntity.getDimensions(livingEntity.getPose()).scale(scale);
            livingEntity.setBoundingBox(new AABB(
                    livingEntity.getX() - newDimensions.width / 2,
                    livingEntity.getY(),
                    livingEntity.getZ() - newDimensions.width / 2,
                    livingEntity.getX() + newDimensions.width / 2,
                    livingEntity.getY() + newDimensions.height,
                    livingEntity.getZ() + newDimensions.width / 2
            ));

            // 同步到客户端
            sync(livingEntity);
        }
    }

    @Override
    public void deShrink(@Nonnull LivingEntity livingEntity) {
        if (isShrunk()) {
            setShrunk(false);
            livingEntity.refreshDimensions();
        }
    }

    @Override
    public void sync(@Nonnull LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            if (PacketHandler.INSTANCE != null) {
                CompoundTag tag = serializeNBT();
                // 确保在发包前设置了正确的状态
                tag.putBoolean("isShrunk", isShrunk);
                tag.putFloat("scale", scale);

                PacketHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity),
                        new PacketSyncShrink(livingEntity.getId(), tag)
                );
            }
        }
    }

    @Override
    public EntityDimensions defaultEntitySize() {
        return defaultEntitySize;
    }

    @Override
    public float defaultEyeHeight() {
        return defaultEyeHeight;
    }

    @Override
    public float scale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        if (this.scale != scale) {
            this.scale = scale;
            sync(livingEntity);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isShrunk", isShrunk);
        tag.putFloat("scale", scale);
        if (defaultEntitySize != null) {
            tag.putFloat("width", defaultEntitySize.width);
            tag.putFloat("height", defaultEntitySize.height);
            tag.putBoolean("fixed", defaultEntitySize.fixed);
        }
        tag.putFloat("defaultEyeHeight", defaultEyeHeight);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        isShrunk = tag.getBoolean("isShrunk");
        scale = tag.getFloat("scale");
        defaultEntitySize = new EntityDimensions(
                tag.getFloat("width"),
                tag.getFloat("height"),
                tag.getBoolean("fixed")
        );
        defaultEyeHeight = tag.getFloat("defaultEyeHeight");
    }
}