package com.jinqinxixi.trinketsandbaubles.client.gui;

import com.jinqinxixi.trinketsandbaubles.client.keybind.KeyBindings;
import com.jinqinxixi.trinketsandbaubles.config.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * What a player is wearing, one entry per ability.
 *
 * <p>Each ability has a name key and one or more description keys, all from the language file, so a
 * player who edits the language file changes this screen too. A description key may carry {@code %s}
 * or {@code %d}, filled from the mod's configuration and key bindings by a function held beside it —
 * the same numbers and keys the item's own tooltip shows, because both read the same sources.
 *
 * <p>Lines that belong to one ability are written under the one name. An accessory whose many effects
 * are summed up in one name — a wither ring is "凋零亲和", not three separate abilities — lists
 * them together under that name. Nothing is paired by position: the name and its descriptions are
 * written side by side, so a description can never land under the wrong name.
 */
@OnlyIn(Dist.CLIENT)
public final class EquippedAbilityReader
{
    private EquippedAbilityReader()
    {
    }

    public record Ability(Component item, Component name, List<Component> lines)
    {
    }

    /** One ability: a name key, and its description keys each with the arguments to fill it. */
    private record Entry(String nameKey, List<Described> described)
    {
    }

    private record Described(String key, Function<ItemStack, Object[]> args)
    {
    }

    private static final Map<String, List<Entry>> ABILITIES = new LinkedHashMap<>();

