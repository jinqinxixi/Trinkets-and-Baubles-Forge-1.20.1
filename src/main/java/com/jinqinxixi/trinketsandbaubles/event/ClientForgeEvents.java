package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.impl.FairyCapability;
import com.jinqinxixi.trinketsandbaubles.capability.impl.TaurusCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay;
import com.jinqinxixi.trinketsandbaubles.network.handler.ClientNetworkHandler;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
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

    // 金牛踏击蓄力状态：开始按下的 tick，-1 表示未在蓄力。
    private static long stampedeStartTick = -1L;
    private static double stampedeStartX = 0.0;
    private static double stampedeStartZ = 0.0;
    private static boolean stampedeCancelled = false;

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
        handleGoblinWolfRider(player);
        handleDragonLightningBolt(player);
        handleTaurusStampede(event, player);
        handleItemToggles(player);
        handlePolarizedStoneToggles();
        handleGlideKey(event, player);
    }

    /**
     * 金牛踏击蓄力的每 tick 处理：按住期间定身 + 蓄力音（渐强）+ 蓄满自动取消。
     *
     * <p>「蓄满返回 false → 强制释放」机制：蓄满那一 tick 播放提示音并取消本次蓄力，玩家松手
     * 也不会冲刺。
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (stampedeStartTick < 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        int chargeTime = RaceAttributesConfig.TAURUS.TAURUS_STAMPEDE_CHARGE_TIME.get();
        long held = Math.max(0, player.tickCount - stampedeStartTick);

        boolean taurusActive = player.getCapability(ModCapabilities.TAURUS_CAPABILITY)
                .map(cap -> cap instanceof TaurusCapability t && t.isActive())
                .orElse(false);
        // 只有雷龙按 B 才有闪电箭蓄力；火/冰龙按 B 不该有任何反应。
        boolean dragonLightning = player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                .map(cap -> cap instanceof DragonCapability d && d.isActive()
                        && "lightning".equals(d.getDragonElement()))
                .orElse(false);

        if (!taurusActive && !dragonLightning) {
            stampedeStartTick = -1L;
            return;
        }

        // 雷龙用自己的蓄力时长（20 tick），音效和定身也各按各的，但「按住蓄力、松手发射」这套行为
        // 照金牛。
        if (dragonLightning) {
            chargeTime = DragonCapability.LIGHTNING_BOLT_CHARGE_TIME;
        }

        if (held >= chargeTime) {
            // 蓄满就作废（和金牛一样）：「蓄满返回 false → 强制释放」，蓄满那一 tick
            // 播放提示音并取消本次蓄力，玩家松手也不会发射，必须重新按一次。
            stampedeStartTick = -1L;
            stampedeCancelled = true;
            player.playSound(taurusActive
                    ? SoundEvents.ZOMBIFIED_PIGLIN_ANGRY
                    : SoundEvents.END_GATEWAY_SPAWN, 0.4F, 0.2F);
            // 立即清掉扣魔预览，不留衰减残影——否则条会缩水一截，看着像还在蓄力。
            com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay.getInstance().clearManaCost();
            return;
        }

        // 定身只对金牛：踏击要蓄力锁位；闪电箭不定身。
        if (taurusActive) {
            player.setDeltaMovement(0.0, player.getDeltaMovement().y, 0.0);
            player.setPos(stampedeStartX, player.getY(), stampedeStartZ);
        }

        // HUD 预览「将扣除的魔力量」：随蓄力时间线性增长。
        float progress = Math.min(1.0F, held / (float) chargeTime);
        float manaCost = (taurusActive
                ? RaceAttributesConfig.TAURUS.TAURUS_STAMPEDE_MANA_COST.get().floatValue()
                : DragonCapability.LIGHTNING_BOLT_COST) * progress;
        com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay.getInstance().setManaCost(manaCost);

        // 蓄力音：金牛用北极熊脚步，雷龙用音符盒贝斯，音调都随蓄力进度渐升。
        if (taurusActive) {
            if (player.tickCount % 4 == 0) {
                player.playSound(SoundEvents.POLAR_BEAR_STEP, 0.3F, Math.min(0.4F + 0.6F * progress, 1.0F));
            }
        } else if (player.tickCount % 2 == 0) {
            player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.2F, Math.min(0.4F + 0.6F * progress, 1.0F));
        }
    }

    /**
     * Reports the jump key while gliding.
     *
     * <p>Gliding itself needs no key: it starts on its own when a player with wings falls, which is
     * the arrangement the mixin beside this sets up. What the key does is push, which is the one thing
     * gliding cannot do by itself — the jump key is what a player presses in the air by reflex, and
     * giving it that meaning is what a pair of wings should do.
     *
     * <p>Only while already gliding. Before that the key is a jump, and on the ground it belongs to
     * the game.
     */
    private static void handleGlideKey(InputEvent.Key event, Player player) {
        if (event.getAction() != GLFW.GLFW_PRESS)
        {
            return;
        }

        if (!player.isFallFlying())
        {
            return;
        }

        if (Minecraft.getInstance().options.keyJump.matches(event.getKey(), event.getScanCode()))
        {
            ClientNetworkHandler.sendGlidePush();
        }
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

    private static void handleGoblinWolfRider(Player player) {
        player.getCapability(ModCapabilities.GOBLINS_CAPABILITY).ifPresent(cap -> {
            if (cap.isActive() && KeyBindings.WOLF_RIDER_KEY.consumeClick()) {
                ClientNetworkHandler.sendWolfRiderKey();
            }
        });
    }

    private static void handleDragonLightningBolt(Player player) {
        // 雷龙的闪电箭与金牛踏击共用种族技能键，也共用下面那套蓄力计时：
        // 按下开始计时、松手把蓄力比例发出去，由服务端按激活种族决定这一按算踏击还是闪电箭。
        // 这里不再自己判断，否则两个种族各计一套时会互相盖掉。
    }

    private static void handleTaurusStampede(InputEvent.Key event, Player player) {
        // 只处理种族技能键的按下/松开。蓄力计时在这里统一走，不挂在某一个种族上：
        // 同一枚键既服务金牛踏击也服务雷龙闪电箭，谁在激活由松手那一刻再判断。
        if (event.getKey() != KeyBindings.RACE_ABILITY_KEY.getKey().getValue()
                && event.getScanCode() != KeyBindings.RACE_ABILITY_KEY.getKey().getValue()) {
            return;
        }

        boolean taurusActive = player.getCapability(ModCapabilities.TAURUS_CAPABILITY)
                .map(cap -> cap instanceof TaurusCapability taurus && taurus.isActive())
                .orElse(false);
        // 只有雷龙按 B 才发射闪电箭；火/冰龙按 B 不该有任何反应。
        boolean dragonLightning = player.getCapability(ModCapabilities.DRAGON_CAPABILITY)
                .map(cap -> cap instanceof DragonCapability dragon && dragon.isActive()
                        && "lightning".equals(dragon.getDragonElement()))
                .orElse(false);

        if (!taurusActive && !dragonLightning) {
            return;
        }

        if (event.getAction() == GLFW.GLFW_PRESS) {
            // 开始蓄力：记录按下时刻与起点（用于踏击定身锁位）。
            if (stampedeStartTick < 0) {
                stampedeStartTick = player.tickCount;
                stampedeStartX = player.getX();
                stampedeStartZ = player.getZ();
            }
            return;
        }

        // 按住不放时 GLFW 会发 REPEAT 事件，忽略它，否则会把蓄力计时重置、导致「按一小会儿就作废」。
        if (event.getAction() == GLFW.GLFW_REPEAT) {
            return;
        }

        if (event.getAction() != GLFW.GLFW_RELEASE || stampedeStartTick < 0) {
            // 没有按下记录（异常，含「蓄满作废后松手」），重置。
            stampedeStartTick = -1L;
            stampedeCancelled = false;
            com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay.getInstance().clearManaCost();
            return;
        }

        // 松开：按蓄力时长计算强度。金牛用 40 tick、闪电箭用 20 tick，各按各的算满蓄。
        int chargeTime = taurusActive
                ? RaceAttributesConfig.TAURUS.TAURUS_STAMPEDE_CHARGE_TIME.get()
                : DragonCapability.LIGHTNING_BOLT_CHARGE_TIME;
        long held = Math.max(0, player.tickCount - stampedeStartTick);
        float charge = Math.min(1.0F, held / (float) chargeTime);
        stampedeStartTick = -1L;
        com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay.getInstance().clearManaCost();

        // 蓄满未松手时已被打断，这一按作废。
        if (stampedeCancelled) {
            stampedeCancelled = false;
            return;
        }

        // 最低蓄力门槛：蓄力不足 10% 不发射。
        if (charge < 0.1F) {
            return;
        }

        if (taurusActive) {
            // 金牛：客户端本地冲刺（移动是客户端预测的，必须在客户端设速度）。
            Vec3 look = player.getLookAngle();
            Vec3 h = new Vec3(look.x, 0.0, look.z);
            if (h.lengthSqr() < 1.0E-4) {
                h = new Vec3(1.0, 0.0, 0.0);
            }
            h = h.normalize();
            double curve = charge * charge;
            double minVel = RaceAttributesConfig.TAURUS.TAURUS_STAMPEDE_VELOCITY_MIN.get();
            double maxVel = RaceAttributesConfig.TAURUS.TAURUS_STAMPEDE_VELOCITY_MAX.get();
            // 空中冲刺速度打折（落地 ×1.0，空中 ×0.25）。
            double groundMultiplier = player.onGround() ? 1.0 : 0.25;
            double speed = (minVel + (maxVel - minVel) * curve) * groundMultiplier;
            player.setDeltaMovement(h.x * speed, 0.0, h.z * speed);
            player.hasImpulse = true;

            // 松开冲刺音：僵尸猪灵怒吼。
            player.playSound(SoundEvents.ZOMBIFIED_PIGLIN_ANGRY, 0.4F, 0.2F);
        } else {
            // 雷龙：发射音 END_GATEWAY_SPAWN。特效与伤害由服务端做。
            player.playSound(SoundEvents.END_GATEWAY_SPAWN, 0.3F, 0.3F);
        }

        ClientNetworkHandler.sendRaceAbility(charge);
    }

    private static void handleItemToggles(Player player) {

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