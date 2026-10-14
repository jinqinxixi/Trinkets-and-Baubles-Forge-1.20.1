package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

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

    /**
     * Keeps the resistance up, every tick, for as long as the shield is worn.
     *
     * <p>Here rather than in the damage handler, and that is the whole of it: a resistance that is only
     * refreshed by being hit is a resistance a player never sees, because the effect a shield grants
     * should be on them from the moment they put it on. The resistance is kept up from the item's
     * tick handler, so it is there from the moment the shield is worn.
     */
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        keepResistance(slotContext.entity());
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        // The resistance goes with the shield. Left behind it would run its two hundred ticks and hold
        // a player at damage reduction they are no longer wearing anything for.
        MobEffectInstance active = slotContext.entity().getEffect(MobEffects.DAMAGE_RESISTANCE);

        if (active != null && active.getDuration() <= RESISTANCE_DURATION) {
            slotContext.entity().removeEffect(MobEffects.DAMAGE_RESISTANCE);
        }
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

    /** Whether this entity is wearing the shield. */
    public static boolean hasShield(LivingEntity entity) {
        return isEquipped(entity);
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof ShieldofHonorItem))
                .isPresent();
    }

    /** How long the resistance the shield keeps lasts, refreshed on every hit. */
    private static final int RESISTANCE_DURATION = 200;

    /**
     * Keeps a resistance effect on the wearer.
     *
     * <p>Applied only when there is none. Nothing is refreshed: the effect lasts two hundred ticks and
     * the next tick after it lapses supplies a fresh one, so a wearer sees it blink off and on every
     * ten seconds.
     *
     * <p>Applying it only when absent is also why this needs no exemption from the stacking rule — the
     * rule only acts on an effect arriving where one already is, and this arrives where there is none.
     */
    private static void keepResistance(LivingEntity entity) {
        MobEffectInstance active = entity.getEffect(MobEffects.DAMAGE_RESISTANCE);

        // 没有抗性才加，抗性还在就什么都不做。抗性效果 200 tick 耗尽后下一 tick 补一个新的，
        // 会有「刚好消失、下一 tick 才补」的短暂空档，这是既有行为。
        if (active != null) {
            return;
        }

        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                RESISTANCE_DURATION, ModConfig.SHIELD_RESISTANCE_LEVEL.get(), false, false));
    }

    /**
     * Stacks one level onto a resistance the wearer already has, and says whether to refuse the effect.
     *
     * <p>Written to raise the incoming effect by one when there is room to raise it, and the answer
     * is whether the game should refuse the effect outright. The answer is
     * "no" in both cases that matter — an effect that was raised is applied by the game as the stronger
     * of the two it now has, and one that could not be raised is still a resistance the wearer may want.
     * The only refusal is an incoming effect weaker than the one already worn, which would otherwise
     * replace a better resistance with a worse one.
     *
     * <p>Nothing is remembered and nothing is flagged. The hook fires once per effect the game applies,
     * and raising the effect in place is enough: there is no second application for this to answer.
     *
     * @return whether the game should refuse the effect
     */
    public static boolean stackResistance(LivingEntity entity, MobEffectInstance incoming) {
        // Only stack on the server. The client may receive the same effect through sync and fire
        // Applicable again; stacking there would double-count the shield's own level.
        if (entity.level().isClientSide()) {
            return false;
        }

        if (incoming.getEffect() != MobEffects.DAMAGE_RESISTANCE) {
            return false;
        }

        MobEffectInstance active = entity.getEffect(MobEffects.DAMAGE_RESISTANCE);

        if (active == null) {
            return false;
        }

        int amplifier = incoming.getAmplifier() + 1;

        // A weaker effect than the one already worn, which the game would apply over it.
        if (active.getAmplifier() > amplifier) {
            return true;
        }

        if (amplifier <= ModConfig.SHIELD_RESISTANCE_STACK_LIMIT.get()) {
            incoming.update(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    incoming.getDuration(), amplifier, incoming.isAmbient(), incoming.isVisible()));
        }

        return false;
    }

    public static float onDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!isEquipped(entity) || entity.level().isClientSide()) {
            return amount;
        }

        if (!canImmuneDamageType(source)) {
            return amount;
        }

        // The shield does not scale the damage itself. It keeps a resistance effect on the wearer, and
        // the game applies that — which is why wearing a second source of resistance raises the level
        // rather than being refused: the effect is the shield's, and a second helping of it stacks.
        keepResistance(entity);

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

        // The stacking rule, which is what makes a second source of resistance worth something rather
        // than being refused. The level shown is the amplifier limit plus one, because amplifier 0 is
        // already level I.
        tooltip.add(Component.translatable("item.trinketsandbaubles.shield_of_honor.tooltip5",
                        ModConfig.SHIELD_RESISTANCE_STACK_LIMIT.get() + 1)
                .withStyle(ChatFormatting.AQUA));

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