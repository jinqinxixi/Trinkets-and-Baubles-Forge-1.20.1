package com.jinqinxixi.trinketsandbaubles.client.curios;

import com.jinqinxixi.trinketsandbaubles.client.race.RaceModelRenderer;
import com.jinqinxixi.trinketsandbaubles.client.race.RaceQuadRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * 饰品「戴上显示外观」的渲染器。
 *
 * <p>用 Curios 的 {@link CuriosRendererRegistry} 给每个会显示外观的饰品登记一个
 * {@link ICurioRenderer}，在玩家佩戴时把外观画到身体上。共五个（海之石、伤害盾、末影王冠、
 * 法埃利斯之爪、装饰饰品），这里补前四个；装饰饰品（cosmetic）本身还没做，留待以后。
 *
 * <p>两类画法：海之石和伤害盾把物品图标贴在胸口（一个平面）；末影王冠画一个三维王冠模型；
 * 法埃利斯之爪在手背画一对爪子（与猫族外观共用同一张贴图）。
 */
@OnlyIn(Dist.CLIENT)
public final class AccessoryRenderers
{
    /** 末影王冠的三维模型，画在玩家头上。 */
    public static final ResourceLocation ENDER_CROWN_MODEL =
            new ResourceLocation("trinketsandbaubles", "item/ender_tiara_model");

    private static final ResourceLocation CLAWS =
            new ResourceLocation("trinketsandbaubles", "textures/races/faelis/claws.png");

    private AccessoryRenderers()
    {
    }

    public static void register()
    {
        CuriosRendererRegistry.register(
                com.jinqinxixi.trinketsandbaubles.items.ModItem.SEA_STONE.get(),
                () -> new ChestItem(0.0F, 0.16F, 0.14F, 0.0F));

        CuriosRendererRegistry.register(
                com.jinqinxixi.trinketsandbaubles.items.ModItem.DAMAGE_SHIELD.get(),
                () -> new ChestItem(0.17F, 0.22F, 0.16F, 0.14F));

        CuriosRendererRegistry.register(
                com.jinqinxixi.trinketsandbaubles.items.ModItem.ENDER_TIARA.get(),
                EnderCrown::new);

        CuriosRendererRegistry.register(
                com.jinqinxixi.trinketsandbaubles.items.ModItem.FAELIS_CLAW.get(),
                FaelisClaw::new);

        // 装饰饰品：按当前外观把对应的种族特征画到玩家身上。
        CuriosRendererRegistry.register(
                com.jinqinxixi.trinketsandbaubles.items.ModItem.COSMETIC.get(),
                com.jinqinxixi.trinketsandbaubles.client.race.CosmeticFeatureRenderer::new);
    }

    /**
     * 胸口贴片：把物品图标画在胸口，海之石和伤害盾共用。
     *
     * <p>{@code ChestItem} 渲染器，一个平面图标贴着胸甲。穿胸甲时图标略微前移，避免被
     * 胸甲模型吞掉。四个偏移参数让两个饰品各就各位。
     */
    private static final class ChestItem implements ICurioRenderer
    {
        private final float offsetX;
        private final float offsetY;
        private final float offsetZ;
        private final float armorShiftX;

        private ChestItem(float offsetX, float offsetY, float offsetZ, float armorShiftX)
        {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.armorShiftX = armorShiftX;
        }

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(
                ItemStack stack, SlotContext slotContext, PoseStack pose,
                RenderLayerParent<T, M> parent, MultiBufferSource buffers, int light,
                float limbSwing, float limbSwingAmount, float partialTicks,
                float ageInTicks, float netHeadYaw, float headPitch)
        {
            if (parent.getModel() instanceof HumanoidModel<?> model)
            {
                LivingEntity entity = slotContext.entity();
                pose.pushPose();
                model.body.translateAndRotate(pose);
                pose.mulPose(Axis.XP.rotationDegrees(180.0F));
                pose.translate(this.offsetX, -this.offsetY, this.offsetZ);

                if (!entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
                {
                    pose.translate(this.offsetX - this.armorShiftX, 0.0F, -(this.offsetZ - 0.2F));
                }

                pose.scale(0.1875F, 0.1875F, 0.1875F);
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        stack, ItemDisplayContext.NONE, light,
                        OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), 0);
                pose.popPose();
            }
        }
    }

    /**
     * 末影王冠：把三维王冠模型画在玩家头顶。
     */
    private static final class EnderCrown implements ICurioRenderer
    {
        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(
                ItemStack stack, SlotContext slotContext, PoseStack pose,
                RenderLayerParent<T, M> parent, MultiBufferSource buffers, int light,
                float limbSwing, float limbSwingAmount, float partialTicks,
                float ageInTicks, float netHeadYaw, float headPitch)
        {
            if (parent.getModel() instanceof HumanoidModel<?> model)
            {
                boolean helmet = !slotContext.entity().getItemBySlot(EquipmentSlot.HEAD).isEmpty();
                pose.pushPose();
                model.head.translateAndRotate(pose);
                pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
                pose.translate(-0.5F, 0.1125F, -0.3F);
                pose.translate(0.0F, helmet ? 0.07F : 0.0F, helmet ? -0.04F : 0.0F);

                RaceModelRenderer.model(pose, buffers, light, ENDER_CROWN_MODEL, -1, -1, -1);
                pose.popPose();
            }
        }
    }

    /**
     * 法埃利斯之爪：在手背各画一只爪子，与猫族外观同一张贴图。
     *
     * <p>猫族（faelis）玩家已经由种族外观画了爪子，这里跳过，避免画两遍。
     */
    private static final class FaelisClaw implements ICurioRenderer
    {
        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(
                ItemStack stack, SlotContext slotContext, PoseStack pose,
                RenderLayerParent<T, M> parent, MultiBufferSource buffers, int light,
                float limbSwing, float limbSwingAmount, float partialTicks,
                float ageInTicks, float netHeadYaw, float headPitch)
        {
            if (slotContext.entity() instanceof AbstractClientPlayer player
                    && parent.getModel() instanceof PlayerModel<?> model)
            {
                boolean slim = "slim".equals(player.getModelName());
                float offsetX = slim ? -12.4F : -18.6F;

                claw(model.rightArm, pose, buffers, light, offsetX);
                claw(model.leftArm, pose, buffers, light, -offsetX);
            }
        }

        private void claw(net.minecraft.client.model.geom.ModelPart arm, PoseStack pose,
                          MultiBufferSource buffers, int light, float offsetX)
        {
            pose.pushPose();
            arm.translateAndRotate(pose);
            pose.scale(0.01F, 0.01F, 0.01F);
            pose.translate(offsetX, 61.0F, -21.0F);
            pose.mulPose(Axis.YP.rotationDegrees(-90.0F));

            RaceQuadRenderer.quad(pose, buffers, CLAWS, 0.0D, 0.0D, 0.0D,
                    0.0F, 0.0F, 32, 32, 32.0D, 32.0D, 32.0F, 32.0F, 0xFFFFFF, light);

            pose.popPose();
        }
    }
}
