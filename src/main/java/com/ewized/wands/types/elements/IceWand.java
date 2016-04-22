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
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class IceWand implements Wand {
    @Override
    public void onAction(Player player, WandType wand) {
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.WHITE, "Ice Wand"));
        Vector3d vector = player.getLocation().getPosition().add(0, -1.65, 0);

        final AtomicInteger deg = new AtomicInteger(240);
        final AtomicReference<UUID> id = new AtomicReference<>();
        UUID uuid = Sponge.getScheduler().createAsyncExecutor(Wands.get()).scheduleAtFixedRate(() -> {
            int number = deg.getAndDecrement();
            if (number > 0) {
                particles(player.getLocation().getExtent(), vector, number * Math.PI / 180);
            } else if (id.get() != null) {
                Sponge.getScheduler().getTaskById(id.get());
            }
        }, 0, 1, TimeUnit.MILLISECONDS).getTask().getUniqueId();
        id.set(uuid);
    }

    /** Show the particles for a cool effect */
    private void particles(World world, Vector3d origin, double Θ) {
        double o = 0.1;
        double r = 0.01;
        double rr = 0.001;
        for (int i = 0; i < 360 * 2; i++) {
            double θ = i * Math.PI / 180;
            double y = r * Math.cos(θ) * Math.cos(Θ);
            double z = r * Math.sin(θ) * Math.cos(Θ);
            double yy = -rr * Math.cos(θ);
            double zz = -rr * Math.sin(θ);
            rr += 0.0125;
            r += 0.0654;

            double xxx = z * Math.cos(Θ) - y * Math.sin(Θ);
            double zzz = z * Math.sin(Θ) + y * Math.cos(Θ);

            double xxxx = zz * Math.cos(Θ) - yy * Math.sin(Θ);
            double zzzz = zz * Math.sin(Θ) + yy * Math.cos(Θ);

            ParticleEffect effect = ParticleEffect.builder().type(ParticleTypes.SNOW_SHOVEL).build();
            ParticleEffect effect2 = ParticleEffect.builder().type(ParticleTypes.SMOKE_NORMAL).build();

            world.spawnParticles(effect, origin.clone().add(xxxx, o += 0.00125, zzzz));
            world.spawnParticles(effect2, origin.clone().add(xxx, o += 0.0576, zzz));
        }
    }
}
