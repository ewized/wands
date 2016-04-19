package com.ewized.wands.types;

import com.google.common.collect.Maps;
import net.year4000.utilities.Conditions;

import java.util.Map;
import java.util.function.Supplier;

/** An enumeration and storage of the wands the system can use */
public class WandTypes {
    private static final Map<String, WandType> types = Maps.newHashMap();

    /** Register a wand into the system */
    public static WandType register(String name, Supplier<Wand> supplier) {
        Conditions.nonNullOrEmpty(name, "name");
        Conditions.nonNull(supplier, "supplier");
        Wand wand = Conditions.nonNull(supplier.get(), "supplier.get()");
        WandType type = new WandType(wand);
        Conditions.condition(types.get(name) != null, "Must not all ready exist");
        types.put(name, type);
        return type;
    }

    public static final WandType ICE_WAND = register("ice", IceWand::new);

}
