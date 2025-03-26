    package com.jinqinxixi.trinketsandbaubles.capability.shrink;


    import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
    import net.minecraft.client.Minecraft;
    import net.minecraft.nbt.CompoundTag;
    import net.minecraft.network.FriendlyByteBuf;
    import net.minecraft.world.entity.Entity;
    import net.minecraft.world.entity.LivingEntity;
    import net.minecraftforge.network.NetworkEvent;

    import java.util.function.Supplier;

    public class PacketSyncShrink {
        private final int entityId;
        private final CompoundTag data;

        public PacketSyncShrink(int entityId, CompoundTag data) {
            this.entityId = entityId;
            this.data = data;
        }

        public static void encode(PacketSyncShrink msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
            buf.writeNbt(msg.data);
        }

        public static PacketSyncShrink decode(FriendlyByteBuf buf) {
            return new PacketSyncShrink(buf.readInt(), buf.readNbt());
        }

        public static void handle(PacketSyncShrink msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    TrinketsandBaublesMod.LOGGER.debug("Received shrink sync packet for entity: {}",
                            livingEntity.getName().getString());
                    livingEntity.getCapability(ModCapabilities.SHRINK_CAPABILITY).ifPresent(cap -> {
                        cap.deserializeNBT(msg.data);
                        TrinketsandBaublesMod.LOGGER.debug("Updated shrink state: isShrunk={}, scale={}",
                                cap.isShrunk(), cap.scale());
                        // 强制更新实体大小
                        livingEntity.refreshDimensions();
                    });
                }
            });
            ctx.get().setPacketHandled(true);
        }

    }