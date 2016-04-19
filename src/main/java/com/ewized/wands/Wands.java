package com.ewized.wands;

import com.ewized.wands.types.WandTypes;
import net.year4000.utilities.sponge.AbstractSpongePlugin;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
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
        return Wands.instance();
    }

    @Listener
    public void enable(GameConstructionEvent event) {
        Wands.debug("Loaded wands");
        WandTypes.values().forEach(AbstractSpongePlugin::debug);
    }
}
