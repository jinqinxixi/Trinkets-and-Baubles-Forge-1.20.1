package com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateEffectsMessage {
    private final MobEffectInstance effect;

    public UpdateEffectsMessage(MobEffectInstance effect) {
        this.effect = effect;
    }

    public static void encode(UpdateEffectsMessage message, FriendlyByteBuf buf) {
        buf.writeNbt(message.effect.save(new CompoundTag()));
    }

    public static UpdateEffectsMessage decode(FriendlyByteBuf buf) {
        return new UpdateEffectsMessage(MobEffectInstance.load(buf.readNbt()));
    }

    public static void handle(UpdateEffectsMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if(Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.addEffect(message.effect);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
