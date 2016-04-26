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
package com.ewized.wands.types.elements;

import com.ewized.wands.Messages;
import com.ewized.wands.Wands;
import com.ewized.wands.types.Wand;
import com.ewized.wands.types.WandType;
import com.flowpowered.math.vector.Vector3d;
import net.year4000.utilities.Conditions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class IceWand implements Wand {
    private static final Wands plugin = Wands.get();
    private static final SpongeExecutorService executor = Sponge.getScheduler().createAsyncExecutor(plugin);

    @Override
    public Text name(CommandSource src) {
        return Text.of(TextColors.AQUA, Messages.WAND_ICE_NAME.get(src));
    }

    @Override
    public void onAction(Player player, WandType wand) {
        player.sendMessage(ChatTypes.ACTION_BAR, name(player));
        Vector3d vector = player.getLocation().getPosition().add(0, -1, 0);
        new Vortex(player.getWorld(), vector).start();
    }

    /** The vortex task */
    public class Vortex implements Runnable {
        private final AtomicInteger deg = new AtomicInteger(360 * 2);
        private final AtomicReference<Task> task = new AtomicReference<>();
        private final World world;
        private final Vector3d origin;

        public Vortex(World world, Vector3d origin) {
            this.world = Conditions.nonNull(world, "world");
            this.origin = Conditions.nonNull(origin, "origin");
        }

        /** Start the vortex task */
        public void start() {
            task.set(executor.scheduleWithFixedDelay(this, 0, 50, TimeUnit.MILLISECONDS).getTask());
        }

        /** Show the particles for a cool effect */
        private void particles(double beta) {
            double o = 0.1;
            double r = 0.01;
            double rr = 0.001;
            for (int i = 0; i < 360 * 1.25; i++) {
                double θ = i * Math.PI / 180;
                double y = r * Math.cos(8 * θ) * 0.125;
                double z = r * Math.sin(6 * θ) * 0.125;
                double yy = -rr * Math.cos(θ);
                double zz = -rr * Math.sin(θ);
                rr += 0.0125;
                r += 0.0654;

                double cosBeta = Math.cos(beta);
                double sinBeta = Math.sin(beta);
                double xxx = z * cosBeta - y * sinBeta;
                double zzz = z * sinBeta + y * cosBeta;
                double xxxx = zz * cosBeta - yy * sinBeta;
                double zzzz = zz * sinBeta + yy * cosBeta;

                // Outer
                if (i % 7 == 0) {
                    ParticleEffect effect = ParticleEffect.builder().type(ParticleTypes.SNOW_SHOVEL).build();
                    world.spawnParticles(effect, origin.add(xxxx, o += 0.00125, zzzz));
                }

                // Inner
                if (i % 3 == 0 || i % 5 == 0) {
                    ParticleType e = i % 3 == 0 ? ParticleTypes.SMOKE_NORMAL : ParticleTypes.REDSTONE;
                    ParticleEffect effect2 = ParticleEffect.builder().type(e).build();
                    world.spawnParticles(effect2, origin.add(xxx, o += 0.0576, zzz));
                }
            }
        }

        @Override
        public void run() {
            deg.getAndDecrement();
            int number = deg.getAndDecrement();
            if (number > 0) {
                particles(number * Math.PI / 180);
            } else if (task.get() != null) {
                task.get().cancel();
            }
        }
    }
}
