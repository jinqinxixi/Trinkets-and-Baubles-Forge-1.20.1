package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class FaelisClawItem extends ModifiableBaubleItem {

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("c0c52c20-4015-11ee-be56-0242ac120002");

    private static AttributeModifier getAttackDamageModifier() {
        return new AttributeModifier(
                ATTACK_DAMAGE_UUID,
                "FaelisClawDamageBoost",
                ModConfig.FAELIS_CLAW_DAMAGE_BOOST.get(),
                AttributeModifier.Operation.MULTIPLY_BASE
        );
    }

    public FaelisClawItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        if (slotContext.entity() instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) slotContext.entity();
            applyAttributes(living);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        if (slotContext.entity() instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) slotContext.entity();
            removeAttributes(living);
        }
    }


    private static void applyAttributes(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance != null) {
            // 先移除已存在的修饰符
            instance.removeModifier(ATTACK_DAMAGE_UUID);
            // 添加新的修饰符
            instance.addPermanentModifier(getAttackDamageModifier());
        }
    }

    private static void removeAttributes(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance != null) {
            instance.removeModifier(ATTACK_DAMAGE_UUID);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (slotContext.entity() instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) slotContext.entity();
            AttributeInstance instance = living.getAttribute(Attributes.ATTACK_DAMAGE);

            if (instance != null) {
                // 检查是否已有我们的修饰符
                boolean hasOurModifier = instance.getModifiers().stream()
                        .anyMatch(mod -> mod.getId().equals(ATTACK_DAMAGE_UUID));

                // 如果没有我们的修饰符，重新应用它
                if (!hasOurModifier) {
                    applyAttributes(living);
                }

                // 验证修饰符的值是否正确
                instance.getModifiers().stream()
                        .filter(mod -> mod.getId().equals(ATTACK_DAMAGE_UUID))
                        .findFirst()
                        .ifPresent(existingMod -> {
                            if (Math.abs(existingMod.getAmount() - ModConfig.FAELIS_CLAW_DAMAGE_BOOST.get()) > 0.0001) {
                                // 如果值不正确，重新应用修饰符
                                removeAttributes(living);
                                applyAttributes(living);
                            }
                        });
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.faelis_claw.tooltip.damage_boost",
                        String.format("%.0f", ModConfig.FAELIS_CLAW_DAMAGE_BOOST.get() * 100))
                .withStyle(ChatFormatting.RED));

        tooltip.add(Component.translatable("item.trinketsandbaubles.faelis_claw.tooltip.bleed_chance",
                        String.format("%.0f", ModConfig.FAELIS_CLAW_BLEED_CHANCE.get() * 100))
                .withStyle(ChatFormatting.DARK_RED));

        tooltip.add(Component.translatable("item.trinketsandbaubles.faelis_claw.tooltip.bleed_info",
                        1.0F,
                        ModConfig.FAELIS_CLAW_BLEED_DURATION.get() / 20)
                .withStyle(ChatFormatting.DARK_GREEN));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof FaelisClawItem))
                .isPresent();
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player) {
            Player player = (Player) event.getSource().getEntity();
            if (isEquipped(player) && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();

                // 检查是否激活了猫妖能力
                player.getCapability(ModCapabilities.FAELES_CAPABILITY).ifPresent(cap -> {
                    if (player.getRandom().nextFloat() < ModConfig.FAELIS_CLAW_BLEED_CHANCE.get()) {
                        int baseDuration = ModConfig.FAELIS_CLAW_BLEED_DURATION.get();
                        // 使用猫妖能力状态来判断
                        int duration = cap.isActive() ?
                                baseDuration :
                                (int)(baseDuration * ModConfig.FAELIS_CLAW_NORMAL_DURATION_MULTIPLIER.get());

                        target.addEffect(new MobEffectInstance(ModEffects.BLEEDING.get(), duration));
                    }
                });
            }
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
}