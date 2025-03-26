package com.jinqinxixi.trinketsandbaubles.events;

import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsRingItem;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaHudOverlay;
import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import com.jinqinxixi.trinketsandbaubles.network.handler.ClientNetworkHandler;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
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
    private static boolean nightVisionEnabled = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || minecraft.getConnection() == null) {
            return;
        }

        handleEscapeAndManaHUD(event, minecraft);
        handleMovementKeys(player);
        handleDragonEffects(event, minecraft, player);
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

    private static void handleDragonEffects(InputEvent.Key event, Minecraft minecraft, Player player) {
        if (!player.hasEffect(ModEffects.DRAGON.get())) {
            return;
        }

        // 夜视切换
        if (event.getKey() == GLFW.GLFW_KEY_I &&
                event.getAction() == GLFW.GLFW_PRESS &&
                minecraft.screen == null) {

            toggleNightVision(player);
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

    private static void toggleNightVision(Player player) {
        nightVisionEnabled = !nightVisionEnabled;
        ClientNetworkHandler.sendDragonNightVision(nightVisionEnabled);

        // 显示状态消息
        Component message = Component.translatable(
                nightVisionEnabled ?
                        "message.trinketsandbaubles.dragon.night_vision.enabled" :
                        "message.trinketsandbaubles.dragon.night_vision.disabled"
        ).withStyle(nightVisionEnabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);

        player.displayClientMessage(message, true);

        // 立即更新客户端效果
        if (!nightVisionEnabled) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
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