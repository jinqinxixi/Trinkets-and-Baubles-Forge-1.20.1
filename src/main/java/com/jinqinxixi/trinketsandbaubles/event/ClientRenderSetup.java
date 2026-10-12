package com.jinqinxixi.trinketsandbaubles.event;

import com.jinqinxixi.trinketsandbaubles.client.race.RaceRenderLayer;
import com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Where the race layer is attached to the player renderers.
 *
 * <p>On the mod bus, and that is the whole reason this is a class of its own rather than one more
 * method beside the GUI overlay. {@code AddLayers} is a setup event: it is posted once while the
 * game starts, on the bus that carries the mod's own lifecycle events, and a listener registered on
 * the other bus never hears it. The two buses are separate registration targets and a class
 * annotated for one is not listening to the other — so an annotation that names only the side, and
 * not the bus, silently listens to the wrong one. The layer is then never added, and the failure
 * looks exactly like drawing code that does not work.
 *
 * <p>Nothing here is conditional on anything. The event arrives once, the renderers exist by then,
 * and the layer is appended to both of them.
 */
@Mod.EventBusSubscriber(modid = TrinketsandBaublesMod.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRenderSetup
{
    /**
     * Adds the race layer to every player renderer.
     *
     * <p>Both skins, because the wide and the slim player are separate renderers with separate model
     * lists and a player whose skin is slim is drawn by the second. Adding the layer to only the
     * first would leave slim players — and every player whose skin asks for the slim arms — without
     * features, which reads as the race not working for some players and not others.
     *
     * <p>Vanilla keeps its own layer list per renderer and this appends to it, so nothing vanilla
     * draws is displaced.
     */
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event)
    {
        int added = 0;

        for (String skin : event.getSkins())
        {
            PlayerRenderer renderer = event.getSkin(skin);

            if (renderer != null)
            {
                addRaceLayer(renderer);
                added++;
            }
        }

        // Said out loud because this event is silent when it is never received. A listener on the
        // wrong bus produces no error and no warning — the layer is simply absent, and the search
        // for why goes into drawing code that was working all along. One line at startup turns that
        // into an answer.
        TrinketsandBaublesMod.LOGGER.info("Race features attached to {} player renderer(s)", added);
    }

    /**
     * Appends the layer, saying what it is being appended to.
     *
     * <p>The cast is what the event's own accessor knows and the compiler does not: a player renderer
     * is a living-entity renderer of a player with a player model, which is exactly what the layer
     * declares itself for. It is done once here rather than at each use.
     */
    @SuppressWarnings("unchecked")
    private static void addRaceLayer(PlayerRenderer renderer)
    {
        LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> typed =
                (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>)
                        (LivingEntityRenderer<?, ?>) renderer;

        typed.addLayer(new RaceRenderLayer(typed));
    }
}