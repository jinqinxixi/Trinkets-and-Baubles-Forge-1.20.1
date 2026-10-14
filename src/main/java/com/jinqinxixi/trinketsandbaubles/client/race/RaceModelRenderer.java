package com.jinqinxixi.trinketsandbaubles.client.race;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Draws the features that are real models rather than flat quads.
 *
 * <p>Horns, a tail, a bell: shapes with depth, authored in Blockbench and shipped as model JSON.
 * They are drawn through the block-model pipeline — the same one that draws an item lying on the
 * ground — because a baked model already holds its own quads, and re-deriving them from the JSON at
 * frame time would be a second implementation of the model loader.
 *
 * <p>That pipeline also gives the three tint slots the models were built with. The faces of a horn
 * name a tint index in the JSON, and those indices are what turn one greyscale texture into a pair
 * of horns in two colours. The colours arrive here and are handed to the quads by index.
 */
public final class RaceModelRenderer
{
    /** The model loader wants a source of randomness; the shapes have none, so one is reused. */
    private static final RandomSource RANDOM = RandomSource.create();

    private RaceModelRenderer()
    {
    }

    /**
     * Draws a baked model at whatever the pose stack is already positioned to.
     *
     * <p>Every direction is asked for its own quads and then the null direction is asked as well,
     * which is how the model loader splits a model into culled and unculled faces: the directional
     * ones are those that can be skipped when a neighbour hides them, and the null one holds the
     * rest. Asking only for the null direction would draw a model with its sides missing.
     *
     * @param color    the tint for faces naming index 0, or -1 to leave them the texture's own
     * @param altColor the tint for index 1, or -1
     * @param auxColor the tint for index 2, or -1
     */
    public static void model(PoseStack pose, MultiBufferSource buffers, int light, ResourceLocation id,
                             int color, int altColor, int auxColor)
    {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(id);

        // The block texture atlas, which is where a model's own texture is stitched. The models here
        // name a texture of their own rather than a block's, and it is bound into this atlas on the
        // strength of the model referring to it.
        //
        // Translucent-and-culled rather than cutout, which is not an interchangeable one: a model drawn with the wrong render type is a model the atlas has no
        // entry for, which is what draws as the black and magenta missing-texture squares.
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS));

        PoseStack.Pose last = pose.last();

        for (Direction direction : Direction.values())
        {
            quads(consumer, last, light, model.getQuads(null, direction, RANDOM), color, altColor, auxColor);
        }

        quads(consumer, last, light, model.getQuads(null, null, RANDOM), color, altColor, auxColor);
    }

    /**
     * Draws one model's worth of quads, tinting each by the index it names.
     *
     * <p>Minus one means the face asked for no tint, and the quad is drawn with white — which
     * multiplies to the texture's own colours. That is the case for a model whose author left the
     * tint indices off, and it should look exactly as it was authored.
     */
    private static void quads(VertexConsumer consumer, PoseStack.Pose pose, int light,
                              List<BakedQuad> quads, int color, int altColor, int auxColor)
    {
        for (BakedQuad quad : quads)
        {
            int tint = -1;

            if (quad.isTinted())
            {
                tint = switch (quad.getTintIndex())
                {
                    case 1 -> altColor;
                    case 2 -> auxColor;
                    default -> color;
                };
            }

            float r = tint == -1 ? 1.0F : (tint >> 16 & 0xFF) / 255.0F;
            float g = tint == -1 ? 1.0F : (tint >> 8 & 0xFF) / 255.0F;
            float b = tint == -1 ? 1.0F : (tint & 0xFF) / 255.0F;

            // The quad is handed over whole, pose and all, rather than being taken apart into
            // vertices. It already carries its own normal and its own winding, and rebuilding those
            // by hand would be undoing work the model loader has already done.
            consumer.putBulkData(pose, quad, r, g, b, light, OverlayTexture.NO_OVERLAY);
        }
    }

    /**
     * Reads the colour to tint a horn's first slot with, or white when the player has chosen the
     * texture's own.
     */
    public static int tintOrWhite(int packed, boolean useColour)
    {
        return useColour ? packed : 0xFFFFFF;
    }

    /** The model path a race's feature lives at, under this mod's own namespace. */
    public static ResourceLocation modelId(String path)
    {
        return new ResourceLocation("trinketsandbaubles", "race/" + path);
    }

    @Nullable
    public static ResourceLocation optionalModelId(String path)
    {
        return modelId(path);
    }
}