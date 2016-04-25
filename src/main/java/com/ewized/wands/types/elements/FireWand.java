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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
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

public class FireWand implements Wand {
    @Override
    public Text name(CommandSource src) {
        return Text.of(TextColors.RED, Messages.WAND_FIRE_NAME.get(src));
    }

    @Override
    public void onAction(Player player, WandType wand) {
        player.sendMessage(ChatTypes.ACTION_BAR, name(player));
        Vector3d vector = player.getLocation().getPosition();
        double Θ = player.getHeadRotation().getY() * Math.PI / 180;

        final AtomicInteger deg = new AtomicInteger(0);
        final AtomicReference<UUID> id = new AtomicReference<>();
        UUID uuid = Sponge.getScheduler().createAsyncExecutor(Wands.get()).scheduleAtFixedRate(() -> {
            int number = deg.getAndIncrement();
            if (number < 360) {
                particles(player.getLocation().getExtent(), vector, Θ, number + 0.25);
                particles(player.getLocation().getExtent(), vector, Θ, number + 0.125);
                particles(player.getLocation().getExtent(), vector, Θ, number);
                particles(player.getLocation().getExtent(), vector, Θ, number - 0.125);
                particles(player.getLocation().getExtent(), vector, Θ, number - 0.25);
            } else if (id.get() != null) {
                Sponge.getScheduler().getTaskById(id.get());
            }
        }, 0, 125, TimeUnit.MILLISECONDS).getTask().getUniqueId();
        id.set(uuid);
    }

    private void particles(World world, Vector3d origin, double Θ, double x) {
        double θ = x * Math.PI / 180;
        double y = 2 * Math.sin(24 * θ);

        // Head rotation fix
        double xx = Math.cos(Θ) - x * Math.sin(Θ);
        double zz = Math.sin(Θ) + x * Math.cos(Θ);

        ParticleEffect effect = ParticleEffect.builder().type(ParticleTypes.FLAME).count(5).build();
        world.spawnParticles(effect, origin.clone().add(xx, y, zz));
    }
}
