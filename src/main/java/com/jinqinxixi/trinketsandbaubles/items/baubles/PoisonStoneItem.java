package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.config.Config;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber
public class PoisonStoneItem extends ModifiableBaubleItem {

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    public PoisonStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        slotContext.entity().removeEffect(MobEffects.POISON);
        slotContext.entity().removeEffect(MobEffects.HUNGER);
    }

    // 检查是否装备
    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof PoisonStoneItem))
                .isPresent();
    }

    // 处理攻击事件
    private static void onAttack(LivingEntity attacker, LivingEntity target) {
        if (isEquipped(attacker)) {
            if (attacker.getRandom().nextFloat() < Config.POISON_STONE_CHANCE.get()) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.POISON,
                        Config.POISON_STONE_DURATION.get(),
                        Config.POISON_STONE_AMPLIFIER.get(),
                        false, // 是否显示粒子
                        true  // 是否显示图标
                ));
            }
        }
    }

    // 获取额外伤害倍率
    private static float getExtraDamageMultiplier(LivingEntity attacker, LivingEntity target) {
        if (isEquipped(attacker) && target.hasEffect(MobEffects.POISON)) {
            return Config.POISON_STONE_DAMAGE_MULTIPLIER.get().floatValue();
        }
        return 1.0F;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            LivingEntity target = event.getEntity();

            // 处理中毒效果
            onAttack(attacker, target);

            // 处理额外伤害
            float multiplier = getExtraDamageMultiplier(attacker, target);
            if (multiplier > 1.0F) {
                event.setAmount(event.getAmount() * multiplier);
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
        tooltip.add(Component.translatable("item.trinketsandbaubles.poison_stone.tooltip.immunity_poison")
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.trinketsandbaubles.poison_stone.tooltip.immunity_hunger")
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.trinketsandbaubles.poison_stone.tooltip.poison_chance",
                        (int)(Config.POISON_STONE_CHANCE.get() * 100))
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.trinketsandbaubles.poison_stone.tooltip.damage_bonus",
                        String.format("%.1f", Config.POISON_STONE_DAMAGE_MULTIPLIER.get()))
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}