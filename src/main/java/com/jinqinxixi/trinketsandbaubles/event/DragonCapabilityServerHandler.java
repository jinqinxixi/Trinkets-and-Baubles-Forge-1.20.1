package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
public class DragonCapabilityServerHandler {
    // 魔力系统接口
    private interface ManaSystem {
        float getMana(Player player, ItemStack stack);
        void consumeMana(Player player, float amount, ItemStack stack);
        boolean hasMana(Player player, float amount, ItemStack stack);
    }

    private static class BotaniaManaSystem implements ManaSystem {
        public static final ItemStack DUMMY_RECEIVER = new ItemStack(net.minecraft.world.item.Items.STICK);// 创建一个虚拟接收者

        @Override
        public float getMana(Player player, ItemStack stack) {
            var handler = vazkii.botania.api.mana.ManaItemHandler.instance();
            return handler.requestMana(DUMMY_RECEIVER, player, Integer.MAX_VALUE, false);
        }

        @Override
        public void consumeMana(Player player, float amount, ItemStack stack) {
            var handler = vazkii.botania.api.mana.ManaItemHandler.instance();
            handler.requestMana(DUMMY_RECEIVER, player, (int)amount, true);
        }

        @Override
        public boolean hasMana(Player player, float amount, ItemStack stack) {
            var handler = vazkii.botania.api.mana.ManaItemHandler.instance();
            return handler.requestMana(DUMMY_RECEIVER, player, (int)amount, false) >= amount;
        }
    }

    private static class IronsSpellsManaSystem implements ManaSystem {
        @Override
        public float getMana(Player player, ItemStack stack) {
            return io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player).getMana();
        }

