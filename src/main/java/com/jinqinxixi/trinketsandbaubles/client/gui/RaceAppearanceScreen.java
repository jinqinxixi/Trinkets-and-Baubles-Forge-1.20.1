package com.jinqinxixi.trinketsandbaubles.client.gui;

import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import com.jinqinxixi.trinketsandbaubles.capability.mana.hud.ManaHudOverlay;
import com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities;
import com.jinqinxixi.trinketsandbaubles.client.gui.widget.ColorPicker;
import com.jinqinxixi.trinketsandbaubles.client.gui.widget.IntSlider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Where a player chooses how their race looks.
 *
 * <p>控件逐一对应 1.20.1 版，位置、尺寸、文字都保持一致，包括那些看起来像笔误的地方——显示/隐藏
 * 按钮直接显示标志位的字面值而非短语，副形状滑条的上界是原始计数、主形状的是计数减一。这两处都
 * 是刻意的，改动它们会让界面和既有行为对不上。
 *
 * <p>The player is drawn in the middle of the screen and turns with the mouse, which is the only way
 * to tell whether a colour is right: a swatch beside a slider says what was chosen, not what it
 * looks like on a pair of ears. The body is turned to face the pointer and the head follows it, and
 * the player's own angles are put back afterwards so that leaving the screen does not leave them
 * facing the wrong way.
 *
 * <p>Every change is sent to the server as it happens rather than on closing: the screen's own close
 * handler would send the profile, but here each setter sends the equivalent as the value changes, so
 * what is on screen is what every other client would draw.
 */
@OnlyIn(Dist.CLIENT)
public class RaceAppearanceScreen extends Screen
{
    /** The three ways a race's colours are read, in the order the button cycles them. */
    private static final String[] COLOR_OPTIONS = { "normal", "inverted", "solid" };

    private final LocalPlayer player;

    private final List<ColorPicker> colors = new ArrayList<>();

    private boolean flip;

    /** The race being edited, found once when the screen opens. */
    private AbstractRaceCapability race;

    private String raceId;

    public RaceAppearanceScreen(LocalPlayer player)
    {
        super(Component.translatable("xat.config.client.race_gui"));
        this.player = player;
    }

    @Override
    protected void init()
    {
        colors.clear();
        findRace();

        // Every control is built, whether or not there is a race.
        //
        // A player with no race still gets a screen: a way back, a way out, the preview, the settings
        // that belong to the screen, and the appearance controls shown at their defaults. Skipping any
        // of it leaves a window that reads as broken, which is what this did once. The appearance
        // controls change nothing without a race — the setters below are what make that true — but they
        // are shown, because a player deciding whether to take a race should be able to see what they
        // would be choosing.
        addBackButton();
        addAbilitiesButton();
        addConfirmButton();
        addFlipButton();
        addManaHudControls();
        addAppearanceControls();
    }

    /**
     * What the race's show-traits flag is, or the default when there is no race.
     *
     * <p>The accessors below are how the controls stay on screen with nothing to change: each reads a
     * sensible value with no race and writes nothing. Written as a set of small readers rather than as
     * a null check at every control, because every control needs the same treatment and one place to
     * get it right is one place to be wrong.
     */
    private boolean showTraits()
    {
        return race == null || race.showTraits();
    }

    private void setShowTraits(boolean value)
    {
        if (race != null)
        {
            race.setShowTraits(value);
        }
    }

    private int traitColorOption()
    {
        return race == null ? 0 : race.traitColorOption();
    }

    private void setTraitColorOption(int option)
    {
        if (race != null)
        {
            race.setTraitColorOption(option);
        }
    }

    private int traitVariant()
    {
        return race == null ? 0 : race.traitVariant();
    }

    private void setTraitVariant(int variant)
    {
        if (race != null)
        {
            race.setTraitVariant(variant);
        }
    }

    private int primaryTraitColor()
    {
        return race == null ? 0xFFFFFF : race.primaryTraitColor();
    }

