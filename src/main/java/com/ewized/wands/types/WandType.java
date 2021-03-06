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

import net.year4000.utilities.Conditions;
import net.year4000.utilities.Utils;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.service.permission.Subject;

/** The type of wand that stores the id and the wand instance */
public class WandType {
    private final String id;
    private final String item;
    private final ItemType itemType;
    private final Wand wand;

    /** Create the wand type with the wand */
    public WandType(String id, String item, ItemType itemType, Wand wand) {
        this.id = Conditions.nonNullOrEmpty(id, "id");
        this.item = Conditions.nonNullOrEmpty(item, "item");
        this.itemType = Conditions.nonNull(itemType,"itemType");
        this.wand = Conditions.nonNull(wand, "wand");
    }

    /** Get the id of the wand */
    public String id() {
        return id;
    }

    /** Get the item name of the wand */
    public String item() {
        return item;
    }

    /** Get the item type */
    public ItemType itemType() {
        return itemType;
    }

    /** Get the wand of this wand type */
    public Wand wand() {
        return wand;
    }

    /** Does the user have the permission to use the wand */
    public boolean hasPermission(Subject subject) {
        return subject.hasPermission("wands." + id());
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
