package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber
public class TeddyBear extends ModifiableBaubleItem {
    // 效果持续时间（以tick为单位，20 ticks = 1秒）
    private static final int REGENERATION_DURATION = 15 * 20;  // 15秒
    private static final int LUCK_DURATION = 30 * 20;         // 30秒
    private static final int HEALTH_BOOST_DURATION = 180 * 20; // 180秒

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    public TeddyBear(Properties properties) {
        super(properties);
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide ) {
            // 新增完整睡眠检测（必须满足条件）
            if (player.isSleepingLongEnough()) {
                // 检查玩家是否装备了泰迪熊
                CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                    handler.findFirstCurio(item -> item.getItem() instanceof TeddyBear)
                            .ifPresent(slotResult -> {
                                applyWakeUpEffects(player);
                                // 新增提示信息
                                player.sendSystemMessage(Component.translatable("item.trinketsandbaubles.teddy_bear.tooltip2")
                                        .withStyle(ChatFormatting.GOLD));
                                player.level().playSound(null, player.blockPosition(),
                                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.2F);
                            });
                });
            }
        }
    }

    private static void applyWakeUpEffects(Player player) {
        // 生命恢复 I (15秒)
        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                REGENERATION_DURATION,
                0, // 等级 0 = 效果 I
                false, // 是否显示粒子
                true, // 是否显示图标
                true  // 是否显示在物品栏
        ));

        // 幸运 (30秒)
        player.addEffect(new MobEffectInstance(
                MobEffects.LUCK,
                LUCK_DURATION,
                0,
                false,
                true,
                true
        ));

        // 生命提升 II (180秒)
        player.addEffect(new MobEffectInstance(
                MobEffects.HEALTH_BOOST,
                HEALTH_BOOST_DURATION,
                1, // 等级 1 = 效果 II
                false,
                true,
                true
        ));
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
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trinketsandbaubles.teddy_bear.tooltip1")
                .withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
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