package com.jinqinxixi.trinketsandbaubles.network.message.DragonsEyeMessage;

import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public class UpdateTargetsMessage {
    private final CompoundTag data;

    public UpdateTargetsMessage(ListTag targets) {
        this.data = new CompoundTag();
        this.data.put("Targets", targets);
    }

    private UpdateTargetsMessage(CompoundTag data) {
        this.data = data;
    }

    public static void encode(UpdateTargetsMessage message, FriendlyByteBuf buf) {
        buf.writeNbt(message.data);
    }

    public static UpdateTargetsMessage decode(FriendlyByteBuf buf) {
        CompoundTag compound = buf.readNbt();
        return new UpdateTargetsMessage(compound != null ? compound : new CompoundTag());
    }

    public static void handle(UpdateTargetsMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 使用 DistExecutor 确保只在客户端执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(message));
        });
        ctx.get().setPacketHandled(true);
    }

    // 这个方法只会在客户端被调用
    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private static void handleClient(UpdateTargetsMessage message) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player != null) {
            ListTag targets = message.data.getList("Targets", 10);

            // 更新龙眼饰品的数据
            CuriosApi.getCuriosInventory(minecraft.player).ifPresent(handler -> {
                handler.findFirstCurio(stack -> stack.getItem() instanceof DragonsEyeItem)
                        .ifPresent(curio -> {
                            ItemStack stack = curio.stack();
                            CompoundTag nbt = stack.getOrCreateTag();
                            nbt.put(DragonsEyeItem.TAG_DRAGONS_EYE_TARGETS, targets);
                        });
            });
        }
    }
}