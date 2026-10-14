package com.jinqinxixi.trinketsandbaubles.network.handler;

import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.DragonBreathMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.DragonFlightToggleMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.StopDragonBreathMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.DragonsEyeToggleMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.ChargeKeyMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.DashKeyPressMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.PolarizedStoneToggleMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.StopChargeMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.WolfRiderKeyMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.RaceAbilityMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 客户端专用的网络处理类
 * 所有从客户端发送到服务器的网络消息都应该通过这个类来处理
 */
@OnlyIn(Dist.CLIENT)
public class  ClientNetworkHandler {

    // 龙息相关消息
    public static void sendDragonBreath() {
        NetworkHandler.sendToServer(new DragonBreathMessage());
    }

    public static void sendStopDragonBreath() {
        NetworkHandler.sendToServer(new StopDragonBreathMessage());
    }

    // 移动相关消息
    public static void sendDashKeyPress() {
        NetworkHandler.sendToServer(new DashKeyPressMessage());
    }

    // 充能相关消息
    public static void sendChargeKey() {
        NetworkHandler.sendToServer(new ChargeKeyMessage());
    }

    public static void sendStopCharge() {
        NetworkHandler.sendToServer(new StopChargeMessage());
    }

    // 龙眼相关消息
    public static void sendDragonsEyeToggle(int mode) {
        NetworkHandler.sendToServer(new DragonsEyeToggleMessage(mode));
    }

    // 偏振石相关消息
    public static void sendPolarizedStoneToggle(boolean isDeflection) {
        NetworkHandler.sendToServer(new PolarizedStoneToggleMessage(isDeflection));
    }
    public static void sendDragonFlightToggle() {
        NetworkHandler.sendToServer(new DragonFlightToggleMessage());
    }

    /** Reports the jump key while airborne, which starts a glide or asks for a push. */
    /** Tells the server a glide has started, which it would otherwise correct away. */
    public static void sendGlideStart() {
        NetworkHandler.sendToServer(
                new com.jinqinxixi.trinketsandbaubles.network.message.Messages.GlideMessage(false));
    }

    /** Asks the server for a push, which it pays for out of the player's mana. */
    public static void sendGlidePush() {
        NetworkHandler.sendToServer(
                new com.jinqinxixi.trinketsandbaubles.network.message.Messages.GlideMessage(true));
    }

    /** 哥布林骑狼按键。 */
    public static void sendWolfRiderKey() {
        NetworkHandler.sendToServer(new WolfRiderKeyMessage());
    }

    /** 种族技能键（金牛踏击等）。charge 为蓄力比例 0~1。 */
    public static void sendRaceAbility(float charge) {
        NetworkHandler.sendToServer(new RaceAbilityMessage(charge));
    }
}