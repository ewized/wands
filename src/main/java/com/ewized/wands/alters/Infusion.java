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
package com.ewized.wands.alters;

import com.ewized.wands.Common;
import com.ewized.wands.Wands;
import com.ewized.wands.types.WandType;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import net.year4000.utilities.Conditions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Infusion implements Runnable {
    private static final List<Vector3i> pending = Lists.newArrayList();
    private static final Wands plugin = Wands.get();
    private static final SpongeExecutorService executor = Sponge.getScheduler().createSyncExecutor(plugin);
    private static final ParticleEffect effectRedStone = ParticleEffect.builder().type(ParticleTypes.REDSTONE).build();
    private static final ParticleEffect effectExplode = ParticleEffect.builder().type(ParticleTypes.EXPLOSION_LARGE).count(2).build();
    private final Vector3i origin;
    private final World world;
    private final AtomicReference<Vector3d> lifting = new AtomicReference<>();
    private final AtomicReference<Task> task = new AtomicReference<>();
    private Entity entity;
    private WandType wandType;
    private Player player;

    public Infusion(Vector3i origin, World world) {
        this.origin = Conditions.nonNull(origin, "origin");
        this.world = Conditions.nonNull(world, "world");
        lifting.set(origin.toDouble().add(0.5, 0, 0.5));
    }

    /** The locations on where the red stone torches should be */
    private List<Vector3d> redStoneTorches() {
        List<Vector3d> points = Lists.newArrayList();
        Vector3d origin = this.origin.toDouble().add(0.5, 0.5, 0.5);
        points.add(origin.add(2, 2, 2));
        points.add(origin.add(2, 2, -2));
        points.add(origin.add(-2, 2, -2));
        points.add(origin.add(-2, 2, 2));
        return points;
    }

    /** Get all the points where to spawn the particles */
    private List<Vector3d> infusionParticles(Vector3d origin) {
        List<Vector3d> points = Lists.newArrayList();
        List<Vector3d> torches = redStoneTorches();
        final int depth = 3;
        points.addAll(Common.line(origin, torches.get(0), depth));
        points.addAll(Common.line(origin, torches.get(1), depth));
        points.addAll(Common.line(origin, torches.get(2), depth));
        points.addAll(Common.line(origin, torches.get(3), depth));
        return points;
    }

    /** Is there an alter currently running at this position */
    public boolean isRunningAtLocation() {
        synchronized (Infusion.class) {
            return pending.contains(origin);
        }
    }

    /** Infuse the selected wand type for the player */
    public void infuse(Player player, WandType wandType, Entity entity) {
        this.player = Conditions.nonNull(player, "player");
        this.wandType = Conditions.nonNull(wandType, "wand");
        this.entity = Conditions.nonNull(entity, "entity");

        // Show the effects
        task.set(executor.scheduleAtFixedRate(this, 0, 125, TimeUnit.MILLISECONDS).getTask());
        synchronized (Infusion.class) {
            pending.add(origin);
        }
    }

    @Override
    public void run() {
        Vector3d lifting = this.lifting.getAndSet(this.lifting.get().add(0, 0.0125, 0));
        infusionParticles(lifting).forEach(vector -> world.spawnParticles(effectRedStone, vector));
        player.playSound(SoundTypes.FUSE, lifting, 0.2, 0.02, 0.1);
        entity.setVelocity(new Vector3d(0, 0.0876, 0));
        Task task = this.task.get();
        boolean stop = false;

        // The entity is gone !?!?!
        if ((!entity.isLoaded() || entity.isRemoved()) && task != null) {
            stop = true;
            player.playSound(SoundTypes.GHAST_MOAN, lifting, 1);
        }

        // After the infusion process
        if (lifting.getY() > origin.getY() + 3 && task != null) {
            stop = true;

            // Sounds
            player.playSound(SoundTypes.EXPLODE, lifting, 1);
            world.spawnParticles(effectExplode, lifting);

            // Add to inventory
            ItemStack stack = ItemStack.of(wandType.itemType(), 1);
            stack.offer(Keys.DISPLAY_NAME, wandType.wand().name(player));
            entity.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
            //player.getInventory().offer(stack);
        }

        // Should the effect stop
        if (stop) {
            synchronized (Infusion.class) {
                pending.remove(origin);
            }
            task.cancel();
        }
    }
}