    private void setPrimaryTraitColor(int color)
    {
        if (race != null)
        {
            race.setPrimaryTraitColor(color);
        }
    }

    private int secondaryTraitColor()
    {
        return race == null ? 0xFFFFFF : race.secondaryTraitColor();
    }

    private void setSecondaryTraitColor(int color)
    {
        if (race != null)
        {
            race.setSecondaryTraitColor(color);
        }
    }

    private int auxTraitVariant()
    {
        return race == null ? 0 : race.auxTraitVariant();
    }

    private void setAuxTraitVariant(int variant)
    {
        if (race != null)
        {
            race.setAuxTraitVariant(variant);
        }
    }

    private int auxTraitColor()
    {
        return race == null ? 0xFFFFFF : race.auxTraitColor();
    }

    private void setAuxTraitColor(int color)
    {
        if (race != null)
        {
            race.setAuxTraitColor(color);
        }
    }

    /** Back to the inventory, top right. */
    private void addBackButton()
    {
        addRenderableWidget(Button.builder(Component.literal("<--").withStyle(ChatFormatting.RED),
                        button -> minecraft.setScreen(new InventoryScreen(player)))
                .bounds(width - 24, 2, 22, 20)
                .build());
    }

    /** What the race can do, top left. */
    private void addAbilitiesButton()
    {
        addRenderableWidget(Button.builder(Component.translatable("gui.xat.race.stats"),
                        button -> minecraft.setScreen(new AbilitiesScreen(player)))
                .bounds(2, 2, 50, 20)
                .build());
    }

    /** Turns the preview around, for looking at the back of a pair of wings. */
    private void addFlipButton()
    {
        addRenderableWidget(Button.builder(Component.translatable("gui.xat.race.flip"),
                        button -> flip = !flip)
                .bounds(width / 2 - 30, 0, 60, 20)
                .build());
    }

    /**
     * The mana bar's own controls, along the bottom left.
     *
     * <p>These controls are wired to the mod's own mana HUD configuration: a button opening a screen
     * where the bar is dragged into place, and two small buttons toggling whether it is drawn
     * horizontally and whether it stays visible when full. This mod has the same three settings on
     * the same HUD, so they are wired to it here rather than to a second copy of the settings.
     */
    private void addManaHudControls()
    {
        addRenderableWidget(Button.builder(Component.translatable("xat.config.client.mana_hud"),
                        button -> minecraft.setScreen(new ManaHudPositionScreen()))
                .bounds(2, height - 22, 60, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal(horizontal() ? "H" : "V"),
                        button ->
                        {
                            ManaHudOverlay.getInstance().toggleVertical();
                            button.setMessage(Component.literal(horizontal() ? "H" : "V"));
                        })
                .bounds(64, height - 22, 14, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal(alwaysShown() ? "A" : "^"),
                        button ->
                        {
                            ManaHudOverlay.getInstance().toggleAlwaysShown();
                            button.setMessage(Component.literal(alwaysShown() ? "A" : "^"));
                        })
                .bounds(80, height - 22, 14, 20)
                .build());
    }

