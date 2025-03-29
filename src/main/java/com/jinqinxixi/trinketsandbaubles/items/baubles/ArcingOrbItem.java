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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ArcingOrbItem extends ModifiableBaubleItem  {
    // NBT 标签常量
    private static final String DASH_COOLDOWN_TAG = "DashCooldown";
    private static final String ARCING_ORB_CHARGING_TAG = "ArcingOrbCharging";
    private static final String CHARGE_AMOUNT_TAG = "ChargeAmount";
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("c9e57654-f302-4bed-97c1-f49d895e8667");

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }
    public ArcingOrbItem(Properties properties) {
        super(properties);
    }

    @Override
    public void applyModifier(Player player, ItemStack stack) {
        // 先调用父类处理随机修饰符（如果有的话）
        super.applyModifier(player, stack);

        // 应用固定的速度属性
        applySpeedAttribute(player);
    }

    @Override
    public void removeModifier(Player player, ItemStack stack) {
        // 先移除随机修饰符
        super.removeModifier(player, stack);

        // 如果没有其他相同物品装备，移除固定属性
        if (!hasSameItemEquipped(player)) {
            removeSpeedAttribute(player);
        }
    }
    private void applySpeedAttribute(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;

        AttributeModifier existing = attribute.getModifier(SPEED_MODIFIER_UUID);
        if (existing == null) {
            attribute.addPermanentModifier(new AttributeModifier(
                    SPEED_MODIFIER_UUID,
                    "trinketsandbaubles.arcing_orb_speed",
                    ModConfig.SPEED_BOOST.get(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        } else if (existing.getAmount() != ModConfig.SPEED_BOOST.get()) {
            // 配置更新时处理
            attribute.removeModifier(SPEED_MODIFIER_UUID);
            attribute.addPermanentModifier(new AttributeModifier(
                    SPEED_MODIFIER_UUID,
                    "trinketsandbaubles.arcing_orb_speed",
                    ModConfig.SPEED_BOOST.get(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private void removeSpeedAttribute(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(SPEED_MODIFIER_UUID);
        }
    }

    private boolean hasSameItemEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> {
                    int count = 0;
                    // 遍历所有 Curios 槽位类型
                    for (String identifier : handler.getCurios().keySet()) {
                        ICurioStacksHandler stackHandler = handler.getCurios().get(identifier);
                        if (stackHandler != null) {
                            // 检查每个槽位
                            for (int i = 0; i < stackHandler.getSlots(); i++) {
                                ItemStack stack = stackHandler.getStacks().getStackInSlot(i);
                                if (stack.getItem() instanceof ArcingOrbItem) {
                                    count++;
                                    if (count >= 2) return true;
                                }
                            }
                        }
                    }
                    return count >= 2;
                })
                .orElse(false);
    }

    //闪避系统
    public void handleDash(Player player, ItemStack stack) {
        float cooldownTicks = stack.getOrCreateTag().getFloat(DASH_COOLDOWN_TAG);

        if (cooldownTicks > 0) {
            return;
        }

        if (ManaData.hasMana(player, ModConfig.DASH_MANA_COST.get().floatValue())) {
            performDash(player);
            ManaData.consumeMana(player, ModConfig.DASH_MANA_COST.get().floatValue());
            stack.getOrCreateTag().putFloat(DASH_COOLDOWN_TAG, ModConfig.DASH_COOLDOWN.get().floatValue());
            playDashEffects(player);
        }
    }

    public void handleCharge(Player player, ItemStack stack) {
        if (!stack.getOrCreateTag().getBoolean(ARCING_ORB_CHARGING_TAG)) {
            startCharging(stack);
        }
        updateCharging(stack, player);
    }

    private void performDash(Player player) {
        // 获取玩家的看向方向
        Vec3 lookVec = player.getLookAngle();

        // 计算水平方向的移动向量
        double x = lookVec.x;
        double z = lookVec.z;

        // 规范化水平向量
        double horizontalLength = Math.sqrt(x * x + z * z);
        if (horizontalLength > 0) {
            x = x / horizontalLength;
            z = z / horizontalLength;
        }

        // 记录起始位置（用于检测实体）
        Vec3 startPos = player.position();

        // 增加速度系数以确保达到准确的距离
        double dashVelocity = 0.3; // 增加初始速度
        double pushFactor = 0.075; // 微调推力系数
        double upwardBoost = 0.05;

        // 获取玩家当前的垂直速度
        double currentVerticalSpeed = player.getDeltaMovement().y;
        if (currentVerticalSpeed < 0) {
            upwardBoost += Math.min(Math.abs(currentVerticalSpeed) * ModConfig.DASH_JUMP_BOOST.get(), 0.1);
        }

        // 设置新的移动向量
        player.setDeltaMovement(
                x * dashVelocity * ModConfig.DASH_DISTANCE.get(),
                currentVerticalSpeed + upwardBoost,
                z * dashVelocity * ModConfig.DASH_DISTANCE.get()
        );

        // 调整推力
        player.push(
                x * ModConfig.DASH_DISTANCE.get() * pushFactor,
                0,
                z * ModConfig.DASH_DISTANCE.get() * pushFactor
        );

        // 检测路径上的实体并应用效果
        if (!player.level().isClientSide) {
            Vec3 endPos = startPos.add(
                    x * ModConfig.DASH_DISTANCE.get(),
                    0,
                    z * ModConfig.DASH_DISTANCE.get()
            );

            AABB dashBox = new AABB(
                    Math.min(startPos.x, endPos.x) - 0.5, startPos.y - 0.5, Math.min(startPos.z, endPos.z) - 0.5,
                    Math.max(startPos.x, endPos.x) + 0.5, startPos.y + 2, Math.max(startPos.z, endPos.z) + 0.5
            );

            List<Entity> entities = player.level().getEntities(player, dashBox,
                    entity -> entity instanceof LivingEntity && entity != player);

            for (Entity entity : entities) {
                if (isOnDashPath(startPos, endPos, entity.position(), 1.0)) {
                    if (entity instanceof LivingEntity living) {
                        // 添加缓慢V效果，持续60刻度（3秒）
                        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
                        // 添加虚弱效果，持续60刻度（3秒），255级
                        living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 255));
                    }
                }
            }
        }

        player.hasImpulse = true;
        player.hurtMarked = true;
        player.fallDistance = 0;
        player.invulnerableTime = 5;

        if (!player.onGround()) {
            player.setNoGravity(true);
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                        serverLevel.getServer().getTickCount() + 5,
                        () -> player.setNoGravity(false)
                ));
            }
        }
    }

    // 添加新方法：检查实体是否在闪避路径上
    private boolean isOnDashPath(Vec3 start, Vec3 end, Vec3 point, double threshold) {
        Vec3 path = end.subtract(start);
        Vec3 toPoint = point.subtract(start);

        // 计算投影长度
        double pathLength = path.length();
        double dot = toPoint.dot(path.normalize());

        // 检查点是否在路径的起点和终点之间
        if (dot < 0 || dot > pathLength) {
            return false;
        }

        // 计算点到路径的距离
        Vec3 projection = start.add(path.normalize().scale(dot));
        return point.distanceTo(projection) <= threshold;
    }

    private void playDashEffects(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            // 播放音效
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS,
                    1.0F, 1.5F);

            // 创建粒子效果，使用原来的 CLOUD 粒子
            Vec3 pos = player.position();
            for (int i = 0; i < 20; i++) {
                double offsetX = (Math.random() - 0.5) * 0.5;
                double offsetY = (Math.random() - 0.5) * 0.5;
                double offsetZ = (Math.random() - 0.5) * 0.5;

                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        1, 0, 0, 0, 0.1);
            }
        }
    }


    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }


    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW; // 使用弓箭的动画效果
    }

    private void startCharging(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(ARCING_ORB_CHARGING_TAG, true);
        stack.getOrCreateTag().putFloat(CHARGE_AMOUNT_TAG, 0.0f);
    }

    public void stopCharging(ItemStack stack, Player player) {
        if (stack.getOrCreateTag().getBoolean(ARCING_ORB_CHARGING_TAG)) {
            float chargeAmount = stack.getOrCreateTag().getFloat(CHARGE_AMOUNT_TAG);
            if (chargeAmount >= ModConfig.MIN_CHARGE.get().floatValue()) {
                releaseEnergyBeam(player, chargeAmount);
            }
            stack.getOrCreateTag().putBoolean(ARCING_ORB_CHARGING_TAG, false);
            stack.getOrCreateTag().putFloat(CHARGE_AMOUNT_TAG, 0.0f);
        }
    }

    private void updateCharging(ItemStack stack, Player player) {
        if (stack.getOrCreateTag().getBoolean(ARCING_ORB_CHARGING_TAG)) {
            float currentCharge = stack.getOrCreateTag().getFloat(CHARGE_AMOUNT_TAG);

            if (currentCharge < ModConfig.MAX_CHARGE.get() &&
                    ManaData.hasMana(player, ModConfig.CHARGE_RATE.get().floatValue())) {
                ManaData.consumeMana(player, ModConfig.CHARGE_RATE.get().floatValue());
                float chargeToAdd = Math.min(
                        ModConfig.CHARGE_RATE.get().floatValue(),
                        ModConfig.MAX_CHARGE.get().floatValue() - currentCharge
                );
                stack.getOrCreateTag().putFloat(CHARGE_AMOUNT_TAG, currentCharge + chargeToAdd);
            }

            // 无论魔力是否足够，都继续显示魔法阵效果
            if (player.level() instanceof ServerLevel serverLevel) {
                Vec3 lookVec = player.getLookAngle();
                float playerScale = player.getScale();
                Vec3 circleCenter = player.getEyePosition().add(lookVec.multiply(2 * playerScale, 2 * playerScale, 2 * playerScale));
                createMagicCircle(serverLevel, circleCenter, currentCharge, player);
            }
        }
    }

    private void createMagicCircle(ServerLevel level, Vec3 center, float chargeAmount, Player player) {
        // 获取玩家当前的缩放比例
        float playerScale = player.getScale(); // 获取玩家当前的缩放比例

        // 根据玩家大小调整魔法阵大小和位置
        double radius = (1.2 + (chargeAmount / ModConfig.MAX_CHARGE.get().floatValue()) * 0.5) * playerScale;
        float time = level.getGameTime() / 20.0f;

        // 根据玩家视线方向创建魔法阵平面
        Vec3 normal = player.getLookAngle();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = normal.cross(up).normalize();
        if (right.length() < 0.001) {
            right = new Vec3(1, 0, 0);
        }
        Vec3 top = right.cross(normal).normalize();

        // 调整魔法阵位置，使其与玩家大小成比例
        center = player.getEyePosition().add(normal.multiply(2 * playerScale, 2 * playerScale, 2 * playerScale));

        // 1. 外层双环
        createDoubleRing(level, center, radius, time, right, top);

        // 2. 六芒星
        createHexagram(level, center, (float) (radius * 0.8), time, right, top);

        // 3. 符文圆环
        createRunicRing(level, center, (float) (radius * 0.9), time, right, top);

        // 4. 能量脉冲
        createEnergyPulse(level, center, radius, time, right, top, chargeAmount);

        // 5. 外围能量漩涡
        createEnergyVortex(level, center, radius, time, right, top);
    }

    // 创建双层旋转光环
    private void createDoubleRing(ServerLevel level, Vec3 center, double radius, float time, Vec3 right, Vec3 top) {
        int points = 16;
        // 外环
        for (int i = 0; i < points; i++) {
            double angle = (i * 2 * Math.PI / points) + time;
            Vec3 outerPos = center.add(
                    right.scale(Math.cos(angle) * radius).add(
                            top.scale(Math.sin(angle) * radius)
                    ));

            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.END_ROD,
                        outerPos.x, outerPos.y, outerPos.z,
                        1, 0, 0, 0, 0);
            }
        }

        // 内环（反向旋转）
        for (int i = 0; i < points; i++) {
            double angle = (i * 2 * Math.PI / points) - time * 1.5;
            Vec3 innerPos = center.add(
                    right.scale(Math.cos(angle) * (radius * 0.85)).add(
                            top.scale(Math.sin(angle) * (radius * 0.85))
                    ));

            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        innerPos.x, innerPos.y, innerPos.z,
                        1, 0, 0, 0, 0);
            }
        }
    }

    // 创建六芒星
    private void createHexagram(ServerLevel level, Vec3 center, float radius, float time, Vec3 right, Vec3 top) {
        int points = 6;
        Vec3[] triangle1 = new Vec3[points / 2];
        Vec3[] triangle2 = new Vec3[points / 2];

        // 创建两个三角形
        for (int i = 0; i < points / 2; i++) {
            double angle1 = (i * 2 * Math.PI / (points / 2)) + time;
            double angle2 = (i * 2 * Math.PI / (points / 2)) - time + Math.PI / points;

            triangle1[i] = center.add(
                    right.scale(Math.cos(angle1) * radius).add(
                            top.scale(Math.sin(angle1) * radius)
                    ));

            triangle2[i] = center.add(
                    right.scale(Math.cos(angle2) * radius).add(
                            top.scale(Math.sin(angle2) * radius)
                    ));
        }

        // 绘制连线
        for (int i = 0; i < points / 2; i++) {
            int next = (i + 1) % (points / 2);
            drawLine(level, triangle1[i], triangle1[next], ParticleTypes.ELECTRIC_SPARK, 5);
            drawLine(level, triangle2[i], triangle2[next], ParticleTypes.ELECTRIC_SPARK, 5);
        }
    }

    // 创建符文环
    private void createRunicRing(ServerLevel level, Vec3 center, float radius, float time, Vec3 right, Vec3 top) {
        int runeCount = 8;
        for (int i = 0; i < runeCount; i++) {
            double angle = (i * 2 * Math.PI / runeCount) + time * 0.5;
            Vec3 runePos = center.add(
                    right.scale(Math.cos(angle) * radius).add(
                            top.scale(Math.sin(angle) * radius)
                    ));

            // 创建符文形状
            createRune(level, runePos, angle, right, top, radius * 0.15f);
        }
    }

    // 创建单个符文
    private void createRune(ServerLevel level, Vec3 center, double angle, Vec3 right, Vec3 top, float size) {
        // 简单的符文形状
        Vec3[] runePoints = new Vec3[4];
        for (int i = 0; i < 4; i++) {
            double runeAngle = angle + (i * Math.PI / 2);
            Vec3 point = center.add(
                    right.scale(Math.cos(runeAngle) * size).add(
                            top.scale(Math.sin(runeAngle) * size)
                    ));
            runePoints[i] = point;

            // 只修改这一个紫色粒子效果，让它不要往上飘
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    point.x, point.y, point.z,
                    1,
                    0, 0, 0, // 将速度都设为0，这样就不会往上飘了
                    0);
        }
    }

    // 创建能量脉冲
    private void createEnergyPulse(ServerLevel level, Vec3 center, double radius, float time, Vec3 right, Vec3 top, float chargeAmount) {
        double pulseRadius = radius * (0.5 + Math.sin(time * 4) * 0.2);
        int pulsePoints = 12;
        float intensity = chargeAmount / ModConfig.MAX_CHARGE.get().floatValue();

        for (int i = 0; i < pulsePoints; i++) {
            double angle = (i * 2 * Math.PI / pulsePoints) + time * 2;
            Vec3 pulsePos = center.add(
                    right.scale(Math.cos(angle) * pulseRadius).add(
                            top.scale(Math.sin(angle) * pulseRadius)
                    ));

            // 根据充能强度改变粒子颜色和密度
            if (i % 2 == 0) {
                level.sendParticles(intensity > 0.7f ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.ELECTRIC_SPARK,
                        pulsePos.x, pulsePos.y, pulsePos.z,
                        1, 0, 0, 0, 0);
            }
        }
    }

    // 创建能量漩涡
    private void createEnergyVortex(ServerLevel level, Vec3 center, double radius, float time, Vec3 right, Vec3 top) {
        int spiralPoints = 2; // 减少螺旋数量
        int pointsPerSpiral = 6; // 减少每个螺旋的点数

        for (int spiral = 0; spiral < spiralPoints; spiral++) {
            double spiralOffset = (2 * Math.PI / spiralPoints) * spiral;

            for (int i = 0; i < pointsPerSpiral; i++) {
                double t = i / (double) pointsPerSpiral;
                double angle = t * Math.PI * 4 + time * 2 + spiralOffset;
                double spiralRadius = radius * (0.4 + t * 0.6);

                Vec3 spiralPos = center.add(
                        right.scale(Math.cos(angle) * spiralRadius).add(
                                top.scale(Math.sin(angle) * spiralRadius)
                        ));

                // 使用更透明的效果
                if (i % 2 == 0) { // 减少粒子数量
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, // 改用更轻盈的粒子
                            spiralPos.x, spiralPos.y, spiralPos.z,
                            1, 0, 0, 0, 0);
                }
            }
        }
    }

    // 辅助方法：绘制线段
    private void drawLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int points) {
        for (int i = 0; i <= points; i++) {
            double t = i / (double) points;
            Vec3 pos = new Vec3(
                    start.x + (end.x - start.x) * t,
                    start.y + (end.y - start.y) * t,
                    start.z + (end.z - start.z) * t
            );

            level.sendParticles(particle,
                    pos.x, pos.y, pos.z,
                    1, 0, 0, 0, 0);
        }
    }




    private void releaseEnergyBeam(Player player, float chargeAmount) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.getEyePosition();
        float playerScale = player.getScale();

        // 增加射程和效果范围
        double range = 40.0 * playerScale;
        double baseRadius = 1.2 * playerScale;

        Vec3 beamStart = playerPos.add(lookVec.scale(2));
        Vec3 beamEnd = beamStart.add(lookVec.scale(range));

        // 创建主效果
        createAdvancedEnergyBeam(serverLevel, beamStart, beamEnd, baseRadius, chargeAmount);

        // 扩大伤害范围
        AABB damageBox = new AABB(
                Math.min(beamStart.x, beamEnd.x) - baseRadius * 0.5,
                Math.min(beamStart.y, beamEnd.y) - baseRadius * 0.5,
                Math.min(beamStart.z, beamEnd.z) - baseRadius * 0.5,
                Math.max(beamStart.x, beamEnd.x) + baseRadius * 0.5,
                Math.max(beamStart.y, beamEnd.y) + baseRadius * 0.5,
                Math.max(beamStart.z, beamEnd.z) + baseRadius * 0.5
        ).inflate(0.5);

        // 伤害处理
        float damage = chargeAmount * ModConfig.DAMAGE_MULTIPLIER.get().floatValue() * 2.0f;
        List<Entity> entities = level.getEntities(player, damageBox, entity -> entity != player);
        for (Entity entity : entities) {
            Vec3 entityPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
            if (isInBeamPath(beamStart, beamEnd, entityPos, baseRadius * 3)) {
                entity.hurt(level.damageSources().indirectMagic(player, player), damage);
                createAdvancedHitEffect(serverLevel, entityPos);
            }
        }

        // 增强音效组合
        level.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS, 0.8F, 0.5F);
        level.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS, 1.5F, 0.8F);
        level.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.0F, 2.0F);
    }

    private void createAdvancedEnergyBeam(ServerLevel level, Vec3 start, Vec3 end, double baseRadius, float chargeAmount) {
        double distance = start.distanceTo(end);
        int particles = (int) (distance * 20); // 增加粒子密度
        Vec3 step = end.subtract(start).scale(1.0 / particles);
        float intensity = chargeAmount / ModConfig.MAX_CHARGE.get().floatValue();
        double time = level.getGameTime() * 0.5;

        // 1. 创建内核光束系统
        createPlasmaCore(level, start, particles, step, baseRadius, time);

        // 2. 创建能量场护盾
        createEnergyShield(level, start, particles, step, baseRadius * 2, time, intensity);

        // 3. 创建离子轨道环
        createIonicRings(level, start, particles, step, baseRadius * 3, time);

        // 4. 创建外围能量涟漪
        createEnergyRipples(level, start, particles, step, baseRadius * 4, time);

        // 5. 创建起点聚变效果
        createFusionCore(level, start, baseRadius * 2);

        // 6. 创建终点湮灭效果
        createAnnihilationPoint(level, end, baseRadius * 3);
    }

    private void createPlasmaCore(ServerLevel level, Vec3 start, int particles, Vec3 step, double radius, double time) {
        // 计算终点位置（使用step和particles来计算）
        Vec3 end = start.add(step.scale(particles));
        Vec3 direction = end.subtract(start).normalize();

        // 保存前一个位置用于连接点
        Vec3 prevPos = null;

        // 每8个点生成一次效果，减少密度
        for (int i = 0; i < particles; i += 8) {
            Vec3 pos = start.add(step.scale(i));
            double progress = i / (double) particles;

            // 创建能量炮的核心光束
            createEnergyCannonCore(level, pos, radius * 0.5, direction, progress, time);

            // 如果有前一个位置，创建连接效果
            if (prevPos != null) {
                createEnergyConnection(level, prevPos, pos, radius * 0.3, time);
            }
            prevPos = pos;

            // 创建周围的能量涡流效果
            if (i % 16 == 0) {
                createEnergyVortex(level, pos, radius * 0.8, direction, time);
            }
        }
    }

    private void createEnergyCannonCore(ServerLevel level, Vec3 pos, double radius, Vec3 direction, double progress, double time) {
        // 创建中心光束
        int segments = 8;
        double segmentAngle = Math.PI * 2 / segments;
        double baseRadius = radius * (0.3 + Math.sin(time * 4 + progress * Math.PI) * 0.1);

        // 创建螺旋状的能量光束
        for (int i = 0; i < segments; i++) {
            double angle = i * segmentAngle + time * 2;
            double x = Math.cos(angle) * baseRadius;
            double y = Math.sin(angle) * baseRadius;

            // 创建基础光束
            Vec3 offset = new Vec3(x, y, 0);
            Vec3 particlePos = pos.add(offset);

            // 发送粒子
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    particlePos.x, particlePos.y, particlePos.z,
                    1,
                    direction.x * 0.01,
                    direction.y * 0.01,
                    direction.z * 0.01,
                    0.01
            );

            // 添加能量效果
            if (i % 2 == 0) {
                level.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        particlePos.x, particlePos.y, particlePos.z,
                        1,
                        direction.x * 0.02,
                        direction.y * 0.02,
                        direction.z * 0.02,
                        0.02
                );
            }
        }

        // 添加中心强光效果
        for (int i = 0; i < 4; i++) {
            level.sendParticles(
                    ParticleTypes.FLASH,
                    pos.x, pos.y, pos.z,
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    private void createEnergyConnection(ServerLevel level, Vec3 start, Vec3 end, double radius, double time) {
        int points = 5;
        Vec3 diff = end.subtract(start);

        for (int i = 0; i <= points; i++) {
            double progress = i / (double) points;
            double offset = Math.sin(progress * Math.PI + time * 3) * radius;

            Vec3 basePos = start.add(diff.scale(progress));
            // 添加螺旋状的能量链接
            double spiralX = Math.cos(progress * Math.PI * 4 + time * 2) * offset;
            double spiralY = Math.sin(progress * Math.PI * 4 + time * 2) * offset;

            Vec3 particlePos = basePos.add(new Vec3(spiralX, spiralY, 0));

            // 发送粒子
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    particlePos.x, particlePos.y, particlePos.z,
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    private void createEnergyVortex(ServerLevel level, Vec3 pos, double radius, Vec3 direction, double time) {
        int vortexPoints = 12;
        double vortexAngle = Math.PI * 2 / vortexPoints;

        for (int i = 0; i < vortexPoints; i++) {
            double angle = i * vortexAngle + time * 3;
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;

            Vec3 vortexPos = pos.add(new Vec3(x, y, 0));

            // 创建涡流效果
            level.sendParticles(
                    i % 2 == 0 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.END_ROD,
                    vortexPos.x, vortexPos.y, vortexPos.z,
                    1,
                    direction.x * 0.05,
                    direction.y * 0.05,
                    direction.z * 0.05,
                    0.05
            );
        }
    }

    private void createEnergyMatrix(ServerLevel level, Vec3 pos, double radius, double progress, double time) {
        // 创建一个旋转的能量魔方结构
        int edges = 8;
        double rotationSpeed = time * 2;
        double pulseScale = 1.0 + Math.sin(time * 3) * 0.2;

        // 创建魔方的边框
        for (int i = 0; i < edges; i++) {
            double angle1 = (i * Math.PI * 2 / edges) + rotationSpeed;
            double angle2 = ((i + 1) * Math.PI * 2 / edges) + rotationSpeed;

            // 内层魔方
            createMatrixEdge(level, pos, radius * 0.6 * pulseScale, angle1, angle2, time, true);
            // 外层魔方
            createMatrixEdge(level, pos, radius * pulseScale, angle1, angle2, time, false);

            // 连接内外层的能量线
            if (i % 2 == 0) {
                createEnergyConnection(level, pos, radius * 0.6 * pulseScale, radius * pulseScale, angle1, time);
            }
        }

        // 创建中心能量核心
        createMatrixCore(level, pos, radius * 0.3, time);
    }

    private void createMatrixEdge(ServerLevel level, Vec3 pos, double radius, double angle1, double angle2, double time, boolean isInner) {
        int points = 5;
        for (int i = 0; i <= points; i++) {
            double blend = i / (double) points;
            double currentAngle = angle1 * (1 - blend) + angle2 * blend;

            double x = Math.cos(currentAngle) * radius;
            double y = Math.sin(currentAngle) * radius;

            Vec3 edgePos = pos.add(new Vec3(x, y, 0));

            // 根据是内层还是外层使用不同的粒子
            level.sendParticles(
                    isInner ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK,
                    edgePos.x, edgePos.y, edgePos.z,
                    1,
                    0, 0, 0,
                    0
            );

            // 添加能量流动效果
            if (Math.random() < 0.3) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        edgePos.x, edgePos.y, edgePos.z,
                        1,
                        (Math.random() - 0.5) * 0.02,
                        (Math.random() - 0.5) * 0.02,
                        (Math.random() - 0.5) * 0.02,
                        0.02
                );
            }
        }
    }

    private void createEnergyConnection(ServerLevel level, Vec3 pos, double innerRadius, double outerRadius, double angle, double time) {
        int points = 3;
        for (int i = 0; i <= points; i++) {
            double progress = i / (double) points;
            double currentRadius = innerRadius * (1 - progress) + outerRadius * progress;

            double x = Math.cos(angle) * currentRadius;
            double y = Math.sin(angle) * currentRadius;

            Vec3 connectionPos = pos.add(new Vec3(x, y, 0));

            // 创建连接线效果
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    connectionPos.x, connectionPos.y, connectionPos.z,
                    1,
                    0, 0, 0,
                    0.01
            );
        }
    }

    private void createMatrixCore(ServerLevel level, Vec3 pos, double radius, double time) {
        // 创建一个复杂的核心结构
        int corePoints = 12;
        for (int i = 0; i < corePoints; i++) {
            double angle = (i * Math.PI * 2 / corePoints) + time * 3;
            double layerRadius = radius * (0.5 + Math.sin(time * 4 + i) * 0.3);

            for (int layer = 0; layer < 3; layer++) {
                double layerOffset = layer * Math.PI / 6;
                double x = Math.cos(angle + layerOffset) * layerRadius;
                double y = Math.sin(angle + layerOffset) * layerRadius;

                Vec3 corePos = pos.add(new Vec3(x, y, 0));

                // 核心粒子
                level.sendParticles(
                        layer == 0 ? ParticleTypes.END_ROD :
                                layer == 1 ? ParticleTypes.SOUL_FIRE_FLAME :
                                        ParticleTypes.ELECTRIC_SPARK,
                        corePos.x, corePos.y, corePos.z,
                        1,
                        0, 0, 0,
                        0.02
                );
            }
        }
    }

    private void createEnergyCrystals(ServerLevel level, Vec3 pos, double radius, double progress, double time) {
        // 创建能量结晶体系统
        int crystalSets = 4;
        for (int set = 0; set < crystalSets; set++) {
            double setAngle = (set * Math.PI * 2 / crystalSets) + time;
            double crystalRadius = radius * (0.7 + Math.sin(time * 2 + set) * 0.3);

            // 创建单个结晶体
            createCrystalFormation(level, pos, crystalRadius, setAngle, time);
        }
    }

    private void createCrystalFormation(ServerLevel level, Vec3 pos, double radius, double baseAngle, double time) {
        int points = 5;
        double crystalTime = time * 2 + baseAngle;

        for (int i = 0; i < points; i++) {
            double angle = baseAngle + (i * Math.PI * 2 / points);
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;

            Vec3 crystalPos = pos.add(new Vec3(x, y, 0));

            // 创建结晶体粒子
            level.sendParticles(ParticleTypes.END_ROD,
                    crystalPos.x, crystalPos.y, crystalPos.z,
                    1,
                    Math.cos(crystalTime) * 0.02,
                    Math.sin(crystalTime) * 0.02,
                    0,
                    0.02
            );

            // 添加能量流动
            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        crystalPos.x, crystalPos.y, crystalPos.z,
                        1,
                        Math.cos(angle) * 0.03,
                        Math.sin(angle) * 0.03,
                        0,
                        0.01
                );
            }
        }
    }

    private void createQuantumEntanglement(ServerLevel level, Vec3 pos, double radius, double progress, double time) {
        // 创建量子纠缠效果
        int entanglementPoints = 6;
        for (int i = 0; i < entanglementPoints; i++) {
            double angle = (i * Math.PI * 2 / entanglementPoints) + time * 1.5;
            double entanglementRadius = radius * (0.8 + Math.sin(time * 3 + i) * 0.2);

            createEntanglementPair(level, pos, entanglementRadius, angle, time);
        }
    }

    private void createEntanglementPair(ServerLevel level, Vec3 pos, double radius, double angle, double time) {
        // 创建一对纠缠粒子
        double x1 = Math.cos(angle) * radius;
        double y1 = Math.sin(angle) * radius;
        double x2 = Math.cos(angle + Math.PI) * radius;
        double y2 = Math.sin(angle + Math.PI) * radius;

        Vec3 pos1 = pos.add(new Vec3(x1, y1, 0));
        Vec3 pos2 = pos.add(new Vec3(x2, y2, 0));

        // 纠缠粒子对
        level.sendParticles(ParticleTypes.END_ROD,
                pos1.x, pos1.y, pos1.z,
                1,
                Math.cos(time) * 0.02,
                Math.sin(time) * 0.02,
                0,
                0.02
        );

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                pos2.x, pos2.y, pos2.z,
                1,
                -Math.cos(time) * 0.02,
                -Math.sin(time) * 0.02,
                0,
                0.02
        );

        // 连接线
        int connectionPoints = 3;
        for (int i = 0; i <= connectionPoints; i++) {
            double progress = i / (double) connectionPoints;
            Vec3 connectionPos = pos1.add(pos2.subtract(pos1).scale(progress));

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    connectionPos.x, connectionPos.y, connectionPos.z,
                    1,
                    0, 0, 0,
                    0.01
            );
        }
    }
    private void createEnergyShield(ServerLevel level, Vec3 start, int particles, Vec3 step,
                                    double radius, double time, float intensity) {
        // 创建3D能量护盾效果
        for (int i = 0; i < particles; i += 2) {
            Vec3 pos = start.add(step.scale(i));
            double progress = i / (double) particles;
            double shieldRadius = radius * (1 + Math.sin(progress * Math.PI * 4 + time) * 0.3);

            int points = 8 + (int)(intensity * 8);
            // 添加垂直方向的循环
            for (int v = 0; v < points/2; v++) {
                double phi = v * Math.PI / (points/2); // 垂直角度
                for (int h = 0; h < points; h++) {
                    double theta = (h * 2 * Math.PI / points) + time * 2; // 水平角度

                    // 使用球坐标系创建3D效果
                    double x = Math.sin(phi) * Math.cos(theta) * shieldRadius;
                    double y = Math.sin(phi) * Math.sin(theta) * shieldRadius;
                    double z = Math.cos(phi) * shieldRadius;

                    Vec3 offset = new Vec3(x, y, z);
                    Vec3 particlePos = pos.add(offset);

                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            particlePos.x, particlePos.y, particlePos.z,
                            1,
                            Math.cos(theta) * 0.01,
                            Math.sin(phi) * 0.01,
                            Math.sin(theta) * 0.01,
                            0.05);
                }
            }
        }
    }

    private void createIonicRings(ServerLevel level, Vec3 start, int particles, Vec3 step,
                                  double radius, double time) {
        // 创建3D离子环
        for (int i = 0; i < particles; i += 15) {
            Vec3 pos = start.add(step.scale(i));
            double ringRadius = radius * (1 + Math.cos(i * 0.1) * 0.2);

            // 创建多个倾斜的环
            for (int ring = 0; ring < 3; ring++) {
                double ringAngle = ring * Math.PI / 3; // 环的倾斜角度
                int ringParticles = 16;

                for (int j = 0; j < ringParticles; j++) {
                    double angle = (j * 2 * Math.PI / ringParticles) + time;

                    // 使用3D旋转矩阵创建倾斜的环
                    double x = Math.cos(angle) * ringRadius;
                    double y = Math.sin(angle) * ringRadius * Math.cos(ringAngle);
                    double z = Math.sin(angle) * ringRadius * Math.sin(ringAngle);

                    Vec3 offset = new Vec3(x, y, z);
                    Vec3 particlePos = pos.add(offset);

                    // 交替使用不同粒子
                    level.sendParticles(
                            j % 2 == 0 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.END_ROD,
                            particlePos.x, particlePos.y, particlePos.z,
                            1,
                            Math.cos(angle) * 0.02,
                            Math.sin(angle) * 0.02,
                            Math.sin(ringAngle) * 0.02,
                            0.02
                    );
                }
            }
        }
    }

    private void createEnergyRipples(ServerLevel level, Vec3 start, int particles, Vec3 step,
                                     double radius, double time) {
        // 创建3D能量涟漪
        for (int i = 0; i < particles; i += 4) {
            Vec3 pos = start.add(step.scale(i));
            double progress = i / (double) particles;
            double rippleRadius = radius * (1 + Math.sin(progress * Math.PI * 6 + time) * 0.4);

            // 创建多层螺旋涟漪
            int spirals = 4;
            for (int spiral = 0; spiral < spirals; spiral++) {
                double spiralOffset = spiral * Math.PI / spirals;

                for (int j = 0; j < 8; j++) {
                    double angle = (j * Math.PI / 4) + time * 1.5;
                    double heightOffset = Math.sin(angle + spiralOffset) * rippleRadius * 0.3;

                    // 创建3D螺旋效果
                    double x = Math.cos(angle) * rippleRadius;
                    double y = Math.sin(angle) * rippleRadius;
                    double z = heightOffset;

                    Vec3 offset = new Vec3(x, y, z);
                    Vec3 particlePos = pos.add(offset);

                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            particlePos.x, particlePos.y, particlePos.z,
                            1,
                            Math.cos(angle) * 0.03,
                            Math.sin(angle) * 0.03,
                            Math.cos(time) * 0.03,
                            0.1);
                }
            }
        }
    }

    private void createFusionCore(ServerLevel level, Vec3 pos, double radius) {
        // 创建起点聚变效果
        for (int i = 0; i < 60; i++) {
            double angle1 = Math.random() * Math.PI * 2;
            double angle2 = Math.random() * Math.PI;
            double r = radius * Math.random();

            double x = Math.sin(angle2) * Math.cos(angle1) * r;
            double y = Math.sin(angle2) * Math.sin(angle1) * r;
            double z = Math.cos(angle2) * r;

            Vec3 particlePos = pos.add(x, y, z);
            Vec3 toCenter = pos.subtract(particlePos).normalize().scale(0.1);

            level.sendParticles(
                    i % 3 == 0 ? ParticleTypes.END_ROD :
                            i % 3 == 1 ? ParticleTypes.SOUL_FIRE_FLAME :
                                    ParticleTypes.ELECTRIC_SPARK,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, toCenter.x, toCenter.y, toCenter.z, 0.1
            );
        }
    }

    private void createAnnihilationPoint(ServerLevel level, Vec3 pos, double radius) {
        // 创建终点湮灭效果
        for (int i = 0; i < 80; i++) {
            double angle1 = Math.random() * Math.PI * 2;
            double angle2 = Math.random() * Math.PI;

            double x = Math.sin(angle2) * Math.cos(angle1);
            double y = Math.sin(angle2) * Math.sin(angle1);
            double z = Math.cos(angle2);

            Vec3 direction = new Vec3(x, y, z);

            level.sendParticles(
                    i % 3 == 0 ? ParticleTypes.END_ROD :
                            i % 3 == 1 ? ParticleTypes.SOUL_FIRE_FLAME :
                                    ParticleTypes.ELECTRIC_SPARK,
                    pos.x, pos.y, pos.z,
                    1,
                    direction.x * 0.3,
                    direction.y * 0.3,
                    direction.z * 0.3,
                    0.2
            );
        }
    }

    private void createAdvancedHitEffect(ServerLevel level, Vec3 pos) {
        // 创建增强型击中效果
        for (int i = 0; i < 40; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 0.8;
            double height = Math.random() * 0.8;

            double x = Math.cos(angle) * radius;
            double y = height;
            double z = Math.sin(angle) * radius;

            level.sendParticles(
                    i % 3 == 0 ? ParticleTypes.END_ROD :
                            i % 3 == 1 ? ParticleTypes.SOUL_FIRE_FLAME :
                                    ParticleTypes.ELECTRIC_SPARK,
                    pos.x, pos.y, pos.z,
                    1, x, y, z, 0.2
            );
        }
    }

    private boolean isInBeamPath(Vec3 start, Vec3 end, Vec3 point, double threshold) {
        Vec3 beamDir = end.subtract(start).normalize();
        Vec3 pointDir = point.subtract(start);
        double projectionLength = pointDir.dot(beamDir);

        if (projectionLength < 0 || projectionLength > start.distanceTo(end)) {
            return false;
        }

        Vec3 projection = start.add(beamDir.scale(projectionLength));
        return point.distanceTo(projection) <= threshold;
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity instanceof Player player) {
            updateCharging(stack, player);
        }
    }


    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (slotContext.entity() instanceof Player player) {
            if (!player.level().isClientSide) {
                float cooldownTicks = stack.getOrCreateTag().getFloat(DASH_COOLDOWN_TAG);
                if (cooldownTicks > 0) {
                    stack.getOrCreateTag().putFloat(DASH_COOLDOWN_TAG, cooldownTicks - 1);
                }
                updateCharging(stack, player);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        // 速度加成信息
        tooltip.add(Component.translatable("item.trinketsandbaubles.arcing_orb.speed_boost",
                        String.format("%.0f", ModConfig.SPEED_BOOST.get() * 100))
                .withStyle(ChatFormatting.BLUE));

        // 伤害系数信息
        tooltip.add(Component.translatable("item.trinketsandbaubles.arcing_orb.damage_multiplier",
                        String.format("%.3f", ModConfig.DAMAGE_MULTIPLIER.get()))
                .withStyle(ChatFormatting.GOLD));

        // 闪避消耗魔力信息
        tooltip.add(Component.translatable("item.trinketsandbaubles.arcing_orb.dash_cost",
                        String.format("%.0f", ModConfig.DASH_MANA_COST.get()))
                .withStyle(ChatFormatting.AQUA));

        // 充能状态信息
        if (stack.getOrCreateTag().getBoolean(ARCING_ORB_CHARGING_TAG)) {
            float chargeAmount = stack.getOrCreateTag().getFloat(CHARGE_AMOUNT_TAG);
            tooltip.add(Component.translatable("item.trinketsandbaubles.arcing_orb.charging",
                            String.format("%.1f", chargeAmount))
                    .withStyle(ChatFormatting.YELLOW));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }


    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        // 先调用父类的onEquip方法，它会处理修饰符的比较和应用
        super.onEquip(slotContext, prevStack, stack);

        // 如果需要应用额外的固定属性，只有在新装备的物品和之前的不同时才应用
        if (slotContext.entity() instanceof Player player &&
                (prevStack.isEmpty() || !hasSameModifier(prevStack, stack))) {
            applySpeedAttribute(player);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        // 先检查是否需要移除速度属性
        if (slotContext.entity() instanceof Player player &&
                (newStack.isEmpty() || !hasSameModifier(newStack, stack))) {
            if (!hasSameItemEquipped(player)) {
                removeSpeedAttribute(player);
            }
        }

        // 然后调用父类的onUnequip方法，它会处理修饰符的移除
        super.onUnequip(slotContext, newStack, stack);
    }

    // 检查是否装备
    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof ArcingOrbItem))
                .isPresent();
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