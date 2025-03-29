package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

public class ShieldofHonorItem extends ModifiableBaubleItem {

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    public ShieldofHonorItem(Properties properties) {
        super(properties);
    }

    private static void spawnProtectionParticles(Player player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        RandomSource random = level.getRandom();
        for (int i = 0; i < 16; i++) {
            double d0 = random.nextGaussian() * 0.02;
            double d1 = random.nextGaussian() * 0.02;
            double d2 = random.nextGaussian() * 0.02;

            double x = player.getX() + (random.nextDouble() - 0.5) * player.getBbWidth();
            double y = player.getY() + 0.5 + (random.nextDouble() - 0.5) * player.getBbHeight();
            double z = player.getZ() + (random.nextDouble() - 0.5) * player.getBbWidth();

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    x, y, z,
                    1,
                    d0, d1, d2,
                    0.0);
        }
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof ShieldofHonorItem))
                .isPresent();
    }

    public static float onDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!isEquipped(entity) || entity.level().isClientSide()) {
            return amount;
        }

        if (!canImmuneDamageType(source)) {
            return amount;
        }

        // 应用基础伤害减免
        if (!entity.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            amount *= (1.0f - ModConfig.SHIELD_DAMAGE_REDUCTION.get().floatValue());
        }

        // 爆炸伤害特殊处理
        if (source.is(DamageTypes.EXPLOSION) ||
                source.is(DamageTypes.PLAYER_EXPLOSION) ||
                source.is(DamageTypes.FIREWORKS)) {
            amount *= ModConfig.SHIELD_EXPLOSION_REDUCTION.get().floatValue();
        }

        // 获取伤害计数
        int damageCount = entity.getPersistentData().getInt("shield_damage_count");

        if (damageCount >= ModConfig.SHIELD_MAX_DAMAGE_COUNT.get()) {
            if (entity instanceof Player player) {
                spawnProtectionParticles(player);
                player.level().playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ANVIL_LAND,
                        SoundSource.PLAYERS,
                        0.5F,
                        1.5F);
            }
            entity.getPersistentData().putInt("shield_damage_count", 0);
            return 0;
        }

        entity.getPersistentData().putInt("shield_damage_count", damageCount + 1);
        return amount;
    }

    private static boolean canImmuneDamageType(DamageSource source) {
        return !source.is(DamageTypes.FALL) &&
                !source.is(DamageTypes.IN_FIRE) &&
                !source.is(DamageTypes.ON_FIRE) &&
                !source.is(DamageTypes.WITHER) &&
                !source.is(DamageTypes.MAGIC) &&
                !source.is(DamageTypes.DROWN);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.shield_of_honor.tooltip1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.trinketsandbaubles.shield_of_honor.tooltip2")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.trinketsandbaubles.shield_of_honor.tooltip.explosion",
                        String.format("%.0f", (1.0f - ModConfig.SHIELD_EXPLOSION_REDUCTION.get()) * 100))
                .withStyle(ChatFormatting.BLUE));

        tooltip.add(Component.translatable("item.trinketsandbaubles.shield_of_honor.tooltip4",
                        ModConfig.SHIELD_MAX_DAMAGE_COUNT.get())
                .withStyle(ChatFormatting.AQUA));

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            boolean hasResistance = player.hasEffect(MobEffects.DAMAGE_RESISTANCE);
            if (!hasResistance) {
                tooltip.add(Component.translatable("item.trinketsandbaubles.shield_of_honor.tooltip.active",
                                (int) (ModConfig.SHIELD_DAMAGE_REDUCTION.get() * 100))
                        .withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.translatable("item.trinketsandbaubles.shield_of_honor.tooltip.inactive")
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("item.trinketsandbaubles.shield_of_honor.tooltip.base",
                            (int) (ModConfig.SHIELD_DAMAGE_REDUCTION.get() * 100))
                    .withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, level, tooltip, flag);
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