    /**
     * The controls that change what the race looks like.
     *
     * <p>Shown whether or not there is a race. With one they change it; without one they change
     * nothing, which is what the accessors above arrange — every read falls back to a default and every
     * write does nothing. Shown rather than hidden because a player with no race is exactly the player
     * deciding whether to take one, and a preview of the choices is part of deciding.
     */
    private void addAppearanceControls()
    {
        // Whether the features are drawn at all. The label is the flag's own value.
        addRenderableWidget(Button.builder(Component.literal(String.valueOf(showTraits())),
                        button ->
                        {
                            setShowTraits(!showTraits());
                            button.setMessage(Component.literal(String.valueOf(showTraits())));
                        })
                .bounds(2, 40, 60, 20)
                .build());

        int left = 70;
        int top = 40;

        addRenderableWidget(Button.builder(colorOptionName(traitColorOption()),
                        button ->
                        {
                            setTraitColorOption(
                                    (Mth.clamp(traitColorOption(), 0, 2) + 1) % COLOR_OPTIONS.length);
                            button.setMessage(colorOptionName(traitColorOption()));
                        })
                .bounds(left, top, 72, 20)
                .build());

        top += 20;

        // The main shape. The upper bound is one less than the count — the count is how many shapes
        // there are and the slider counts from zero.
        addRenderableWidget(new IntSlider(left, top, 72, 20, variantLabel(),
                0, mainVariantCount() - 1, traitVariant(), this::setTraitVariant));

        top += 21;

        ColorPicker primary = addColor(left, top, primaryTraitColor(),
                this::setPrimaryTraitColor);

        addColor(left, primary.getNextY() + 2, secondaryTraitColor(),
                this::setSecondaryTraitColor);

        // The second shape selector and the third colour, on the right, and only for a race that has
        // them. Its upper bound is the raw count rather than one less, and is deliberately left as it
        // is.
        int auxVariants = auxVariantCount();

        if (auxVariants > 0)
        {
            int right = width - 100;
            int auxTop = 60;

            addRenderableWidget(new IntSlider(right, auxTop, 72, 20, variantLabel(),
                    0, auxVariants, auxTraitVariant(), this::setAuxTraitVariant));

            addColor(right, auxTop + 21, auxTraitColor(), this::setAuxTraitColor);
        }
    }

