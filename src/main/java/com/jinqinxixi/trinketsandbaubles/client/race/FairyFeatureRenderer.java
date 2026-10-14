package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.capability.api.IRaceAppearance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * A fairy's wings, in whichever of the two sets the player picked.
 *
 * <p>Two sets, and which one is drawn depends on the shape selector. The flat pair is two quads that
 * open while the player is in the air; the other is two boxes that flap, built in code. Choosing
 * between them is the whole of what that slider does for this race, which is why it was doing
 * nothing before — the renderer drew the flat pair and never looked at the value.
 *
 * <p>The boxes are drawn for shape one and the quads for shape zero. A race
 * whose shape is left at nought gets the flat pair, which is the set that needs nothing baked and so
 * is the one that always works.
 */
public final class FairyFeatureRenderer implements RaceFeatureRenderer
{
    private static final ResourceLocation WINGS =
            new ResourceLocation("trinketsandbaubles", "textures/races/fairy/fairy_wings.png");

    /** The texture the flat pair is cut from. */
    private static final float TILE_WIDTH = 36.0F;
    private static final float TILE_HEIGHT = 42.0F;

    /**
     * The wing beat, in degrees, read once per tick while airborne.
     *
     * <p>A cycle rather than a curve: the angle steps through this list and
     * wraps, which gives a beat that is quick at the ends and slow in the middle without any
     * trigonometry.
     */
    private static final float[] FLAP_CYCLE =
            { 64.0F, 48.0F, 32.0F, 16.0F, 1.0F, 1.0F, 16.0F, 32.0F, 48.0F, 64.0F };

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
        // Shape zero is the flat pair, shape one the boxes. A player who has never touched the
        // selector has shape zero and gets the pair that needs no baked model.
        if (appearance.traitVariant() == 1)
        {
            modelWings(player, parentModel, pose, buffer, light, appearance, limbSwing, limbSwingAmount, partialTick);
        }
        else
        {
            flatWings(player, parentModel, pose, buffer, light, appearance, partialTick);
        }
    }

    /**
     * The pair cut from a texture, which open while the player is in the air.
     *
     * <p>The angle is counted rather than curved: the player's age is multiplied by a rate and the
     * result closes the wing by that many degrees, so the beat starts fast and settles.
     */
    private void flatWings(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> parentModel,
                           PoseStack pose, MultiBufferSource buffer, int light,
                           IRaceAppearance appearance, float partialTick)
    {
        RaceColors colours = RaceColors.of(appearance);

        int openAngle = player.onGround()
                ? 0
                : Math.min((int) ((player.tickCount + partialTick) * 24.0F % 60.0F), 59);
        float angle = Math.max(50.0F - openAngle, 0.0F);

        pose.pushPose();

        // On the back, like the dragon's.
        parentModel.body.translateAndRotate(pose);

        // Authored in a different scale and axis, brought to the player's units and stood upright.
        pose.scale(0.0625F, 0.0625F, 0.0625F);
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        pose.translate(0.0F, -2.0F, 0.0F);

        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
        {
            pose.translate(-0.4F, -1.0F, 0.0F);
        }

        // Two wings, each in a pose of its own so that one's opening angle is not carried into the
        // other's. The z the quad is drawn at places it on one side of the spine or the other, and
        // the opening angle is taken the opposite way for the second.
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.translate(-1.0F, 0.0F, 0.0F);
        quad(pose, buffer, light, colours.primary(), -1.0D);
        pose.popPose();

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-angle));
        pose.translate(-1.0F, 0.0F, 0.0F);
        quad(pose, buffer, light, colours.primary(), 1.0D);
        pose.popPose();

        pose.popPose();
    }

    /** One flat wing, drawn as a single quad. */
    private void quad(PoseStack pose, MultiBufferSource buffer, int light, int colour, double z)
    {
        RaceQuadRenderer.quad(pose, buffer, WINGS, -18.0D, 0.0D, z,
                0.0F, 0.0F, 36, 42, 16.0D, 16.0D, TILE_WIDTH, TILE_HEIGHT, colour, light);
    }

    /**
     * The pair built from boxes, which flap.
     *
     * <p>The sizes come from the model's own shape: two boxes of no width, sixteen tall and eight
     * deep, one set two units to the left and three forward of the other. There is no scale applied
     * to them — this model is drawn at the player's own
     * size, unlike the flat pair which is authored small and scaled up — and adding one here is what
     * makes the wings come out the wrong size and in the wrong place.
     *
     * <p>Drawn without culling, deliberately. These are flat boxes with no
     * width, so half their faces point away from the camera and are discarded by default, which
     * leaves a wing that shows from one side only.
     *
     * <p>Each wing is pitched about the yaw axis by an amount that depends on what the player is
     * doing: folded when still, beating through a fixed cycle while airborne, and swinging with the
     * stride otherwise. The pitch is written onto the baked part rather than onto the pose, which is
     * what makes the two wings move independently.
     */
    private void modelWings(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> parentModel,
                            PoseStack pose, MultiBufferSource buffer, int light,
                            IRaceAppearance appearance,
                            float limbSwing, float limbSwingAmount, float partialTick)
    {
        ModelPart right = FairyWingModel.right();
        ModelPart left = FairyWingModel.left();

        if (right == null || left == null)
        {
            // The boxes were never baked. The flat pair is drawn instead of nothing: a player who
            // picked the second shape and got no wings at all would read that as the race being
            // broken rather than as a model that failed to load.
            flatWings(player, parentModel, pose, buffer, light, appearance, partialTick);
            return;
        }

        pose.pushPose();

        // On the back, in the body's own space. Nothing is culled here, so this is the only
        // placement the model gets.
        parentModel.body.translateAndRotate(pose);

        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
        {
            pose.translate(0.0F, -0.1F, 0.06F);
            pose.scale(1.1F, 1.1F, 1.1F);
        }

        // How far the player has moved this tick, which decides whether they count as still. The
        // The fixed threshold, eight hundredths of a block.
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;
        boolean onGround = player.onGround();
        float flap = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;

        if (Math.abs(dx) < 0.08D && Math.abs(dz) < 0.08D && onGround)
        {
            // Still: folded, held slightly open so that they read as wings and not as a panel.
            right.yRot = -0.5F;
            left.yRot = 0.5F;
        }
        else if (!onGround && !player.isPassenger())
        {
            // Airborne: the beat, stepped through a fixed cycle rather than carried by a curve.
            double time = player.level().getGameTime() + player.tickCount + partialTick;
            float beat = FLAP_CYCLE[(int) (time * 3.0D % FLAP_CYCLE.length)] * (float) (Math.PI / 180.0);

            left.yRot = beat;
            right.yRot = -beat;
        }
        else
        {
            // Walking: they swing with the stride, the two opposed.
            right.yRot = -flap;
            left.yRot = flap;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(FAKE_WINGS));

        // 这里不关闭剔除：1.12.2 版在这两次绘制周围关闭了剔除，1.20.1 版没有。这里采用 1.20.1
        // 版的做法，不关闭剔除，盒子按同样的方式绘制。
        right.render(pose, consumer, light, OverlayTexture.NO_OVERLAY);
        left.render(pose, consumer, light, OverlayTexture.NO_OVERLAY);

        pose.popPose();
    }

    /** The texture the box wings are drawn with, which is a separate image from the flat pair's. */
    private static final ResourceLocation FAKE_WINGS =
            new ResourceLocation("trinketsandbaubles", "textures/races/fairy/fairy_wings_fake.png");
}