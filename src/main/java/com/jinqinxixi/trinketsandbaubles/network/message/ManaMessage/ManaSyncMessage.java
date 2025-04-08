package com.jinqinxixi.trinketsandbaubles.network.message.ManaMessage;

import com.jinqinxixi.trinketsandbaubles.network.handler.ClientManaNetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ManaSyncMessage {
    private final float mana;
    private final float maxMana;

    public ManaSyncMessage(float mana, float maxMana) {
        this.mana = mana;
        this.maxMana = maxMana;
    }

    // 编码消息数据
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(mana);
        buf.writeFloat(maxMana);
    }

    // 解码消息数据
    public static ManaSyncMessage decode(FriendlyByteBuf buf) {
        return new ManaSyncMessage(
                buf.readFloat(),
                buf.readFloat()
        );
    }

    // 消息处理
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 保证只在客户端执行处理逻辑
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientManaNetworkHandler.handleManaSync(this)
            );
        });
        ctx.get().setPacketHandled(true);
    }

    public float getMana() { return mana; }
    public float getMaxMana() { return maxMana; }
}