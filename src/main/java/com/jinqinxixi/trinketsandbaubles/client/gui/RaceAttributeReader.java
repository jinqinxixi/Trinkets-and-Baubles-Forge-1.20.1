package com.jinqinxixi.trinketsandbaubles.client.gui;

import com.jinqinxixi.trinketsandbaubles.config.RaceAttributesConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads a race's figures out of the configuration they are defined in.
 *
 * <p>The values live as one field per attribute on a per-race configuration object, and there is no
 * list of them anywhere — the class is the list. Rather than write out sixteen names for each of
 * seven races, and have to remember to add a name every time the configuration gains a field, the
 * fields are read as they are.
 *
 * <p>Reflection, and it is worth saying why that is acceptable here. This is a screen drawn on the
 * client for a player looking at their own race; it runs when the screen is opened rather than per
 * frame, and it reads public fields of a class in this same mod. The alternative — a hand-written
 * table — is a second copy of the configuration's shape that goes wrong silently.
 *
 * <p>Only the attributes a race actually changes are returned. A field left at nought is a race
 * saying nothing about that attribute, and listing it would fill the panel with rows of zero.
 */
final class RaceAttributeReader
{
    private RaceAttributeReader()
    {
    }

    /**
     * The race's configured values, keyed by the field's own name.
     *
     * <p>In the order the fields are declared, which is the order the configuration file lists them,
     * so that the panel reads the same way as the file a player may have edited.
     *
     * @param raceId the mod's own identifier for the race
     * @return the values that are not nought, or an empty map for a race with no configuration
     */
    static Map<String, Double> read(String raceId)
    {
        Map<String, Double> values = new LinkedHashMap<>();

        Object config = configFor(raceId);

        if (config == null)
        {
            return values;
        }

        for (Field field : config.getClass().getDeclaredFields())
        {
            if (!ForgeConfigSpec.DoubleValue.class.isAssignableFrom(field.getType()))
            {
                // The per-race extras are mixed in with the shared attributes and are all doubles of
                // the same type, so nothing needs excluding here — anything that is not one is not a
                // figure and is skipped.
                continue;
            }

            try
            {
                field.setAccessible(true);

                ForgeConfigSpec.DoubleValue value = (ForgeConfigSpec.DoubleValue) field.get(config);

                if (value == null)
                {
                    continue;
                }

                double amount = value.get();

                if (amount != 0.0D)
                {
                    values.put(field.getName(), amount);
                }
            }
            catch (IllegalAccessException e)
            {
                // A field that cannot be read is left out rather than failing the screen. The panel is
                // a description of a race, and a missing row is better than a screen that will not
                // open.
            }
        }

        return values;
    }

    /**
     * The configuration object for a race.
     *
     * <p>The mod's identifiers differ from the configuration's field names in case and in a few
     * spellings, so this maps one to the other rather than deriving a name.
     */
    private static Object configFor(String raceId)
    {
        if (raceId == null)
        {
            return null;
        }

        return switch (raceId.toLowerCase())
        {
            case "dragon" -> RaceAttributesConfig.DRAGON;
            case "dwarves" -> RaceAttributesConfig.DWARVES;
            case "elves" -> RaceAttributesConfig.ELVES;
            case "faeles" -> RaceAttributesConfig.FAELES;
            case "fairy" -> RaceAttributesConfig.FAIRY;
            case "goblins" -> RaceAttributesConfig.GOBLINS;
            case "titan" -> RaceAttributesConfig.TITAN;
            default -> null;
        };
    }
}