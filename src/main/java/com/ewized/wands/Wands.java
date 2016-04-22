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
package com.ewized.wands;

import com.ewized.wands.types.WandType;
import com.ewized.wands.types.WandTypes;
import com.google.common.collect.ImmutableMap;
import net.year4000.utilities.Reflections;
import net.year4000.utilities.sponge.AbstractSpongePlugin;
import net.year4000.utilities.sponge.protocol.Packet;
import net.year4000.utilities.sponge.protocol.PacketListener;
import net.year4000.utilities.sponge.protocol.PacketTypes;
import net.year4000.utilities.sponge.protocol.Packets;
import net.year4000.utilities.sponge.protocol.proxy.ProxyEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import java.util.Optional;

@Plugin(
    id = "com.ewized.wands",
    name = "Wands",
    description = "Various wands that are fun to mess around with",
    authors = {"ewized"}
)
public class Wands extends AbstractSpongePlugin {
    private Packets packets;

    /** Get the current instance of wands */
    public static Wands get() {
        return Wands.instance();
    }

    @Listener
    public void enable(GameInitializationEvent event) {
        packets = Packets.manager(this);
        Wands.debug("Loaded wands");
        WandTypes.values().forEach(AbstractSpongePlugin::debug);
        packets.registerListener(PacketTypes.of(PacketTypes.State.PLAY, PacketTypes.Binding.INBOUND, 0x08), onRightClick);
    }

    @Listener
    public void deisable(GameStoppingEvent event) {
        Sponge.getScheduler().getScheduledTasks(this).forEach(Task::cancel);
    }

    /** Find the wand type by the raw minecraft locale name */
    private Optional<WandType> findByRawName(String rawName) {
        return WandTypes.values().stream().filter(wand -> rawName.contains(wand.item())).findFirst();
    }

    // Packet workaround
    private PacketListener onRightClick = (player, packet) -> {
        // Item
        ProxyEntity entity = ProxyEntity.of(player);
        String rawItemName = Reflections.field(packet.mcPacket(), "field_149580_e").get().toString();
        Optional<WandType> wand = findByRawName(rawItemName);

        if (wand.isPresent()) { // check if item is a wand
            Packet newPacket = new Packet(PacketTypes.of(PacketTypes.State.PLAY, PacketTypes.Binding.OUTBOUND, 0x0B));
            newPacket.inject(ImmutableMap.of("field_148981_a", entity.entityId(), "field_148980_b", 0));
            packets.sendPacket(player, newPacket);
            wand.get().wand().onAction(player, wand.get());
        }
        return true; // dont leave alone
    };

    @Listener
    public void action(InteractBlockEvent.Secondary block, @First Player player) {
       // todo we need this to fire see onRightClick
    }
}
