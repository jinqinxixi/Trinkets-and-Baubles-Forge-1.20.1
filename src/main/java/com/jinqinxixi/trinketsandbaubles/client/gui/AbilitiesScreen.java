package com.jinqinxixi.trinketsandbaubles.client.gui;

import com.jinqinxixi.trinketsandbaubles.capability.attribute.AttributeRegistry;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.client.gui.widget.LabelList;
import com.jinqinxixi.trinketsandbaubles.client.gui.widget.TextPanel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * What the player is wearing, and what each piece does.
 *
 * <p>Three parts. The list on the left is every accessory with something to say, named the way the
 * item names itself — a player put the ring on and the ring is what they recognise. Picking one opens
 * its description in the middle. The panel on the right is the race's figures, read from the same
 * configuration the ring's own tooltip reads.
 *
 * <p>Built from the items rather than from a table kept here. Every accessory already describes itself
 * — its tooltip is the list of things it grants — so this reads those lines instead of a second copy
 * that would be free to disagree with them.
 *
 * <p>Arriving from the appearance screen, and going back to it. Both are about a race and a player
 * moves between them while deciding what to wear.
 */
@OnlyIn(Dist.CLIENT)
public class AbilitiesScreen extends Screen
{
    /**
     * How much wider the list is than its longest name.
     *
     * <p>Ten, raised by four. The names are English and this mod's are Chinese, whose characters are
     * wider; the row's text is clipped a few pixels short of the row, and with the margin of ten the
     * clipping took the last character off the longest Chinese names. Four pixels returns it without
     * the highlight visibly widening.
     */
    private static final int LIST_TEXT_MARGIN = 14;
    private static final int MIN_LIST_WIDTH = 40;
    private static final int MAX_LIST_WIDTH = 150;

    /** The height of one row of the list. */
    private static final int ROW_HEIGHT = 20;

    private final LocalPlayer player;

    private AbstractRaceCapability race;

    private String raceId;

    private int listWidth;

    /** The description of whatever is selected, rebuilt each time the selection changes. */
    @Nullable
    private TextPanel description;

    public AbilitiesScreen(LocalPlayer player)
    {
        super(Component.translatable("gui.xat.race.stats"));
        this.player = player;
    }

    @Override
    protected void init()
    {
        findRace();

        addExitButtons();

        List<EquippedAbilityReader.Ability> abilities = EquippedAbilityReader.read(player);

        // The arithmetic: the longest name plus ten, never below forty nor above a
        // hundred and fifty. The highlight is drawn two pixels narrower than the list, which leaves it
        // a pixel or two past the longest name — a frame that fits the name rather than a bar that
        // runs on past it.
        listWidth = MIN_LIST_WIDTH;

        for (EquippedAbilityReader.Ability ability : abilities)
        {
            listWidth = Math.max(listWidth, font.width(ability.name()) + LIST_TEXT_MARGIN);
        }

        listWidth = Math.min(listWidth, MAX_LIST_WIDTH);

        if (!abilities.isEmpty())
        {
            addRenderableWidget(new LabelList<EquippedAbilityReader.Ability>(
                    minecraft, 10, 32, height - 84, listWidth,
                    ROW_HEIGHT, abilities, EquippedAbilityReader.Ability::name, this::select));
        }

        // The figures, against the right edge, at: left at `width - listWidth - 50`, width
        // `listWidth + 40`.
        addRenderableWidget(new TextPanel(minecraft, listWidth + 40, 32,
                height - 14, width - listWidth - 50, attributeLines()));
    }

