package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.capability.api.IBaseRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraftforge.common.capabilities.Capability;

import java.util.Map;

/**
 * Draws a player's race features on top of the player.
 *
 * <p>The one place this mod attaches to the player model. A render layer is handed the model that
 * was just drawn and the pose it was drawn with, and everything here happens inside that: the layer
 * is added to the player renderer once, at client setup, and from then on every player drawn is a
 * candidate.
 *
 * <p>It runs for every player, not only the one at the keyboard. A race is part of what a player is,
 * and a client that drew its own player's ears and nobody else's would show a multiplayer world in
 * which every other player is human.
 *
 * <p>Nothing is drawn for a player with no race, with one switched off, or with one that has no
 * features of its own. Nothing is drawn for an invisible player either — vanilla already hides them,
 * and a pair of ears hanging in the air would give the trick away.
 */
public class RaceRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    public RaceRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent)
    {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int light,
                       AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (player.isInvisible())
        {
            return;
        }

        for (Map.Entry<String, Capability<? extends IBaseRaceCapability>> entry
                : ModCapabilities.RACE_CAPABILITIES.entrySet())
        {
            AbstractRaceCapability race = activeRace(player, entry.getValue());

            if (race == null || !race.showTraits())
            {
                continue;
            }

            RaceFeatureRenderer renderer = RaceFeatureRenderers.forRace(entry.getKey());

            if (renderer == null)
            {
                // A race with nothing to draw. Titan and human are the plain cases: what sets them
                // apart is what they can do rather than what they look like.
                continue;
            }

            renderer.render(player, this.getParentModel(), pose, buffer, light, race,
                    limbSwing, limbSwingAmount, partialTick, ageInTicks);

            // One race is drawn. A player holds a capability for every race the mod has, and only
            // the one they are wearing is active; drawing the rest would stack every pair of ears in
            // the mod on one head.
            return;
        }
    }

    /**
     * The race this player is wearing, or null when the capability is absent or switched off.
     *
     * <p>Generic over the capability's own declared type so that each field keeps what it was
     * declared with. The caller only ever reads the shared half, which is what lets one walk serve
     * every race without the table being flattened to something wider than its contents.
     */
    private static <T extends IBaseRaceCapability> AbstractRaceCapability activeRace(
            AbstractClientPlayer player, Capability<T> capability)
    {
        IBaseRaceCapability present = player.getCapability(capability).resolve().orElse(null);

        if (present instanceof AbstractRaceCapability race && race.isActive())
        {
            return race;
        }

        return null;
    }
}