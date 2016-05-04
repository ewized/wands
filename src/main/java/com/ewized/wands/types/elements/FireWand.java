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

import com.ewized.wands.Common;
import com.ewized.wands.Messages;
import com.ewized.wands.types.Wand;
import com.ewized.wands.types.WandType;
import com.flowpowered.math.TrigMath;
import com.flowpowered.math.vector.Vector3d;
import net.year4000.utilities.Conditions;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.concurrent.TimeUnit;

public class FireWand implements Wand {
    private static final ParticleEffect effect = ParticleEffect.builder().type(ParticleTypes.FLAME).count(5).build();

    @Override
    public Text name(CommandSource src) {
        return Text.of(TextColors.RED, Messages.WAND_FIRE_NAME.get(src));
    }

    @Override
    public void onAction(Player player, WandType wand) {
        player.sendMessage(ChatTypes.ACTION_BAR, name(player));
        Vector3d vector = player.getLocation().getPosition();
        double theta = player.getHeadRotation().getY() * TrigMath.DEG_TO_RAD;
        new Blast(player.getWorld(), vector, theta).start();
    }

    public class Blast implements Runnable {
        private final World world;
        private final Vector3d origin;
        private final double theta;
        private Vector3d last;
        private int deg = 0;
        private Task task;

        public Blast(World world, Vector3d origin, double theta) {
            this.world = Conditions.nonNull(world, "world");
            this.origin = Conditions.nonNull(origin, "origin");
            this.last = origin;
            this.theta = theta;
        }

        public void start() {
            task = executor.scheduleWithFixedDelay(this, 0, 125, TimeUnit.MILLISECONDS).getTask();
        }

        private void particles(double Θ, double x) {
            double θ = x * TrigMath.DEG_TO_RAD;
            double y = 2 * TrigMath.sin(24 * θ);

            // Head rotation fix
            double xx = TrigMath.cos(Θ) - x * TrigMath.sin(Θ);
            double zz = TrigMath.sin(Θ) + x * TrigMath.cos(Θ);

            Vector3d point = origin.add(xx, y, zz);
            Common.line(last, point, 2).forEach(vector -> world.spawnParticles(effect, vector));
            last = point;
        }

        @Override
        public void run() {
            int number = deg++;
            if (number < 360) {
                particles(theta, number);
            } else if (task != null) {
                task.cancel();
            }
        }
    }
}
