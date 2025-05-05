package com.jinqinxixi.trinketsandbaubles.mixin;

import com.jinqinxixi.trinketsandbaubles.config.ModifierConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import top.theillusivec4.curios.client.ClientEventHandler;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;

import java.util.UUID;

@Mixin(ClientEventHandler.class)
public class CuriosTooltipMixin {

//    @Redirect(
//            method = "onTooltip",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Ltop/theillusivec4/curios/api/CuriosApi;getAttributeModifiers(Ltop/theillusivec4/curios/api/SlotContext;Ljava/util/UUID;Lnet/minecraft/world/item/ItemStack;)Lcom/google/common/collect/Multimap;"
//            ),
//            remap = false
//
//    )
//    private Multimap<Attribute, AttributeModifier> redirectGetAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
//        if (!TrinketsandBaublesMod.MIXIN_ENABLED) {
//            return CuriosApi.getAttributeModifiers(slotContext, uuid, stack);
//        }
//
//        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
//
//        if (ModifierConfig.load().isItemModifiable(itemId)) {
//            // 如果物品在配置中，返回空的 Multimap
//            return HashMultimap.create();
//        }
//
//        // 否则调用原始方法
//        return CuriosApi.getAttributeModifiers(slotContext, uuid, stack);
//    }
}