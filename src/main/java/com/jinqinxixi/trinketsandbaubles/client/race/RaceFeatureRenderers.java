package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.client.race.elf.RaceElfFeatureRenderer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Which drawing code belongs to which race.
 *
 * <p>A table rather than a lookup in each renderer, because the mapping is the whole of what ties a
 * race id to its appearance and it is worth being able to read off in one place which races have
 * features and which do not.
 *
 * <p>Races that draw nothing are absent on purpose. Titan and human are set apart by what they can
 * do rather than by what they look like, and a row for them holding a renderer that draws nothing
 * would say the opposite of the truth.
 */
public final class RaceFeatureRenderers
{
    private static final Map<String, RaceFeatureRenderer> BY_RACE = new HashMap<>();

    static
    {
        BY_RACE.put("elves", new RaceElfFeatureRenderer());
    }

    private RaceFeatureRenderers()
    {
    }

    /** The renderer for a race, or null when it has no features to draw. */
    @Nullable
    public static RaceFeatureRenderer forRace(String raceId)
    {
        return raceId == null ? null : BY_RACE.get(raceId);
    }
}