    static
    {
        // The dragon's ring, whose lines name a key each — read from the bindings so a rebound key is
        // shown as it is bound.
        entry("dragons_ring", "breath_dragon",
                desc("tooltip12", KeyBindings.DRAGON_BREATH_KEY));
        entry("dragons_ring", "immunity_fire", desc("tooltip13"));
        entry("dragons_ring", "night_vision",
                desc("tooltip14", KeyBindings.DRAGON_NIGHT_VISION_KEY));
        entry("dragons_ring", "greedy_eyes",
                desc("tooltip15", KeyBindings.TOGGLE_DRAGONS_EYE_MODE));
        entry("dragons_ring", "creative_flight",
                desc("tooltip16", KeyBindings.DRAGON_FLIGHT_TOGGLE_KEY));

        entry("dwarves_ring", "lucky", desc("tooltip12"));
        entry("dwarves_ring", "skilled_miner",
                desc("tooltip13"), desc("tooltip14"));

        entry("elves_ring", "skilled_archer", desc("tooltip12"));
        entry("elves_ring", "forest_swiftness", desc("tooltip13"));

        entry("faelis_ring", "unarmed_strike", desc("tooltip12"));
        entry("faelis_ring", "milk_boon", desc("tooltip13"));
        entry("faelis_ring", "heavy_armor", desc("tooltip14"));

        entry("fairies_ring", "creative_flight", desc("tooltip12"));
        entry("fairies_ring", "elytra_flight",
                desc("tooltip13", KeyBindings.DRAGON_FLIGHT_TOGGLE_KEY));

        // The glide both winged races share, whose two lines are the ones this mod adds to either
        // ring's tooltip after the numbered ones. One ability with two facts.
        entry("dragons_ring", "elytra_flight", desc("glide.hint"), desc("glide.boost_hint"));
        entry("fairies_ring", "elytra_flight", desc("glide.hint"), desc("glide.boost_hint"));

        entry("goblins_ring", "skilled_rider", desc("tooltip13"), desc("tooltip16"));
        entry("goblins_ring", "blast_resistance", desc("tooltip14"));
        entry("goblins_ring", "creeper_friend", desc("tooltip15"));

        entry("titan_ring", "large_hands", desc("tooltip12"));
        entry("titan_ring", "heavy", desc("tooltip13"));
        entry("titan_ring", "stampede", desc("tooltip14"));

        entry("taurus_ring", "stampede",
                desc("tooltip12"),
                desc("tooltip13", KeyBindings.RACE_ABILITY_KEY));

        // The other accessories, each ability with its description keys and the arguments it needs.
        entry("dragons_eye", "immunity_fire", desc("tooltip1"));
        entry("dragons_eye", "night_vision", desc("tooltip2", KeyBindings.DRAGON_NIGHT_VISION_KEY));
        entry("dragons_eye", "greedy_eyes", desc("tooltip3"));

        // The three element-tinted eyes. Each states its own immunity first, then the facts its family
        // shares, each under the name it belongs to.
        entry("dragons_eye_fire", "immunity_fire_breath", desc("effect"));
        entry("dragons_eye_fire", "immunity_fire", desc("shared1"));
        entry("dragons_eye_fire", "night_vision", desc("shared2"));
        entry("dragons_eye_fire", "greedy_eyes", desc("shared3"));

        entry("dragons_eye_ice", "immunity_ice_breath", desc("effect"));
        entry("dragons_eye_ice", "frost_walker", desc("effect1"));
        entry("dragons_eye_ice", "immunity_fire", desc("shared1"));
        entry("dragons_eye_ice", "night_vision", desc("shared2"));
        entry("dragons_eye_ice", "greedy_eyes", desc("shared3"));

        entry("dragons_eye_lightning", "immunity_lightning_breath", desc("effect"));
        entry("dragons_eye_lightning", "immunity_fire", desc("shared1"));
        entry("dragons_eye_lightning", "night_vision", desc("shared2"));
        entry("dragons_eye_lightning", "greedy_eyes", desc("shared3"));

        entry("glow_ring", "night_vision", desc("tooltip1"));

        entry("sea_stone", "ocean_breath", desc("tooltip1"));
        entry("sea_stone", "affinity_water", desc("tooltip2"));

        entry("inertia_null_stone", "nullify_kinetic",
                desc("tooltip1"), desc("tooltip2"), desc("tooltip3"));

        // The greater inertia stone grants five things at once — knockback resistance, speed, jump,
        // fall reduction and the climb itself — summed up in one ability.
        entry("greater_inertia_stone", "climbing",
                desc("tooltip.knockback"), desc("tooltip.speed"), desc("tooltip.jump"),
                desc("tooltip.fall"), desc("tooltip4"));

        entry("weightless_stone", "weightless", desc("tooltip1"));

        entry("teddy_bear", "well_rested", desc("tooltip1"));

        // The shield of honor: a resistance the wearer keeps, a reduced explosion, a hit counter that
        // negates a hit, and the stacking rule — four facts, all under the one "safe guard" name.
        // Registered as "damage_shield"; the tooltip keys keep the shield_of_honor name.
        entry("damage_shield", "safe_guard",
                desc("tooltip2"),
                desc("tooltip.explosion", s -> new Object[] {
                        (int) ((1.0D - ModConfig.SHIELD_EXPLOSION_REDUCTION.get()) * 100) }),
                desc("tooltip4", s -> new Object[] { ModConfig.SHIELD_MAX_DAMAGE_COUNT.get() }),
                desc("tooltip5", s -> new Object[] { ModConfig.SHIELD_RESISTANCE_STACK_LIMIT.get() + 1 }));

        entry("wither_ring", "affinity_dark",
                desc("tooltip.immunity"),
                desc("tooltip.effect", s -> new Object[] { pct(ModConfig.WITHER_RING_CHANCE.get()) }));

        entry("faelis_claw", "vicious_strike",
                desc("tooltip.damage_boost", s -> new Object[] { pct(ModConfig.FAELIS_CLAW_DAMAGE_BOOST.get()) }));

        entry("ender_tiara", "ender_queen",
                desc("tooltip.enderman_follow"),
                desc("tooltip.damage_immunity", s -> new Object[] { (int) (ModConfig.DAMAGE_IMMUNITY_CHANCE.get() * 100) }));

        entry("poison_stone", "immunity_poison",
                desc("tooltip.immunity_poison"), desc("tooltip.immunity_hunger"));
        entry("poison_stone", "affinity_poison",
                desc("tooltip.poison_chance", s -> new Object[] { (int) (ModConfig.POISON_STONE_CHANCE.get() * 100) }),
                desc("tooltip.damage_bonus", s -> new Object[] { one(ModConfig.POISON_STONE_DAMAGE_MULTIPLIER.get()) }));

        entry("arcing_orb", "dodging",
                desc("dash_cost", s -> new Object[] {
                        one(ModConfig.DASH_MANA_COST.get()),
                        KeyBindings.DASH_KEY.getKey().getDisplayName().getString() }));
        entry("arcing_orb", "arc_beam",
                desc("damage_multiplier", s -> new Object[] {
                        three(ModConfig.DAMAGE_MULTIPLIER.get()),
                        KeyBindings.CHARGE_KEY.getKey().getDisplayName().getString() }));

        // The polarized stone, whose two modes are switched with keys — read from the bindings.
        entry("polarized_stone", "magnetic",
                desc("usage.attraction", s -> new Object[] {
                        KeyBindings.ATTRACTION_TOGGLE_KEY.getKey().getDisplayName().getString() }),
                desc("usage.deflection", s -> new Object[] {
                        KeyBindings.DEFLECTION_TOGGLE_KEY.getKey().getDisplayName().getString() }));
    }

    /** A percent value as a whole number, which is how the tooltips show a chance. */
    private static String pct(double value)
    {
        return String.format("%.0f", value * 100);
    }

