package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.impl.FairyCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay;
import com.jinqinxixi.trinketsandbaubles.network.handler.ClientNetworkHandler;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID, value = Dist.CLIENT)
public class ClientForgeEvents {
    private static boolean wasCharging = false;
    private static boolean wasBreathing = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || minecraft.getConnection() == null) {
            return;
        }

        handleEscapeAndManaHUD(event, minecraft);
        handleMovementKeys(player);
        handleDragonCapability(event, minecraft, player);
        handleFairyCapability(player);
        handleItemToggles(player);
        handlePolarizedStoneToggles();
    }

    private static void handleEscapeAndManaHUD(InputEvent.Key event, Minecraft minecraft) {
        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE && event.getAction() == GLFW.GLFW_PRESS) {
            ManaHudOverlay.getInstance().handleEscapeKey();
        }

        if (KeyBindings.MANA_HUD_POSITION_KEY.consumeClick()) {
            ManaHudOverlay.getInstance().toggleDragMode();
        }
    }

    private static void handleMovementKeys(Player player) {
        while (KeyBindings.DASH_KEY.consumeClick()) {
            ClientNetworkHandler.sendDashKeyPress();
        }

        boolean isCharging = KeyBindings.CHARGE_KEY.isDown();
        if (isCharging != wasCharging) {
            if (isCharging) {
                ClientNetworkHandler.sendChargeKey();
            } else {
                ClientNetworkHandler.sendStopCharge();
            }
            wasCharging = isCharging;
        }
    }

    private static void handleDragonCapability(InputEvent.Key event, Minecraft minecraft, Player player) {
        player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
            if (cap instanceof DragonCapability dragonCap && dragonCap.isActive()) {
                if (KeyBindings.DRAGON_FLIGHT_TOGGLE_KEY.consumeClick()) {
                    ClientNetworkHandler.sendDragonFlightToggle();
                }
                boolean isBreathing = KeyBindings.DRAGON_BREATH_KEY.isDown();
                if (isBreathing != wasBreathing) {
                    TrinketsandBaublesMod.LOGGER.info("Dragon breath key state changed. isBreathing: {}, wasBreathing: {}",
                            isBreathing, wasBreathing);
                    if (isBreathing) {
                        if (!dragonCap.isDragonBreathActive()) {
                            TrinketsandBaublesMod.LOGGER.info("Sending dragon breath activation");
                            ClientNetworkHandler.sendDragonBreath();
                        }
                    } else {
                        if (dragonCap.isDragonBreathActive()) {
                            TrinketsandBaublesMod.LOGGER.info("Sending dragon breath deactivation");
                            ClientNetworkHandler.sendStopDragonBreath();
                        }
                    }
                    wasBreathing = isBreathing;
                }
            }
        });
    }

    private static void handleFairyCapability(Player player) {
        player.getCapability(ModCapabilities.FAIRY_CAPABILITY).ifPresent(cap -> {
            if (cap instanceof FairyCapability fairyCap && fairyCap.isActive()) {
                if (KeyBindings.DRAGON_FLIGHT_TOGGLE_KEY.consumeClick()) {
                    ClientNetworkHandler.sendDragonFlightToggle();
                }
            }
        });
    }

    private static void handleItemToggles(Player player) {
        // 检查玩家是否有龙族能力且激活
        AtomicBoolean hasDragonCapability = new AtomicBoolean(false);
        player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
            if (cap instanceof DragonCapability dragonCap && dragonCap.isActive()) {
                hasDragonCapability.set(true);
            }
        });

        // 处理夜视功能 - 注意这里的修改，移除了对龙族能力的检查
        if (KeyBindings.DRAGON_NIGHT_VISION_KEY.consumeClick()) {
            TrinketsandBaublesMod.LOGGER.info("Night vision toggle requested");
            ClientNetworkHandler.sendDragonsEyeToggle(1);
        }
    }
    private static void handlePolarizedStoneToggles() {
        // 偏振石模式切换
        while (KeyBindings.ATTRACTION_TOGGLE_KEY.consumeClick()) {
            ClientNetworkHandler.sendPolarizedStoneToggle(false);
        }
        while (KeyBindings.DEFLECTION_TOGGLE_KEY.consumeClick()) {
            ClientNetworkHandler.sendPolarizedStoneToggle(true);
        }
    }
}