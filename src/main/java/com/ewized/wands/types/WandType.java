package com.ewized.wands.types;

import net.year4000.utilities.Conditions;
import net.year4000.utilities.Utils;

/** The type of wand that stores the id and the wand instance */
public class WandType {
    private final String id;
    private final Wand wand;

    /** Create the wand type with the wand */
    public WandType(String id, Wand wand) {
        this.id = Conditions.nonNullOrEmpty(id, "id");
        this.wand = Conditions.nonNull(wand, "wand");
    }

    @Override
    public int hashCode() {
        return Utils.hashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return Utils.equals(this, other);
    }

    @Override
    public String toString() {
        return Utils.toString(this);
    }
}
