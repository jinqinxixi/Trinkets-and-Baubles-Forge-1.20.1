package com.jinqinxixi.trinketsandbaubles.capability.api;

/**
 * The appearance a race shows on the player wearing it.
 *
 * <p>Separate from {@link IBaseRaceCapability} because the two answer different questions and only
 * one of them is about the world. That interface is what a race does — attributes, movement, what
 * breaking a block costs — and it is read on the server. This is what a race looks like, it is read
 * while drawing a frame, and it is worth being able to say so.
 *
 * <p>Implemented by the same capability objects rather than by a second one, so that a race is one
 * thing with two faces rather than two things that have to be kept in step.
 *
 * <p>Every part is a value with a default, because a race that says nothing about its appearance
 * should still be drawn — the alternative is a race that exists in the world and cannot be seen,
 * which reads as the feature being broken rather than as it being unconfigured.
 */
public interface IRaceAppearance
{
    /**
     * Whether this race's features are drawn at all.
     *
     * <p>On by default, so that a race which never touches this still appears. Turning it off is
     * the player's own choice and hides the features without taking the race away.
     */
    boolean showTraits();

    void setShowTraits(boolean show);

    /**
     * Which of the race's shapes to draw, where a race ships more than one.
     *
     * <p>An index into whatever the race's own renderer knows about, not an item or a name. A race
     * with a single shape ignores it, and one with several is free to use any range it likes as
     * long as zero means the first.
     */
    int traitVariant();

    void setTraitVariant(int variant);

    /**
     * The colour the features are drawn in, as packed ARGB.
     *
     * <p>Two of them, because several races are drawn in two tones — an inner ear and an outer one,
     * a wing membrane and its leading edge — and the two are chosen independently. A race drawn in
     * one colour sets both and ignores the pair.
     */
    int primaryTraitColor();

    void setPrimaryTraitColor(int colour);

    int secondaryTraitColor();

    void setSecondaryTraitColor(int colour);

    /**
     * How {@link #primaryTraitColor()} and {@link #secondaryTraitColor()} are read.
     *
     * <p>Zero means the features take their colour from the player's own skin — the natural look,
     * and what a player who has configured nothing gets. A value above zero means the colours above
     * were chosen deliberately and are used as they stand.
     *
     * <p>Kept apart from the colours themselves rather than folded into them, because "follow the
     * skin" and "use this exact colour" are the same value doing different things, and a player
     * switching back to the skin should not lose the colour they had picked.
     */
    int traitColorOption();

    void setTraitColorOption(int option);
}