    /**
     * Opens one ability's description, over the left of the figures.
     *
     * <p>Headed by the accessory it comes from, which is what a player recognises — they put the ring
     * on, and the ring is the thing they chose. An internal identifier would say where the ability is
     * registered and nothing to a player reading it.
     *
     * <p>Replaces the previous description rather than adding beside it: two on screen at once would be
     * two panels nobody asked for, and the one being read has been replaced by the player's own choice.
     *
     * <p>Its width is fixed, and it is narrower than the screen on purpose: a paragraph
     * that runs the full width of a wide window is a line of text a reader loses their place in.
     */
    private void select(EquippedAbilityReader.Ability ability)
    {
        if (description != null)
        {
            removeWidget(description);
        }

        List<Component> lines = new ArrayList<>();

        // The accessory, in the colour the game gives a rare name, so that the heading and the ring a
        // player is holding are visibly the same thing.
        lines.add(ability.item().copy().withStyle(ChatFormatting.DARK_AQUA));
        lines.add(Component.empty());

        // The ability's own name, in the colour the mod's tooltips use for a heading.
        lines.add(ability.name().copy().withStyle(ChatFormatting.GOLD));

        lines.addAll(ability.lines());

        // The layout: left at `listWidth + 20`, width `width - 2*listWidth - 80`.
        int left = listWidth + 20;
        int panelWidth = width - listWidth - 50 - listWidth - 30;

        description = addRenderableWidget(new TextPanel(minecraft, Math.max(panelWidth, 80), 32,
                height - 14, left, lines));
    }

    /**
     * Finds the race this player is wearing.
     *
     * <p>Walked rather than asked for by name, because nothing on the client knows which of the races
     * a player holds is the active one — the capability that says it is active is the answer.
     */
    private void findRace()
    {
        for (var entry : ModCapabilities.RACE_CAPABILITIES.entrySet())
        {
            var present = player.getCapability(entry.getValue()).resolve().orElse(null);

            if (present instanceof AbstractRaceCapability candidate && candidate.isActive())
            {
                this.race = candidate;
                this.raceId = entry.getKey();
                return;
            }
        }
    }

    /**
     * The figures the race changes, as lines.
     *
     * <p>Read and formatted the way the ring's own shift-tooltip reads them, because these are the
     * same figures shown in two places: the value comes from the race's configuration, whether it is
     * a multiplier comes from the attribute registry, and the wording comes from the registry's own
     * translation key. A second way of deciding any of those three would be a second answer to a
     * question that already has one.
     *
     * <p>Empty when there is no race, which leaves the panel blank rather than showing figures that
     * belong to nothing.
     */
    private List<Component> attributeLines()
    {
        List<Component> lines = new ArrayList<>();

        if (raceId == null)
        {
            return lines;
        }

        var attributes = RaceAttributeReader.read(raceId);

        for (var entry : attributes.entrySet())
        {
            String field = entry.getKey();
            double value = entry.getValue();

            if (value == 0.0D)
            {
                continue;
            }

            AttributeRegistry.AttributeEntry attribute = AttributeRegistry.get(registryKey(field));

            if (attribute == null)
            {
                // A configuration field with no attribute behind it — the per-race extras that are
                // not attribute modifiers at all, such as a mana cost. Left out rather than shown
                // with a name that does not exist.
                continue;
            }

            String name = Component.translatable(attribute.getTranslationKey()).getString();

            String display = attribute.isPercentage()
                    ? String.format("%s %s%.1f%%", name, value > 0 ? "+" : "", value * 100.0D)
                    : String.format("%s %s%.2f", name, value > 0 ? "+" : "", value);

            lines.add(Component.literal(display)
                    .withStyle(value > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
        }

        return lines;
    }

    /**
     * The attribute registry's name for a configuration field.
     *
     * <p>The two differ only by the race's own prefix: a race's configuration carries the shared
     * attributes unchanged and its own extras with the race's name in front, and the registry holds
     * them without it. Stripping the prefix is what lets one lookup serve both.
     */
    private String registryKey(String field)
    {
        String upper = field.toUpperCase();

        for (String prefix : List.of("DRAGON_", "DWARVES_", "ELVES_", "FAELES_", "FAIRY_", "GOBLINS_", "TITAN_"))
        {
            if (upper.startsWith(prefix))
            {
                return upper.substring(prefix.length());
            }
        }

        return upper;
    }

    private void addExitButtons()
    {
        addRenderableWidget(Button.builder(Component.literal("<--"),
                        button -> minecraft.setScreen(new RaceAppearanceScreen(player)))
                .bounds(2, 2, 50, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("X").withStyle(ChatFormatting.RED),
                        button -> minecraft.setScreen(new InventoryScreen(player)))
                .bounds(width - 16, 2, 14, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Does not pause the game.
     *
     * <p>The appearance screen behind this one does not, and a player moving between the two should
     * not have the world stop and start as they do.
     */
    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}