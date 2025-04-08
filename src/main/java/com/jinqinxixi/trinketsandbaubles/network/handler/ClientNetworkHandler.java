package com.jinqinxixi.trinketsandbaubles.network.handler;

import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.DragonBreathMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.DragonFlightToggleMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.DragonNightVisionMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.StopDragonBreathMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.DragonsEyeToggleMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.ChargeKeyMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.DashKeyPressMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.PolarizedStoneToggleMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.StopChargeMessage;
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

    public static void sendDragonNightVision(boolean enabled) {
        NetworkHandler.sendToServer(new DragonNightVisionMessage(enabled));
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
}