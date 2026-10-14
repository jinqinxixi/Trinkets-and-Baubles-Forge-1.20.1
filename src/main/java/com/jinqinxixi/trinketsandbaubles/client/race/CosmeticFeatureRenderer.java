package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.api.IRaceAppearance;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.items.baubles.CosmeticFeature;
import com.jinqinxixi.trinketsandbaubles.items.baubles.CosmeticItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * 装饰饰品的渲染器：按当前外观（feature）把对应的种族特征画到玩家身上。
 *
 * <p>装饰饰品不是种族、没有自己的 capability。颜色：有激活种族就借那个种族的颜色，没有则
 * 全白。外观分发到已有的种族特征绘制器——角用 {@link HornFeatureRenderer} 的各个静态工厂，耳用
 * {@link EarFeatureRenderer}，翅用 {@link FairyFeatureRenderer}/{@link DragonFeatureRenderer}，
 * 猫灵用 {@link FaelisFeatureRenderer}，金牛用 {@link TaurusFeatureRenderer}。
 */
@OnlyIn(Dist.CLIENT)
public final class CosmeticFeatureRenderer implements ICurioRenderer
{
    @Override
    @SuppressWarnings("unchecked")
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack pose,
            RenderLayerParent<T, M> parent, MultiBufferSource buffers, int light,
            float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (!(slotContext.entity() instanceof AbstractClientPlayer player)
                || !(parent.getModel() instanceof PlayerModel<?>))
        {
            return;
        }

        PlayerModel<AbstractClientPlayer> model = (PlayerModel<AbstractClientPlayer>) (Object) parent.getModel();
        CosmeticFeature feature = CosmeticItem.featureOf(stack);
        IRaceAppearance appearance = appearance(player);

        switch (feature)
        {
            case FAIRY ->
                    new FairyFeatureRenderer().render(player, model, pose, buffers, light, appearance,
                            limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case ELF ->
                    new EarFeatureRenderer(EarFeatureRenderer.ELF).render(player, model, pose, buffers,
                            light, appearance, limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case GOBLIN ->
                    new EarFeatureRenderer(EarFeatureRenderer.GOBLIN).render(player, model, pose, buffers,
                            light, appearance, limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case FAELIS ->
                    // 照原版：耳朵外观 = 耳朵 + 爪子，无尾巴。
                    new FaelisFeatureRenderer(true, false, true).render(player, model, pose, buffers, light, appearance,
                            limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case FAELIS_TAIL ->
                    // 照原版：尾巴外观 = 只有尾巴，无耳朵和爪子。
                    new FaelisFeatureRenderer(false, true, false).render(player, model, pose, buffers, light, appearance,
                            limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case DRAGON ->
                    new DragonFeatureRenderer().render(player, model, pose, buffers, light, appearance,
                            limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case TAURUS ->
                    HornFeatureRenderer.taurusHorns("taurus/tuarian_horns")
                            .render(player, model, pose, buffers, light, appearance,
                                    limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case TAURUS_F ->
                    HornFeatureRenderer.taurusHorns("taurus/tuarian_horns_f")
                            .render(player, model, pose, buffers, light, appearance,
                                    limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case TAURUS_BELL ->
            {
                HornFeatureRenderer.taurusHorns("taurus/tuarian_horns")
                        .render(player, model, pose, buffers, light, appearance,
                                limbSwing, limbSwingAmount, partialTicks, ageInTicks);
                TaurusFeatureRenderer.renderBell(player, model, pose, buffers, light, appearance, partialTicks);
            }

            case TAURUS_F_BELL ->
            {
                HornFeatureRenderer.taurusHorns("taurus/tuarian_horns_f")
                        .render(player, model, pose, buffers, light, appearance,
                                limbSwing, limbSwingAmount, partialTicks, ageInTicks);
                TaurusFeatureRenderer.renderBell(player, model, pose, buffers, light, appearance, partialTicks);
            }

            case TAURIAN_BELL ->
                    TaurusFeatureRenderer.renderBell(player, model, pose, buffers, light, appearance, partialTicks);

            case SUCCUBUS ->
                    HornFeatureRenderer.succubusHorns("succubus/succubus_horns")
                            .render(player, model, pose, buffers, light, appearance,
                                    limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case GENERIC_HORNS ->
                    HornFeatureRenderer.genericHorns("generic/horns", false)
                            .render(player, model, pose, buffers, light, appearance,
                                    limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case GENERIC_HORNS_INVERTED ->
                    HornFeatureRenderer.genericHorns("generic/horns_inverted", true)
                            .render(player, model, pose, buffers, light, appearance,
                                    limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            case DRAGON_HORNS ->
                    HornFeatureRenderer.dragonHorns("dragon/dragon_horns")
                            .render(player, model, pose, buffers, light, appearance,
                                    limbSwing, limbSwingAmount, partialTicks, ageInTicks);

            // HUMAN / DWARF / TITAN / TAURUS_BELL / TAURUS_F_BELL / TAURIAN_BELL 无外观。
            default ->
            {
            }
        }
    }

    /**
     * 玩家当前激活种族的颜色；没有激活种族时借「人类」能力的外观（无种族时借 NONE 种族
     * 的 appearance，能随调色界面变）。
     */
    private static IRaceAppearance appearance(AbstractClientPlayer player)
    {
        for (var entry : ModCapabilities.RACE_CAPABILITIES.entrySet())
        {
            var present = player.getCapability(entry.getValue()).resolve().orElse(null);
            if (present instanceof AbstractRaceCapability race && race.isActive())
            {
                return race;
            }
        }

        // 无激活种族：借人类能力的 appearance，它是「默认种族」，颜色能被调色界面调。
        var human = player.getCapability(ModCapabilities.HUMAN_CAPABILITY).resolve().orElse(null);
        return human instanceof IRaceAppearance appearance ? appearance : CosmeticDefaultAppearance.INSTANCE;
    }
}