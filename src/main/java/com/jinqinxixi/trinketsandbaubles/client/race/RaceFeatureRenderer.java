package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Draws one race's features.
 *
 * <p>One implementation per race that has any, named by the race id it serves. A race with nothing
 * to draw — titan, human — has no implementation, and the layer draws nothing for it rather than
 * having an implementation that draws nothing.
 *
 * <p>The appearance arrives as the capability that holds it, so an implementation reads the shape
 * and colour it needs from one place and does not have to be handed each of them in turn.
 */
public interface RaceFeatureRenderer
{
    /**
     * Draws this race's features on a player who is already being drawn.
     *
     * @param player       the player, whose body is already on the pose stack
     * @param parentModel  the model that was just drawn, for the head and body parts to hang from
     * @param pose         the stack, positioned at the player's feet when this is called
     * @param buffer       the buffer to draw into
     * @param light        packed light at the player
     * @param appearance   the race's shape and colour, as the player set them
     * @param ageInTicks   the player's age, for anything that animates
     */
    void render(AbstractClientPlayer player,
                PlayerModel<AbstractClientPlayer> parentModel,
                PoseStack pose,
                MultiBufferSource buffer,
                int light,
                AbstractRaceCapability appearance,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks);
}