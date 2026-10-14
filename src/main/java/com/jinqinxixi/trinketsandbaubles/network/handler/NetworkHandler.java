package com.jinqinxixi.trinketsandbaubles.network.handler;

import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.*;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.DragonsEyeToggleMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.GreedyEyesSyncMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.UpdateEffectsMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage.UpdateTargetsMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.ManaMessage.ManaSyncMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.SilentRocketPacket;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.ChargeKeyMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.DashKeyPressMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.PolarizedStoneToggleMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.StopChargeMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.WolfRiderKeyMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.RaceAbilityMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.EffectsRenderPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;


public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0";
    private static int packetId = 0;

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TrinketsandBaublesMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        int id = 0; // 使用明确的ID计数

        // 首先注册需要在两端都注册的消息
        registerCommonMessages();

        // 然后注册服务器端接收的消息
        registerServerBoundMessages();

        // 最后注册客户端接收的消息
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> registerClientOnlyMessages());
    }

    // 在两端都需要注册的消息
    private static void registerCommonMessages() {
        // 魔力同步消息需要在两端都注册
        INSTANCE.messageBuilder(ManaSyncMessage.class, nextId())
                .encoder(ManaSyncMessage::encode)
                .decoder(ManaSyncMessage::decode)
                .consumerMainThread(ManaSyncMessage::handle)
                .add();

        // 添加龙息同步消息
        INSTANCE.messageBuilder(SyncDragonBreathMessage.class, nextId())
                .encoder(SyncDragonBreathMessage::encode)
                .decoder(SyncDragonBreathMessage::decode)
                .consumerMainThread(SyncDragonBreathMessage::handle)
                .add();

        INSTANCE.messageBuilder(UpdateTargetsMessage.class, nextId())
                .encoder(UpdateTargetsMessage::encode)
                .decoder(UpdateTargetsMessage::decode)
                .consumerMainThread(UpdateTargetsMessage::handle)
                .add();

        // 贪婪之眼同步包（服务端发、客户端收），两端注册，见 PLAN 第 6.5 节。
        INSTANCE.messageBuilder(GreedyEyesSyncMessage.class, nextId())
                .encoder(GreedyEyesSyncMessage::encode)
                .decoder(GreedyEyesSyncMessage::decode)
                .consumerMainThread(GreedyEyesSyncMessage::handle)
                .add();

        // 滑翔推进的静音火箭同步（服务端发、客户端收），两端注册。
        INSTANCE.messageBuilder(SilentRocketPacket.class, nextId())
                .encoder(SilentRocketPacket::encode)
                .decoder(SilentRocketPacket::decode)
                .consumerMainThread(SilentRocketPacket::handle)
                .add();

        INSTANCE.messageBuilder(DragonFlightToggleMessage.class, nextId())
                .encoder(DragonFlightToggleMessage::encode)
                .decoder(DragonFlightToggleMessage::decode)
                .consumerMainThread(DragonFlightToggleMessage::handle)
                .add();

        INSTANCE.messageBuilder(SyncAllDragonStatesMessage.class, nextId())
                .encoder(SyncAllDragonStatesMessage::encode)
                .decoder(SyncAllDragonStatesMessage::decode)
                .consumerMainThread(SyncAllDragonStatesMessage::handle)
                .add();

        // 服务端→客户端的特效渲染（闪电箭、魔法球、龙息等自定义粒子）。必须两端都注册：服务端要
        // 发送它，客户端要接收它；只注册在客户端会让服务端发送时因 id 未注册而静默失败，特效谁都
        // 看不见。
        INSTANCE.messageBuilder(EffectsRenderPacket.class, nextId())
                .encoder(EffectsRenderPacket::encode)
                .decoder(EffectsRenderPacket::new)
                .consumerMainThread(EffectsRenderPacket::handle)
                .add();

    }

    private static void registerServerBoundMessages() {
        // 服务器接收的消息
        INSTANCE.messageBuilder(DragonBreathMessage.class, nextId())
                .encoder(DragonBreathMessage::encode)
                .decoder(DragonBreathMessage::decode)
                .consumerMainThread(DragonBreathMessage::handle)
                .add();

        INSTANCE.messageBuilder(StopDragonBreathMessage.class, nextId())
                .encoder(StopDragonBreathMessage::encode)
                .decoder(StopDragonBreathMessage::decode)
                .consumerMainThread(StopDragonBreathMessage::handle)
                .add();


        INSTANCE.messageBuilder(DashKeyPressMessage.class, nextId())
                .encoder(DashKeyPressMessage::encode)
                .decoder(DashKeyPressMessage::decode)
                .consumerMainThread(DashKeyPressMessage::handle)
                .add();

        INSTANCE.messageBuilder(StopChargeMessage.class, nextId())
                .encoder(StopChargeMessage::encode)
                .decoder(StopChargeMessage::decode)
                .consumerMainThread(StopChargeMessage::handle)
                .add();


        INSTANCE.messageBuilder(ChargeKeyMessage.class, nextId())
                .encoder(ChargeKeyMessage::encode)
                .decoder(ChargeKeyMessage::decode)
                .consumerMainThread(ChargeKeyMessage::handle)
                .add();

        INSTANCE.messageBuilder(DragonsEyeToggleMessage.class, nextId())
                .encoder(DragonsEyeToggleMessage::encode)
                .decoder(DragonsEyeToggleMessage::decode)
                .consumerMainThread(DragonsEyeToggleMessage::handle)
                .add();

        INSTANCE.messageBuilder(UpdateEffectsMessage.class, nextId())
                .encoder(UpdateEffectsMessage::encode)
                .decoder(UpdateEffectsMessage::decode)
                .consumerMainThread(UpdateEffectsMessage::handle)
                .add();

        // Gliding: a glide starting, and a push being asked for.
        INSTANCE.messageBuilder(com.jinqinxixi.trinketsandbaubles.network.message.Messages.GlideMessage.class, nextId())
                .encoder(com.jinqinxixi.trinketsandbaubles.network.message.Messages.GlideMessage::encode)
                .decoder(com.jinqinxixi.trinketsandbaubles.network.message.Messages.GlideMessage::decode)
                .consumerMainThread(com.jinqinxixi.trinketsandbaubles.network.message.Messages.GlideMessage::handle)
                .add();

        // 哥布林骑狼按键
        INSTANCE.messageBuilder(WolfRiderKeyMessage.class, nextId())
                .encoder(WolfRiderKeyMessage::encode)
                .decoder(WolfRiderKeyMessage::decode)
                .consumerMainThread(WolfRiderKeyMessage::handle)
                .add();

        // 种族技能键（金牛踏击等）
        INSTANCE.messageBuilder(RaceAbilityMessage.class, nextId())
                .encoder(RaceAbilityMessage::encode)
                .decoder(RaceAbilityMessage::decode)
                .consumerMainThread(RaceAbilityMessage::handle)
                .add();
    }

    // 仅客户端需要的消息
    private static void registerClientOnlyMessages() {


        INSTANCE.messageBuilder(PolarizedStoneToggleMessage.class, nextId())
                .encoder(PolarizedStoneToggleMessage::encode)
                .decoder(PolarizedStoneToggleMessage::decode)
                .consumerMainThread(PolarizedStoneToggleMessage::handle)
                .add();

    }

    // 发送消息到客户端的方法
    public static void sendToClient(Object message, ServerPlayer player) {
        if (!player.level().isClientSide()) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }

    // 发送消息到服务器的方法
    public static void sendToServer(Object message) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            INSTANCE.sendToServer(message);
        }
    }
}