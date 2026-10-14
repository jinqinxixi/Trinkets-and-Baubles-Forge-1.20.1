package com.jinqinxixi.trinketsandbaubles.client.race;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * The pair of wings built from boxes rather than cut out of a texture.
 *
 * <p>A fairy has two sets of wings and both are shipped: flat quads that open and close, and this —
 * two real boxes that flap. Which one a player gets is their choice, through the shape selector on
 * the appearance screen, and the two are not variations on each other: one is a picture and one is a
 * shape, and they read differently from the side.
 *
 * <p>Built in code rather than loaded from a model file. Two boxes of no
 * thickness, laid out from the shoulder, with the layer's own texture size of thirty-two square.
 */
public final class FairyWingModel
{
    /** The layer this model is baked under, named so the loader can find it. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation("trinketsandbaubles", "fairy_wings"), "main");

    /** The baked parts, filled once at client setup and used from then on. */
    private static ModelPart wings;

    private FairyWingModel()
    {
    }

    /**
     * Describes the layer to the loader.
     *
     * <p>Two parts, a right wing and a left, each a flat box sixteen wide and eight tall. They are
     * placed a little apart and behind the shoulders, which is where they are drawn from.
     */
    public static LayerDefinition createLayer()
    {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, 0.0F, 0.0F, 16.0F, 8.0F),
                PartPose.offset(-2.0F, 1.0F, 2.0F));

        root.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, 0.0F, 0.0F, 16.0F, 8.0F),
                PartPose.offset(1.0F, 1.0F, 2.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    /**
     * Takes the baked parts once they exist.
     *
     * <p>A model cannot be built until the loader has run, so the layer is described first and handed
     * over afterwards. Until then {@link #wings()} answers nothing, and the renderer falls back to
     * the flat pair rather than drawing a wing that was never made.
     */
    public static void bake(ModelPart baked)
    {
        wings = baked;
    }

    /** The baked parts, or null before the loader has run. */
    public static ModelPart wings()
    {
        return wings;
    }

    /** The right wing, or null before the loader has run. */
    public static ModelPart right()
    {
        return wings == null ? null : wings.getChild("right_wing");
    }

    public static ModelPart left()
    {
        return wings == null ? null : wings.getChild("left_wing");
    }
}