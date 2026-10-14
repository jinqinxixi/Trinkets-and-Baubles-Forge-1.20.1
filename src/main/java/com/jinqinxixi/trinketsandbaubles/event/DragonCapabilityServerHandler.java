package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.entity.BreathProjectile;
import com.jinqinxixi.trinketsandbaubles.network.message.Messages.EffectsRenderPacket;
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
        // 喷的是会飞行的龙息球（BreathProjectile），命中实体伤害+点燃，命中地面生成滞留药水云。
        // 这里持续喷（每 3 tick 从眼睛射出一颗），球的飞行、命中、伤害、地面云全部走投射物自身逻辑。
        if (player.tickCount % 3 != 0) {
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 eye = player.getEyePosition();
        double yaw = Math.toRadians(player.getYRot() + 90.0F);
        double headX = player.getX() + 0.3F * Math.cos(yaw);
        double headZ = player.getZ() + 0.3F * Math.sin(yaw);
        double headY = player.getY() + player.getEyeHeight() * 0.8;

        // 龙息颜色跟随玩家种族外观的副色（getBreathColor = secondary trait color），
        // 玩家在种族外观界面改副色，龙息就跟着变。元素由龙戒/龙宝石变体决定（火/冰/雷）。
        String element = "fire";
        int color = 0xFF0000;
        var dragonCapOpt = player.getCapability(ModCapabilities.DRAGON_CAPABILITY).resolve();
        if (dragonCapOpt.isPresent() && dragonCapOpt.get() instanceof DragonCapability dragonCap) {
            element = dragonCap.getDragonElement();
            // 无元素的基础巨龙龙息按火算（点燃），"none" 是给 buff 图标区分用的。
            if ("none".equals(element)) {
                element = "fire";
            }
            color = dragonCap.secondaryTraitColor();
        }
        BreathProjectile breath = new BreathProjectile(level, player, color, element);
        breath.setPos(headX, headY, headZ);
        breath.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0.0F);
        level.addFreshEntity(breath);

        // 龙息声音：末影龙发射音，音量 0.5，音高 0.4/(随机*0.4+0.8)，
        // 每次发射龙息球（每 3 tick）播一次。
        level.playSound(null, headX, headY, headZ,
                SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS,
                0.5F, 0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        // 龙息已完全改由 BreathProjectile 投射物负责（onServerTick 发射），这里不再喷旧粒子。
    }
}