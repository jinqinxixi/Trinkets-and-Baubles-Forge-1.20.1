package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.api.IDragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.glide.GlideHandler;

import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.init.ModElements;
import com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects;
import com.jinqinxixi.trinketsandbaubles.traits.elements.Element;
import com.jinqinxixi.trinketsandbaubles.traits.elements.ItemElements;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.SyncAllDragonStatesMessage;
import com.jinqinxixi.trinketsandbaubles.network.message.DragonRingMessage.SyncDragonBreathMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

public class DragonCapability extends AbstractRaceCapability implements IDragonCapability {
    /** 闪电箭特效的颜色。 */
    private static final int BOLT_COLOR = 2515356;

    /** 闪电箭满蓄伤害默认值。 */
    public static final float LIGHTNING_BOLT_DAMAGE = 40.0F;

    /**
     * 闪电箭满蓄魔力消耗默认值。
     *
     * <p>魔力上限默认 100，龙族的 +300 加成把它抬到 400，一发正好花掉一半。
     */
    public static final float LIGHTNING_BOLT_COST = 200.0F;

    /** 闪电箭满蓄所需 tick 数默认值。 */
    public static final int LIGHTNING_BOLT_CHARGE_TIME = 20;

    // 魔力系统接口
    private interface ManaSystem {
        float getMana(Player player, ItemStack stack);
        void consumeMana(Player player, float amount, ItemStack stack);
        boolean hasMana(Player player, float amount, ItemStack stack);
    }
    public float getCurrentMana() {
        return getManaSystem().getMana(player, BotaniaManaSystem.DUMMY_RECEIVER);
    }

    /**
     * 公开魔力桥接，供贪婪之眼等外部系统复用私有 {@link ManaSystem}（自动支持 Iron's Spells /
     * Botania / 内部魔力三选一）。
     *
     * <p>{@code hasMana} 通过则 {@code consumeMana} 并返回 true，否则 false。
     */
    public boolean spendMana(float amount) {
        ManaSystem mana = getManaSystem();
        if (mana.hasMana(player, amount, BotaniaManaSystem.DUMMY_RECEIVER)) {
            mana.consumeMana(player, amount, BotaniaManaSystem.DUMMY_RECEIVER);
            return true;
        }
        return false;
    }

    private class IronsSpellsManaSystem implements ManaSystem {
        @Override
        public float getMana(Player player, ItemStack stack) {
            return io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player).getMana();
        }

