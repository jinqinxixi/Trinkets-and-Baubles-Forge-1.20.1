package com.jinqinxixi.trinketsandbaubles.items.baubles;

import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import com.jinqinxixi.trinketsandbaubles.capability.attribute.AttributeRegistry;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.init.ModElements;
import com.jinqinxixi.trinketsandbaubles.modifier.ModifiableBaubleItem;
import com.jinqinxixi.trinketsandbaubles.traits.elements.Element;
import com.jinqinxixi.trinketsandbaubles.traits.elements.IElementProvider;
import com.jinqinxixi.trinketsandbaubles.util.RaceRingUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class DragonsRingItem extends ModifiableBaubleItem implements IElementProvider {

    private static final Modifier[] MODIFIERS = Modifier.values();

    @Override
    public Modifier[] getModifiers() {
        return MODIFIERS;
    }

    /**
     * 龙戒的元素变体：火、冰、雷。
     *
     * <p>在调用时才取注册对象，不写成静态字段：元素注册表在类加载时还没填好，静态初始化里调
     * {@code get()} 会抛「Registry Object not present」。
     */
    @Override
    public Element[] getSubElements() {
        return new Element[]{
                ModElements.FIRE.get(),
                ModElements.ICE.get(),
                ModElements.LIGHTNING.get()
        };
    }

    /**
     * 名字按元素变体区分：{@code dragon_ring.fire} / {@code .ice} / {@code .lightning}。
     *
     * <p>创造栏里的三个变体是同一个物品加不同的元素 NBT，物品本身分不出来，名字要靠这里读 NBT
     * 决定——不这么做三个变体就是同一个名字、同一段描述，看起来像复制了三份。
     */
    @Override
    public String getDescriptionId(ItemStack stack) {
        Element element = getPrimaryElement(stack);
        if (!element.isNone()) {
            String name = element.getName().toLowerCase();
            if ("fire".equals(name) || "ice".equals(name) || "lightning".equals(name)) {
                return super.getDescriptionId() + "." + name;
            }
        }
        return super.getDescriptionId();
    }

    public DragonsRingItem(Properties properties) {
        super(properties);
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof DragonsRingItem))
                .isPresent();
    }

    private void applyDragonBuff(LivingEntity entity, ItemStack stack) {
        if (entity instanceof ServerPlayer serverPlayer) {
            if (RaceRingUtil.hasMultipleRaceRings(serverPlayer)) {
                serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        cap.setActive(false);
                    }
                });
                return;
            }

            serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                if (!cap.isActive()) {
                    AbstractRaceCapability.clearAllRaceAbilities(serverPlayer);
                    cap.setActive(true);
                }
            });
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        LivingEntity entity = slotContext.entity();

        if (entity instanceof ServerPlayer serverPlayer) {
            if (RaceRingUtil.hasMultipleRaceRings(serverPlayer)) {
                serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        cap.setActive(false);
                    }
                });
                return;
            }

            if (isEquipped(entity)) {
                serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    // 元素只在变化时才同步：换一枚不同元素的龙戒时能力仍是激活状态，只在「未激活」
                    // 时读会让元素永远停在第一次戴上的那枚。读一次 NBT 很便宜，但没必要每 tick 都写。
                    if (cap instanceof DragonCapability dragonCap) {
                        String ringElement = dragonCap.readElementFromRing(stack);
                        if (!ringElement.equals(dragonCap.getDragonElement())) {
                            dragonCap.applyElementFromRing(stack);
                        }
                    }
                    if (!cap.isActive()) {
                        applyDragonBuff(entity, stack);
                    }
                });
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);

        LivingEntity entity = slotContext.entity();
        if (entity instanceof ServerPlayer serverPlayer &&
                (prevStack.isEmpty() || !hasSameModifier(prevStack, stack))) {

            if (!RaceRingUtil.hasMultipleRaceRings(serverPlayer)) {
                if (isEquipped(entity)) {
                    // 戴上就把这枚戒指的元素写进能力，不等下一个 tick。
                    serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                        if (cap instanceof DragonCapability dragonCap) {
                            dragonCap.applyElementFromRing(stack);
                        }
                    });
                    applyDragonBuff(entity, stack);
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (entity instanceof ServerPlayer serverPlayer &&
                (newStack.isEmpty() || !hasSameModifier(newStack, stack))) {

            if (!isEquipped(entity)) {
                serverPlayer.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                    if (cap.isActive()) {
                        // 如果玩家已经死亡，不要恢复任何种族
                        if (serverPlayer.isDeadOrDying()) {
                            // 直接设置为非激活状态，不要恢复任何种族
                            cap.setActive(false);
                            // 清除掉保存的死亡状态数据
                            CompoundTag playerData = serverPlayer.getPersistentData();
                            if (playerData.contains("DragonCapability")) {
                                playerData.remove("DragonCapability");
                            }
                        } else {
                            // 正常的取消装备流程
                            cap.setActive(false);
                            CompoundTag playerData = serverPlayer.getPersistentData();
                            String savedRace = playerData.getString("SavedRace");
                            if (!savedRace.isEmpty()) {
                                RaceRingUtil.activateRace(serverPlayer, savedRace);
                            }
                        }
                    }
                });
            }
        }

        super.onUnequip(slotContext, newStack, stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            // 遍历所有属性并显示非零值
            for (Map.Entry<String, AttributeRegistry.AttributeEntry> entry : AttributeRegistry.getAll().entrySet()) {
                try {
                    String attributeName = entry.getKey();
                    // 直接从DRAGON实例获取值
                    double value = 0;
                    switch (attributeName) {
                        case "MAX_HEALTH":
                            value = RaceAttributesConfig.DRAGON.MAX_HEALTH.get();
                            break;
                        case "FOLLOW_RANGE":
                            value = RaceAttributesConfig.DRAGON.FOLLOW_RANGE.get();
                            break;
                        case "MOVEMENT_SPEED":
                            value = RaceAttributesConfig.DRAGON.MOVEMENT_SPEED.get();
                            break;
                        case "ATTACK_SPEED":
                            value = RaceAttributesConfig.DRAGON.ATTACK_SPEED.get();
                            break;
                        case "ATTACK_DAMAGE":
                            value = RaceAttributesConfig.DRAGON.ATTACK_DAMAGE.get();
                            break;
                        case "SWIM_SPEED":
                            value = RaceAttributesConfig.DRAGON.SWIM_SPEED.get();
                            break;
                        case "FLYING_SPEED":
                            value = RaceAttributesConfig.DRAGON.FLYING_SPEED.get();
                            break;
                        case "ENTITY_GRAVITY":
                            value = RaceAttributesConfig.DRAGON.ENTITY_GRAVITY.get();
                            break;
                        case "BLOCK_REACH":
                            value = RaceAttributesConfig.DRAGON.BLOCK_REACH.get();
                            break;
                        case "ENTITY_REACH":
                            value = RaceAttributesConfig.DRAGON.ENTITY_REACH.get();
                            break;
                        case "NAMETAG_DISTANCE":
                            value = RaceAttributesConfig.DRAGON.NAMETAG_DISTANCE.get();
                            break;
                        case "ARMOR":
                            value = RaceAttributesConfig.DRAGON.ARMOR.get();
                            break;
                        case "ARMOR_TOUGHNESS":
                            value = RaceAttributesConfig.DRAGON.ARMOR_TOUGHNESS.get();
                            break;
                        case "KNOCKBACK_RESISTANCE":
                            value = RaceAttributesConfig.DRAGON.KNOCKBACK_RESISTANCE.get();
                            break;
                        case "ATTACK_KNOCKBACK":
                            value = RaceAttributesConfig.DRAGON.ATTACK_KNOCKBACK.get();
                            break;
                        case "LUCK":
                            value = RaceAttributesConfig.DRAGON.LUCK.get();
                            break;
                        case "STEP_HEIGHT":
                            value = RaceAttributesConfig.DRAGON.STEP_HEIGHT.get();
                            break;
                    }

                    // 如果值不为0，添加到描述中
                    if (value != 0) {
                        AttributeRegistry.AttributeEntry attr = entry.getValue();
                        String displayText;

                        if (attr.isPercentage()) {
                            // 百分比属性，保留2位小数
                            displayText = String.format("%s %s%.1f%%",
                                    Component.translatable(attr.getTranslationKey()).getString(),
                                    value > 0 ? "+" : "",
                                    value * 100);
                        } else {
                            // 固定值属性，保留2位小数
                            displayText = String.format("%s %s%.2f",
                                    Component.translatable(attr.getTranslationKey()).getString(),
                                    value > 0 ? "+" : "",
                                    value);
                        }

                        tooltip.add(Component.literal(displayText)
                                .withStyle(value > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
                    }
                } catch (Exception e) {
                }
            }
        } else {
            String dragonBreathKeyName = KeyBindings.DRAGON_BREATH_KEY.getKey().getDisplayName().getString();
            String nightVisionKeyName = KeyBindings.DRAGON_NIGHT_VISION_KEY.getKey().getDisplayName().getString();
            String toggleModeKeyName = KeyBindings.TOGGLE_DRAGONS_EYE_MODE.getKey().getDisplayName().getString();
            String flightToggleKeyName = KeyBindings.DRAGON_FLIGHT_TOGGLE_KEY.getKey().getDisplayName().getString();

            // 简短描述
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip11")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip12",
                            dragonBreathKeyName)
                    .withStyle(ChatFormatting.RED));
            // 按元素显示免疫与专属能力（火：火免疫；冰：冰免疫 + 冰霜行者；雷：雷免疫 + 闪电箭）。
            Element ringElement = getPrimaryElement(stack);
            String elementName = ringElement.isNone() ? "fire" : ringElement.getName().toLowerCase();
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip13."
                            + elementName)
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip14",
                            nightVisionKeyName)
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip15",
                            toggleModeKeyName)
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.tooltip16",
                            flightToggleKeyName)
                    .withStyle(ChatFormatting.DARK_AQUA));
            tooltip.add(Component.translatable("item.trinketsandbaubles.glide.hint")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.trinketsandbaubles.glide.boost_hint")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.press_shift")
                    .withStyle(ChatFormatting.GRAY));

            // 在详细属性前添加魔力系统信息
            if (level != null && level.isClientSide) {
                Player player = net.minecraft.client.Minecraft.getInstance().player;
                if (player != null) {
                    player.getCapability(ModCapabilities.DRAGON_CAPABILITY).ifPresent(cap -> {
                        if (cap instanceof DragonCapability dragonCap) {
                            // 只在启用了植物魔法的情况下显示魔力信息
                            if (ModConfig.USE_BOTANIA_MANA.get() && net.minecraftforge.fml.ModList.get().isLoaded("botania")) {
                                // 显示魔力系统类型
                                tooltip.add(Component.translatable("item.trinketsandbaubles.dragons_ring.mana_system", "Botania")
                                        .withStyle(ChatFormatting.GOLD));

                                // 通过能力来获取当前魔力值
                                float currentMana = dragonCap.getCurrentMana();
                                tooltip.add(Component.translatable(
                                        "item.trinketsandbaubles.dragons_ring.current_mana",
                                        (int) currentMana
                                ).withStyle(ChatFormatting.AQUA));
                            }
                        }
                    });
                }
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
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