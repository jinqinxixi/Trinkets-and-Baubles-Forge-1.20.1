package com.jinqinxixi.trinketsandbaubles.init;

import com.jinqinxixi.trinketsandbaubles.traits.elements.AirElement;
import com.jinqinxixi.trinketsandbaubles.traits.elements.DarkElement;
import com.jinqinxixi.trinketsandbaubles.traits.elements.EarthElement;
import com.jinqinxixi.trinketsandbaubles.traits.elements.Element;
import com.jinqinxixi.trinketsandbaubles.traits.elements.FireElement;
import com.jinqinxixi.trinketsandbaubles.traits.elements.IceElement;
import com.jinqinxixi.trinketsandbaubles.traits.elements.LightElement;
import com.jinqinxixi.trinketsandbaubles.traits.elements.LightningElement;
import com.jinqinxixi.trinketsandbaubles.traits.elements.NeutralElement;
import com.jinqinxixi.trinketsandbaubles.traits.elements.PoisonElement;
import com.jinqinxixi.trinketsandbaubles.traits.elements.VoidElement;
import com.jinqinxixi.trinketsandbaubles.traits.elements.WaterElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * The elements, and the registry they live in.
 *
 * <p>A registry of its own rather than an enum, which lets a pack add an element without touching
 * this mod. The entries are ordinary objects with no behaviour, so
 * the registry is doing nothing a list could not — except that a name added from outside has
 * somewhere to go.
 *
 * <p>The order matters in one place only: {@code NEUTRAL} is what "no element" is, and several
 * callers ask whether an element is that one. Everything else is looked up by name.
 */
public class ModElements
{
    public static final ResourceLocation REGISTRY_ID =
            new ResourceLocation("trinketsandbaubles", "elements");

    public static final DeferredRegister<Element> ELEMENTS =
            DeferredRegister.create(REGISTRY_ID, "trinketsandbaubles");

    private static final Supplier<IForgeRegistry<Element>> REGISTRY =
            ELEMENTS.makeRegistry(RegistryBuilder::new);

    public static final RegistryObject<Element> NEUTRAL = ELEMENTS.register("neutral", NeutralElement::new);
    public static final RegistryObject<Element> AIR = ELEMENTS.register("air", AirElement::new);
    public static final RegistryObject<Element> DARK = ELEMENTS.register("dark", DarkElement::new);
    public static final RegistryObject<Element> EARTH = ELEMENTS.register("earth", EarthElement::new);
    public static final RegistryObject<Element> FIRE = ELEMENTS.register("fire", FireElement::new);
    public static final RegistryObject<Element> ICE = ELEMENTS.register("ice", IceElement::new);
    public static final RegistryObject<Element> LIGHT = ELEMENTS.register("light", LightElement::new);
    public static final RegistryObject<Element> LIGHTNING = ELEMENTS.register("lightning", LightningElement::new);
    public static final RegistryObject<Element> POISON = ELEMENTS.register("poison", PoisonElement::new);
    public static final RegistryObject<Element> VOID = ELEMENTS.register("void", VoidElement::new);
    public static final RegistryObject<Element> WATER = ELEMENTS.register("water", WaterElement::new);

    /** Handed out where an empty array is wanted, so that callers share one rather than allocating. */
    public static final Element[] EMPTY = new Element[0];

    public static IForgeRegistry<Element> registry()
    {
        return REGISTRY.get();
    }

    public static Element byName(ResourceLocation id)
    {
        return registry().getValue(id);
    }

    /**
     * Hands the registry to the mod bus.
     *
     * <p>Called from the mod's constructor, which is the only time a deferred register can be
     * attached — after that the bus has already been walked and anything registered would be
     * silently absent.
     */
    public static void register(IEventBus modEventBus)
    {
        ELEMENTS.register(modEventBus);
    }

    private ModElements()
    {
    }
}