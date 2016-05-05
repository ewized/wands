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
import com.flowpowered.math.TrigMath;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.year4000.utilities.Conditions;
import net.year4000.utilities.Utils;
import net.year4000.utilities.reflection.Reflections;
import net.year4000.utilities.reflection.SignatureLookup;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Infusion implements Runnable, Comparable<Infusion> {
    private static final List<Infusion> pending = Lists.newArrayList();
    private static final Wands plugin = Wands.get();
    private static final SpongeExecutorService executor = Sponge.getScheduler().createAsyncExecutor(plugin);
    private static final ParticleEffect effectRedStone = ParticleEffect.builder().type(ParticleTypes.REDSTONE).build();
    private static final ParticleEffect effectExplode = ParticleEffect.builder().type(ParticleTypes.EXPLOSION_LARGE).count(2).build();
    private static final Vector3d standOffSet = new Vector3d(0, -0.85, 0);
    private static final Vector3d standRotation = new Vector3d(0, -3.75, 0);
    private static final Vector3d standArmRotation = new Vector3d(-145, -90, 0);
    private static final Vector3d liftingAnimation = new Vector3d(0, 0.001, 0);
    private static final Cause standCause = Cause.source(Wands.get()).build();
    private static final Field standInvisible;
    static {
        Field field = null;
        try {
            // The first boolean is the one we need
            field = SignatureLookup.fields("Z", Class.forName("net.minecraft.entity.item.EntityArmorStand")).findSorted().first();
        } catch (ClassNotFoundException nothing) {
        } finally {
            standInvisible = field;
        }
    }
    private final Vector3i origin;
    private final World world;
    private Vector3d lifting;
    private Task task;
    private Entity entity;
    private WandType wandType;
    private Player player;
    private int ticks;
    private ArmorStand armorStand;
    private int rotation;

    public Infusion(Vector3i origin, World world) {
        this.origin = Conditions.nonNull(origin, "origin");
        this.world = Conditions.nonNull(world, "world");
        lifting = origin.toDouble().add(0.5, 1, 0.5);
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
        synchronized (pending) {
            return pending.contains(this);
        }
    }

    /** Clean up the infusion alters from the world */
    public static void cleanUp(World world) {
        Conditions.nonNull(world, "world");
        world.getLoadedChunks().forEach(Infusion::cleanUp);
    }

    /** Clean up the infusion alters from the world */
    public static void cleanUp(Chunk chunk) {
        Conditions.nonNull(chunk, "chunk");
        synchronized (pending) {
            pending.stream()
                    .filter(infusion -> chunk.getPosition().equals(new Vector3i(infusion.origin.getX() >> 4, 0, infusion.origin.getZ() >> 4)))
                    .forEach(infusion -> {
                        infusion.task.cancel();
                        infusion.armorStand.remove();
                        infusion.entity.remove();
                    });
        }
    }

    /** Create the armor stand used for the infusion animation */
    private void createArmorStand() {
        armorStand = (ArmorStand) world.createEntity(EntityTypes.ARMOR_STAND, lifting.add(standOffSet)).orElse(null);
        Reflections.setter(armorStand, standInvisible, true); // Until Invisible bug
        entity.getWorld().spawnEntity(armorStand, standCause);
        // armorStand.offer(Keys.INVISIBLE, true);
        armorStand.offer(Keys.RIGHT_ARM_ROTATION, standArmRotation);
        armorStand.offer(Keys.ARMOR_STAND_HAS_GRAVITY, false);
        armorStand.offer(Keys.ARMOR_STAND_HAS_ARMS, true);
        armorStand.offer(Keys.INVISIBILITY_IGNORES_COLLISION, true);
        armorStand.offer(Keys.ARMOR_STAND_IS_SMALL, true);
        armorStand.setItemInHand(entity.get(Keys.REPRESENTED_ITEM).get().createStack());
    }

    /** Infuse the selected wand type for the player */
    public void infuse(Player player, WandType wandType, Entity entity) {
        this.player = Conditions.nonNull(player, "player");
        this.wandType = Conditions.nonNull(wandType, "wand");
        this.entity = Conditions.nonNull(entity, "entity");
        // Set up the thingy at the center of the alter
        createArmorStand();
        entity.offer(Keys.INVISIBLE, true);
        // Show the effects
        task = executor.scheduleAtFixedRate(this, 0, 50, TimeUnit.MILLISECONDS).getTask();
        synchronized (pending) {
            pending.add(this);
        }
    }

    @Override
    public void run() {
        double theta = rotation++ * TrigMath.DEG_TO_RAD;
        double x = TrigMath.cos(theta) / 2;
        double z = TrigMath.sin(theta) / 2;
        Vector3d lifting = this.lifting.add(x, 0, z);
        this.lifting = this.lifting.add(liftingAnimation);
        armorStand.setLocationAndRotation(new Location<>(world, lifting.add(standOffSet)), armorStand.getRotation().add(standRotation));
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
            entity.offer(Keys.INVISIBLE, false);
        }
        // Should the effect stop
        if (stop) {
            synchronized (pending) {
                pending.remove(this);
                armorStand.remove();
                task.cancel();
            }
        }
    }

    @Override
    public int compareTo(Infusion other) {
        return origin.compareTo(other.origin);
    }

    @Override
    public int hashCode() {
        return origin.hashCode();
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
