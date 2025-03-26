package com.jinqinxixi.trinketsandbaubles.mixin;

import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class BlockStateMixin {

    @Inject(method = "hasCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void onHasCorrectToolForDrops(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (player.hasEffect(ModEffects.DWARVES.get())) {
            int requiredTier = getRequiredTier(state);
            if (requiredTier > 0) {
                int adjustedTier = requiredTier - 1;
                ItemStack tool = player.getMainHandItem();
                int toolTier = getToolTier(tool);
                if (toolTier >= adjustedTier) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    private int getRequiredTier(BlockState state) {
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return 4;
        } else if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
            return 3;
        } else if (state.is(BlockTags.NEEDS_STONE_TOOL)) {
            return 2;
        } else {
            return 0;
        }
    }

    private int getToolTier(ItemStack tool) {
        if (tool.getItem() instanceof DiggerItem diggerItem) {
            return getTierLevel(diggerItem.getTier());
        }
        return 0;
    }

    private int getTierLevel(net.minecraft.world.item.Tier tier) {
        // 根据工具材质返回对应的等级
        if (tier == net.minecraft.world.item.Tiers.NETHERITE) {
            return 5;
        } else if (tier == net.minecraft.world.item.Tiers.DIAMOND) {
            return 4;
        } else if (tier == net.minecraft.world.item.Tiers.IRON) {
            return 3;
        } else if (tier == net.minecraft.world.item.Tiers.STONE) {
            return 2;
        } else if (tier == net.minecraft.world.item.Tiers.WOOD || tier == net.minecraft.world.item.Tiers.GOLD) {
            return 1;
        }
        return 0;
    }
}