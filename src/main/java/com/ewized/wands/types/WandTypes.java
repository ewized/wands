package com.ewized.wands.types;

import com.ewized.wands.Wands;
import com.ewized.wands.types.elements.FireWand;
import com.ewized.wands.types.elements.IceWand;
import com.ewized.wands.types.elements.WaterWand;
import com.ewized.wands.types.elements.WindWand;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.year4000.utilities.Conditions;
import net.year4000.utilities.utils.UtilityConstructError;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/** An enumeration and storage of the wands the system can use */
public class WandTypes {
    private static final Map<String, WandType> types = Maps.newHashMap();

    private WandTypes() {
        UtilityConstructError.raise();
    }

    /** Register a wand into the system */
    public static WandType register(String name, Supplier<Wand> supplier) {
        Conditions.nonNullOrEmpty(name, "name");
        Conditions.nonNull(supplier, "supplier");
        Wand wand = Conditions.nonNull(supplier.get(), "supplier.get()");
        WandType type = new WandType(name, wand);
        Conditions.condition(types.get(name) == null, "Must not all ready exist");
        Wands.debug("Wand %s has been added.", name);
        types.put(name, type);
        return type;
    }

    /** Get the immutable collection of values */
    public static Collection<WandType> values() {
        return ImmutableList.copyOf(types.values());
    }

    /** Get the immutable collections of keys*/
    public static Collection<String> keys() {
        return ImmutableSet.copyOf(types.keySet());
    }

    public static final WandType ICE_WAND = register("ice", IceWand::new);
    public static final WandType FIRE_WAND = register("fire", FireWand::new);
    public static final WandType WIND_WAND = register("wind", WindWand::new);
    public static final WandType WATER_WAND = register("water", WaterWand::new);

}
