package com.jinqinxixi.trinketsandbaubles;

import com.jinqinxixi.trinketsandbaubles.block.ModBlocks;

import com.jinqinxixi.trinketsandbaubles.capability.network.RaceCapabilityNetworking;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsEyeItem;
import com.jinqinxixi.trinketsandbaubles.items.baubles.DragonsRingItem;
import com.jinqinxixi.trinketsandbaubles.items.baubles.PolarizedStoneItem;
import com.jinqinxixi.trinketsandbaubles.items.baubles.ShieldofHonorItem;
import com.jinqinxixi.trinketsandbaubles.client.renderer.DragonsEyeRenderer;
import com.jinqinxixi.trinketsandbaubles.items.ModCreativeModeTab;
import com.jinqinxixi.trinketsandbaubles.items.ModItem;

import com.jinqinxixi.trinketsandbaubles.loot.LootTableHandler;
import com.jinqinxixi.trinketsandbaubles.capability.mana.ManaData;
import com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay;
import com.jinqinxixi.trinketsandbaubles.modifier.CurioAttributeEvents;
import com.jinqinxixi.trinketsandbaubles.network.handler.NetworkHandler;
import com.jinqinxixi.trinketsandbaubles.recast.AnvilRecastRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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

        // 通用事件监听
        MinecraftForge.EVENT_BUS.register(LootTableHandler.class);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(ManaData.class);
        // 注册网络处理器
        RaceCapabilityNetworking.init();

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
            MinecraftForge.EVENT_BUS.addListener(
                    EventPriority.NORMAL,
                    false,
                    RenderLevelStageEvent.class,
                    DragonsEyeRenderer::onRenderWorld
            );
        }
        // First aid mod 兼容性
        if (FMLLoader.getLoadingModList().getModFileById("firstaid") != null) {
            MinecraftForge.EVENT_BUS.register(new Object() {
                @SubscribeEvent
                public void onDamage(FirstAidLivingDamageEvent event) {
                    // 检查是否启用了功能
                    if (!ModConfig.ENABLE_DAMAGE_SHIELD_EFFECT.get()) {
                        return;
                    }

                    Player player = event.getEntity();

                    // 使用新的API方法查找护盾饰品
                    Optional<SlotResult> result = CuriosApi.getCuriosInventory(player)
                            .resolve()
                            .flatMap(handler -> handler.findFirstCurio(ModItem.DAMAGE_SHIELD.get()));

                    if (result.isPresent()) {
                        AbstractPlayerDamageModel after = event.getAfterDamage();
                        if (after.HEAD.currentHealth < 1 || (after.BODY.currentHealth < 1)) {
                            // 使用配置的触发概率
                            if (player.getRandom().nextDouble() < ModConfig.DAMAGE_SHIELD_TRIGGER_CHANCE.get()) {
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            });
        }
    }

    private void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == net.minecraftforge.fml.config.ModConfig.Type.COMMON) {
            DragonsEyeItem.initializeOreGroups();
            DragonsRingItem.initializeOreGroups();
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
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 初始化战利品配置
            ModConfig.loadLootConfig();
            AnvilRecastRegistry.registerAllRecipes();

            CurioAttributeEvents.init();
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
}
