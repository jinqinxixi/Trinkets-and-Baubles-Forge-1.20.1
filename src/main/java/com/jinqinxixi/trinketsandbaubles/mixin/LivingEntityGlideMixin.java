package com.jinqinxixi.trinketsandbaubles.mixin;

import com.jinqinxixi.trinketsandbaubles.mixinutil.GlideHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** 滑翔能力钩子：让无鞘翅种族也能获得滑翔标记。见 {@link GlideHooks}。 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityGlideMixin extends Entity
{
    public LivingEntityGlideMixin(EntityType<?> type, Level level)
    {
        super(type, level);
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/world/entity/LivingEntity.setSharedFlag(IZ)V"
            ),
            method = "updateFallFlying"
    )
    private boolean pocketnetwork$setFlag(boolean value)
    {
        return GlideHooks.canFly((LivingEntity) (Object) this, this.getSharedFlag(7), value);
    }
}
