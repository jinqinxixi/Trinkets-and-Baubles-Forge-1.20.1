package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.capability.api.IRaceAppearance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * A dragon's wings, drawn in three sections that move with the player.
 *
 * <p>Three quads hung off the back, each carrying the next: a root, a middle and a tip. Every number
 * is the reference implementation's, including the ones that only matter in flight — the sections
 * splay, rise and sweep back as the player leaves the ground, and the pair beats while they are in
 * the air.
 *
 * <p>On the ground the wings are folded and still. That is a deliberate state rather than an absence
 * of animation: a pair of wings beating while its owner stands still reads as a bug.
 *
 * <p>The two tints are the wing membrane and its leading arms, which is why the model names a second
 * texture when the player has picked two colours. Drawn from one texture when they have not, which
 * keeps a player who never opened the palette looking exactly as the texture was painted.
 */
public final class DragonFeatureRenderer implements RaceFeatureRenderer
{
    private static final ResourceLocation WINGS =
            texture("dragon/dragon_wings");
    private static final ResourceLocation WINGS_ARMS =
            texture("dragon/dragon_wings_arms");
    private static final ResourceLocation WINGS_LEATHER =
            texture("dragon/dragon_wings_leather");

    /** The texture's own size, which the bands are measured against. */
    private static final float TILE_WIDTH = 64.0F;
    private static final float TILE_HEIGHT = 64.0F;

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
        boolean slim = "slim".equals(player.getModelName());

        RaceColors colours = RaceColors.of(appearance);

        int primary = colours.primary();
        int secondary = colours.secondary();

        pose.pushPose();

        // On the back, which is where wings belong and what makes them follow the body rather than
        // the head.
        parentModel.body.translateAndRotate(pose);

        // The model is authored in a different scale and axis to the player's own parts, so it is
        // brought down to the player's units and turned upright before anything is placed.
        pose.scale(0.0625F, 0.0625F, 0.0625F);
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        pose.translate(0.0F, -2.0F, 0.0F);

        // A chestplate sits over the back, and wings that kept the body's offset would pass through
        // it. Moved back and down to clear it, matching the reference.
        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
        {
            pose.translate(-0.4F, -1.0F, 0.0F);
        }

        boolean onGround = player.onGround();
        float cycle = onGround ? 0.0F : (player.tickCount + partialTick) * 0.04F;
        float phase = cycle - Mth.floor(cycle);

        // One beat is a rise and a fall, not a sine: the wing sweeps down fast and returns slowly,
        // and the split at 0.45 is where the reference puts the changeover.
        float flap = phase < 0.45F
                ? -Mth.cos(phase / 0.45F * (float) Math.PI)
                : Mth.cos((phase - 0.45F) / 0.55F * (float) Math.PI);

        float forwardFlap = Math.max(0.0F, flap);
        float backwardFlap = Math.max(0.0F, -flap);
        float downFlap = 0.5F + 0.5F * flap;

        // The whole wing's placement, as one set of numbers: where it hangs, how far it sweeps back
        // in flight, and how it turns.
        float anchorX = (slim ? 0.0F : 0.2F) + ((onGround ? 1.0F : -1.0F) - downFlap * 1.5F);
        float anchorY = onGround ? -2.0F : -3.0F;
        float separation = (onGround ? 0.0F : 2.0F) + backwardFlap * 0.15F;
        float rootYaw = onGround ? -40.0F : 60.0F + flap * 40.0F;
        float rootPitch = onGround ? 0.0F : 10.0F;
        float rootRoll = onGround ? 0.0F : 15.0F + flap * 17.0F;
        float midYaw = -flap * (12.0F + forwardFlap * 24.0F + backwardFlap * 12.0F);

        // The tip carries the middle's own turn and adds to it, so the wing curls rather than hinging
        // flat at each joint. Squaring the flap here is what makes the curl gather towards the tips.
        float tipYaw = onGround ? 12.0F : midYaw * 1.05F + flap * Math.abs(flap) * 32.0F;

        wing(pose, buffer, light, primary, secondary, slim,
                anchorX, anchorY, separation, rootYaw, rootPitch, rootRoll, midYaw, tipYaw, 1);
        wing(pose, buffer, light, primary, secondary, slim,
                anchorX, anchorY, separation, rootYaw, rootPitch, rootRoll, midYaw, tipYaw, -1);

