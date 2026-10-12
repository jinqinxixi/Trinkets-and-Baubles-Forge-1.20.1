package com.jinqinxixi.trinketsandbaubles.client.race.elf;

import com.jinqinxixi.trinketsandbaubles.client.race.RaceFeatureRenderer;
import com.jinqinxixi.trinketsandbaubles.client.race.RaceQuadRenderer;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * An elf's ears.
 *
 * <p>Two quads, mirrored, splayed away from the skull, coloured in one or two tones. Every number is
 * the reference implementation's: the scale, the offsets that put the ear against the head, the
 * rotation that angles it back, and the four bands of the texture the outer, inner and shaded parts
 * are cut from. They were arrived at by looking at the result, and a second set of numbers invented
 * here would be a second guess at the same question.
 *
 * <p>An elf has one shape, so nothing here reads the variant. The reference has a second, smaller
 * elf style only in the sense that other races reuse this drawing code with their own numbers; a
 * race that ships two shapes branches here.
 */
public class RaceElfFeatureRenderer implements RaceFeatureRenderer
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("trinketsandbaubles", "textures/races/elf/ears.png");

    /** Drawn small and scaled up as a whole, so the offsets below stay readable. */
    private static final float SCALE = 0.30F;

    /** Where the ear sits in the head's own space: out to the side, down to the jaw, back behind. */
    private static final double OFFSET_X = 0.72D;
    private static final double OFFSET_Y = -1.5D;
    private static final double OFFSET_Z = -0.4D;

    /** How far back the ear is angled away from the face, in degrees. */
    private static final float YAW = 26.0F;

    /**
     * The texture's own size, which the bands are measured against.
     *
     * <p>Sixteen wide and sixty-four tall: four bands stacked down a strip, the outer ear at the top,
     * then the two tones, then the shaded underside.
     */
    private static final float TILE_WIDTH = 16.0F;
    private static final float TILE_HEIGHT = 64.0F;

    @Override
    public void render(AbstractClientPlayer player,
                       PlayerModel<AbstractClientPlayer> parentModel,
                       PoseStack pose,
                       MultiBufferSource buffer,
                       int light,
                       AbstractRaceCapability appearance,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTick,
                       float ageInTicks)
    {
        pose.pushPose();

        // Hanging off the head rather than the body, which is what makes them turn with it. The
        // model part applies its own rotation and translation to the stack, so everything after this
        // is in the head's own space and follows a player looking up, down or around.
        parentModel.head.translateAndRotate(pose);

        // Worn headgear is drawn slightly proud of the skull, and ears that kept the skull's offset
        // would poke through a helmet. Moved out and widened a little, matching the reference.
        if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty())
        {
            pose.translate(0.0F, -0.02F, -0.045F);
            pose.scale(1.1F, 1.1F, 1.1F);
        }

        pose.scale(SCALE, SCALE, SCALE);

        int primary = colourFor(appearance, true);
        int secondary = colourFor(appearance, false);

        // Turned to face outward, the near ear drawn, then turned twice that to face the other way
        // and the far ear drawn with its width negated. The reference draws the pair this way rather
        // than pushing a second pose, and the shape was adjusted against the result.
        pose.mulPose(Axis.YP.rotationDegrees(-YAW));

        ear(pose, buffer, light, primary, secondary, OFFSET_X, 1.0D);

        pose.mulPose(Axis.YP.rotationDegrees(YAW * 2.0F));

        ear(pose, buffer, light, primary, secondary, -OFFSET_X, -1.0D);

        pose.popPose();
    }

    /**
     * Draws one ear at a given offset and width.
     *
     * <p>Two tones when the player has picked two, one when they have not. A player who left the
     * palette alone is drawn in the texture's own colours, which is why the two arriving equal is
     * the signal to draw the plain band rather than the tinted pair.
     *
     * <p>The shaded underside is drawn a hair in front of the others, at the same place. Two quads
     * coplanar to the last bit would fight for the same depth and flicker as the camera moved.
     */
    private void ear(PoseStack pose, MultiBufferSource buffer, int light,
                     int primary, int secondary, double x, double width)
    {
        if (primary == secondary)
        {
            // One colour: the base band, then the shaded underside in the same colour.
            RaceQuadRenderer.quad(pose, buffer, TEXTURE, x, OFFSET_Y, OFFSET_Z,
                    0.0F, 0.0F, 16, 16, width, 1.0D, TILE_WIDTH, TILE_HEIGHT, primary, light);

            RaceQuadRenderer.quad(pose, buffer, TEXTURE, x, OFFSET_Y, OFFSET_Z + 1.0E-4D,
                    0.0F, 48.0F, 16, 16, width, 1.0D, TILE_WIDTH, TILE_HEIGHT, primary, light);
        }
        else
        {
            // Two colours: the outer shell in the first, the inner in the second, and the shaded
            // underside in the second as well — it is the inside of the ear that is in shadow.
            RaceQuadRenderer.quad(pose, buffer, TEXTURE, x, OFFSET_Y, OFFSET_Z,
                    0.0F, 16.0F, 16, 16, width, 1.0D, TILE_WIDTH, TILE_HEIGHT, primary, light);

            RaceQuadRenderer.quad(pose, buffer, TEXTURE, x, OFFSET_Y, OFFSET_Z,
                    0.0F, 32.0F, 16, 16, width, 1.0D, TILE_WIDTH, TILE_HEIGHT, secondary, light);

            RaceQuadRenderer.quad(pose, buffer, TEXTURE, x, OFFSET_Y, OFFSET_Z + 1.0E-4D,
                    0.0F, 48.0F, 16, 16, width, 1.0D, TILE_WIDTH, TILE_HEIGHT, secondary, light);
        }
    }

    /**
     * The colour to draw in, reading the palette the way the reference reads it.
     *
     * <p>Which of the two colours is the "primary" one swaps with the colour option, and the option
     * also decides whether either is used at all: at zero the features take the player's own colours
     * and both of these come back the same, which is what sends the drawing down its single-tone
     * path.
     */
    private static int colourFor(AbstractRaceCapability appearance, boolean outer)
    {
        if (appearance.traitColorOption() == 0)
        {
            // The natural look, and what a player who has configured nothing gets: the texture's own
            // colours, which is to say no tint at all.
            return 0xFFFFFF;
        }

        boolean swap = appearance.traitColorOption() == 1;

        if (outer)
        {
            return swap ? appearance.secondaryTraitColor() : appearance.primaryTraitColor();
        }

        return swap ? appearance.primaryTraitColor() : appearance.secondaryTraitColor();
    }
}