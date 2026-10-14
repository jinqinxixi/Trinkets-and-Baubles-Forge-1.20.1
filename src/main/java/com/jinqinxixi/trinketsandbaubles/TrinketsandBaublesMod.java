package com.jinqinxixi.trinketsandbaubles;

import com.jinqinxixi.trinketsandbaubles.block.ModBlocks;

import com.jinqinxixi.trinketsandbaubles.capability.network.RaceCapabilityNetworking;
import com.jinqinxixi.trinketsandbaubles.compat.FirstAidCompat;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsRingItem;
import com.jinqinxixi.trinketsandbaubles.items.baubles.PolarizedStoneItem;
import com.jinqinxixi.trinketsandbaubles.items.baubles.ShieldofHonorItem;
import com.jinqinxixi.trinketsandbaubles.items.ModCreativeModeTab;
import com.jinqinxixi.trinketsandbaubles.items.ModItem;

import com.jinqinxixi.trinketsandbaubles.loot.LootTableHandler;
import com.jinqinxixi.trinketsandbaubles.init.ModElements;
import com.jinqinxixi.trinketsandbaubles.init.ModEntities;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay;
import com.jinqinxixi.trinketsandbaubles.modifier.CurioAttributeEvents;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import com.jinqinxixi.trinketsandbaubles.recast.AnvilRecastRegistry;
import com.jinqinxixi.trinketsandbaubles.util.ScanSystem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import com.jinqinxixi.trinketsandbaubles.traits.elements.Element;
import com.jinqinxixi.trinketsandbaubles.traits.elements.ItemElements;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

import static com.jinqinxixi.trinketsandbaubles.modeffects.ModEffects.EFFECTS;

