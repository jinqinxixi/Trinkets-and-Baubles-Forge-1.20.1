package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.items.baubles.EnderQueensCrownItem;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
public class EnderQueensCrownEvents {
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!event.getEntity().level().isClientSide) {
            // 当玩家受到伤害时
            if (event.getEntity() instanceof Player player &&
                    EnderQueensCrownItem.isEquipped(player)) {

                // 处理玩家受伤
                if (EnderQueensCrownItem.onDamage(player, event.getSource())) {
                    event.setAmount(0);
                }

                // 让末影人攻击伤害了玩家的目标
                if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                    EnderQueensCrownItem.aggroNearbyEndermen(player, attacker);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEndermanTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof EnderMan enderman &&
                event.getNewTarget() instanceof Player player && // 在 1.20.1 中使用 getNewTarget
                EnderQueensCrownItem.isEquipped(player) &&
                enderman.getPersistentData().getBoolean(EnderQueensCrownItem.FRIENDLY_ENDERMAN_TAG)) {

            event.setCanceled(true);
            enderman.setTarget(null);
        }
    }

    @SubscribeEvent
    public static void onEnderTeleport(EntityTeleportEvent.EnderEntity event) {
        if (event.getEntity() instanceof EnderMan enderman) {
            // 如果是友好/受控的末影人，使用 NBT 检查
            if (enderman.getPersistentData().getBoolean(EnderQueensCrownItem.FRIENDLY_ENDERMAN_TAG) ||
                    enderman.getPersistentData().getBoolean(EnderQueensCrownItem.CROWN_CONTROLLED_TAG) ||
                    enderman.getPersistentData().getBoolean(EnderQueensCrownItem.CROWN_SUMMONED_TAG)) {

                // 取消所有随机传送
                if (!enderman.hasCustomName()) {
                    event.setCanceled(true);
                }
            }
        }
    }
}