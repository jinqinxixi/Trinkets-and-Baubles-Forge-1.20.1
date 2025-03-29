package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;

import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

public class EnderQueensCrownItem extends ModifiableBaubleItem {

    // NBT 标签常量
    public static final String CROWN_CONTROLLED_TAG = "CrownControlled";
    public static final String CROWN_SUMMONED_TAG = "CrownSummoned";
    public static final String FRIENDLY_ENDERMAN_TAG = "FriendlyEnderman";

    public EnderQueensCrownItem(Properties properties) {
        super(properties);
    }
    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.ender_queens_crown.tooltip.enderman_follow"));
        tooltip.add(Component.translatable("item.trinketsandbaubles.ender_queens_crown.tooltip.damage_immunity",
                (int) (ModConfig.DAMAGE_IMMUNITY_CHANCE.get() * 100)).withStyle(ChatFormatting.DARK_RED));
        if (ModConfig.WATER_DAMAGE_ENABLED.get()) {
            tooltip.add(Component.translatable("item.trinketsandbaubles.ender_queens_crown.tooltip.water_damage"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (slotContext.entity() instanceof Player player && !player.level().isClientSide) {
            if (ModConfig.WATER_DAMAGE_ENABLED.get() && player.isInWater()) {
                player.hurt(player.damageSources().drown(), 1.0F);
            }

            AABB searchBox = player.getBoundingBox().inflate(ModConfig.ENDERMAN_FOLLOW_RANGE.get());
            player.level().getEntitiesOfClass(EnderMan.class, searchBox).forEach(enderman -> {
                if (enderman.getTarget() == player) {
                    enderman.setTarget(null);
                    setEndermanToFollow(enderman, player);
                }

                boolean isControlled = enderman.getPersistentData().getBoolean(CROWN_CONTROLLED_TAG);
                boolean isSummoned = enderman.getPersistentData().getBoolean(CROWN_SUMMONED_TAG);

                if (!isControlled && !isSummoned) {
                    setEndermanToFollow(enderman, player);
                } else {
                    if (enderman.getTarget() == null || enderman.getTarget().isDeadOrDying()) {
                        enderman.setTarget(null);
                        double followDistance = enderman.distanceToSqr(player);

                        if (followDistance > 256) {
                            enderman.randomTeleport(
                                    player.getX() + (player.getRandom().nextDouble() - 0.5) * 8.0,
                                    player.getY(),
                                    player.getZ() + (player.getRandom().nextDouble() - 0.5) * 8.0,
                                    true
                            );
                        } else if (followDistance > 25) {
                            enderman.getNavigation().moveTo(player, 1.0);
                        }
                    }
                }
            });
        }
    }
    private static void setEndermanToFollow(EnderMan enderman, Player player) {
        enderman.setTarget(null);
        enderman.setPersistenceRequired();
        enderman.getPersistentData().putBoolean(CROWN_CONTROLLED_TAG, true);
        enderman.getPersistentData().putBoolean(FRIENDLY_ENDERMAN_TAG, true);

        enderman.getNavigation().setCanFloat(true);
        enderman.addTag("crown_controlled");

        if (enderman.distanceToSqr(player) > 100) {
            enderman.randomTeleport(
                    player.getX() + (player.getRandom().nextDouble() - 0.5) * 4.0,
                    player.getY(),
                    player.getZ() + (player.getRandom().nextDouble() - 0.5) * 4.0,
                    true
            );
        }
    }

    // 检查是否装备
    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof EnderQueensCrownItem))
                .isPresent();
    }

    // 处理伤害事件
    public static boolean onDamage(LivingEntity entity, DamageSource source) {
        if (!isEquipped(entity) || entity.level().isClientSide) {
            return false;
        }

        if (!canImmuneDamageType(source)) {
            return false;
        }

        if (entity.getRandom().nextFloat() < ModConfig.DAMAGE_IMMUNITY_CHANCE.get()) {
            teleportRandomly(entity);
            summonEnderman(entity);
            if (source.getEntity() instanceof LivingEntity attacker) {
                aggroNearbyEndermen(entity, attacker);
            }
            return true;
        }

        return false;
    }

    private static boolean canImmuneDamageType(DamageSource source) {
        // 可以免疫的伤害类型
        return !source.is(DamageTypes.FALL) &&    // 不能免疫摔落伤害
                !source.is(DamageTypes.IN_FIRE) && // 不能免疫火焰伤害
                !source.is(DamageTypes.ON_FIRE) && // 不能免疫燃烧伤害
                !source.is(DamageTypes.DROWN) &&   // 不能免疫溺水伤害
                !source.is(DamageTypes.WITHER) &&  // 不能免疫凋零伤害
                !source.is(DamageTypes.MAGIC);     // 不能免疫魔法伤害（中毒）
    }

    private static void teleportRandomly(LivingEntity entity) {
        RandomSource random = entity.getRandom();
        double range = ModConfig.TELEPORT_RANGE.get();
        double d0 = entity.getX() + (random.nextDouble() - 0.5D) * range;
        double d1 = entity.getY() + (random.nextDouble() - 0.5D) * range;
        double d2 = entity.getZ() + (random.nextDouble() - 0.5D) * range;

        if (entity.randomTeleport(d0, d1, d2, true)) {
            entity.level().playSound(null,
                    entity.xo, entity.yo, entity.zo,
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS,
                    1.0F, 1.0F);
        }
    }

    private static void summonEnderman(LivingEntity entity) {
        Level level = entity.level();
        EnderMan enderman = EntityType.ENDERMAN.create(level);
        if (enderman != null) {
            enderman.moveTo(entity.getX(), entity.getY(), entity.getZ());
            enderman.getPersistentData().putBoolean(CROWN_SUMMONED_TAG, true);
            enderman.getPersistentData().putBoolean(FRIENDLY_ENDERMAN_TAG, true);
            if (entity instanceof Player player) {
                setEndermanToFollow(enderman, player);
            }
            level.addFreshEntity(enderman);
        }
    }


    public static void aggroNearbyEndermen(LivingEntity defender, LivingEntity attacker) {
        if (defender.level().isClientSide) return;

        if (attacker instanceof Player && defender instanceof EnderMan defenderEnderman) {
            boolean isDefenderFriendly = defenderEnderman.getPersistentData().getBoolean(FRIENDLY_ENDERMAN_TAG);
            if (isDefenderFriendly) {
                return;
            }
        }

        if (attacker instanceof EnderMan attackerEnderman) {
            boolean isAttackerFriendly = attackerEnderman.getPersistentData().getBoolean(FRIENDLY_ENDERMAN_TAG);
            if (isAttackerFriendly) {
                return;
            }
        }

        AABB searchBox = defender.getBoundingBox().inflate(ModConfig.ENDERMAN_FOLLOW_RANGE.get());
        defender.level().getEntitiesOfClass(EnderMan.class, searchBox).forEach(enderman -> {
            boolean isControlled = enderman.getPersistentData().getBoolean(CROWN_CONTROLLED_TAG);
            boolean isSummoned = enderman.getPersistentData().getBoolean(CROWN_SUMMONED_TAG);

            if (isControlled || isSummoned) {
                if (!(attacker instanceof Player) &&
                        !(attacker instanceof EnderMan &&
                                ((EnderMan) attacker).getPersistentData().getBoolean(FRIENDLY_ENDERMAN_TAG))) {
                    enderman.setTarget(attacker);

                    if (enderman.distanceToSqr(attacker) > 100) {
                        enderman.randomTeleport(
                                attacker.getX() + (attacker.getRandom().nextDouble() - 0.5) * 4.0,
                                attacker.getY(),
                                attacker.getZ() + (attacker.getRandom().nextDouble() - 0.5) * 4.0,
                                true
                        );
                    }
                }
            }
        });
    }
    @Override
    public int getEnchantmentValue() {
        return 0; // 附魔等级为0
    }

    // 禁止任何形式的附魔（包括铁砧）
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack); // 调用父类初始化逻辑 [^2]
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

    }
}