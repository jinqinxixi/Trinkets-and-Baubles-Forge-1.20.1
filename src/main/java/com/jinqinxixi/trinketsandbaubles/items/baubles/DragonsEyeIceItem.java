package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

public class DragonsEyeIceItem extends DragonsEyeItem {
    private static final int FROST_RANGE = 2;
    private static BlockPos lastFrostPos = null; // 添加跟踪上一个位置
    public DragonsEyeIceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // 先执行父类的逻辑
        super.curioTick(slotContext, stack);

        // 确保在服务器端执行
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        Level level = player.level();
        BlockPos currentPos = player.blockPosition().below();

        // 如果上一个冰霜方块位置存在且不是当前位置，则还原为水
        if (lastFrostPos != null && !lastFrostPos.equals(currentPos)) {
            if (level.getBlockState(lastFrostPos).is(Blocks.FROSTED_ICE)) {
                level.setBlock(lastFrostPos, Blocks.WATER.defaultBlockState(), 3);
            }
            lastFrostPos = null;
        }

        // 如果玩家站在水面上且不在水中，创建霜冰路径
        if (canFreezeWater(level, currentPos)) {
            createFrostPath(player);
            lastFrostPos = currentPos.immutable();
        }
    }

    private static boolean canFreezeWater(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.WATER)) {
            return false;
        }
        if (!state.getFluidState().isSource()) {
            return false;
        }
        return level.getBlockState(pos.above()).isAir();
    }

    private static void createFrostPath(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        for (int x = -FROST_RANGE; x <= FROST_RANGE; x++) {
            for (int z = -FROST_RANGE; z <= FROST_RANGE; z++) {
                BlockPos pos = playerPos.offset(x, -1, z);
                if (canFreezeWater(serverLevel, pos)) {
                    serverLevel.setBlock(pos, Blocks.FROSTED_ICE.defaultBlockState(), 3);
                }
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);

        if (slotContext.entity() instanceof Player player) {
            player.addEffect(new MobEffectInstance(ModEffects.ICE_RESISTANCE.get(), -1, 0, true, true));
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (slotContext.entity() instanceof Player player) {
            player.removeEffect(ModEffects.ICE_RESISTANCE.get());
            // 移除最后的冰霜方块
            if (lastFrostPos != null && !player.level().isClientSide) {
                if (player.level().getBlockState(lastFrostPos).is(Blocks.FROSTED_ICE)) {
                    player.level().setBlock(lastFrostPos, Blocks.WATER.defaultBlockState(), 3);
                }
                lastFrostPos = null;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.trinketsandbaubles.dragons_eye_ice.effect")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.trinketsandbaubles.dragons_eye_ice.effect1")
                .withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}