        pose.popPose();

        // The horns, in whichever of the three shapes the player picked.
        //
        // Three shapes rather than one, and they are not variations on a theme: the first two are the
        // plain pair used by every other horned race, drawn upside down or upright, and only the
        // third is a dragon's own. A player who says nothing gets the third, which is the one that
        // belongs to this race.
        switch (appearance.auxTraitVariant())
        {
            case 0 -> HornFeatureRenderer.genericHorns("generic/horns_inverted", true)
                    .render(player, parentModel, pose, buffer, light, appearance,
                            limbSwing, limbSwingAmount, partialTick, ageInTicks);

            case 1 -> HornFeatureRenderer.genericHorns("generic/horns", false)
                    .render(player, parentModel, pose, buffer, light, appearance,
                            limbSwing, limbSwingAmount, partialTick, ageInTicks);

            default -> HornFeatureRenderer.dragonHorns("dragon/dragon_horns")
                    .render(player, parentModel, pose, buffer, light, appearance,
                            limbSwing, limbSwingAmount, partialTick, ageInTicks);
        }
    }

    /**
     * Draws one wing as its three sections.
     *
     * <p>Each section is positioned inside the previous one's pose, so a turn at the root carries the
     * middle and the tip with it, and a turn at the middle carries the tip. That is what makes the
     * wing bend instead of moving in one piece.
     *
     * @param side 1 for one wing, -1 for the other; every rotation about the yaw and roll axes is
     *             taken the opposite way for the second, which is what mirrors the pair
     */
    private void wing(PoseStack pose, MultiBufferSource buffer, int light,
                      int primary, int secondary, boolean slim,
                      float anchorX, float anchorY, float separation,
                      float rootYaw, float rootPitch, float rootRoll,
                      float midYaw, float tipYaw, int side)
    {
        double rootX = -12.0D;
        double rootY = slim ? -19.0D : -20.0D;

        pose.pushPose();
        pose.translate(anchorX, anchorY, side * separation);
        pose.scale(0.9F, 0.9F, 0.9F);
        pose.mulPose(Axis.YP.rotationDegrees(side * rootYaw));
        pose.mulPose(Axis.XP.rotationDegrees(-side * rootPitch));
        pose.mulPose(Axis.ZP.rotationDegrees(-rootRoll));

        section(pose, buffer, light, primary, secondary, -12.0D, rootY, 48.0F, 16, 8);

        pose.pushPose();
        pose.translate(-12.0D, rootY, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(-side * midYaw));
        pose.translate(12.0D, -rootY, 0.0D);

        section(pose, buffer, light, primary, secondary, -28.0D, rootY, 16.0F, 32, 16);

        pose.pushPose();
        pose.translate(-28.0D, rootY, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(-side * tipYaw));
        pose.translate(28.0D, -rootY, 0.0D);

        section(pose, buffer, light, primary, secondary, -36.0D, rootY, 0.0F, 16, 8);

        pose.popPose();
        pose.popPose();
        pose.popPose();
    }

    /**
     * Draws one section of a wing.
     *
     * <p>One texture when the player has picked one colour, three when they have picked two: the
     * membrane, its arms and its leather in different images, each drawn over the last so that the
     * parts that should read as separate do.
     */
    private void section(PoseStack pose, MultiBufferSource buffer, int light,
                         int primary, int secondary, double x, double y, float u, int uWidth, int width)
    {
        if (primary == secondary)
        {
            RaceQuadRenderer.quad(pose, buffer, WINGS, x, y, 0.0D,
                    u, 0.0F, uWidth, 64, width, 32.0D, TILE_WIDTH, TILE_HEIGHT, primary, light);
        }
        else
        {
            RaceQuadRenderer.quad(pose, buffer, WINGS_ARMS, x, y, 0.0D,
                    u, 0.0F, uWidth, 64, width, 32.0D, TILE_WIDTH, TILE_HEIGHT, primary, light);

            RaceQuadRenderer.quad(pose, buffer, WINGS_LEATHER, x, y, 0.0D,
                    u, 0.0F, uWidth, 64, width, 32.0D, TILE_WIDTH, TILE_HEIGHT, secondary, light);
        }
    }

    private static ResourceLocation texture(String path)
    {
        return new ResourceLocation("trinketsandbaubles", "textures/races/" + path + ".png");
    }
}