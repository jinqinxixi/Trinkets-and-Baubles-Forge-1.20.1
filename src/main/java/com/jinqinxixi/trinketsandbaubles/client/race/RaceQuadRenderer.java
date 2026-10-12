package com.jinqinxixi.trinketsandbaubles.client.race;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws a race's features, which are flat quads cut out of a texture.
 *
 * <p>Follows the reference implementation rather than a reading of the API. The vertex call it makes
 * is the one that carries the pose's own matrices — a position matrix and a normal matrix — and the
 * shorter overload that takes only coordinates is not enough for the vertex format an entity render
 * type uses. A quad written with the short form ends its vertex with elements unset, and the buffer
 * throws rather than filling them in.
 *
 * <p>The normal is passed through the pose's normal matrix and points along the model's own up axis,
 * which is what the reference does. Getting the direction from memory would be a guess at how the
 * shader lights a flat surface; this is the answer that was arrived at by looking at the result.
 *
 * <p>Coordinates are in the space of whatever is on the pose stack when a draw is made, so a caller
 * positions the feature by moving the stack and then draws at the origin of the shape. The matrices
 * handed to the vertex call are that same stack, read at the moment of the draw.
 */
public final class RaceQuadRenderer
{
    private RaceQuadRenderer()
    {
    }

    /**
     * Draws one quad from a texture, tinted.
     *
     * <p>The tint multiplies the texture rather than replacing it, which is what lets one greyscale
     * ear texture be drawn in any colour a player picks without a copy of the image per colour.
     *
     * @param pose      the stack, already positioned where the quad belongs
     * @param buffers   the buffer source for the frame being drawn
     * @param texture   the image, whose bands are named by the four texture arguments
     * @param x         left edge, in the stack's own units
     * @param y         bottom edge
     * @param z         depth; a small offset separates two quads drawn on the same plane
     * @param u         left edge of the band in the texture, in texture pixels
     * @param v         top edge of the band
     * @param uWidth    width of the band, in texture pixels
     * @param vHeight   height of the band
     * @param width     width to draw, in stack units; negative mirrors the quad
     * @param height    height to draw
     * @param tileWidth the texture's full width, which the band is measured against
     * @param tileHeight its full height
     * @param rgb       tint, as packed RGB, multiplied into the texture
     * @param light     packed light at the feature's position
     */
    public static void quad(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture,
                            double x, double y, double z,
                            float u, float v, int uWidth, int vHeight,
                            double width, double height,
                            float tileWidth, float tileHeight,
                            int rgb, int light)
    {
        // The render type is asked for per quad, which is what makes one texture per draw call
        // possible: the buffer is bound to the texture when it is requested, and two ears from the
        // same image share the one that is already open.
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(texture));

        PoseStack.Pose last = pose.last();

        float r = (rgb >> 16 & 0xFF) / 255.0F;
        float g = (rgb >> 8 & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;

        // Texture coordinates are named in pixels and read as a fraction of the whole image, which
        // is why the tile size arrives with every call rather than being assumed to be 16: an ear
        // texture is its own size and the bands are positions inside it.
        float u0 = u / tileWidth;
        float u1 = (u + uWidth) / tileWidth;
        float v0 = v / tileHeight;
        float v1 = (v + vHeight) / tileHeight;

        // Counter-clockwise from the bottom left, which is the winding the entity render types
        // expect. A four-cornered quad drawn the other way round is culled and simply not there.
        vertex(consumer, last, light, x, y + height, z, u0, v1, r, g, b);
        vertex(consumer, last, light, x + width, y + height, z, u1, v1, r, g, b);
        vertex(consumer, last, light, x + width, y, z, u1, v0, r, g, b);
        vertex(consumer, last, light, x, y, z, u0, v0, r, g, b);
    }

    /**
     * Writes one corner.
     *
     * <p>Every element the entity vertex format carries is named: position through the pose's
     * position matrix, colour, texture coordinate, overlay, light, and a normal through the pose's
     * normal matrix. Leaving any of them out is what a buffer refuses to accept.
     *
     * <p>Overlay is the default rather than the player's own. It is what tints a vertex red while a
     * player is being hurt, and features that turned red with the player would be a step too far —
     * an ear is not part of the body that takes damage.
     */
    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int light,
                               double x, double y, double z,
                               float u, float v,
                               float r, float g, float b)
    {
        consumer.vertex(pose.pose(), (float) x, (float) y, (float) z)
                .color(r, g, b, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}