        @Override
        public void consumeMana(Player player, float amount, ItemStack stack) {
            // 确保最小消耗为1点魔力
            float actualAmount = Math.max(1.0f, amount);
            io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player).addMana(-actualAmount);
        }

        @Override
        public boolean hasMana(Player player, float amount, ItemStack stack) {
            // 确保最小检查为1点魔力
            float actualAmount = Math.max(1.0f, amount);
            return getMana(player, stack) >= actualAmount;
        }
    }

    private static class InternalManaSystem implements ManaSystem {
        @Override
        public float getMana(Player player, ItemStack stack) {
            return ManaData.getMana(player);
        }

        @Override
        public void consumeMana(Player player, float amount, ItemStack stack) {
            ManaData.consumeMana(player, amount);
        }

        @Override
        public boolean hasMana(Player player, float amount, ItemStack stack) {
            return ManaData.hasMana(player, amount);
        }
    }

    private static ManaSystem getManaSystem() {
        if (shouldUseIronsSpellsMana()) {
            return new IronsSpellsManaSystem();
        }
        if (shouldUseBotaniaMana()) {
            return new BotaniaManaSystem();
        }
        return new InternalManaSystem();
    }

    private static boolean shouldUseBotaniaMana() {
        return net.minecraftforge.fml.ModList.get().isLoaded("botania") && ModConfig.USE_BOTANIA_MANA.get();
    }

    private static boolean shouldUseIronsSpellsMana() {
        try {
            Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData");
            return ModConfig.USE_IRONS_SPELLS_MANA.get();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap instanceof DragonCapability dragonCap) {
                        if (!dragonCap.isActive() || !dragonCap.isDragonBreathActive()) {
                            return;
                        }

                        ManaSystem manaSystem = getManaSystem();
                        float manaCost = RaceAttributesConfig.DRAGON.DRAGON_BREATH_MANA_COST.get().floatValue();

                        boolean hasSufficientMana = false;
                        if (manaSystem instanceof BotaniaManaSystem) {
                            // 植物魔法使用间隔检查
                            if (player.tickCount % RaceAttributesConfig.DRAGON.DRAGON_MANA_CHECK_INTERVAL.get() == 0) {
                                if (manaSystem.hasMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER)) {
                                    manaSystem.consumeMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER);
                                    hasSufficientMana = true;
                                }
                            } else {
                                hasSufficientMana = true; // 非检查间隔时允许继续
                            }
                        } else {
                            // 其他魔力系统每tick检查
                            float tickCost = Math.max(1.0f, manaCost / 20f); // 确保每tick至少消耗1点魔力
                            if (manaSystem.hasMana(player, tickCost, BotaniaManaSystem.DUMMY_RECEIVER)) {
                                manaSystem.consumeMana(player, tickCost, BotaniaManaSystem.DUMMY_RECEIVER);
                                hasSufficientMana = true;
                            }
                        }

                        if (!hasSufficientMana) {
                            dragonCap.toggleDragonBreath();
                            return;
                        }

                        handleDragonBreathDamage(player, level);
                    }
                });
            }
        }
    }

    private static void handleDragonBreathDamage(Player player, ServerLevel level) {
        Vec3 look = player.getLookAngle();
        Vec3 playerPos = player.getEyePosition();
        double range = 15.0;
        double baseSpread = 1.0;
        double startDistance = 1.0;
        double maxSpreadMultiplier = 2.0;

        level.getEntities(player, player.getBoundingBox().inflate(range + 2),
                entity -> {
                    if (!(entity instanceof LivingEntity) || entity == player) {
                        return false;
                    }

                    Vec3 targetPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
                    Vec3 toTarget = targetPos.subtract(playerPos);
                    double projectionLength = toTarget.dot(look);

                    if (projectionLength < startDistance || projectionLength > range) {
                        return false;
                    }

                    Vec3 projection = playerPos.add(look.scale(projectionLength));
                    double distanceFromLine = targetPos.distanceTo(projection);
                    double distanceRatio = projectionLength / range;
                    double currentSpread = baseSpread * (1 + (distanceRatio * maxSpreadMultiplier));

                    return distanceFromLine <= currentSpread;
                }
        ).forEach(target -> {
            if (target instanceof LivingEntity living) {
                double distance = target.position().subtract(playerPos).length();
                float damage = RaceAttributesConfig.DRAGON.DRAGON_BREATH_BASE_DAMAGE.get().floatValue() *
                        (float)(1.0 - (distance / range) * RaceAttributesConfig.DRAGON.DRAGON_BREATH_DECAY_RATE.get());
                damage = Math.max(RaceAttributesConfig.DRAGON.DRAGON_BREATH_MIN_DAMAGE.get().floatValue(), damage);

                target.setRemainingFireTicks(100);
                target.hurt(player.damageSources().playerAttack(player), damage);
            }
        });
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
            if (cap instanceof DragonCapability dragonCap) {
                if (!dragonCap.isActive() || !dragonCap.isDragonBreathActive()) {
                    return;
                }

                if (!player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {
                    spawnDragonBreathParticles(player, serverLevel);
                }
            }
        });
    }

    private static void spawnDragonBreathParticles(Player player, ServerLevel serverLevel) {
        RandomSource random = player.getRandom();
        Vec3 look = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 startPos = eyePos.add(look.x, look.y, look.z);

        double range = 15.0;
        double spread = 0.5;
        double startDistance = 0.0;

        // 主要火焰效果
        for (int i = 1; i <= range - startDistance; i++) {
            createFireParticles(serverLevel, random, look, startPos, i, range, spread);
        }

        // 龙息特效
        createDragonBreathParticles(serverLevel, random, look, startPos);

        // 音效
        playDragonBreathSounds(serverLevel, random, player);
    }

    private static void createFireParticles(ServerLevel serverLevel, RandomSource random,
                                            Vec3 look, Vec3 startPos, int i, double range, double spread) {
        double distance = i / 2.0;
        int particles = 5 + (int)(i / 2);

        for (int p = 0; p < particles; p++) {
            double spreadFactor = spread * (i / range);
            double angle = random.nextDouble() * Math.PI * 2;
            double offsetX = Math.cos(angle) * spreadFactor * random.nextDouble();
            double offsetY = Math.sin(angle) * spreadFactor * random.nextDouble();
            double offsetZ = Math.cos(angle) * spreadFactor * random.nextDouble();

            double x = startPos.x + look.x * distance + offsetX;
            double y = startPos.y + look.y * distance + offsetY;
            double z = startPos.z + look.z * distance + offsetZ;

            // 火焰粒子
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 1,
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.1,
                    0.01);

            // 烟雾效果
            if (random.nextFloat() < 0.3) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 1,
                        0, 0.1, 0, 0.01);
            }

            // 火星效果
            if (random.nextFloat() < 0.1) {
                serverLevel.sendParticles(ParticleTypes.LAVA, x, y, z, 1,
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.2,
                        0.01);
            }
        }

        // 魂焰中心线
        if (i % 1 == 0) {
            createSoulFireParticles(serverLevel, random, look, startPos, distance);
        }
    }

    private static void createSoulFireParticles(ServerLevel serverLevel, RandomSource random,
                                                Vec3 look, Vec3 startPos, double distance) {
        for (int centerParticles = 0; centerParticles < 3; centerParticles++) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    startPos.x + look.x * distance + (random.nextDouble() - 0.5) * 0.2,
                    startPos.y + look.y * distance + (random.nextDouble() - 0.5) * 0.2,
                    startPos.z + look.z * distance + (random.nextDouble() - 0.5) * 0.2,
                    1,
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * 0.02,
                    0.01
            );
        }
    }

    private static void createDragonBreathParticles(ServerLevel serverLevel, RandomSource random,
                                                    Vec3 look, Vec3 startPos) {
        Vec3 dragonBreathPos = startPos.add(look.multiply(0.5, 0.5, 0.5));
        for (int i = 0; i < 2; i++) {
            serverLevel.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    dragonBreathPos.x + (random.nextDouble() - 0.5) * 0.5,
                    dragonBreathPos.y + (random.nextDouble() - 0.5) * 0.5,
                    dragonBreathPos.z + (random.nextDouble() - 0.5) * 0.5,
                    1,
                    look.x * 0.2 + (random.nextDouble() - 0.5) * 0.1,
                    look.y * 0.2 + (random.nextDouble() - 0.5) * 0.1,
                    look.z * 0.2 + (random.nextDouble() - 0.5) * 0.1,
                    0.01
            );
        }
    }

    private static void playDragonBreathSounds(ServerLevel serverLevel, RandomSource random, Player player) {
        if (random.nextFloat() < 0.4f) {
            serverLevel.playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT,
                    SoundSource.PLAYERS,
                    0.3f,
                    0.7f + random.nextFloat() * 0.3f);
        }

        if (random.nextFloat() < 0.4f) {
            serverLevel.playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.PLAYERS,
                    0.2f,
                    0.8f + random.nextFloat() * 0.2f);
        }
    }
}