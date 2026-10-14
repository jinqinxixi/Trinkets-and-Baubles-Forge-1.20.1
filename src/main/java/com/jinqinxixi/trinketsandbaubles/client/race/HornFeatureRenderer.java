package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.capability.api.IRaceAppearance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * The horns shared by the races that have them, drawn from model JSON.
 *
 * <p>Each race is a model plus the transform that puts it on the head. The transform is a small
 * sequence of turns and nudges — which axis, how far, in what order — and the order is what makes it
 * work: two rotations about the same axes in the other order land the horns somewhere else. Every
 * sequence here is the reference implementation's, kept as it was.
 *
 * <p>Horns hang off the head, so they turn with it. A model placed against the body instead would
 * slide out of the skull the moment the player looked up.
 *
 * <p>The tint indices the models name are what turn one greyscale horn texture into a pair of horns
 * in up to three colours. A race drawn in one colour passes the same value for all three and the
 * model's faces all come out alike.
 */
public final class HornFeatureRenderer implements RaceFeatureRenderer
{
    private final ResourceLocation modelId;
    private final Consumer<PoseStack> transform;

    /**
     * Whether this model names one tint slot rather than three.
     *
     * <p>The plain horns and the dragon's do; the bull's does not. It decides whether the model's
     * first slot is filled from the race's third colour or its first, and whether the other two slots
     * are given anything at all.
     */
    private final boolean oneColour;

    public HornFeatureRenderer(ResourceLocation modelId, Consumer<PoseStack> transform, boolean oneColour)
    {
        this.modelId = modelId;
        this.transform = transform;
        this.oneColour = oneColour;
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
        pose.pushPose();

        // On the head, so the horns turn with it rather than with the body.
        parentModel.head.translateAndRotate(pose);

        // The transform that places this particular pair. Run inside the head's own space, which is
        // what the model's author was looking at when they wrote the numbers down.
        transform.accept(pose);

        RaceColors colours = RaceColors.of(appearance);

        // Which colours the model's tint slots are filled from: the plain horns take only the third
        // colour, and the bull's take all three.
        //
        // -1 for a slot nothing is given to, which draws that face with white — the texture's own
        // colours. That is not the same as passing the third colour for all three: a model with two
        // slots would then draw both in the same tint, and the others are deliberately left alone.
        int primary = oneColour ? colours.aux() : colours.primary();
        int alt = oneColour ? -1 : colours.secondary();
        int aux = oneColour ? -1 : colours.aux();

        RaceModelRenderer.model(pose, buffer, light, modelId, primary, alt, aux);

        pose.popPose();
    }

    /**
     * The pair of horns that sit upright on the crown, tilted back.
     *
     * <p>Drawn upside down and then turned back the right way, which is how the model was authored:
     * the reference turns it a half circle about Z, lifts it, tilts it most of the way over about X,
     * and then nudges it into place.
     */
    public static HornFeatureRenderer genericHorns(String modelPath, boolean inverted)
    {
        ResourceLocation id = RaceModelRenderer.modelId(modelPath);

        if (inverted)
        {
            return new HornFeatureRenderer(id, pose ->
            {
                pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
                pose.translate(0.0D, 0.0D, -0.7D);
                pose.mulPose(Axis.XP.rotationDegrees(-20.0F));
                pose.translate(-0.5F, -0.5F, 0.5F);
            }, true);
        }

        return new HornFeatureRenderer(id, pose ->
        {
            pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
            pose.translate(0.0D, 1.2D, 0.42D);
            pose.mulPose(Axis.XP.rotationDegrees(135.0F));
            pose.translate(-0.5F, -0.5F, 0.5F);
        }, true);
    }

    /**
     * The horns that curl forward and out, used by the succubus.
     *
     * <p>One tint slot, like the other plain pairs — this model takes the third colour
     * and leaves the other two empty.
     *
     * <p>Present for the sake of a race this mod does not currently have. It is kept because the
     * model and its texture are already here and the drawing code is the only thing missing, so a
     * race added later needs no new drawing.
     */
    public static HornFeatureRenderer succubusHorns(String modelPath)
    {
        return new HornFeatureRenderer(RaceModelRenderer.modelId(modelPath), pose ->
        {
            pose.mulPose(Axis.XP.rotationDegrees(190.0F));
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.scale(0.9F, 0.9F, 0.9F);
            pose.translate(-0.5F, -0.275F, 0.1875F);
            pose.translate(0.0D, 0.2D, -1.0D);
        }, true);
    }

    /** A dragon's, smaller and set further forward than the generic pair. */
    public static HornFeatureRenderer dragonHorns(String modelPath)
    {
        return new HornFeatureRenderer(RaceModelRenderer.modelId(modelPath), pose ->
        {
            pose.translate(0.27D, -0.48D, -0.3D);
            pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
            pose.scale(0.54F, 0.54F, 0.54F);
        }, true);
    }

    /**
     * The bull's horns, in the two shapes the race ships.
     *
     * <p>Larger than the others and lifted, and the only pair here that takes all three colours: its
     * model names a horn, the ridge around its base, and a third tone, and all three are filled.
     * That difference is what the constructor's flag carries.
     *
     * <p>Kept for the same reason as the succubus pair — the model and texture are here and only the
     * race is missing.
     */
    public static HornFeatureRenderer taurusHorns(String modelPath)
    {
        return new HornFeatureRenderer(RaceModelRenderer.modelId(modelPath), pose ->
        {
            pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
            pose.scale(1.5F, 1.5F, 1.5F);
            pose.translate(-0.5F, -0.5F, 0.5F);
            pose.translate(0.0D, 0.2D, -1.0D);
        }, false);
    }
}