        @Override
        public void consumeMana(Player player, float amount, ItemStack stack) {
            // 对于铁魔法，确保消耗量向上取整，最小为1
            float actualAmount = Math.max(1.0f, (float) Math.ceil(amount));
            io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player).addMana(-actualAmount);
        }

        @Override
        public boolean hasMana(Player player, float amount, ItemStack stack) {
            // 对于铁魔法，确保检查量向上取整，最小为1
            float actualAmount = Math.max(1.0f, (float) Math.ceil(amount));
            return getMana(player, stack) >= actualAmount;
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

        @Override
        public boolean hasMana(Player player, float amount, ItemStack stack) {
            return ManaData.hasMana(player, amount);
        }
    }

    private class BotaniaManaSystem implements ManaSystem {
        private static final ItemStack DUMMY_RECEIVER = new ItemStack(net.minecraft.world.item.Items.STICK); // 创建一个虚拟接收者

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

    private boolean flightEnabled = true;
    public boolean dragonBreathActive = false;

    /** 当前龙元素（fire/ice/lightning），由龙戒变体或龙宝石变体决定，默认火。 */
    private String dragonElement = "fire";

    /** 冰霜行者上次结冰的位置，避免同一格重复冻结。 */
    private BlockPos lastFrostPos;

    public DragonCapability(Player player) {
        super(player);
        this.scaleFactor = RaceAttributesConfig.DRAGON.DRAGON_SCALE_FACTOR.get().floatValue();
    }

    private ManaSystem getManaSystem() {
        if (shouldUseIronsSpellsMana()) {
            return new IronsSpellsManaSystem();
        }
        if (shouldUseBotaniaMana()) {
            return new BotaniaManaSystem();
        }
        return new InternalManaSystem();
    }

    private boolean shouldUseBotaniaMana() {
        return net.minecraftforge.fml.ModList.get().isLoaded("botania") && ModConfig.USE_BOTANIA_MANA.get();
    }

    private boolean shouldUseIronsSpellsMana() {
        try {
            Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData");
            return ModConfig.USE_IRONS_SPELLS_MANA.get();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    protected void registerAttributeValues() {
        // 基础属性
        registerValue("MAX_HEALTH", RaceAttributesConfig.DRAGON.MAX_HEALTH::get);
        registerValue("FOLLOW_RANGE", RaceAttributesConfig.DRAGON.FOLLOW_RANGE::get);
        registerValue("KNOCKBACK_RESISTANCE", RaceAttributesConfig.DRAGON.KNOCKBACK_RESISTANCE::get);
        registerValue("MOVEMENT_SPEED", RaceAttributesConfig.DRAGON.MOVEMENT_SPEED::get);
        registerValue("FLYING_SPEED", RaceAttributesConfig.DRAGON.FLYING_SPEED::get);

        // 战斗相关
        registerValue("ATTACK_DAMAGE", RaceAttributesConfig.DRAGON.ATTACK_DAMAGE::get);
        registerValue("ATTACK_KNOCKBACK", RaceAttributesConfig.DRAGON.ATTACK_KNOCKBACK::get);
        registerValue("ATTACK_SPEED", RaceAttributesConfig.DRAGON.ATTACK_SPEED::get);

        // 防御相关
        registerValue("ARMOR", RaceAttributesConfig.DRAGON.ARMOR::get);
        registerValue("ARMOR_TOUGHNESS", RaceAttributesConfig.DRAGON.ARMOR_TOUGHNESS::get);

        // 特殊能力
        registerValue("LUCK", RaceAttributesConfig.DRAGON.LUCK::get);

        // Forge添加的属性
        registerValue("SWIM_SPEED", RaceAttributesConfig.DRAGON.SWIM_SPEED::get);
        registerValue("NAMETAG_DISTANCE", RaceAttributesConfig.DRAGON.NAMETAG_DISTANCE::get);
        registerValue("ENTITY_GRAVITY", RaceAttributesConfig.DRAGON.ENTITY_GRAVITY::get);
        registerValue("STEP_HEIGHT", RaceAttributesConfig.DRAGON.STEP_HEIGHT::get);
        registerValue("BLOCK_REACH", RaceAttributesConfig.DRAGON.BLOCK_REACH::get);
        registerValue("ENTITY_REACH", RaceAttributesConfig.DRAGON.ENTITY_REACH::get);
    }

    @Override
    public String getRaceName() {
        return "Dragon";
    }

    @Override
    public String getRaceId() {
        return "dragon";
    }

    @Override
    public float getManaBonus() {
        return RaceAttributesConfig.DRAGON.DRAGON_MANA_BONUS.get().floatValue();
    }

    @Override
    protected void onTick() {
        if (!isActive) return;

        updateFlightAbility();

        // 按当前元素给对应颜色的龙效果；先把别的元素的龙效果清掉，换元素变龙时才不会叠两个 buff。
        MobEffect current =
                com.jinqinxixi.trinketsandbaubles.modeffects.DragonsEffect.dragonEffect(getDragonElement());
        MobEffectInstance present = player.getEffect(current);
        if (present == null || present.getDuration() < 10) {
            com.jinqinxixi.trinketsandbaubles.modeffects.DragonsEffect.clearDragonEffect(player);
            player.addEffect(new MobEffectInstance(
                    current,
                    30,
                    0,
                    false,
                    false,
                    false
            ));
        }


        // 处理飞行魔力消耗
        if (flightEnabled && !player.isCreative() && !player.isSpectator() && player.getAbilities().flying) {
            float manaCost = RaceAttributesConfig.DRAGON.DRAGON_FLIGHT_MANA_COST.get().floatValue();
            ManaSystem manaSystem = getManaSystem();

            // 根据魔力系统类型使用不同的消耗逻辑
            boolean hasSufficientMana = false;
            if (manaSystem instanceof BotaniaManaSystem) {
                // 植物魔法使用间隔检查
                if (tickCounter % RaceAttributesConfig.DRAGON.DRAGON_MANA_CHECK_INTERVAL.get() == 0) {
                    if (manaSystem.hasMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER)) {
                        manaSystem.consumeMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER);
                        hasSufficientMana = true;
                    }
                } else {
                    // 在间隔期间也要检查是否有足够的魔力
                    hasSufficientMana = manaSystem.hasMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER);
                }
            } else {
                // 其他魔力系统每tick检查
                float tickCost = Math.max(1.0f, manaCost / 20f); // 确保每tick至少消耗1点魔力
                if (manaSystem instanceof IronsSpellsManaSystem) {
                    // 铁魔法系统特殊处理：向上取整确保最小消耗为1
                    tickCost = (float) Math.ceil(tickCost);
                }
                if (manaSystem.hasMana(player, tickCost, BotaniaManaSystem.DUMMY_RECEIVER)) {
                    manaSystem.consumeMana(player, tickCost, BotaniaManaSystem.DUMMY_RECEIVER);
                    hasSufficientMana = true;
                }
            }

            // 如果没有足够的魔力，禁用飞行并显示消息
            if (!hasSufficientMana) {
                player.getAbilities().flying = false;
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.trinketsandbaubles.dragon.no_mana")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
            }
        }

        // 处理龙息音效
        if (dragonBreathActive && !player.level().isClientSide) {
            if (player.getRandom().nextFloat() < 0.4f) {
                player.level().playSound(
                        null,
                        player,
                        SoundEvents.BLAZE_SHOOT,
                        SoundSource.PLAYERS,
                        0.3f,
                        0.7f + player.getRandom().nextFloat() * 0.3f
                );
            }

            if (player.getRandom().nextFloat() < 0.4f) {
                player.level().playSound(
                        null,
                        player,
                        SoundEvents.FIRE_AMBIENT,
                        SoundSource.PLAYERS,
                        0.2f,
                        0.8f + player.getRandom().nextFloat() * 0.2f
                );
            }
        }

        // 冰龙：冰霜行者（走过水面结冰、冻结岩浆）。
        if (!player.level().isClientSide && "ice".equals(getDragonElement())) {
            handleFrostWalker();
        }
    }

    /** 冰霜行者：落地时在玩家脚下结霜冰并冻结周围液体。 */
    private void handleFrostWalker() {
        if (!player.onGround() || player.isSpectator()) {
            return;
        }
        BlockPos pos = player.blockPosition();
        if (pos.equals(lastFrostPos)) {
            return;
        }
        lastFrostPos = pos;
        frostWalker(player.level(), player.blockPosition());
    }

    private void frostWalker(Level level, BlockPos pos) {
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos p = pos.offset(x, -1, z);
                BlockState state = level.getBlockState(p);
                // 水 → 霜冰；岩浆 → 黑曜石（cooled magma 对应方块，本项目无该方块则只冻水）。
                if (state.is(Blocks.WATER) && state.getFluidState().isSource()
                        && level.getBlockState(p.above()).isAir()) {
                    level.setBlock(p, Blocks.FROSTED_ICE.defaultBlockState(), 3);
                }
            }
        }
    }

    /**
     * 雷龙：闪电箭。
     *
     * <p>从眼睛射线 15 格取命中点：命中实体取实体身上，命中方块取方块表面，都没命中取视线 15 格处。
     * 然后在「玩家 → 命中点」画一道闪电、在命中点画一颗落雷球，并对命中点周围 1 格内的实体造成
     * 伤害。苦力怕另有特例——蓄力够时召唤真雷把它充能，这是唯一会落下真雷的情形。
     *
     * @param multiplier 蓄力比例 0~1，伤害与苦力怕特例都按它缩放
     */
    public void handleLightningBolt(float multiplier)
    {
        if (!isActive || player.level().isClientSide || !"lightning".equals(getDragonElement()))
        {
            return;
        }
        if (!(player.level() instanceof ServerLevel level))
        {
            return;
        }

        float charge = Math.max(0.0F, Math.min(1.0F, multiplier));

        // 最低蓄力门槛：蓄力不足 10% 不发射。
        if (charge < 0.1F)
        {
            return;
        }

        // 魔力：先按蓄力比例扣，扣不起就不发。
        float realCost = LIGHTNING_BOLT_COST * charge;
        ManaSystem mana = getManaSystem();
        if (!mana.hasMana(player, realCost, BotaniaManaSystem.DUMMY_RECEIVER))
        {
            return;
        }
        mana.consumeMana(player, realCost, BotaniaManaSystem.DUMMY_RECEIVER);

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(15.0));

        // 射线检测：先测方块，再在「到方块为止」的那段里测实体，实体优先。
        // 用 player.pick 只测方块不测实体，所以准星指着怪时命中的是怪背后的方块，怪就没伤害了。
        BlockHitResult blockHit = level.clip(new ClipContext(
                start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        Vec3 limit = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : end;
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(15.0)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, player, start, limit, searchBox,
                e -> !e.isSpectator() && e.isAlive());

        Vec3 hitLoc = end;
        if (entityHit != null)
        {
            hitLoc = entityHit.getLocation().add(0.0, entityHit.getEntity().getBbHeight() * 0.5F, 0.0);
        }
        else if (blockHit.getType() == HitResult.Type.BLOCK)
        {
            hitLoc = blockHit.getLocation();
        }

        // 闪电特效：一道从玩家到命中点，一颗落在命中点。
        new com.jinqinxixi.trinketsandbaubles.network.message.Messages.EffectsRenderPacket(
                1, start.x, start.y, start.z, hitLoc.x, hitLoc.y, hitLoc.z, BOLT_COLOR, 0.9F, 3.0F)
                .sendNear(player);
        new com.jinqinxixi.trinketsandbaubles.network.message.Messages.EffectsRenderPacket(
                2, hitLoc.x, hitLoc.y, hitLoc.z, hitLoc.x, hitLoc.y, hitLoc.z, BOLT_COLOR, 0.9F, 3.0F)
                .sendNear(level);

        boolean pvp = player.getServer() != null && player.getServer().isPvpAllowed();
        float damage = LIGHTNING_BOLT_DAMAGE * charge;
        DamageSource source = level.damageSources().indirectMagic(player, player);

        // 命中点 1 格范围扩 1 格的立方体（splash 盒）。
        AABB splash = new AABB(
                Mth.floor(hitLoc.x), Mth.floor(hitLoc.y), Mth.floor(hitLoc.z),
                Mth.floor(hitLoc.x) + 1, Mth.floor(hitLoc.y) + 1, Mth.floor(hitLoc.z) + 1)
                .inflate(1.0);

        for (Entity target : level.getEntities(player, splash, e -> !e.isSpectator() && e.isAlive()))
        {
            if (target instanceof Player && !pvp)
            {
                continue;
            }
            // 苦力怕特例：蓄力够时落下真雷把它充能（唯一落真雷的情形）。
            if (charge > 0.33F && target instanceof Creeper creeper)
            {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null)
                {
                    bolt.moveTo(creeper.getX(), creeper.getY(), creeper.getZ());
                    bolt.setVisualOnly(true);
                    creeper.thunderHit(level, bolt);
                    creeper.clearFire();
                }
            }
            target.hurt(source, damage);
        }
    }

    @Override
    public void toggleFlight() {
        if (!isActive) return;

        flightEnabled = !flightEnabled;

        if (!flightEnabled && !player.isCreative()) {
            disableDragonFlight();
        }

        if (player instanceof ServerPlayer serverPlayer) {
            Component message = Component.translatable(
                    flightEnabled ?
                            "message.trinketsandbaubles.dragon.flight.enabled" :
                            "message.trinketsandbaubles.dragon.flight.disabled"
            ).withStyle(flightEnabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);
            serverPlayer.displayClientMessage(message, true);
        }

        sync();
    }

    @Override
    public void toggleDragonBreath() {
        TrinketsandBaublesMod.LOGGER.info("toggleDragonBreath called. Current state: {}, Using Iron's Spells: {}",
                dragonBreathActive, shouldUseIronsSpellsMana());


        boolean shouldActivate = !dragonBreathActive;

        if (shouldActivate) {
            ManaSystem manaSystem = getManaSystem();
            float manaCost = RaceAttributesConfig.DRAGON.DRAGON_BREATH_MANA_COST.get().floatValue();

            if (!manaSystem.hasMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER)) {
                TrinketsandBaublesMod.LOGGER.info("Not enough mana to activate dragon breath");
                return;
            }
        }

        // 明确设置状态而不是切换
        setDragonBreathActive(shouldActivate);
    }

    // 添加新方法
    public boolean setDragonBreathActive(boolean active) {
        // 如果要激活龙息，先检查魔力
        if (active && !dragonBreathActive) {
            ManaSystem manaSystem = getManaSystem();
            float manaCost = RaceAttributesConfig.DRAGON.DRAGON_BREATH_MANA_COST.get().floatValue();

            if (!manaSystem.hasMana(player, manaCost, BotaniaManaSystem.DUMMY_RECEIVER)) {
                TrinketsandBaublesMod.LOGGER.info("Not enough mana to activate dragon breath");

                // 添加玩家提示消息
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.trinketsandbaubles.dragon.no_mana_breath")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
                return false;
            }
        }

        if (dragonBreathActive != active) {
            dragonBreathActive = active;

            if (!player.level().isClientSide) {
                TrinketsandBaublesMod.LOGGER.info("Dragon breath state set to: {}", dragonBreathActive);

                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                            new SyncDragonBreathMessage(dragonBreathActive, serverPlayer.getId())
                    );
                }
            }
        }
        return true;
    }

    @Override
    public void setActive(boolean active) {
        if (this.isActive == active) return;

        if (!active) {
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                        new SyncAllDragonStatesMessage(
                                false,  // flightEnabled
                                false,  // dragonBreathActive
                                dragonElement,
                                serverPlayer.getId()
                        )
                );
            }

            super.setActive(false);
            this.isActive = false;

            // 摘戒指/失去种族能力后清掉免疫 buff：免疫是永久时长，不手动清会一直残留。
            if (!player.level().isClientSide) {
                player.removeEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE);
                player.removeEffect(com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.ICE_RESISTANCE.get());
                player.removeEffect(com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.LIGHTNING_RESISTANCE.get());
            }

            if (!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
            }
        } else {
            super.setActive(true);
            this.isActive = true;
            updateFlightAbility();
        }
    }

    @Override
    public void sync() {
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                    new SyncAllDragonStatesMessage(flightEnabled, dragonBreathActive, dragonElement, serverPlayer.getId())
            );
        }
        super.sync();
    }

    @Override
    public boolean isFlightEnabled() {
        return flightEnabled;
    }

    @Override
    public boolean isDragonBreathActive() {
        return dragonBreathActive;
    }

    /** 当前龙元素（fire/ice/lightning）。 */
    public String getDragonElement() {
        return dragonElement == null ? "fire" : dragonElement;
    }

    /** 设置龙元素，由龙戒/龙宝石的变体 NBT 决定。 */
    public void setDragonElement(String element) {
        this.dragonElement = element == null || element.isEmpty() ? "fire" : element;
    }

    /**
     * 从龙戒/龙宝石的物品堆读取元素并写入。无元素（基础巨龙）记作 "none"，与明确的火 "fire" 区分，
     * 让两者用不同的 buff 图标（黑灰 vs 红）。
     */
    public void applyElementFromRing(ItemStack ring) {
        Element element = ItemElements.getPrimary(ring, ModElements.NEUTRAL.get());
        String name = element == null || element.isNone() ? "none" : element.getName().toLowerCase();
        String previous = dragonElement;
        setDragonElement(name.isEmpty() ? "none" : name);

        // 换元素变身时玩家往往还是龙（active），不走 setActive(false) 的清理，上一个元素的免疫
        // buff 是永久时长、会一直挂着。元素真变了就把免疫清掉，让 onTick/ScanSystem 按新元素重给。
        if (!player.level().isClientSide && !previous.equals(dragonElement)) {
            player.removeEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE);
            player.removeEffect(com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.ICE_RESISTANCE.get());
            player.removeEffect(com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.LIGHTNING_RESISTANCE.get());
        }

        // 元素变了要同步给客户端，否则客户端判断「是不是雷龙」时读到的是旧值，按 B 会误触发。
        if (!player.level().isClientSide) {
            sync();
        }
    }

    /** 只读戒指堆上的元素名，不写字段。用于「元素变没变」的比较，避免每 tick 都写一次。 */
    public String readElementFromRing(ItemStack ring) {
        Element element = ItemElements.getPrimary(ring, ModElements.NEUTRAL.get());
        String name = element == null || element.isNone() ? "none" : element.getName().toLowerCase();
        return name.isEmpty() ? "none" : name;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("FlightEnabled", flightEnabled);
        tag.putBoolean("DragonBreathActive", dragonBreathActive);
        tag.putString("DragonElement", dragonElement);
    }

    @Override
    protected void loadAdditional(CompoundTag tag) {
        super.loadAdditional(tag);
        flightEnabled = tag.contains("FlightEnabled") ? tag.getBoolean("FlightEnabled") : true;
        dragonBreathActive = tag.contains("DragonBreathActive") ? tag.getBoolean("DragonBreathActive") : false;
        dragonElement = tag.contains("DragonElement") ? tag.getString("DragonElement") : "fire";
    }

    private void disableDragonFlight() {
        if (!player.isCreative() && (player.getAbilities().mayfly || player.getAbilities().flying)) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(0.05f);
            player.onUpdateAbilities();
        }

        // Nothing to undo for the glide: it is the game's own, and the race it was granted by is no
        // longer active, which is what the gliding check reads.
    }

    public void handleGameModeChange(GameType newGameMode) {
        if (!player.isCreative() && !player.isSpectator()) {
            if (newGameMode != GameType.SURVIVAL) {
                player.getAbilities().setFlyingSpeed(0.05f);
            } else {
                player.getAbilities().setFlyingSpeed(0.05f * RaceAttributesConfig.DRAGON.DRAGON_FLIGHT_SPEED.get().floatValue());
            }
            player.onUpdateAbilities();
        }
    }

    @Override
    public void updateFlightAbility() {
        if (!isActive || !flightEnabled) {
            return;
        }

        // 只对非创造/旁观模式的玩家修改飞行能力
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = true;
            player.getAbilities().setFlyingSpeed(0.05f * RaceAttributesConfig.DRAGON.DRAGON_FLIGHT_SPEED.get().floatValue());
            player.onUpdateAbilities();
        } else if (flightEnabled) {
            // 创造/旁观模式下只确保可以飞行
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level) {
        // 龙族不需要特殊的破坏方块逻辑
    }

    /**
     * The colours this race's features are drawn in before a player chooses any.
     *
     * <p>The race's own default values, which are what make it look right out of the box:
     * an elf's ears begin in skin tones and a dragon's wings in its own dark purple, rather than in
     * the white that a race with no colours of its own would fall back to.
     */
    @Override
    protected int defaultPrimaryColor() {
        return 0x323232;
    }

    @Override
    protected int defaultSecondaryColor() {
        return 0x911AB9;
    }

    @Override
    protected int defaultAuxColor() {
        return 0x323232;
    }
}