    private static String one(double value)
    {
        return String.format("%.1f", value);
    }

    private static String three(double value)
    {
        return String.format("%.3f", value);
    }

    /** A description with no arguments. */
    private static Described desc(String key)
    {
        return new Described(key, s -> new Object[0]);
    }

    /** A description whose single argument is a key's display name. */
    private static Described desc(String key, net.minecraft.client.KeyMapping binding)
    {
        return new Described(key, s -> new Object[] { binding.getKey().getDisplayName().getString() });
    }

    private static Described desc(String key, Function<ItemStack, Object[]> args)
    {
        return new Described(key, args);
    }

    private static void entry(String id, String name, Described... described)
    {
        List<Entry> entries = ABILITIES.computeIfAbsent(id, k -> new ArrayList<>());

        // An ability named again for the same accessory adds its facts to the one already there,
        // rather than becoming a second row — a ring that states its glide twice would otherwise show
        // two identical names in the list.
        for (Entry existing : entries)
        {
            if (existing.nameKey().equals(name))
            {
                List<Described> merged = new ArrayList<>(existing.described());
                merged.addAll(List.of(described));
                entries.set(entries.indexOf(existing), new Entry(name, merged));
                return;
            }
        }

        entries.add(new Entry(name, List.of(described)));
    }

    public static List<Ability> read(Player player)
    {
        List<Ability> found = new ArrayList<>();

        CuriosApi.getCuriosInventory(player).resolve().ifPresent(handler ->
        {
            for (String identifier : handler.getCurios().keySet())
            {
                ICurioStacksHandler stackHandler = handler.getCurios().get(identifier);

                if (stackHandler == null)
                {
                    continue;
                }

                for (int i = 0; i < stackHandler.getSlots(); i++)
                {
                    ItemStack stack = stackHandler.getStacks().getStackInSlot(i);

                    if (!stack.isEmpty())
                    {
                        found.addAll(describe(stack));
                    }
                }
            }
        });

        // The race the player currently is, whether from wearing a ring or from having eaten one of
        // the potions that make the change permanent. Its abilities belong on this screen either way —
        // a player who drank a dragon's gem has a dragon's breath and no ring to show for it.
        found.addAll(raceAbilities(player));

        return found;
    }

    /**
     * The abilities of the race the player currently is.
     *
     * <p>Read from the ring that grants the race, because the ring is where those abilities are written
     * down — its tooltip states them. The ring is not worn in this case, so its item stack is made only
     * to take its name and to fill its tooltip; nothing is added to the player's inventory.
     */
    private static List<Ability> raceAbilities(Player player)
    {
        for (var entry : com.jinqinxixi.trinketsandbaubles.capability.registry.ModCapabilities.RACE_CAPABILITIES.entrySet())
        {
            var present = player.getCapability(entry.getValue()).resolve().orElse(null);

            if (!(present instanceof com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability race)
                    || !race.isActive())
            {
                continue;
            }

            // The ring that grants this race, found by the race's own id.
            String ringId = switch (entry.getKey().toLowerCase())
            {
                case "dragon" -> "dragons_ring";
                case "dwarves" -> "dwarves_ring";
                case "elves" -> "elves_ring";
                case "faeles" -> "faelis_ring";
                case "fairy" -> "fairies_ring";
                case "goblins" -> "goblins_ring";
                case "titan" -> "titan_ring";
                case "taurus" -> "taurus_ring";
                default -> "";
            };

            if (ringId.isEmpty())
            {
                continue;
            }

            var item = ForgeRegistries.ITEMS.getValue(
                    new net.minecraft.resources.ResourceLocation(
                            com.jinqinxixi.trinketsandbaubles.TrinketsandBaublesMod.MOD_ID, ringId));

            if (item != null)
            {
                // Only when the ring is not already being worn, so an equipped ring is not listed twice.
                if (!isWearing(player, item))
                {
                    ItemStack standIn = new ItemStack(item);

                    // 龙族按元素区分描述（火/冰/雷），而这里造的是裸堆、没有元素 NBT，能力界面就
                    // 永远显示默认的「巨龙之戒」。玩家吃龙宝石变身时戒指并不在身上，元素的真相在
                    // 能力里（已随状态同步到客户端），把它写到这枚替身堆上，描述才能按元素走。
                    if ("dragon".equals(entry.getKey().toLowerCase())
                            && present instanceof com.jinqinxixi.trinketsandbaubles.capability.impl.DragonCapability dragon)
                    {
                        com.jinqinxixi.trinketsandbaubles.traits.elements.ItemElements.setPrimary(
                                standIn,
                                switch (dragon.getDragonElement())
                                {
                                    case "ice" -> com.jinqinxixi.trinketsandbaubles.init.ModElements.ICE.get();
                                    case "lightning" -> com.jinqinxixi.trinketsandbaubles.init.ModElements.LIGHTNING.get();
                                    case "fire" -> com.jinqinxixi.trinketsandbaubles.init.ModElements.FIRE.get();
                                    default -> com.jinqinxixi.trinketsandbaubles.init.ModElements.NEUTRAL.get();
                                });
                    }

                    return describe(standIn);
                }
            }
        }

        return List.of();
    }

