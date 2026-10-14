package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.capability.api.IRaceAppearance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A taurus's horns, in the two shapes the race ships, and the neck bell.
 *
 * <p>The shape selector picks the horns: zero is the female pair, one the male pair. A second shape
 * selector — the auxiliary one — turns the bell on and off. The bell is its
 * own animated model, hung from the neck and swung by the player's movement.
 */
public final class TaurusFeatureRenderer implements RaceFeatureRenderer
{
    private final HornFeatureRenderer femaleHorns = HornFeatureRenderer.taurusHorns("taurus/tuarian_horns_f");
    private final HornFeatureRenderer maleHorns = HornFeatureRenderer.taurusHorns("taurus/tuarian_horns");
    private static final TaurusBellRenderer BELL = new TaurusBellRenderer();

    /**
     * 单独画牛铃，供装饰饰品的三个带铃外观（TAURUS_BELL / TAURUS_F_BELL / TAURIAN_BELL）直接调用。
     */
    public static void renderBell(AbstractClientPlayer player,
                                  PlayerModel<AbstractClientPlayer> parentModel,
                                  PoseStack pose,
                                  MultiBufferSource buffer,
                                  int light,
                                  IRaceAppearance appearance,
                                  float partialTick)
    {
        BELL.render(player, parentModel, pose, buffer, light, appearance, partialTick);
    }

    @Override
    public void render(AbstractClientPlayer player,
                       PlayerModel<AbstractClientPlayer> parentModel,
                       PoseStack pose,
                       MultiBufferSource buffer,
                       int light,
                       IRaceAppearance appearance,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTick,
                       float ageInTicks)
    {
        if (!appearance.showTraits())
        {
            return;
        }

        // 雄角为形状 1，其余（含默认 0）为雌角。
        if (appearance.traitVariant() == 1)
        {
            maleHorns.render(player, parentModel, pose, buffer, light, appearance,
                    limbSwing, limbSwingAmount, partialTick, ageInTicks);
        }
        else
        {
            femaleHorns.render(player, parentModel, pose, buffer, light, appearance,
                    limbSwing, limbSwingAmount, partialTick, ageInTicks);
        }

        // 副形状 0 = 戴铃铛（auxVariant == 0 时画铃）。
        if (appearance.auxTraitVariant() == 0)
        {
            BELL.render(player, parentModel, pose, buffer, light, appearance, partialTick);
        }
    }

    /**
     * The bull's neck bell, swung by the player's movement.
     *
     * <p>A model hung off the body, tinted with the race's third colour. The sway is a fixed set of
     * numbers — the bell lags behind a turn and swings out through it, eased rather than snapped.
     */
    private static final class TaurusBellRenderer
    {
        private static final ResourceLocation BELL = RaceModelRenderer.modelId("taurus/tuarian_cowbell");
        private static final Map<AbstractClientPlayer, Sway> SWAY = new WeakHashMap<>();

        private void render(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> parentModel,
                            PoseStack pose, MultiBufferSource buffer, int light,
                            IRaceAppearance appearance, float partialTick)
        {
            boolean slim = "slim".equals(player.getModelName());

            Sway sway = SWAY.computeIfAbsent(player, key -> new Sway());

            double dx = player.getX() - player.xOld;
            double dz = player.getZ() - player.zOld;
            Vec3 look = player.getViewVector(partialTick);
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            double forward = dx * look.x + dz * look.z;

            sway.amplitude = Math.min(sway.amplitude + horizontal * 1.5D, 1.0D) * 0.97D;
            double targetTilt = -forward * 6.0D * sway.amplitude;
            sway.tiltX = (sway.tiltX + (targetTilt - sway.tiltX) * 0.08D) * 0.92D;

            double bellTiltX = Math.max(-1.0D, Math.min(1.0D, sway.tiltX * sway.amplitude)) * -30.0D;
            double phase = (player.tickCount + partialTick) * 0.35D;
            double angleZ = Math.sin(phase + sway.tiltX * 0.3D) * sway.amplitude * 15.0D;

            boolean chest = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
            int aux = RaceColors.of(appearance).aux();

            pose.pushPose();
            parentModel.body.translateAndRotate(pose);
            pose.mulPose(Axis.ZP.rotationDegrees((float) angleZ));
            pose.mulPose(Axis.XP.rotationDegrees((float) bellTiltX));
            pose.mulPose(Axis.XP.rotationDegrees(-15.0F));
            pose.translate(0.0D, 0.44D + (slim ? 0.0D : 0.12D), -0.16D);
            float scale = slim ? 0.7F : 1.0F;
            pose.scale(scale, scale, scale);
            pose.translate(0.0D, chest ? 0.12D : 0.10D, chest ? -0.04D : 0.04D);
            pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
            pose.translate(-0.5F, -0.5F, 0.5F);
            pose.translate(0.0D, 0.2D, -1.0D);

            RaceModelRenderer.model(pose, buffer, light, BELL, aux, -1, -1);
            pose.popPose();
        }
    }

    /** How far the bell is currently swung, and how much movement is feeding it. */
    private static final class Sway
    {
        private double amplitude;
        private double tiltX;
    }
}