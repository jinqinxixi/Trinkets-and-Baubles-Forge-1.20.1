package com.jinqinxixi.trinketsandbaubles.items.baubles;
import com.jinqinxixi.trinketsandbaubles.items.ModItem;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber
public class DragonsEyeFireItem extends DragonsEyeItem {

    public DragonsEyeFireItem(Properties properties) {
        super(properties);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            // 火免疫：火焰伤害 + 冰火传说的龙火伤害。
            boolean fire = event.getSource().is(DamageTypeTags.IS_FIRE)
                    || event.getSource().is(ResourceKey.create(Registries.DAMAGE_TYPE,
                            new ResourceLocation("iceandfire", "dragon_fire")));

            if (fire) {
                if (CuriosApi.getCuriosInventory(player).resolve().isPresent() &&
                        CuriosApi.getCuriosInventory(player).resolve().get()
                                .findFirstCurio(ModItem.DRAGONS_EYE_FIRE.get()).isPresent()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
    }
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.trinketsandbaubles.dragons_eye_fire.effect")
                .withStyle(ChatFormatting.RED));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
