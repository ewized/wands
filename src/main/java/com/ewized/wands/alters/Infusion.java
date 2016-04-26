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
import com.google.common.collect.Sets;
import net.year4000.utilities.Conditions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Infusion implements Runnable {
    private static final List<Vector3i> pending = Lists.newArrayList();
    private static final Wands plugin = Wands.get();
    private static final SpongeExecutorService executor = Sponge.getScheduler().createAsyncExecutor(plugin);
    private static final ParticleEffect effectRedStone = ParticleEffect.builder().type(ParticleTypes.REDSTONE).build();
    private static final ParticleEffect effectExplode = ParticleEffect.builder().type(ParticleTypes.EXPLOSION_LARGE).count(2).build();
    private final Vector3i origin;
    private final World world;
    private final AtomicReference<Vector3d> lifting = new AtomicReference<>();
    private final AtomicReference<Task> task = new AtomicReference<>();
    private Entity entity;
    private WandType wandType;
    private Player player;
    private int ticks;

    public Infusion(Vector3i origin, World world) {
        this.origin = Conditions.nonNull(origin, "origin");
        this.world = Conditions.nonNull(world, "world");
        lifting.set(origin.toDouble().add(0.5, 1, 0.5));
    }

    /** Make sure the alter is really there */
    public boolean isAlter() {
        for (Vector3d vector : redStoneTorches()) {
            if (!world.getLocation(vector).getBlock().getType().equals(BlockTypes.REDSTONE_TORCH)) {
                return false;
            }
        }
        return world.getLocation(origin).getBlock().getType().equals(BlockTypes.REDSTONE_BLOCK);
    }

    /** The locations on where the red stone torches should be */
    private List<Vector3d> redStoneTorches() {
        List<Vector3d> points = Lists.newArrayList();
        Vector3d origin = this.origin.toDouble().add(0.5, 3.5, 0.5);
        points.add(origin.add(2, 0, 2));
        points.add(origin.add(2, 0, -2));
        points.add(origin.add(-2, 0, -2));
        points.add(origin.add(-2, 0, 2));
        return points;
    }

    /** Get all the points where to spawn the particles */
    private Set<Vector3d> infusionParticles(Vector3d origin) {
        Set<Vector3d> points = Sets.newHashSet();
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
        entity.setVelocity(new Vector3d(0, 0.078, 0));
        entity.setLocation(new Location<>(world, lifting));
        Task task = this.task.get();
        boolean stop = false;

        if (ticks++ % 2 == 0) { // show particles every 2 ticks
            infusionParticles(lifting).forEach(vector -> world.spawnParticles(effectRedStone, vector));
        }

        if (ticks % 3 == 0) { // Sound every 3 ticks
            player.playSound(SoundTypes.FUSE, lifting, 0.2, 0.02, 0.1);
        }

        // The entity is gone !?!?!
        if ((!entity.isLoaded() || entity.isRemoved()) && task != null) {
            stop = true;
            player.playSound(SoundTypes.GHAST_MOAN, lifting, 1);
        }

        // After the infusion process
        if (lifting.getY() > origin.getY() + 3.5 && task != null) {
            stop = true;

            // Sounds
            player.playSound(SoundTypes.EXPLODE, lifting, 1);
            world.spawnParticles(effectExplode, lifting);

            // Add to inventory
            ItemStack stack = ItemStack.of(wandType.itemType(), 1);
            stack.offer(Keys.DISPLAY_NAME, wandType.wand().name(player));
            entity.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
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
