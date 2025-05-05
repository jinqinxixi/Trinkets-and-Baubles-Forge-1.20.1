package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import vazkii.botania.api.mana.ManaItemHandler;

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

    // ==================== 魔力系统集成 ====================

    private interface ManaSystem {
        float getMana(Player player, ItemStack stack);
        void consumeMana(Player player, float amount, ItemStack stack);
    }

    private class IronsSpellsManaSystem implements ManaSystem {
        @Override
        public float getMana(Player player, ItemStack stack) {
            return io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player).getMana();
        }

        @Override
        public void consumeMana(Player player, float amount, ItemStack stack) {
            // 确保消耗量向上取整且最小为1
            float actualAmount = Math.max(1.0f, (float) Math.ceil(amount));
            io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player).addMana(-actualAmount);
        }
    }

    private class BotaniaManaSystem implements ManaSystem {
        @Override
        public float getMana(Player player, ItemStack stack) {
            return ManaItemHandler.instance().requestMana(
                    stack,
                    player,
                    Integer.MAX_VALUE,
                    false  // 不实际消耗
            );
        }

        @Override
        public void consumeMana(Player player, float amount, ItemStack stack) {
            ManaItemHandler.instance().requestManaExactForTool(
                    stack,
                    player,
                    (int)amount,
                    true  // 实际消耗
            );
        }
    }

    private class InternalManaSystem implements ManaSystem {
        @Override
        public float getMana(Player player, ItemStack stack) {
            return ManaData.getMana(player);
        }

        @Override
        public void consumeMana(Player player, float amount, ItemStack stack) {
            ManaData.consumeMana(player, amount);
        }
    }

    // 判断使用哪个魔力系统
    private ManaSystem getManaSystem() {
        if (shouldUseIronsSpellsMana()) {
            return new IronsSpellsManaSystem();
        }
        if (shouldUseBotaniaMana()) {
            return new BotaniaManaSystem();
        }
        return new InternalManaSystem();
    }

    private boolean shouldUseIronsSpellsMana() {
        return ModList.get().isLoaded("irons_spellbooks") && ModConfig.USE_IRONS_SPELLS_MANA.get();
    }

    private boolean shouldUseBotaniaMana() {
        return ModList.get().isLoaded("botania") && ModConfig.USE_BOTANIA_MANA.get();
    }

    // ==================== 物品功能 ====================
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        boolean deflectionActive = isDeflectionActive(stack);
        boolean attractionActive = isAttractionActive(stack);

        // 魔力消耗信息
        tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.mana_cost",
                        ModConfig.POLARIZED_STONE_DEFLECTION_MANA_COST.get())
                .withStyle(ChatFormatting.BLUE));

        // 模式状态
        tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.deflection_status",
                        Component.translatable("item.trinketsandbaubles.polarized_stone." + (deflectionActive ? "enabled" : "disabled")))
                .withStyle(deflectionActive ? ChatFormatting.GREEN : ChatFormatting.GRAY));

        tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.attraction_status",
                        Component.translatable("item.trinketsandbaubles.polarized_stone." + (attractionActive ? "enabled" : "disabled")))
                .withStyle(attractionActive ? ChatFormatting.GREEN : ChatFormatting.GRAY));

        // 使用说明
        String attractionKeyName = KeyBindings.ATTRACTION_TOGGLE_KEY.getKey().getDisplayName().getString();
        String deflectionKeyName = KeyBindings.DEFLECTION_TOGGLE_KEY.getKey().getDisplayName().getString();

        tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.usage.attraction",
                        attractionKeyName)
                .withStyle(ChatFormatting.YELLOW));

        tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.usage.deflection",
                        deflectionKeyName)
                .withStyle(ChatFormatting.YELLOW));

        // 当前魔力系统信息
        if (ModConfig.USE_BOTANIA_MANA.get() && ModList.get().isLoaded("botania")) {
            tooltip.add(Component.translatable("item.trinketsandbaubles.polarized_stone.mana_system", "Botania")
                    .withStyle(ChatFormatting.GOLD));

            if (level != null && level.isClientSide) {
                Player player = net.minecraft.client.Minecraft.getInstance().player;
                if (player != null) {
                    float currentMana = getCurrentMana(player, stack);
                    tooltip.add(Component.translatable(
                            "item.trinketsandbaubles.polarized_stone.current_mana",
                            (int) currentMana
                    ).withStyle(ChatFormatting.AQUA));
                }
            }
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                toggleDeflectionMode(stack, player);
            } else {
                toggleAttractionMode(stack, player);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void toggleDeflectionMode(ItemStack stack, Player player) {
        boolean newDeflection = !stack.getOrCreateTag().getBoolean(DEFLECTION_MODE_TAG);
        stack.getOrCreateTag().putBoolean(DEFLECTION_MODE_TAG, newDeflection);
        player.displayClientMessage(Component.translatable(
                "item.trinketsandbaubles.polarized_stone.deflection_" + (newDeflection ? "on" : "off")), true);
    }

    private void toggleAttractionMode(ItemStack stack, Player player) {
        boolean newAttraction = !stack.getOrCreateTag().getBoolean(ATTRACTION_MODE_TAG);
        stack.getOrCreateTag().putBoolean(ATTRACTION_MODE_TAG, newAttraction);
        player.displayClientMessage(Component.translatable(
                "item.trinketsandbaubles.polarized_stone.attraction_" + (newAttraction ? "on" : "off")), true);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity instanceof Player player) {
            boolean isInCurio = CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item == stack))
                    .isPresent();

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
        return stack.getOrCreateTag().getBoolean(ATTRACTION_MODE_TAG);
    }

    private boolean isDeflectionActive(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(DEFLECTION_MODE_TAG);
    }

    private void updateManaConsumption(Player player, ItemStack stack) {
        if (!isDeflectionActive(stack)) return;

        if (player.tickCount % 20 == 0) {
            float manaCost = ModConfig.POLARIZED_STONE_DEFLECTION_MANA_COST.get().floatValue();
            ManaSystem manaSystem = getManaSystem();
            float currentMana = manaSystem.getMana(player, stack);

            // 如果是铁魔法系统，确保消耗量向上取整且最小为1
            if (manaSystem instanceof IronsSpellsManaSystem) {
                manaCost = Math.max(1.0f, (float) Math.ceil(manaCost));
            }

            if (currentMana < manaCost) {
                stack.getOrCreateTag().putBoolean(DEFLECTION_MODE_TAG, false);
                player.displayClientMessage(Component.translatable(
                        "item.trinketsandbaubles.polarized_stone.no_mana"), true);
            } else {
                manaSystem.consumeMana(player, manaCost, stack);
            }
        }
    }

    private float getCurrentMana(Player player, ItemStack stack) {
        return getManaSystem().getMana(player, stack);
    }

    private void attractItemsAndXP(Level level, Player player) {
        Vec3 playerPos = player.position().add(0, 0.75, 0);
        double range = ModConfig.POLARIZED_STONE_ATTRACTION_RANGE.get();
        AABB attractionBox = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range
        );

        // 吸引物品
        level.getEntitiesOfClass(ItemEntity.class, attractionBox).forEach(item -> {
            if (!item.isRemoved() && item.getOwner() != player) {
                Vec3 motion = playerPos.subtract(item.position()).normalize()
                        .scale(ModConfig.POLARIZED_STONE_ATTRACTION_SPEED.get());
                item.setDeltaMovement(motion);
                item.hasImpulse = true;
            }
        });

        // 吸引经验球
        level.getEntitiesOfClass(ExperienceOrb.class, attractionBox).forEach(orb -> {
            if (!orb.isRemoved()) {
                Vec3 motion = playerPos.subtract(orb.position()).normalize()
                        .scale(ModConfig.POLARIZED_STONE_ATTRACTION_SPEED.get());
                orb.setDeltaMovement(motion);
                orb.hasImpulse = true;
            }
        });
    }

    private void deflectProjectiles(Level level, Player player) {
        AABB deflectionBox = player.getBoundingBox()
                .inflate(ModConfig.POLARIZED_STONE_DEFLECTION_RANGE.get());
        level.getEntitiesOfClass(Projectile.class, deflectionBox).forEach(projectile -> {
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
        });
    }

    private boolean isHostileProjectile(Projectile projectile, Player player) {
        if (projectile instanceof AbstractArrow arrow) {
            return arrow.getOwner() != player;
        }
        if (projectile instanceof Fireball fireball) {
            return fireball.getOwner() != player;
        }
        return false;
    }

    private void convertProjectileToParticles(Level level, Vec3 projectilePos, Vec3 motion) {
        if (level instanceof ServerLevel serverLevel) {
            int rings = 3;
            int particlesPerRing = 20;
            double maxRadius = 1.5;
            double expandSpeed = 0.6;

            Vec3 normal = motion.normalize();
            Vec3 basis = Math.abs(normal.y) < 0.999 ? new Vec3(0, 1, 0).cross(normal).normalize()
                    : new Vec3(1, 0, 0).cross(normal).normalize();
            Vec3 perpendicular = normal.cross(basis);

            for (int ring = 0; ring < rings; ring++) {
                double radius = (ring + 1) * (maxRadius / rings);
                double progress = ring / (double) rings;

                for (int i = 0; i < particlesPerRing; i++) {
                    double angle = (i * 2 * Math.PI) / particlesPerRing;
                    Vec3 offset = basis.scale(Math.cos(angle) * radius)
                            .add(perpendicular.scale(Math.sin(angle) * radius));

                    ParticleOptions particle = switch (ring % 3) {
                        case 0 -> ParticleTypes.END_ROD;
                        case 1 -> ParticleTypes.SOUL_FIRE_FLAME;
                        default -> ParticleTypes.DRAGON_BREATH;
                    };

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
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}