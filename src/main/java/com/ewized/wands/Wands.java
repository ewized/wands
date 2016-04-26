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
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import net.year4000.utilities.sponge.AbstractSpongePlugin;
import net.year4000.utilities.sponge.protocol.Packet;
import net.year4000.utilities.sponge.protocol.PacketType;
import net.year4000.utilities.sponge.protocol.PacketTypes;
import net.year4000.utilities.sponge.protocol.Packets;
import net.year4000.utilities.sponge.protocol.proxy.ProxyEntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Plugin(
    id = "com.ewized.wands",
    name = "Wands",
    description = "Various wands that are fun to mess around with",
    authors = {"ewized"},
    dependencies = {@Dependency(id = "net.year4000.utilities")}
)
public class Wands extends AbstractSpongePlugin {
    private Packets packets;

    /** Get the current instance of wands */
    public static Wands get() {
        return Wands.instance(Wands.class);
    }

    @Listener
    public void enable(GameInitializationEvent event) {
        packets = Packets.manager(this);
        Wands.debug("Loaded wands");
        WandTypes.values().forEach(Wands::debug);
    }

    @Listener
    public void disable(GameStoppingEvent event) {
        Sponge.getScheduler().getScheduledTasks(this).forEach(Task::cancel);
    }

    /** Find the wand type by the raw minecraft locale name */
    private Optional<WandType> findByRawName(String rawName) {
        return WandTypes.values().stream().filter(wand -> rawName.contains(wand.item())).findFirst();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Listener
    public void wand(InteractBlockEvent.Secondary event, @First Player player) {
        final PacketType PLAY_CLIENT_ANIMATION = PacketTypes.of(PacketTypes.State.PLAY, PacketTypes.Binding.OUTBOUND, 0x0B); // temp until 1.9
        player.getItemInHand().ifPresent(itemStack -> {
            findByRawName(itemStack.getTranslation().getId()).ifPresent(wand -> {
                if (wand.hasPermission(player) && itemStack.get(Keys.DISPLAY_NAME).isPresent()) {
                    packets.sendPacket(player, new Packet(PLAY_CLIENT_ANIMATION).injector()
                            .add(ProxyEntityPlayerMP.of(player).entityId()) // Entity Id
                            .add(0) // Swing Arm
                            .inject());
                    wand.wand().onAction(player, wand);
                    event.setCancelled(true);
                } else {
                    debug(player.getName() + " does not have permission to use " + wand);
                }
            });
        });
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Listener
    public void alter(InteractBlockEvent.Secondary event, @First Player player) {
        player.getItemInHand().ifPresent(itemStack -> {
            if (itemStack.toString().contains("item.potion@16384")) { // is mundane potion
                BlockRay<World> ray = BlockRay.from(player).blockLimit(10).build();
                while (ray.hasNext()) {
                    BlockRayHit<World> hit = ray.next();
                    if (hit.getLocation().getBlock().getType().equals(BlockTypes.REDSTONE_BLOCK)) {
                        player.getWorld()
                            .getEntities(entity -> entity.getType().equals(EntityTypes.ITEM))
                            .stream()
                            .filter(e -> e.getLocation().getBlockPosition().sub(0, 1, 0).equals(hit.getBlockPosition()))
                            .findAny().ifPresent(item -> {
                            findByRawName(item.toString()).ifPresent(wandType -> {


                                Vector3d originA = item.getLocation().getBlockPosition().toDouble().add(0.5, 0.5, 0.5);
                                AtomicReference<Vector3d> originF = new AtomicReference<>(originA.clone());
                                AtomicReference<Task> task = new AtomicReference<>();
                                Task id = Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(() -> {
                                    Vector3d origin = originF.getAndSet(originF.get().add(0, 0.0125, 0));
                                    List<Vector3d> points = Lists.newArrayList();
                                    points.addAll(Common.line(origin, originA.clone().add(2, 2, 2), 3));
                                    points.addAll(Common.line(origin, originA.clone().add(2, 2, -2), 3));
                                    points.addAll(Common.line(origin, originA.clone().add(-2, 2, -2), 3));
                                    points.addAll(Common.line(origin, originA.clone().add(-2, 2, 2), 3));

                                    for (Vector3d point : points) {
                                        item.getWorld().spawnParticles(ParticleEffect.builder().type(ParticleTypes.REDSTONE).build(), point);
                                    }

                                    item.setVelocity(new Vector3d(0, 0.085, 0));

                                    if (originF.get().getY() > originA.getY() + 3) {
                                        task.get().cancel();
                                        item.remove();
                                        ItemStack stack = ItemStack.of(wandType.itemType(), 1);
                                        stack.offer(Keys.DISPLAY_NAME, wandType.wand().name(player));
                                        stack.offer(Keys.HIDE_ENCHANTMENTS, true);
                                        player.getInventory().offer(stack);
                                        player.playSound(SoundTypes.EXPLODE, origin, 1);
                                        item.getWorld().spawnParticles(ParticleEffect.builder().type(ParticleTypes.EXPLOSION_LARGE).build(), origin);
                                    }
                                }, 0, 125, TimeUnit.MILLISECONDS).getTask();
                                task.set(id);
                            });
                        });
                        return;
                    }
                }
            }
        });
    }

    public static void log(Object object, Object... args) {
        log(get(), object, args);
    }

    public static void debug(Object object, Object... args) {
        debug(get(), object, args);
    }
}
