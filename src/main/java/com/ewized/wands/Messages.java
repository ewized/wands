package com.ewized.wands;

import net.year4000.utilities.cache.QuickCache;
import net.year4000.utilities.locale.ClassLocaleManager;
import net.year4000.utilities.locale.LocaleKeys;
import net.year4000.utilities.locale.Translatable;
import net.year4000.utilities.sponge.SpongeLocale;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public enum Messages implements LocaleKeys<CommandSource, Text> {
    // Wand names
    WAND_FIRE_NAME,
    WAND_ICE_NAME,
    WAND_WATER_NAME,
    WAND_WIND_NAME,
    ;

    @Override
    public Translatable<Text> apply(Optional<CommandSource> player) {
        if (player.isPresent()) {
            return new SpongeLocale(Factory.inst.get(), player.get());
        }

        return new SpongeLocale(Factory.inst.get());
    }

    public static class Factory extends ClassLocaleManager {
        static QuickCache<Messages.Factory> inst = QuickCache.builder(Messages.Factory.class).build();

        public Factory() {
            super("https://raw.githubusercontent.com/ewized/wands/master/src/main/resources/locales/");
        }
    }
}
