package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.capability.api.IRaceAppearance;

/**
 * The colours a race's features are drawn in.
 *
 * <p>One place, because every feature reads the same three values the same way and a second copy of
 * the rule is a second chance to get it wrong. The rule is: the colour option swaps
 * which of the player's colours is used for which part, and decides nothing else.
 *
 * <p>Writing that rule out is what caught a mistake here. The option being zero had been read as
 * "leave the texture alone" and the colours forced to white, which made the Normal setting look
 * broken — the two colour pickers did nothing while it was selected. The intended behaviour is not
 * that: zero means the colours are used as they stand, and the pickers are live in every setting.
 *
 * <p>The third colour has no swap. It is used by the parts that are neither the main shape nor its
 * lining — a horn's ridge, a tail's tip — and the option has nothing to say about it.
 */
public record RaceColors(int primary, int secondary, int aux)
{
    /**
     * Reads the colours out of a race's appearance.
     *
     * @param appearance the race, which holds the three colours and the option that orders them
     */
    public static RaceColors of(IRaceAppearance appearance)
    {
        int option = appearance.traitColorOption();

        // Inverted swaps the pair; solid uses the first colour for both; normal uses them as they
        // stand. All three fall out of these two expressions.
        int primary = option == 1 ? appearance.secondaryTraitColor() : appearance.primaryTraitColor();
        int secondary = option > 0 ? appearance.primaryTraitColor() : appearance.secondaryTraitColor();

        return new RaceColors(primary, secondary, appearance.auxTraitColor());
    }

    /**
     * Whether the two colours came out the same.
     *
     * <p>Asked by the features that are drawn in one tone, which is the test that decides it: a pair
     * of colours that are equal means draw the plain band rather than the two-tone one, and it is
     * what makes the solid option take effect without any code knowing about it.
     */
    public boolean singleTone()
    {
        return primary == secondary;
    }
}