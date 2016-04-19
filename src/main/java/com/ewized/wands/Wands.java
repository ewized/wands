package com.ewized.wands;

import net.year4000.utilities.sponge.AbstractSpongePlugin;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
    id = "com.ewized.wands",
    name = "Wands",
    description = "Various wands that are fun to mess around with",
    authors = {"ewized"}
)
public class Wands extends AbstractSpongePlugin {
    /** Get the current instance of wands */
    public static Wands get() {
        return AbstractSpongePlugin.instance();
    }
}