@Mod(TrinketsandBaublesMod.MOD_ID)
public class TrinketsandBaublesMod
{
    public static final String MOD_ID = "trinketsandbaubles";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TrinketsandBaublesMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        NetworkHandler.register();
        // 注册基础内容（两端都需要）
        ModItem.register(modEventBus);
        ModCreativeModeTab.register(modEventBus);
        ModBlocks.register(modEventBus);
        EFFECTS.register(modEventBus);
        ModElements.register(modEventBus);
        ModEntities.register(modEventBus);
        // 注册配方里「匹配某个元素物品」的 ingredient 序列化器（龙元素变体配方用它）。
        modEventBus.addListener((net.minecraftforge.registries.RegisterEvent event) -> {
            if (event.getRegistryKey().equals(net.minecraftforge.registries.ForgeRegistries.Keys.RECIPE_SERIALIZERS))
            {
                net.minecraftforge.common.crafting.CraftingHelper.register(
                        com.jinqinxixi.trinketsandbaubles.util.recipes.ElementIngredient.ID,
                        com.jinqinxixi.trinketsandbaubles.util.recipes.ElementIngredient.Serializer.INSTANCE);
            }
        });
        modEventBus.register(ModConfig.class);
        // 通用事件监听
        MinecraftForge.EVENT_BUS.register(LootTableHandler.class);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(ManaData.class);
        // 注册网络处理器
        RaceCapabilityNetworking.init();
        // First aid mod 兼容性处理
        tryRegisterFirstAidCompat();
        // 配置注册
        ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                ModConfig.SPEC,
                "trinketsandbaubles-common.toml"
        );
        ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                RaceAttributesConfig.SPEC,
                "trinketsandbaubles-races.toml"
        );

        // 添加配置加载事件监听器
        modEventBus.addListener(this::onConfigLoad);

        // 客户端专属内容集中处理
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
        }
    }

    private void tryRegisterFirstAidCompat() {
        if (FMLLoader.getLoadingModList().getModFileById("firstaid") != null) {
            try {
                // 尝试加载 FirstAid 的类
                Class.forName("ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent");
                // 如果成功加载，注册兼容性处理器
                MinecraftForge.EVENT_BUS.register(new FirstAidCompat());
                LOGGER.info("FirstAid compatibility enabled");
            } catch (ClassNotFoundException e) {
                LOGGER.error("FirstAid API classes not found, compatibility disabled");
            }
        }
    }

    private void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == net.minecraftforge.fml.config.ModConfig.Type.COMMON) {
            // 组式矿石探测已替换为贪婪之眼（GreedyEyesSystem），无需在这里初始化矿石组。
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class ModEvents {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            float newDamage = ShieldofHonorItem.onDamage(
                    event.getEntity(),
                    event.getSource(),
                    event.getAmount()
            );
            event.setAmount(newDamage);
        }

        /**
         * Stacks a resistance arriving on a wearer who already has one.
         *
         * <p>The shield keeps a resistance on its wearer, and the tooltip promises that receiving
         * the same effect again is worth a level — so a potion of resistance on top of the shield's
         * own raises the level rather than being refused by an equal effect. Only a wearer of the
         * shield is answered, so everyone else's resistance behaves as it always did.
         *
         * <p>The effect is changed in place, and the event is refused only when the answer says so.
         * The shield's own resistance is not exempted — it does not need to be, because the shield
         * applies it only when the wearer has none, and an effect arriving on a wearer who has none is
         * not stacked.
         */
        @SubscribeEvent
        public static void onEffectApplicable(MobEffectEvent.Applicable event) {
            if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)
                    || !ShieldofHonorItem.hasShield(player)) {
                return;
            }

            MobEffectInstance effect = event.getEffectInstance();

            if (effect != null && ShieldofHonorItem.stackResistance(player, effect)) {
                event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onMouseInput(InputEvent.MouseButton.Pre event) {
            ManaHudOverlay overlay = ManaHudOverlay.getInstance();
            if (!overlay.canDrag) return;

            Minecraft minecraft = Minecraft.getInstance();
            Window window = minecraft.getWindow();
            double guiScale = window.getGuiScale();

            double scaledX = minecraft.mouseHandler.xpos() / guiScale;
            double scaledY = minecraft.mouseHandler.ypos() / guiScale;

            if (event.getAction() == InputConstants.PRESS) {
                // 在拖动模式下，阻止所有鼠标点击事件被传递给游戏
                if (overlay.mouseClicked(scaledX, scaledY, event.getButton())) {
                    event.setCanceled(true);
                }
                // 即使点击了其他地方，也不隐藏鼠标
                if (overlay.canDrag) {
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            ManaHudOverlay overlay = ManaHudOverlay.getInstance();
            if (!overlay.canDrag) return;

            Minecraft minecraft = Minecraft.getInstance();
            Window window = minecraft.getWindow();
            double guiScale = window.getGuiScale();

            double mouseX = minecraft.mouseHandler.xpos() / guiScale;
            double mouseY = minecraft.mouseHandler.ypos() / guiScale;

            overlay.mouseDragged(mouseX, mouseY);
        }
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // 注册饰品的「戴上显示外观」渲染器（海之石、伤害盾、末影王冠、法埃利斯之爪）。
            com.jinqinxixi.trinketsandbaubles.client.curios.AccessoryRenderers.register();

            // 注册吸引模式材质参数
            ItemProperties.register(
                    ModItem.POLARIZED_STONE.get(),
                    new ResourceLocation(MOD_ID, "attraction_mode"),
                    (stack, level, entity, seed) ->
                            stack.hasTag() && stack.getTag().getBoolean(PolarizedStoneItem.ATTRACTION_MODE_TAG) ? 1.0F : 0.0F
            );

            // 注册防御模式材质参数
            ItemProperties.register(
                    ModItem.POLARIZED_STONE.get(),
                    new ResourceLocation(MOD_ID, "deflection_mode"),
                    (stack, level, entity, seed) ->
                            stack.hasTag() && stack.getTag().getBoolean(PolarizedStoneItem.DEFLECTION_MODE_TAG) ? 1.0F : 0.0F
            );

            // 龙戒/龙宝石的元素变体材质：读物品堆上的元素 NBT，模型据此切到火/冰/雷贴图。
            // 0.25=火、0.5=冰、0.75=雷，与模型 overrides 里的阈值对应。
            ResourceLocation elementProperty = new ResourceLocation(MOD_ID, "element");
            for (Item item : new Item[]{ModItem.DRAGONS_RING.get(), ModItem.DRAGON_GEM.get()}) {
                ItemProperties.register(
                        item,
                        elementProperty,
                        (stack, level, entity, seed) -> {
                            Element element = ItemElements.getPrimary(stack,
                                    com.jinqinxixi.trinketsandbaubles.init.ModElements.NEUTRAL.get());
                            if (element == com.jinqinxixi.trinketsandbaubles.init.ModElements.FIRE.get()) {
                                return 0.25F;
                            }
                            if (element == com.jinqinxixi.trinketsandbaubles.init.ModElements.ICE.get()) {
                                return 0.5F;
                            }
                            if (element == com.jinqinxixi.trinketsandbaubles.init.ModElements.LIGHTNING.get()) {
                                return 0.75F;
                            }
                            return 0.0F;
                        }
                );
            }

            // 装饰饰品的图标按外观变体切换：predicate 值是当前外观序号（8~17），模型 overrides 据此
            // 换成对应的种族特征模型（牛角/魅魔角/通用角/龙角/猫尾），0~7 用基础图标。
            ItemProperties.register(
                    ModItem.COSMETIC.get(),
                    new ResourceLocation(MOD_ID, "cosmetic"),
                    (stack, level, entity, seed) ->
                            com.jinqinxixi.trinketsandbaubles.items.baubles.CosmeticItem.featureOf(stack).ordinal()
            );

            // 魔法糖果的图标按堆叠比例切换：堆得越多贴图越换（0.25/0.5/0.75 三档）。
            ItemProperties.register(
                    ModItem.MANA_CANDY.get(),
                    new ResourceLocation(MOD_ID, "candy"),
                    (stack, level, entity, seed) -> (float) stack.getCount() / (float) stack.getMaxStackSize()
            );
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 初始化战利品配置
            ModConfig.loadLootConfig();
            AnvilRecastRegistry.registerAllRecipes();

   //         CurioAttributeEvents.init();
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        // 获取玩家当前生命值和最大生命值
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();

        // 如果当前生命值超过最大生命值，进行调整
        if (currentHealth > maxHealth) {
            player.hurt(player.damageSources().generic(), 0f);
            player.setHealth(maxHealth);
        }
        // 如果属性未正确应用，强制刷新
        else if (Math.abs(currentHealth - player.getHealth()) > 0.01f) {
            player.hurt(player.damageSources().generic(), 0f);
            player.setHealth(currentHealth);
        }
    }
}
