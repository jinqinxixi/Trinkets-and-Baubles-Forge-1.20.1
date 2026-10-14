package com.jinqinxixi.trinketsandbaubles.util;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 龙眼 + 龙种族的免疫/夜视维持。
 *
 * <p>寻宝探测（贪婪之眼）已拆到 {@link GreedyEyesSystem}；本类只负责免疫效果（火/冰/雷抗性）、
 * 夜视，以及「是否有探测资格」的判定（有激活龙族或佩戴龙眼），供贪婪之眼复用。
 */
@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
public class ScanSystem {
    private static final Map<UUID, PlayerScanState> PLAYER_SCAN_STATES = new HashMap<>();

    public static class PlayerScanState {
        public boolean nightVisionEnabled = false;
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PLAYER_SCAN_STATES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        if (event.player instanceof ServerPlayer player) {
            applyEffects(player);
        }
    }

    public static void toggleNightVision(ServerPlayer player) {
        PlayerScanState state = getPlayerState(player);
        state.nightVisionEnabled = !state.nightVisionEnabled;

        player.displayClientMessage(
                Component.translatable("item.dragons_eye.night_vision." +
                        (state.nightVisionEnabled ? "on" : "off")), true);

        if (!state.nightVisionEnabled) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    public static boolean hasNightVision(Player player) {
        return getPlayerState(player).nightVisionEnabled;
    }

    private static void applyEffects(ServerPlayer player) {
        boolean hasDragonCapability = player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                .map(cap -> cap instanceof DragonCapability && cap.isActive())
                .orElse(false);

        boolean hasDragonsEye = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof DragonsEyeItem)
                        .isPresent())
                .orElse(false);

        if (!hasDragonCapability && !hasDragonsEye) {
            return;
        }

        PlayerScanState state = getPlayerState(player);

        // 龙变身按元素给对应免疫（buff 图标三种颜色区分）；龙眼则维持火焰抗性。
        if (hasDragonCapability) {
            String element = player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                    .map(cap -> cap instanceof DragonCapability d ? d.getDragonElement() : "fire")
                    .orElse("fire");
            MobEffect immunity = switch (element)
            {
                case "ice" -> com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.ICE_RESISTANCE.get();
                case "lightning" -> com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.LIGHTNING_RESISTANCE.get();
                default -> MobEffects.FIRE_RESISTANCE;
            };
            if (!player.hasEffect(immunity) || player.getEffect(immunity).getDuration() <= 20) {
                player.addEffect(new MobEffectInstance(
                        immunity,
                        Integer.MAX_VALUE,
                        0,
                        true,
                        false
                ));
            }
        } else if (!player.hasEffect(MobEffects.FIRE_RESISTANCE) ||
                player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration() <= 20) {
            MobEffectInstance fireResistance = new MobEffectInstance(
                    MobEffects.FIRE_RESISTANCE,
                    Integer.MAX_VALUE,
                    0,
                    true,
                    false
            );
            player.addEffect(fireResistance);
        }

        // 冰/雷龙眼的抗性效果也在这里每 tick 维持，否则喝牛奶或 /clear 会清掉它们、且要脱戴
        // 才能恢复（issue #21）。判断依据是具体装备的是哪个龙眼，而不是有没有效果。
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            boolean hasIceEye = handler.findFirstCurio(stack ->
                    stack.getItem() instanceof com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeIceItem)
                    .isPresent();
            boolean hasLightningEye = handler.findFirstCurio(stack ->
                    stack.getItem() instanceof com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeLightningItem)
                    .isPresent();

            if (hasIceEye && (!player.hasEffect(com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.ICE_RESISTANCE.get()) ||
                    player.getEffect(com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.ICE_RESISTANCE.get()).getDuration() <= 20)) {
                player.addEffect(new MobEffectInstance(
                        com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.ICE_RESISTANCE.get(),
                        Integer.MAX_VALUE, 0, true, false));
            }

            if (hasLightningEye && (!player.hasEffect(com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.LIGHTNING_RESISTANCE.get()) ||
                    player.getEffect(com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.LIGHTNING_RESISTANCE.get()).getDuration() <= 20)) {
                player.addEffect(new MobEffectInstance(
                        com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.LIGHTNING_RESISTANCE.get(),
                        Integer.MAX_VALUE, 0, true, false));
            }
        });

        if (state.nightVisionEnabled) {
            if (!player.hasEffect(MobEffects.NIGHT_VISION) ||
                    player.getEffect(MobEffects.NIGHT_VISION).getDuration() <= 20) {
                MobEffectInstance nightVision = new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        Integer.MAX_VALUE,
                        0,
                        true,
                        false
                );
                player.addEffect(nightVision);
            }
        }
    }

    /** 是否有寻宝探测资格：有激活龙族或佩戴龙眼。供 {@link GreedyEyesSystem} 复用。 */
    public static boolean canPlayerScan(Player player) {
        boolean hasDragonCapability = player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                .map(cap -> cap instanceof DragonCapability && cap.isActive())
                .orElse(false);

        boolean hasDragonsEye = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof DragonsEyeItem)
                        .isPresent())
                .orElse(false);

        return hasDragonCapability || hasDragonsEye;
    }

    public static PlayerScanState getPlayerState(Player player) {
        return PLAYER_SCAN_STATES.computeIfAbsent(player.getUUID(), k -> new PlayerScanState());
    }
}
