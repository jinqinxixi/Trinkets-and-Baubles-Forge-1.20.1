package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

public class DragonsEyeItem extends ModifiableBaubleItem {
    public static final String TAG_IS_INITIALIZED = "IsInitialized";

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    public DragonsEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        if (!stack.getOrCreateTag().getBoolean(TAG_IS_INITIALIZED)) {
            stack.getOrCreateTag().putBoolean(TAG_IS_INITIALIZED, true);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (slotContext.entity() instanceof ServerPlayer player) {
            player.removeEffect(MobEffects.FIRE_RESISTANCE);
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        String nightVisionKeyName = KeyBindings.DRAGON_NIGHT_VISION_KEY.getKey().getDisplayName().getString();
        String toggleModeKeyName = KeyBindings.TOGGLE_DRAGONS_EYE_MODE.getKey().getDisplayName().getString();
        tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_eye.tooltip1")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_eye.tooltip2",
                        nightVisionKeyName)  // 传入当前设置的键位名称
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.dragons_eye.tooltip3",
                        toggleModeKeyName,   // 传入切换键位名称
                        ModConfig.RENDER_RANGE.get()) // 传入扫描范围
                .withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}