    /** Whether the player is wearing a particular accessory, in any slot. */
    private static boolean isWearing(Player player, net.minecraft.world.item.Item item)
    {
        boolean[] worn = { false };

        CuriosApi.getCuriosInventory(player).resolve().ifPresent(handler ->
        {
            for (String identifier : handler.getCurios().keySet())
            {
                ICurioStacksHandler stackHandler = handler.getCurios().get(identifier);

                if (stackHandler == null)
                {
                    continue;
                }

                for (int i = 0; i < stackHandler.getSlots(); i++)
                {
                    if (stackHandler.getStacks().getStackInSlot(i).is(item))
                    {
                        worn[0] = true;
                        return;
                    }
                }
            }
        });

        return worn[0];
    }

    private static List<Ability> describe(ItemStack stack)
    {
        String id = ForgeRegistries.ITEMS.getKey(stack.getItem()) == null
                ? ""
                : ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath();

        List<Entry> entries = ABILITIES.get(id);

        if (entries == null)
        {
            return List.of();
        }

        // The item's own tooltip, colours and filled numbers included. Taken whole rather than
        // re-translated, because the colour a line was written in is part of what it says — a red line
        // about a cost and a green one about a boon are read differently — and re-translating drops it.
        List<Component> tooltip = new ArrayList<>();
        stack.getItem().appendHoverText(stack, null, tooltip, net.minecraft.world.item.TooltipFlag.Default.NORMAL);

        // The description lines, in the order the tooltip states them. The table below lists the same
        // facts in the same order, so the two are matched by position — the first ability gets the
        // first fact, the second the next, and a name that gathers several facts gets them in order.
        //
        // Every line is considered, including the first: appendHoverText states the item's facts and
        // nothing else — the item's own name is added by the game when it builds a tooltip, outside this
        // method — so skipping a line here would shift every description onto the ability above it.
        List<Component> facts = new ArrayList<>();

        for (Component line : tooltip)
        {
            if (line.getString().isBlank())
            {
                continue;
            }

            String stripped = line.getString().replace("\u00a7", "").trim();

            // A heading, an instruction, or a slot listing, rather than a fact about the item.
            if (stripped.endsWith("：") || stripped.endsWith(":")
                    || stripped.toLowerCase().contains("shift")
                    || stripped.contains("栏位"))
            {
                continue;
            }

            // The race's size, which is a figure rather than an ability and was never in the table.
            if (stripped.contains("正常体型"))
            {
                continue;
            }

            // The item's attribute lines, which the game appends to every tooltip of an item carrying
            // attribute modifiers — "◆ 愤怒：攻击伤害 +6%". They are statistics rather than an ability,
            // and they were never in the table, so they are not shown. The diamond is the marker the
            // game puts in front of every one of them.
            if (stripped.startsWith("\u25c6") || stripped.startsWith("◆")
                    || stripped.contains("◆") || stripped.contains("\u25c6"))
            {
                continue;
            }

            facts.add(line);
        }

        List<Ability> abilities = new ArrayList<>();
        int factIndex = 0;

        for (Entry entry : entries)
        {
            List<Component> lines = new ArrayList<>();

            for (Described ignored : entry.described())
            {
                if (factIndex < facts.size())
                {
                    lines.add(facts.get(factIndex++));
                }
            }

            if (!lines.isEmpty())
            {
                // The immunity row of an element-variant item names its own element: the static entry
                // says "fire immunity" for all three dragon rings, which reads wrong on an ice one.
                String nameKey = entry.nameKey();
                if (stack.getItem() instanceof com.jinqinxixi.trinketsandbaubles.traits.elements.IElementProvider provider
                        && "immunity_fire".equals(nameKey))
                {
                    String element = provider.getPrimaryElement(stack).getName().toLowerCase();
                    if ("ice".equals(element) || "lightning".equals(element))
                    {
                        nameKey = "immunity_" + element;
                    }
                }

                abilities.add(new Ability(
                        stack.getHoverName(),
                        Component.translatable("ability.trinketsandbaubles." + nameKey),
                        lines));
            }
        }

        return abilities;
    }
}
