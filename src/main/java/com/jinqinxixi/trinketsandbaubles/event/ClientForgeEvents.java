package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsRingItem;
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
        handleItemToggles(player);
    }

    private static void handleEscapeAndManaHUD(InputEvent.Key event, Minecraft minecraft) {
        // ESC键处理
        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE && event.getAction() == GLFW.GLFW_PRESS) {
            ManaHudOverlay.getInstance().handleEscapeKey();
        }

        // 魔力条位置切换
        if (KeyBindings.MANA_HUD_POSITION_KEY.consumeClick()) {
            ManaHudOverlay.getInstance().toggleDragMode();
        }
    }

    private static void handleMovementKeys(Player player) {
        // 闪避处理
        while (KeyBindings.DASH_KEY.consumeClick()) {
            ClientNetworkHandler.sendDashKeyPress();
        }

        // 充能处理
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
                // 飞行能力切换
                if (KeyBindings.DRAGON_FLIGHT_TOGGLE_KEY.consumeClick()) {
                    ClientNetworkHandler.sendDragonFlightToggle();
                }

                // 夜视切换
                if (KeyBindings.DRAGON_NIGHT_VISION_KEY.consumeClick()) {
                    ClientNetworkHandler.sendDragonNightVision(!dragonCap.isNightVisionEnabled());
                }

                // 龙息处理
                boolean isBreathing = KeyBindings.DRAGON_BREATH_KEY.isDown();
                if (isBreathing != wasBreathing) {
                    if (isBreathing) {
                        ClientNetworkHandler.sendDragonBreath();
                    } else {
                        ClientNetworkHandler.sendStopDragonBreath();
                    }
                    wasBreathing = isBreathing;
                }
            }
        });
    }

    private static void handleItemToggles(Player player) {
        // 龙眼和龙戒指模式切换
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.findFirstCurio(stack -> stack.getItem() instanceof DragonsEyeItem)
                    .ifPresent(curio -> {
                        if (KeyBindings.TOGGLE_DRAGONS_EYE_MODE.consumeClick()) {
                            ClientNetworkHandler.sendDragonsEyeToggle(0);
                        }
                        if (KeyBindings.TOGGLE_DRAGONS_EYE_VISION.consumeClick()) {
                            ClientNetworkHandler.sendDragonsEyeToggle(1);
                        }
                    });

            handler.findFirstCurio(stack -> stack.getItem() instanceof DragonsRingItem)
                    .ifPresent(curio -> {
                        if (KeyBindings.TOGGLE_DRAGONS_EYE_MODE.consumeClick()) {
                            ClientNetworkHandler.sendDragonsEyeToggle(0);
                        }
                    });
        });

        // 偏振石模式切换
        while (KeyBindings.ATTRACTION_TOGGLE_KEY.consumeClick()) {
            ClientNetworkHandler.sendPolarizedStoneToggle(false);
        }
        while (KeyBindings.DEFLECTION_TOGGLE_KEY.consumeClick()) {
            ClientNetworkHandler.sendPolarizedStoneToggle(true);
        }
    }
}