package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber
public class DragonsEyeFireItem extends DragonsEyeItem {

    public DragonsEyeFireItem(Properties properties) {
        super(properties);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingAttackEvent event) {   //LivingAttackEvent fires before LivingHurtEvent, I think you will prefer this.
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            // 检查是否是龙火伤害
            if (event.getSource().is(ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation("iceandfire", "dragon_fire")))) {

                // 检查玩家是否有这个效果
                if (CuriosApi.getCuriosHelper().findEquippedCurio(ModItem.DRAGON_FIRE_EYES.get(), player).isPresent()) {
                    // 取消伤害
                    event.setCanceled(true);
                }
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);

        if (slotContext.entity() instanceof Player player) {
            // 添加永久效果（-1表示持续时间无限）
            player.addEffect(new MobEffectInstance(ModEffects.FIRE_RESISTANCE.get(), -1, 0, true, true));
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (slotContext.entity() instanceof Player player) {
            // 移除效果
            player.removeEffect(ModEffects.FIRE_RESISTANCE.get());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.trinketsandbaubles.dragons_eye_fire.effect")
                .withStyle(ChatFormatting.RED));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
