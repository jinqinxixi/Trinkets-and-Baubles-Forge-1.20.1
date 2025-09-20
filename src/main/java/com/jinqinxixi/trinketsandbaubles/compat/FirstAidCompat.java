package com.jinqinxixi.trinketsandbaubles.compat;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.items.ModItem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

public class FirstAidCompat {
    @SubscribeEvent
    public void onDamage(FirstAidLivingDamageEvent event) {
        if (!ModConfig.ENABLE_DAMAGE_SHIELD_EFFECT.get()) {
            return;
        }

        Player player = event.getEntity();
        Optional<SlotResult> result = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(handler -> handler.findFirstCurio(ModItem.DAMAGE_SHIELD.get()));

        if (result.isPresent()) {
            AbstractPlayerDamageModel after = event.getAfterDamage();
            if (after.HEAD.currentHealth < 1 || (after.BODY.currentHealth < 1)) {
                if (player.getRandom().nextDouble() < ModConfig.DAMAGE_SHIELD_TRIGGER_CHANCE.get()) {
                    event.setCanceled(true);
                }
            }
        }
    }
}