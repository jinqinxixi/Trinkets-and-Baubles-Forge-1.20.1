package com.jinqinxixi.trinketsandbaubles.capability.base;

import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.api.IRaceAppearance;
import com.jinqinxixi.trinketsandbaubles.capability.attribute.AttributeRegistry;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.capability.network.RaceCapabilityNetworking;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;

import com.jinqinxixi.trinketsandbaubles.util.RaceScaleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractRaceCapability implements IBaseRaceCapability, IRaceAppearance {
    protected boolean isActive = false;
    protected float scaleFactor = 1.0f;
    protected final Player player;
    protected int tickCounter = 0;
    protected float permanentManaDecrease = 0.0f;

    /**
     * How this race is drawn on the player wearing it.
     *
     * <p>Held here rather than on the client, and synced like everything else about a race, because
     * the shape and colour a player picked are part of who they are rather than part of what this
     * machine happens to have cached. A player who picked green ears sees green ears on every
     * client, and the server is the one that decides.
     *
     * <p>Defaults are the ones that need no configuration: features shown, the first shape, and the
     * colours taken from the player's own skin. A race that never touches any of this is drawn, and
     * looks like the player it is on.
     */
    protected boolean showTraits = true;

    protected int traitVariant = 0;

    protected int primaryTraitColor = 0xFFFFFF;

    protected int secondaryTraitColor = 0xFFFFFF;

    /** Zero means take the colours from the player's skin; above zero means use them as given. */
    protected int traitColorOption = 0;


    public AbstractRaceCapability(Player player) {
        this.player = player;
    }

    protected interface AttributeValueProvider {
        double getValue();
    }
    protected final Map<String, AttributeValueProvider> attributeValues = new HashMap<>();

    // 子类必须实现此方法来注册各个属性的值
    protected abstract void registerAttributeValues();

    // 在子类中使用这个方法注册属性值
    protected void registerValue(String attributeName, AttributeValueProvider provider) {
        if (attributeName == null || provider == null) {
            return;
        }
        attributeValues.put(attributeName, provider);
    }
    @Override
    public void applyAttributes() {
            if (!isActive) return;

        if (attributeValues.isEmpty()) {
            registerAttributeValues();
        }

        attributeValues.forEach((name, provider) -> {
            AttributeRegistry.AttributeEntry entry = AttributeRegistry.get(name);
            if (entry != null) {
                double value = provider.getValue();
                if (value != 0.0) {
                    addAttributeModifier(player,
                            entry.getAttribute(),
                            entry.getUuid(),
                            getRaceName() + " " + name,
                            value,
                            entry.getOperation());
                }
            }
        });

        player.setHealth(player.getHealth());
    }
    @Override
    public void removeAttributes() {
        attributeValues.keySet().forEach(name -> {
            AttributeRegistry.AttributeEntry entry = AttributeRegistry.get(name);
            if (entry != null) {
                removeAttributeModifier(player, entry.getAttribute(), entry.getUuid());
            }
        });

        player.setHealth(player.getHealth());
    }

    @Override
    public void validateAndFixAttributes() {
        if (!isActive) return;

        attributeValues.forEach((name, provider) -> {
            AttributeRegistry.AttributeEntry entry = AttributeRegistry.get(name);
            if (entry != null) {
                double value = provider.getValue();
                if (value != 0.0) {
                    validateAndFixAttribute(
                            entry.getAttribute(),
                            entry.getUuid(),
                            value,
                            getRaceName() + " " + name,
                            entry.getOperation());
                }
            }
        });
    }

    // 抽象方法：获取种族特定的魔力加成值和种族名称
    public abstract float getManaBonus();


    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        if (this.isActive == active) return;

        if (active) {
            // 先设置状态
            this.isActive = true;
            // 应用体型
            applyScaleFactor();
            // 应用属性
            applyAttributes();
            // 增加魔力值
            float manaBonus = getManaBonus();
            if (!player.level().isClientSide) {
                ManaData.modifyMaxMana(player, manaBonus);
            }
        } else {
            // 移除魔力值
            float manaBonus = getManaBonus();
            if (!player.level().isClientSide) {
                ManaData.modifyMaxMana(player, -manaBonus);
            }
            // 移除属性
            removeAttributes();
            // 重置体型
            resetScaleFactor();
            // 设置状态
            this.isActive = false;
        }

        sync();
    }

    public void applyEffects() {
        if (!isActive) return;

        applyAttributes();
        applyScaleFactor();
        sync();
    }

    @Override
    public float getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public void setScaleFactor(float scale) {
        this.scaleFactor = scale;
        if (isActive) {
            RaceScaleHelper.setSmoothModelScale(player, scale, 20);
        }
    }

    @Override
    public float getPermanentManaDecrease() {
        return permanentManaDecrease;
    }

    @Override
    public void setPermanentManaDecrease(float value) {
        this.permanentManaDecrease = value;
    }



    @Override
    public void tick() {
        if (!isActive || player.level().isClientSide) {
            return;
        }
        tickCounter++;
        onTick();
    }

    /**
     * Whether this race's features are drawn, with the setting applied as it happens.
     *
     * <p>Written through to the client on every change, so that a player turning their own features
     * off sees them go at once rather than at the next time something else about the race happens to
     * be synced.
     */
    @Override
    public boolean showTraits() {
        return showTraits;
    }

    @Override
    public void setShowTraits(boolean show) {
        if (this.showTraits == show) {
            return;
        }

        this.showTraits = show;
        sync();
    }

    @Override
    public int traitVariant() {
        return traitVariant;
    }

    @Override
    public void setTraitVariant(int variant) {
        if (this.traitVariant == variant) {
            return;
        }

        this.traitVariant = variant;
        sync();
    }

    @Override
    public int primaryTraitColor() {
        return primaryTraitColor;
    }

    @Override
    public void setPrimaryTraitColor(int colour) {
        if (this.primaryTraitColor == colour) {
            return;
        }

        this.primaryTraitColor = colour;
        sync();
    }

    @Override
    public int secondaryTraitColor() {
        return secondaryTraitColor;
    }

    @Override
    public void setSecondaryTraitColor(int colour) {
        if (this.secondaryTraitColor == colour) {
            return;
        }

        this.secondaryTraitColor = colour;
        sync();
    }

    @Override
    public int traitColorOption() {
        return traitColorOption;
    }

    @Override
    public void setTraitColorOption(int option) {
        if (this.traitColorOption == option) {
            return;
        }

        this.traitColorOption = option;
        sync();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("active", isActive);
        tag.putFloat("permanentManaDecrease", permanentManaDecrease);
        saveAppearance(tag);
        saveAdditional(tag);
        return tag;
    }

    /**
     * Writes the appearance out, under names of its own.
     *
     * <p>Kept apart from {@link #saveAdditional} because the two belong to different questions —
     * that one is what a race does, this one is what it looks like — and a subclass overriding that
     * one should not have to know these keys exist.
     */
    protected void saveAppearance(CompoundTag tag) {
        tag.putBoolean("showTraits", showTraits);
        tag.putInt("traitVariant", traitVariant);
        tag.putInt("primaryTraitColor", primaryTraitColor);
        tag.putInt("secondaryTraitColor", secondaryTraitColor);
        tag.putInt("traitColorOption", traitColorOption);
    }

    /**
     * Reads the appearance back, leaving the defaults for anything absent.
     *
     * <p>Absent is the ordinary case for a player who has never opened the appearance controls, and
     * it has to mean "unchanged" rather than "zero": a default of zero for the colour would draw
     * every such player's features black.
     */
    protected void loadAppearance(CompoundTag tag) {
        if (tag.contains("showTraits")) {
            showTraits = tag.getBoolean("showTraits");
        }

        if (tag.contains("traitVariant")) {
            traitVariant = tag.getInt("traitVariant");
        }

        if (tag.contains("primaryTraitColor")) {
            primaryTraitColor = tag.getInt("primaryTraitColor");
        }

        if (tag.contains("secondaryTraitColor")) {
            secondaryTraitColor = tag.getInt("secondaryTraitColor");
        }

        if (tag.contains("traitColorOption")) {
            traitColorOption = tag.getInt("traitColorOption");
        }
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.isActive = tag.getBoolean("active");
        this.permanentManaDecrease = tag.getFloat("permanentManaDecrease");
        loadAppearance(tag);
        if (tag.contains("CurrentMaxMana")) {
            ManaData.setMaxMana(player, tag.getFloat("CurrentMaxMana"));
        }
        loadAdditional(tag);
    }

    @Override
    public void sync() {
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            RaceCapabilityNetworking.sendToPlayer(this, serverPlayer);
        }
    }


    public void applyPermanentManaModifier(float amount, boolean isBonus) {
        if (!player.level().isClientSide) {
            // 直接修改容器的魔力值
            ManaData.modifyMaxMana(player, isBonus ? amount : -amount);
        }
    }

    // 抽象方法
    protected abstract void onTick();

    @Override
    public void forceRemoveAllModifiers() {
        removeAttributes();
    }

    protected void saveAdditional(CompoundTag tag) {
        // 只保存当前容器的魔力值
        tag.putFloat("CurrentMaxMana", ManaData.getMaxMana(player));
    }

    protected void loadAdditional(CompoundTag tag) {
        // 直接恢复容器的魔力值
        if (tag.contains("CurrentMaxMana")) {
            ManaData.setMaxMana(player, tag.getFloat("CurrentMaxMana"));
        }
    }
    public void updateStateOnly(boolean active, float scale) {
        this.isActive = active;
        this.scaleFactor = scale;
        if (isActive) {
            RaceScaleHelper.setSmoothModelScale(player, scale, 20);
        }
    }

    /**
     * Takes the appearance from a sync packet.
     *
     * <p>Written without going through the setters, which each call {@link #sync()} back to the
     * server — on the client that would be a reply to a packet that just arrived, and the two would
     * talk to each other for as long as the connection lasted.
     *
     * <p>Separate from {@link #updateStateOnly} rather than folded into it because the two arrive in
     * one packet but mean different things: one is what the race does, the other is what it looks
     * like, and a caller that has only one of them to hand should be able to say so.
     */
    public void updateAppearance(boolean show, int variant, int primary, int secondary, int colorOption) {
        this.showTraits = show;
        this.traitVariant = variant;
        this.primaryTraitColor = primary;
        this.secondaryTraitColor = secondary;
        this.traitColorOption = colorOption;
    }

    // 静态方法用于清除所有种族能力
    public static void clearAllRaceAbilities(Player player) {

        // 记录当前激活的种族
        StringBuilder activeRaces = new StringBuilder();
        ModCapabilities.RACE_CAPABILITIES.forEach((raceName, capability) -> {
            player.getCapability(capability).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap && abstractCap.isActive) {
                    activeRaces.append(raceName).append(", ");
                }
            });
        });
        // 清除所有种族能力
        ModCapabilities.RACE_CAPABILITIES.forEach((raceName, capability) -> {
            player.getCapability(capability).ifPresent(cap -> {
                if (cap instanceof AbstractRaceCapability abstractCap && abstractCap.isActive) {
                    abstractCap.setActive(false);
                }
            });
        });

    }

    // 辅助方法：属性修改器管理
    protected void addAttributeModifier(Player player, Attribute attribute, UUID uuid,
                                        String name, double value, AttributeModifier.Operation operation) {
        var instance = player.getAttribute(attribute);
        if (instance != null) {
            var modifier = instance.getModifier(uuid);
            if (modifier == null || Math.abs(modifier.getAmount() - value) > 0.0001) {
                instance.removeModifier(uuid);
                instance.addTransientModifier(new AttributeModifier(uuid, name, value, operation));
            }
        }
    }

    protected void removeAttributeModifier(Player player, Attribute attribute, UUID uuid) {
        var instance = player.getAttribute(attribute);
        if (instance != null && instance.getModifier(uuid) != null) {
            instance.removeModifier(uuid);
        }
    }

    protected void validateAndFixAttribute(Attribute attribute, UUID uuid,
                                           double value, String name,
                                           AttributeModifier.Operation operation) {
        var instance = player.getAttribute(attribute);
        if (instance != null) {
            var modifier = instance.getModifier(uuid);
            if (modifier == null || Math.abs(modifier.getAmount() - value) > 0.0001) {
                instance.removeModifier(uuid);
                instance.addTransientModifier(new AttributeModifier(uuid, name, value, operation));
            }
        }
    }

    // 辅助方法：检查属性修改器是否存在
    protected boolean hasAttributeModifier(Attribute attribute, UUID uuid) {
        var instance = player.getAttribute(attribute);
        return instance != null && instance.getModifier(uuid) != null;
    }

    // 辅助方法：获取属性修改器的值
    protected double getAttributeModifierValue(Attribute attribute, UUID uuid) {
        var instance = player.getAttribute(attribute);
        if (instance != null) {
            var modifier = instance.getModifier(uuid);
            if (modifier != null) {
                return modifier.getAmount();
            }
        }
        return 0.0;
    }
    public void applyScaleFactor() {
        if (isActive) {
            RaceScaleHelper.setSmoothModelScale(player, scaleFactor, 20);
        }
    }

    protected void resetScaleFactor() {
        RaceScaleHelper.setSmoothModelScale(player, 1.0f, 20);
        float currentScale = RaceScaleHelper.getCurrentModelScale(player);
        if (Math.abs(currentScale - 1.0f) > 0.01f) {
            // 如果没有正确恢复，强制设置
            RaceScaleHelper.setModelScale(player, 1.0f);
        }
    }

    protected void handleWallClimbInternal(double climbSpeed, double horizontalDrag) {
        // 检查玩家是否在墙上且按着W键
        if (!player.onGround() && !player.isInWater() && !player.isInLava() && player.zza > 0) {
            if (isPlayerTouchingWall()) {
                Vec3 motion = player.getDeltaMovement();

                // 检查玩家是否在潜行
                if (player.isShiftKeyDown()) {
                    // 如果玩家正在潜行，将垂直速度设为0，保持在当前位置
                    player.setDeltaMovement(motion.x, 0, motion.z);
                } else {
                    // 正常爬墙
                    player.setDeltaMovement(motion.x, climbSpeed, motion.z);
                }

                // 重置下落距离以防止摔落伤害
                player.resetFallDistance();

                // 减小水平移动以增加稳定性
                player.setDeltaMovement(player.getDeltaMovement().multiply(horizontalDrag, 1.0, horizontalDrag));
            }
        } else {
            // 如果玩家贴着墙但没有按W键，检查是否在潜行
            if (isPlayerTouchingWall() && player.isShiftKeyDown()) {
                // 如果玩家在潜行，保持在当前位置
                player.setDeltaMovement(player.getDeltaMovement().multiply(0.7, 0, 0.7));
                player.resetFallDistance();
            }
        }
    }

    protected boolean isPlayerTouchingWall() {
        AABB boundingBox = player.getBoundingBox();
        AABB checkBox = boundingBox.inflate(0.15, 0, 0.15);
        Level level = player.level();

        double[] checkPoints = {
                player.getX() - 0.3, player.getX() + 0.3,
                player.getZ() - 0.3, player.getZ() + 0.3
        };

        int minY = (int) Math.floor(boundingBox.minY);
        int maxY = (int) Math.ceil(boundingBox.maxY);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = minY; y <= maxY; y++) {
            pos.set(checkPoints[0], y, player.getZ());
            if (isValidWall(level, pos)) return true;

            pos.set(checkPoints[1], y, player.getZ());
            if (isValidWall(level, pos)) return true;

            pos.set(player.getX(), y, checkPoints[2]);
            if (isValidWall(level, pos)) return true;

            pos.set(player.getX(), y, checkPoints[3]);
            if (isValidWall(level, pos)) return true;
        }

        return false;
    }

    protected boolean isValidWall(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (!blockState.isAir() && blockState.isSolid() &&
                !(blockState.getBlock() instanceof LadderBlock) &&
                !(blockState.getBlock() instanceof VineBlock)) {
            VoxelShape shape = blockState.getCollisionShape(level, pos);
            return !shape.isEmpty();
        }
        return false;
    }
}
