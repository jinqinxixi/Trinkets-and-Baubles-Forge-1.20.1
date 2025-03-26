package com.jinqinxixi.trinketsandbaubles.events;

import com.jinqinxixi.trinketsandbaubles.config.Config;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.modEffects.ModEffects;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.SyncDragonBreathMessage;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;


@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID)
public class DragonEffectServerEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                boolean hasEffect = player.hasEffect(ModEffects.DRAGON.get());
                boolean isBreathing = player.getPersistentData().getBoolean("DragonBreathActive");

                // 如果没有效果但状态是开启的，关闭状态
                if (!hasEffect && isBreathing) {
                    player.getPersistentData().putBoolean("DragonBreathActive", false);
                    NetworkHandler.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                            new SyncDragonBreathMessage(false, player.getId())
                    );
                    continue;
                }

                // 检查玩家是否有龙效果且正在使用龙息
                if (hasEffect && isBreathing) {
                    // 检查魔力值
                    if (ManaData.getMana(player) >= 0.5f) {
                        // 消耗魔力
                        ManaData.consumeMana(player, 0.5f);

                        // 定期同步状态（每秒一次）
                        if (event.getServer().getTickCount() % 20 == 0) {
                            NetworkHandler.INSTANCE.send(
                                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                                    new SyncDragonBreathMessage(true, player.getId())
                            );
                        }

                        // 处理实体伤害
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
                                float damage = Config.DRAGON_BREATH_BASE_DAMAGE.get().floatValue() *
                                        (float)(1.0 - (distance / range) * Config.DRAGON_BREATH_DECAY_RATE.get());
                                damage = Math.max(Config.DRAGON_BREATH_MIN_DAMAGE.get().floatValue(), damage);

                                target.setRemainingFireTicks(100);
                                target.hurt(player.damageSources().playerAttack(player), damage);
                            }
                        });
                    } else {
                        // 魔力不足，停止喷火
                        player.getPersistentData().putBoolean("DragonBreathActive", false);
                        NetworkHandler.INSTANCE.send(
                                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                                new SyncDragonBreathMessage(false, player.getId())
                        );
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // 检查是否正在喷火
        boolean isBreathing = player.getPersistentData().getBoolean("DragonBreathActive");

        if (!isBreathing) {
            return;
        }

        // 确保在服务端且玩家有龙效果
        if (!player.level().isClientSide && player.hasEffect(ModEffects.DRAGON.get())) {
            Level level = player.level();
            if (level instanceof ServerLevel serverLevel) {
                RandomSource random = player.getRandom();
                Vec3 look = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition(1.0F);
                Vec3 startPos = eyePos.add(look.x, look.y, look.z); // 从玩家前方1格开始

                double range = 15.0;
                double spread = 0.5;
                double startDistance = 0.0;

                // 主要火焰效果
                for (int i = 1; i <= range - startDistance; i++) {
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
                        serverLevel.sendParticles(
                                ParticleTypes.FLAME,
                                x, y, z,
                                1,
                                (random.nextDouble() - 0.5) * 0.1,
                                (random.nextDouble() - 0.5) * 0.1,
                                (random.nextDouble() - 0.5) * 0.1,
                                0.01
                        );

                        // 烟雾效果
                        if (random.nextFloat() < 0.3) {
                            serverLevel.sendParticles(
                                    ParticleTypes.SMOKE,
                                    x, y, z,
                                    1,
                                    0, 0.1, 0,
                                    0.01
                            );
                        }

                        // 火星效果
                        if (random.nextFloat() < 0.1) {
                            serverLevel.sendParticles(
                                    ParticleTypes.LAVA,
                                    x, y, z,
                                    1,
                                    (random.nextDouble() - 0.5) * 0.2,
                                    (random.nextDouble() - 0.5) * 0.2,
                                    (random.nextDouble() - 0.5) * 0.2,
                                    0.01
                            );
                        }
                    }

                    // 魂焰中心线
                    if (i % 1 == 0) {
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
                }

                // 龙息特效
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

                // 播放音效
                if (random.nextFloat() < 0.4f) {
                    serverLevel.playSound(
                            null, // null means all players can hear
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLAZE_SHOOT,
                            SoundSource.PLAYERS,
                            0.3f,
                            0.7f + random.nextFloat() * 0.3f
                    );
                }

                if (random.nextFloat() < 0.4f) {
                    serverLevel.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.FIRE_AMBIENT,
                            SoundSource.PLAYERS,
                            0.2f,
                            0.8f + random.nextFloat() * 0.2f
                    );
                }
            }
        }
    }
}