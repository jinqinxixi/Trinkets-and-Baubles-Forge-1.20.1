package com.jinqinxixi.trinketsandbaubles.mixin;

import com.jinqinxixi.trinketsandbaubles.mixinutil.GlideHooks;
import com.jinqinxixi.trinketsandbaubles.capability.glide.GlideHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 客户端滑翔钩子：把本模组的滑翔标记接入本地玩家。见 {@link GlideHooks}。 */
@Mixin({LocalPlayer.class})
public class LocalPlayerGlideMixin
{
    @Inject(
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "net/minecraft/client/player/LocalPlayer.getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
            ),
            method = {"aiStep"}
    )
    public void pocketnetwork$checkFlight(CallbackInfo cb)
    {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null)
        {
            return;
        }

        boolean started = GlideHooks.startFlight(player);

        // Said out loud only when a glide was started, or when one could have been. Logging every tick
        // would be noise; logging nothing leaves the question of whether this runs at all unanswered,
        // and a hook that never runs and a hook that decides "no" look identical from outside.
        if (started)
        {
            com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.LOGGER.info(
                    "Glide client: started a glide (onGround={}, fallFlying={}, race={})",
                    player.onGround(), player.isFallFlying(), GlideHandler.hasGlidingRace(player));

            com.jinqinxixi.trinketsandbaubles.network.handler.ClientNetworkHandler.sendGlideStart();
        }
        else if (!player.onGround() && !player.isFallFlying()
                && GlideHandler.hasGlidingRace(player))
        {
            // Airborne, has the race, not gliding — this is the case that should have started one, so
            // if it appears the conditions below are what is stopping it.
            com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.LOGGER.info(
                    "Glide client: would glide but did not (inWater={}, levitation={})",
                    player.isInWater(), player.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION));
        }
    }

    @ModifyVariable(
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "net/minecraft/client/player/LocalPlayer.getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
            ),
            method = {"aiStep"}
    )
    public ItemStack pocketnetwork$affixEmptyStack(ItemStack stack)
    {
        return ItemStack.EMPTY;
    }
}
