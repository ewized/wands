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

import static org.spongepowered.api.text.format.TextColors.RED;

import com.ewized.wands.Wands;
import com.ewized.wands.types.Wand;
import com.ewized.wands.types.WandType;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.world.World;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class FireWand implements Wand {
    @Override
    public void onAction(Player player, WandType wand) {
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(RED, "FireWand"));
        Vector3d vector = player.getLocation().getPosition();
        double Θ = player.getHeadRotation().getY() * Math.PI / 180;
        double radius = 0.05;

        final AtomicInteger deg = new AtomicInteger(30);
        final AtomicReference<UUID> id = new AtomicReference<>();
        UUID uuid = Sponge.getScheduler().createAsyncExecutor(Wands.get()).scheduleAtFixedRate(() -> {
            int number = deg.getAndDecrement();
            if (number > 0) {
                particles(player.getLocation().getExtent(), vector, Θ, radius * Math.tan(number));
            } else if (id.get() != null) {
                Sponge.getScheduler().getTaskById(id.get());
            }
        }, 0, 125, TimeUnit.MILLISECONDS).getTask().getUniqueId();
        id.set(uuid);
    }

    private void particles(World world, Vector3d origin, double Θ, double radius) {
        // Sin wave 3 periods
        for (int i = 0; i < 360; i++) {
            double θ = i * Math.PI / 180;
            double y = 3 * Math.sin(2 * θ);
            double x = radius * i;
            double z = 1;

            // Head rotation fix
            double xx = z * Math.cos(Θ) - x * Math.sin(Θ);
            double zz = z * Math.sin(Θ) + x * Math.cos(Θ);

            ParticleEffect effect = ParticleEffect.builder().type(ParticleTypes.FLAME).build();
            world.spawnParticles(effect, origin.clone().add(xx, y, zz));
        }
    }
}
