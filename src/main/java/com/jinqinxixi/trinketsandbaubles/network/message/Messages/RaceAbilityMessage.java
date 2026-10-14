package com.jinqinxixi.trinketsandbaubles.network.message.Messages;

import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.impl.TaurusCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 种族技能键：按当前激活种族分发到对应能力（金牛踏击、雷龙闪电箭等）。
 *
 * <p>{@code charge} 是蓄力比例 0~1（按住时长的平方曲线），服务端据此决定踏击伤害与击退。对
 * 闪电箭这类瞬时技能，该值忽略。
 */
public class RaceAbilityMessage
{
    private final float charge;

    public RaceAbilityMessage(float charge)
    {
        this.charge = charge;
    }

    public static RaceAbilityMessage decode(FriendlyByteBuf buf)
    {
        return new RaceAbilityMessage(buf.readFloat());
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeFloat(this.charge);
    }

    public static void handle(RaceAbilityMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();
            if (player != null)
            {
                // 金牛：踏击。
                player.getCapability(ModCapabilities.TAURUS_CAPABILITY).ifPresent(cap ->
                {
                    if (cap.isActive())
                    {
                        ((TaurusCapability) cap).handleStampede(message.charge);
                    }
                });

                // 雷龙：闪电箭（蓄力比例由 charge 携带，和金牛踏击共用同一套 press/release 计时）。
                player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap ->
                {
                    if (cap.isActive() && cap instanceof DragonCapability dragon)
                    {
                        dragon.handleLightningBolt(message.charge);
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}