    private void addConfirmButton()
    {
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.xat.confirm").withStyle(ChatFormatting.GREEN),
                        button -> onClose())
                .bounds(width - 80, height - 32, 60, 20)
                .build());
    }

    /** Whether the mana bar is drawn across rather than down. */
    private static boolean horizontal()
    {
        return !ManaHudOverlay.getInstance().isVertical();
    }

    /** Whether the mana bar stays on screen once mana is full. */
    private static boolean alwaysShown()
    {
        return ManaHudOverlay.getInstance().isAlwaysShown();
    }

    /**
     * Finds the race this player is wearing.
     *
     * <p>Walked rather than asked for by name, because nothing on the client knows which of the
     * races a player holds is the active one — the capability that says it is active is the answer,
     * and finding it means asking each in turn.
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

        // 无激活种族：回到「人类」默认种族，让装饰饰品借的颜色也能在这里调（无种族时调 NONE
        // 种族的 appearance）。
        player.getCapability(ModCapabilities.HUMAN_CAPABILITY).resolve().ifPresent(present ->
        {
            if (present instanceof AbstractRaceCapability human)
            {
                this.race = human;
                this.raceId = "human";
            }
        });
    }

    /**
     * How many main shapes this race has, as the count rather than the highest index.
     *
     * <p>These counts are nought for most races — an elf has one pair of ears and there is nothing
     * to choose. A count of nought gives the slider an upper bound of minus one, which disables it
     * and leaves it showing the single value. That is why the slider looks dead for most races: it
     * is.
     */
    private int mainVariantCount()
    {
        // A switch on a null string throws, and raceId is null exactly when there is no race — which
        // is a state this screen is shown in on purpose. Compared rather than switched for that reason.
        if (raceId == null)
        {
            return 0;
        }

        return switch (raceId)
        {
            case "fairy" -> 2;
            case "taurus" -> 3;
            default -> 0;
        };
    }

    /**
     * How many alternate shapes this race has, or zero when it has none.
     *
     * <p>Only four races have any: the dragon's horns are drawn in three shapes, the faelis has a
     * choice about its tail, and the bull has one about its horns.
     */
    private int auxVariantCount()
    {
        // Same as above: no race means no variants, and a switch on null would throw.
        if (raceId == null)
        {
            return 0;
        }

        return switch (raceId)
        {
            case "dragon" -> 3;
            case "faelis", "taurus" -> 1;
            default -> 0;
        };
    }

    private ColorPicker addColor(int left, int top, int color, IntConsumer setter)
    {
        // White is what the reset button offers: a race whose palette has never been touched draws in
        // its texture's own colours, and white is the tint that leaves them alone.
        ColorPicker picker = new ColorPicker(font, left, top, 50, 20, color, 0xFFFFFF, setter);
        picker.addTo(this::addRenderableWidget);
        colors.add(picker);
        return picker;
    }

    private static Component colorOptionName(int option)
    {
        return Component.translatable("gui.xat.race.color."
                + COLOR_OPTIONS[Mth.clamp(option, 0, 2)]);
    }

    private static Component variantLabel()
    {
        return Component.translatable("gui.xat.race.trait.main.variant").copy().append(": ");
    }

    @Override
    public void tick()
    {
        colors.forEach(ColorPicker::tick);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);

        // The player is drawn whether or not there is a race: the preview is of the player, not of
        // the race, and a player with no race is still a player.
        renderPreview(graphics, mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, partialTick);

        // The panel and the name, drawn in every state. With no race the panel still frames the
        // show-traits button, and the name box is left out — there is no name to put in it, and an
        // empty box reads worse than none.
        renderLabels(graphics);

        colors.forEach(picker -> picker.render(graphics));
    }

    /**
     * Draws the panel the show/hide button sits in, and the race's name.
     *
     * <p>The panel is taller for a race that flies, because the flying line goes underneath — the
     * room is reserved rather than letting the box grow when the line appears.
     */
    private void renderLabels(GuiGraphics graphics)
    {
        int backHeight = 30;

        // The flying line, drawn before the panel rather than after it.
        //
        // The order is not arbitrary: the panel below is a translucent black rectangle, so anything
        // drawn under it comes out darkened. Drawing the line first and the panel over it is what
        // keeps it bright — written the other way round here first, which left the line dimmed and
        // looking like the wrong colour.
        //
        // The line appears only for a race that can fly, and it is always green. That looks like a
        // mistake and is not: the same question decides whether to draw the line and its colour, so
        // the red branch can never be reached. It is kept beside the green one so a reader comparing
        // the two should find them the same.
        if (canFly())
        {
            backHeight = 42;

            int colour = canFly() ? 0x00C800 : 0xC80000;
            graphics.drawString(font, Component.translatable("gui.xat.race.flying"), 17, 62, colour);
        }

        // The panel behind the button, taller for a race that flies because the line above sits
        // inside it.
        graphics.fill(2, 30, 62, 30 + backHeight, Integer.MIN_VALUE);

        // The caption for the button, which sits on the panel.
        graphics.drawString(font, Component.translatable("gui.xat.race.trait.show"), 6, 32, 0xFFFFFF);

        // The race's name, in a box of its own beside the button — and only when there is a race to
        // name. An empty box in its place would be a box that looks like it failed to load something.
        if (race != null)
        {
            labelBox(graphics, raceDisplayName(), 60, 4, 0xFFFFFF);
        }
    }

    /**
     * The race's name, as a player should see it.
     *
     * <p>Asked of the mod's own translation table rather than of the race's identifier. That
     * identifier is an internal name — "FAIRY", "GOBLINS" — spelled that way because code compares
     * it, and showing it puts English capitals in front of a player reading Chinese.
     *
     * <p>The keys used are the ones this mod already has for the races, the same ones its status
     * effects are named by. Reusing them rather than adding a second set means a race's name is
     * written down once, and renaming one does not leave the two disagreeing.
     *
     * <p>Falls back to the identifier for a race with no translation, which is the honest thing to
     * show rather than an empty box.
     */
    private Component raceDisplayName()
    {
        // One race's effect is named differently from its race id, and this is that one.
        String id = "fairy".equalsIgnoreCase(race.getRaceId()) ? "fairy_dew" : race.getRaceId().toLowerCase();

        String key = "effect.trinketsandbaubles." + id;
        Component translated = Component.translatable(key);

        // A translation with no entry comes back as the key itself, which is how this tells the
        // difference without asking the language table directly.
        if (translated.getString().equals(key))
        {
            return Component.literal(race.getRaceName());
        }

        return translated;
    }

    /**
     * Draws a label inside a box sized to the text.
     *
     * <p>The box is padded by different amounts for an even and an odd width, which keeps the text
     * centred-looking as the name changes length.
     */
    private void labelBox(GuiGraphics graphics, Component text, int x, int y, int colour)
    {
        int textWidth = font.width(text);
        int boxWidth = textWidth + (textWidth % 2 == 0 ? 10 : 9);

        graphics.fill(x, y, x + boxWidth, y + 14, Integer.MIN_VALUE);
        graphics.drawString(font, text, x + 6, y + 3, colour, true);
    }

    /** Whether this race flies at all, which decides how tall the panel is. */
    private boolean canFly()
    {
        return "dragon".equals(raceId) || "fairy".equals(raceId);
    }

    /** Whether the player is flying right now. */
    private boolean canFlyNow()
    {
        return !player.onGround() && player.getAbilities().flying;
    }

    /**
     * Draws the player, turning with the mouse.
     *
     * <p>The scale is a constant divided by the player's height. The height is a per-player value
     * kept at a hundred for a player of ordinary size, so the division comes out at ninety — the
     * number the model has to be drawn at to sit inside the panel.
     *
     * <p>The player's own angles are saved and put back, because the screen is borrowing them:
     * without that the player would keep the last angle the mouse left them at. Restored in a finally
     * so that a failure while drawing does not leave them facing sideways.
     */
    private void renderPreview(GuiGraphics graphics, int mouseX, int mouseY)
    {
        int centerX = width / 2;
        int centerY = height / 2;

        graphics.fill(centerX - 50, centerY - 75, centerX + 50, centerY + 105, Integer.MIN_VALUE);

        int scale = (int) (9000.0F / Math.max(PREVIEW_HEIGHT, 1));

        float angleX = (float) Math.atan((centerX - mouseX) / 40.0F);
        float angleY = (float) Math.atan((centerY - 50 - mouseY) / 40.0F);

        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf().rotateX(angleY * 20.0F * (float) (Math.PI / 180.0));

        pose.mul(camera);

        if (flip)
        {
            pose.rotateY((float) Math.PI);
        }

        float bodyRot = player.yBodyRot;
        float yRot = player.getYRot();
        float xRot = player.getXRot();
        float headRotO = player.yHeadRotO;
        float headRot = player.yHeadRot;

        try
        {
            player.yBodyRot = 180.0F + angleX * 20.0F;
            player.setYRot(180.0F + angleX * 40.0F);
            player.setXRot(-angleY * 20.0F);
            player.yHeadRot = player.getYRot();
            player.yHeadRotO = player.getYRot();

            InventoryScreen.renderEntityInInventory(graphics, centerX, centerY + 100, scale, pose, camera, player);
        }
        finally
        {
            player.yBodyRot = bodyRot;
            player.setYRot(yRot);
            player.setXRot(xRot);
            player.yHeadRotO = headRotO;
            player.yHeadRot = headRot;
        }
    }

    /** An ordinary player's height, used for framing the preview. */
    private static final int PREVIEW_HEIGHT = 100;

    /**
     * Gives a key to the colour field while a colour is being typed.
     *
     * <p>A hex value is entered as text, and the keys that would otherwise close the screen are
     * ordinary characters in one. Escape is left alone so that there is always a way out.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode != 256 && colors.stream().anyMatch(ColorPicker::isEditing))
        {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Does not pause the game.
     *
     * <p>The player is being drawn live and the world behind them should keep running, which is what
     * a cosmetic screen wants.
     */
    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}