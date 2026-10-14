package com.jinqinxixi.trinketsandbaubles.client.race;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Which drawing code belongs to which race.
 *
 * <p>A table rather than a lookup in each renderer, because the mapping is the whole of what ties a
 * race id to its appearance and it is worth being able to read off in one place which races have
 * features and which do not.
 *
 * <p>Only the races this mod has a row for. Dwarves and titan are absent on purpose: what sets them
 * apart is what they can do rather than what they look like, and a row holding a renderer that draws
 * nothing would say the opposite of the truth.
 *
 * <p>The races that share a feature share its renderer and differ only in their numbers: two of them
 * wear the same pair of ears. A race with several features is one row holding a renderer that draws
 * all of them, so that a race appears here once however much it has.
 */
public final class RaceFeatureRenderers
{
    private static final Map<String, RaceFeatureRenderer> BY_RACE = new HashMap<>();

    static
    {
        // Ears: the same pair of quads, with the offsets, scale and splay that make each its own.
        BY_RACE.put("elves", new EarFeatureRenderer(EarFeatureRenderer.ELF));
        BY_RACE.put("goblins", new EarFeatureRenderer(EarFeatureRenderer.GOBLIN));

        // Ears, a tail and claws.
        BY_RACE.put("faeles", new FaelisFeatureRenderer());

        // Wings, which are drawn in sections that move.
        BY_RACE.put("fairy", new FairyFeatureRenderer());

        // Wings and the pair of horns.
        BY_RACE.put("dragon", new DragonFeatureRenderer());

        // The bull's horns and bell.
        BY_RACE.put("taurus", new TaurusFeatureRenderer());
    }

    private RaceFeatureRenderers()
    {
    }

    /** The renderer for a race, or null when it has no features to draw. */
    public static RaceFeatureRenderer forRace(String raceId)
    {
        return raceId == null ? null : BY_RACE.get(raceId);
    }

    /**
     * Every model this mod draws from, so that the loader can be told to bake them.
     *
     * <p>A model that is never registered is never baked, and asking for one that was not baked
     * returns the missing-model placeholder rather than throwing — which draws as a pair of black
     * and magenta squares and says nothing about why.
     */
    public static List<ResourceLocation> models()
    {
        return List.of(
                RaceModelRenderer.modelId("generic/horns"),
                RaceModelRenderer.modelId("generic/horns_inverted"),
                RaceModelRenderer.modelId("dragon/dragon_horns"),
                RaceModelRenderer.modelId("faelis/tail"),
                RaceModelRenderer.modelId("taurus/tuarian_horns"),
                RaceModelRenderer.modelId("taurus/tuarian_horns_f"),
                RaceModelRenderer.modelId("taurus/tuarian_cowbell"),
                RaceModelRenderer.modelId("succubus/succubus_horns")
        );
    }
}