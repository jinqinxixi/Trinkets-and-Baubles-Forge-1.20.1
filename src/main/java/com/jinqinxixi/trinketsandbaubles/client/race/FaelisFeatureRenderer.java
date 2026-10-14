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
 * A faelis: a cat's ears, a tail that follows, and claws.
 *
 * <p>The ears are the shared pair with this race's own numbers. The tail and the claws are this
 * race's alone.
 *
 * <p>The tail is the only part of any race that is animated by what the player is doing rather than
 * by a clock. It hangs off the back and swings against the direction of travel, lagging behind the
 * turn, and settles when the player stops — a cat's tail carried rather than dragged. The reference
 * builds that out of the player's movement this tick against their view direction, and the numbers
 * here are its own.
 *
 * <p>The sway is kept per player in a weak map, so that a player leaving takes theirs with them. It
 * is state with a history — the tilt is eased towards its target rather than set — and a shared one
 * would have every player's tail moving together.
 */
public final class FaelisFeatureRenderer implements RaceFeatureRenderer
{
    private static final ResourceLocation TAIL_MODEL = RaceModelRenderer.modelId("faelis/tail");

    private static final ResourceLocation CLAWS =
            new ResourceLocation("trinketsandbaubles", "textures/races/faelis/claws.png");

    /** The pose each player's tail is in, remembered between frames. */
    private static final Map<AbstractClientPlayer, Sway> SWAY = new WeakHashMap<>();

    private final EarFeatureRenderer ears = new EarFeatureRenderer(EarFeatureRenderer.FAELIS);

    /** 画哪些部分：耳朵 / 尾巴 / 爪子。种族本身渲染用全套；装饰外观按选择只画一部分。 */
    private final boolean drawEars;
    private final boolean drawTail;
    private final boolean drawClaws;

    /** 全套：耳朵 + 尾巴 + 爪子（猫灵种族本身的渲染）。 */
    public FaelisFeatureRenderer()
    {
        this(true, true, true);
    }

    /**
     * 部分渲染，供装饰外观用：照原版，「耳朵」外观 = 耳朵 + 爪子、无尾巴；「尾巴」外观 = 只有尾巴。
     */
    public FaelisFeatureRenderer(boolean ears, boolean tail, boolean claws)
    {
        this.drawEars = ears;
        this.drawTail = tail;
        this.drawClaws = claws;
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
        if (drawEars)
        {
            ears.render(player, parentModel, pose, buffer, light, appearance,
                    limbSwing, limbSwingAmount, partialTick, ageInTicks);
        }

        if (drawTail)
        {
            tail(player, parentModel, pose, buffer, light, appearance, partialTick);
        }

        if (drawClaws)
        {
            claws(player, parentModel, pose, buffer, light);
        }
    }

    /**
     * The tail, hung off the back and swung by the player's movement.
     *
     * <p>The swing is driven by how fast the player is moving sideways across their own view, not by
     * how fast they are moving: walking straight ahead barely moves it, and turning does. That is
     * what a tail does — it is carried behind and swings outward through a turn.
     */
    private void tail(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> parentModel,
                      PoseStack pose, MultiBufferSource buffer, int light,
                      IRaceAppearance appearance, float partialTick)
    {
        boolean slim = "slim".equals(player.getModelName());

        Sway sway = SWAY.computeIfAbsent(player, key -> new Sway());

        // How far the player has moved this tick, and how much of that was across their view.
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;
        Vec3 look = player.getViewVector(partialTick);
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double forward = dx * look.x + dz * look.z;

        sway.amplitude = Math.min(sway.amplitude + horizontal * (slim ? 1.5D : 1.35D),
                slim ? 1.0D : 0.9D) * (slim ? 0.95D : 0.94D);

        double followStrength = slim ? 6.0D : 4.5D;
        double targetTilt = -forward * followStrength * sway.amplitude;

        // Eased rather than set, and damped each frame, so the tail trails the movement instead of
        // snapping to it. Both numbers are the reference's.
        sway.tiltX = (sway.tiltX + (targetTilt - sway.tiltX) * 0.08D) * 0.9D;

        double maxTilt = slim ? 0.8D : 0.6D;
        double tailTilt = Math.max(-maxTilt, Math.min(maxTilt, sway.tiltX * sway.amplitude))
                * (slim ? -20.0D : -14.0D);

        double phase = (player.tickCount + partialTick) * (slim ? 0.35D : 0.33D);
        double angleZ = Math.sin(phase + sway.tiltX * 0.3D) * sway.amplitude * (slim ? 20.0D : 18.0D);

        boolean chest = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty();

        RaceColors colours = RaceColors.of(appearance);

        int primary = colours.primary();
        int secondary = colours.secondary();

        pose.pushPose();

        parentModel.body.translateAndRotate(pose);

        pose.mulPose(Axis.YP.rotationDegrees((float) angleZ));
        pose.mulPose(Axis.XP.rotationDegrees((float) tailTilt));
        pose.mulPose(Axis.XP.rotationDegrees(15.0F));
        pose.translate(0.0D, slim ? 1.15D : 1.27D, slim ? 0.3D : 0.34D);

        float scale = slim ? 0.7F : 0.82F;
        pose.scale(scale, scale, scale);
        pose.translate(0.0D, chest ? 0.12D : 0.1D, chest ? -0.04D : 0.04D);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
        pose.translate(-0.5F, -0.5F, 0.5F);
        pose.translate(0.0D, 0.2D, -1.0D);

        RaceModelRenderer.model(pose, buffer, light, TAIL_MODEL, primary, secondary, -1);

        pose.popPose();
    }

    /**
     * The claws, drawn on the back of each hand.
     *
     * <p>One quad per hand, hung off the arm so it follows a swing. The arm's own pose is applied
     * first, then the claw's scale brings the model's units down to the player's, and the offset
     * places it at the fingers.
     *
     * <p>The offset differs with the player's model because a slim arm is a different width, and a
     * claw placed for a wide arm would float beside a slim one.
     */
    private void claws(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> parentModel,
                       PoseStack pose, MultiBufferSource buffer, int light)
    {
        boolean slim = "slim".equals(player.getModelName());
        float offsetX = slim ? -12.4F : -18.6F;

        claw(parentModel.rightArm, pose, buffer, light, offsetX);
        claw(parentModel.leftArm, pose, buffer, light, -offsetX);
    }

    /**
     * One claw.
     *
     * <p>The claw is not tinted. It is a picture of a claw rather than a shape to be coloured — the
     * reference draws it white, which leaves the texture's own colours alone.
     */
    private void claw(net.minecraft.client.model.geom.ModelPart arm, PoseStack pose,
                      MultiBufferSource buffer, int light, float offsetX)
    {
        pose.pushPose();
        arm.translateAndRotate(pose);

        // The model's units, brought down and moved to the fingers: out along the arm, down past the
        // hand, and back towards the body.
        pose.scale(0.01F, 0.01F, 0.01F);
        pose.translate(offsetX, 61.0F, -21.0F);
        pose.mulPose(Axis.YP.rotationDegrees(-90.0F));

        RaceQuadRenderer.quad(pose, buffer, CLAWS, 0.0D, 0.0D, 0.0D,
                0.0F, 0.0F, 32, 32, 32.0D, 32.0D, 32.0F, 32.0F, 0xFFFFFF, light);

        pose.popPose();
    }

    /** How far a tail is currently swung, and how much movement is feeding it. */
    private static final class Sway
    {
        private double amplitude;
        private double tiltX;
    }
}