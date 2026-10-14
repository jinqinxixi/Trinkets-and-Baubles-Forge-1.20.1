package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.capability.api.IRaceAppearance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * The ears shared by the races that have them.
 *
 * <p>Three races wear the same pair of quads with different numbers, which is why this is one class
 * with a style per race rather than three classes holding near-identical drawing code. The style is
 * the whole of what makes an elf's ear different from a goblin's: the texture, the scale, where it
 * sits against the skull, how far it splays, and whether it is tilted as well as turned.
 *
 * <p>Every number is the reference implementation's. They were arrived at by looking at the result,
 * and a second set of numbers invented here would be a second guess at the same question.
 *
 * <p>A faelis has a tilt as well as a splay and so takes the second path through the drawing; the
 * other two leave the tilt at zero and take the first. The two paths exist because a tilt has to be
 * applied about a different axis, and rotating about an axis by nothing is not the same as not
 * rotating.
 */
public final class EarFeatureRenderer implements RaceFeatureRenderer
{
    /** An elf's ear: long, upright, angled back. */
    public static final Style ELF = new Style(
            texture("elf"), 0.30F, -1.5D, -0.4D, 0.72D, 26.0F, 0.0F);

    /** A goblin's: shorter, higher on the skull, angled back harder and tipped. */
    public static final Style GOBLIN = new Style(
            texture("goblin"), 0.34F, -1.2D, -0.6D, 0.5D, 30.0F, -10.0F);

    /** A faelis': larger, set lower, tipped the other way. */
    public static final Style FAELIS = new Style(
            texture("faelis"), 0.30F, -2.4D, -0.72D, -0.3D, 26.0F, 0.0F);

    /** How big the ear's texture is, which the bands below are measured against. */
    private static final float TILE_WIDTH = 16.0F;
    private static final float TILE_HEIGHT = 64.0F;

    private final Style style;

    public EarFeatureRenderer(Style style)
    {
        this.style = style;
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

        // Hanging off the head rather than the body, which is what makes them turn with it. The
        // model part applies its own rotation and translation, so everything after this is in the
        // head's own space and follows a player looking up, down or around.
        parentModel.head.translateAndRotate(pose);

        // Worn headgear is drawn slightly proud of the skull, and ears that kept the skull's offset
        // would poke through a helmet. Moved out and widened a little, matching the reference.
        if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty())
        {
            pose.translate(0.0F, -0.02F, -0.045F);
            pose.scale(1.1F, 1.1F, 1.1F);
        }

        pose.scale(style.scale(), style.scale(), style.scale());

        // The two colours, decided in one place so that every feature reads them the same way.
        RaceColors colours = RaceColors.of(appearance);

        int primary = colours.primary();
        int secondary = colours.secondary();

        if (style.pitch() == 0.0F)
        {
            // Turned to face outward, the near ear drawn, then turned twice that to face the other
            // way and the far ear drawn with its width negated.
            pose.mulPose(Axis.YP.rotationDegrees(-style.yaw()));
            ear(pose, buffer, light, primary, secondary, style.offsetX(), 1.0D);

            pose.mulPose(Axis.YP.rotationDegrees(style.yaw() * 2.0F));
            ear(pose, buffer, light, primary, secondary, -style.offsetX(), -1.0D);
        }
        else
        {
            // A tipped ear is drawn in a pose of its own either side, so that the tilt is not
            // carried over into the other ear's turn.
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-style.yaw()));
            pose.mulPose(Axis.XP.rotationDegrees(style.pitch()));
            ear(pose, buffer, light, primary, secondary, style.offsetX(), 1.0D);
            pose.popPose();

            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(style.yaw()));
            pose.mulPose(Axis.XP.rotationDegrees(style.pitch()));
            ear(pose, buffer, light, primary, secondary, -style.offsetX(), -1.0D);
            pose.popPose();
        }

        pose.popPose();
    }

    /**
     * Draws one ear.
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
            RaceQuadRenderer.quad(pose, buffer, style.texture(), x, style.y(), style.z(),
                    0.0F, 0.0F, 16, 16, width, 1.0D, TILE_WIDTH, TILE_HEIGHT, primary, light);

            RaceQuadRenderer.quad(pose, buffer, style.texture(), x, style.y(), style.z() + 1.0E-4D,
                    0.0F, 48.0F, 16, 16, width, 1.0D, TILE_WIDTH, TILE_HEIGHT, primary, light);
        }
        else
        {
            RaceQuadRenderer.quad(pose, buffer, style.texture(), x, style.y(), style.z(),
                    0.0F, 16.0F, 16, 16, width, 1.0D, TILE_WIDTH, TILE_HEIGHT, primary, light);

            RaceQuadRenderer.quad(pose, buffer, style.texture(), x, style.y(), style.z(),
                    0.0F, 32.0F, 16, 16, width, 1.0D, TILE_WIDTH, TILE_HEIGHT, secondary, light);

            RaceQuadRenderer.quad(pose, buffer, style.texture(), x, style.y(), style.z() + 1.0E-4D,
                    0.0F, 48.0F, 16, 16, width, 1.0D, TILE_WIDTH, TILE_HEIGHT, secondary, light);
        }
    }

    private static ResourceLocation texture(String race)
    {
        return new ResourceLocation("trinketsandbaubles", "textures/races/" + race + "/ears.png");
    }

    /**
     * The numbers that make one race's ear its own.
     *
     * <p>{@code offsetX} is how far out from the middle of the head the ear is placed and is negated
     * for the far ear, so a negative value there means the pair starts crossed and opens outward.
     */
    public record Style(ResourceLocation texture, float scale, double y, double z,
                        double offsetX, float yaw, float pitch)
    {
    }
}