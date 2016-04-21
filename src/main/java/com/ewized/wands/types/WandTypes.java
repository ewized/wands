/*
 Copyright (C) 2016 ewized

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package com.ewized.wands.types;

import static org.spongepowered.api.item.ItemTypes.DIAMOND_HOE;
import static org.spongepowered.api.item.ItemTypes.GOLDEN_HOE;
import static org.spongepowered.api.item.ItemTypes.IRON_HOE;
import static org.spongepowered.api.item.ItemTypes.WOODEN_HOE;

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
import org.spongepowered.api.item.ItemType;

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
    public static WandType register(String name, ItemType itemType, Supplier<Wand> supplier) {
        Conditions.nonNullOrEmpty(name, "name");
        Conditions.nonNull(supplier, "supplier");
        Wand wand = Conditions.nonNull(supplier.get(), "supplier.get()");
        String item = itemType.getTranslation().getId().substring(itemType.getTranslation().getId().lastIndexOf("."));
        WandType type = new WandType(name, item, wand);
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

    public static final WandType ICE_WAND = register("ice", IRON_HOE, IceWand::new);
    public static final WandType FIRE_WAND = register("fire", GOLDEN_HOE, FireWand::new);
    public static final WandType WIND_WAND = register("wind", WOODEN_HOE, WindWand::new);
    public static final WandType WATER_WAND = register("water", DIAMOND_HOE, WaterWand::new);
}
