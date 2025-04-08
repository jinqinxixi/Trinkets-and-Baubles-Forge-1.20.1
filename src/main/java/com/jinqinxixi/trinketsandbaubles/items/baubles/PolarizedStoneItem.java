package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

public class PolarizedStoneItem extends ModifiableBaubleItem {

    // NBT 标签常量
    public static final String DEFLECTION_MODE_TAG = "DeflectionMode";
    public static final String ATTRACTION_MODE_TAG = "AttractionMode";



    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    public PolarizedStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        boolean deflectionActive = isDeflectionActive(stack);
        boolean attractionActive = isAttractionActive(stack);

        // 添加魔力消耗信息
        tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.mana_cost",
                        ModConfig.POLARIZED_STONE_DEFLECTION_MANA_COST.get())
                .withStyle(ChatFormatting.BLUE));

        // 添加模式状态信息
        tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.deflection_status",
                        Component.translatable("item.trinketsandbaubles.polarized_stone." + (deflectionActive ? "enabled" : "disabled")))
                .withStyle(deflectionActive ? ChatFormatting.GREEN : ChatFormatting.GRAY));

        tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.attraction_status",
                        Component.translatable("item.trinketsandbaubles.polarized_stone." + (attractionActive ? "enabled" : "disabled")))
                .withStyle(attractionActive ? ChatFormatting.GREEN : ChatFormatting.GRAY));

        // 添加使用说明
        tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.usage.attraction")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.usage.deflection")
                .withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    // 检查是否装备
    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof PolarizedStoneItem))
                .isPresent();
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                boolean newDeflection = !stack.getOrCreateTag().getBoolean(DEFLECTION_MODE_TAG);
                stack.getOrCreateTag().putBoolean(DEFLECTION_MODE_TAG, newDeflection);
                player.displayClientMessage(Component.translatable(
                        "item.trinketsandbaubles.polarized_stone.deflection_" + (newDeflection ? "on" : "off")), true);
            } else {
                boolean newAttraction = !stack.getOrCreateTag().getBoolean(ATTRACTION_MODE_TAG);
                stack.getOrCreateTag().putBoolean(ATTRACTION_MODE_TAG, newAttraction);
                player.displayClientMessage(Component.translatable(
                        "item.trinketsandbaubles.polarized_stone.attraction_" + (newAttraction ? "on" : "off")), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity instanceof Player player) {
            // 检查物品是否在饰品栏中
            boolean isInCurio = CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item == stack))
                    .isPresent();

            // 只有当物品不在饰品栏时才在这里处理
            if (!isInCurio) {
                updateManaConsumption(player, stack);
            }

            if (isAttractionActive(stack)) {
                attractItemsAndXP(level, player);
            }
            if (isDeflectionActive(stack)) {
                deflectProjectiles(level, player);
            }
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (slotContext.entity() instanceof Player player) {
            if (!player.level().isClientSide) {
                // 在饰品栏中时在这里处理
                updateManaConsumption(player, stack);
                if (isAttractionActive(stack)) {
                    attractItemsAndXP(player.level(), player);
                }
                if (isDeflectionActive(stack)) {
                    deflectProjectiles(player.level(), player);
                }
            }
        }
    }

    private boolean isAttractionActive(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ATTRACTION_MODE_TAG);
    }

    private boolean isDeflectionActive(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(DEFLECTION_MODE_TAG);
    }

    private void updateManaConsumption(Player player, ItemStack stack) {
        boolean isDeflecting = stack.hasTag() && stack.getTag().getBoolean(DEFLECTION_MODE_TAG);
        if (!isDeflecting) {
            return;
        }

        // 每20tick（1秒）检查一次魔力消耗
        if (player.tickCount % 20 == 0) {
            float manaCost = ModConfig.POLARIZED_STONE_DEFLECTION_MANA_COST.get().floatValue();
            float currentMana = ManaData.getMana(player);

            if (currentMana < manaCost) {
                // 魔力不足，关闭防御模式
                stack.getOrCreateTag().putBoolean(DEFLECTION_MODE_TAG, false);
                player.displayClientMessage(Component.translatable(
                        "item.trinketsandbaubles.polarized_stone.no_mana"), true);
            } else {
                // 消耗魔力（每秒消耗一次完整的配置值）
                ManaData.consumeMana(player, manaCost);
            }
        }
    }

    private void attractItemsAndXP(Level level, Player player) {
        Vec3 playerPos = player.position().add(0, 0.75, 0);
        double range = ModConfig.POLARIZED_STONE_ATTRACTION_RANGE.get();
        AABB attractionBox = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range
        );

        // 吸引物品
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, attractionBox);
        for (ItemEntity item : items) {
            if (!item.isRemoved() && item.getOwner() != player) {
                Vec3 motion = playerPos.subtract(item.position()).normalize()
                        .scale(ModConfig.POLARIZED_STONE_ATTRACTION_SPEED.get());
                item.setDeltaMovement(motion);
                item.hasImpulse = true;
            }
        }

        // 吸引经验球
        List<ExperienceOrb> xpOrbs = level.getEntitiesOfClass(ExperienceOrb.class, attractionBox);
        for (ExperienceOrb orb : xpOrbs) {
            if (!orb.isRemoved()) {
                Vec3 motion = playerPos.subtract(orb.position()).normalize()
                        .scale(ModConfig.POLARIZED_STONE_ATTRACTION_SPEED.get());
                orb.setDeltaMovement(motion);
                orb.hasImpulse = true;
            }
        }
    }

    private boolean isHostileProjectile(Projectile projectile, Player player) {
        if (projectile instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow) projectile;
            return arrow.getOwner() != player;
        }
        if (projectile instanceof Fireball) {
            Fireball fireball = (Fireball) projectile;
            return fireball.getOwner() != player;
        }
        return false;
    }

    private void deflectProjectiles(Level level, Player player) {
        AABB deflectionBox = player.getBoundingBox()
                .inflate(ModConfig.POLARIZED_STONE_DEFLECTION_RANGE.get());
        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, deflectionBox);

        for (Projectile projectile : projectiles) {
            if (!projectile.isRemoved() && isHostileProjectile(projectile, player)) {
                Vec3 position = projectile.position();
                Vec3 motion = projectile.getDeltaMovement();

                convertProjectileToParticles(level, position, motion);
                projectile.discard();

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F + (float) level.getRandom().nextDouble() * 0.2F);
            }
        }
    }

    private void convertProjectileToParticles(Level level, Vec3 projectilePos, Vec3 motion) {
        if (level instanceof ServerLevel serverLevel) {
            int rings = 3;
            int particlesPerRing = 20;
            double maxRadius = 1.5;
            double expandSpeed = 0.6;

            Vec3 normal = motion.normalize();
            Vec3 basis;
            if (Math.abs(normal.y) < 0.999) {
                basis = new Vec3(0, 1, 0).cross(normal).normalize();
            } else {
                basis = new Vec3(1, 0, 0).cross(normal).normalize();
            }
            Vec3 perpendicular = normal.cross(basis);

            for (int ring = 0; ring < rings; ring++) {
                double radius = (ring + 1) * (maxRadius / rings);
                double progress = ring / (double) rings;

                for (int i = 0; i < particlesPerRing; i++) {
                    double angle = (i * 2 * Math.PI) / particlesPerRing;

                    Vec3 offset = basis.scale(Math.cos(angle) * radius)
                            .add(perpendicular.scale(Math.sin(angle) * radius));

                    ParticleOptions particle;
                    switch (ring % 3) {
                        case 0:
                            particle = ParticleTypes.END_ROD;
                            break;
                        case 1:
                            particle = ParticleTypes.SOUL_FIRE_FLAME;
                            break;
                        default:
                            particle = ParticleTypes.DRAGON_BREATH;
                            break;
                    }

                    Vec3 particleVelocity = offset.normalize().scale(expandSpeed * (1 - progress));

                    serverLevel.sendParticles(
                            particle,
                            projectilePos.x + offset.x,
                            projectilePos.y + offset.y,
                            projectilePos.z + offset.z,
                            0,
                            particleVelocity.x,
                            particleVelocity.y,
                            particleVelocity.z,
                            0.02
                    );
                }
            }

            level.playSound(null,
                    projectilePos.x, projectilePos.y, projectilePos.z,
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.NEUTRAL,
                    0.7F,
                    1.2F
            );
        }
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
}