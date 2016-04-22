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

import com.ewized.wands.types.Wand;
import com.ewized.wands.types.WandType;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

public class IceWand implements Wand {
    @Override
    public void onAction(Player player, WandType wand) {
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.WHITE, "Ice Wand"));
        Vector3d vector = player.getLocation().getPosition().add(0, 1.65, 0);
        Vector3d pos = player.getHeadRotation();//.normalize();
        double t = pos.getY() * Math.PI/180;


        double o = 0.1;
        double r = 0.1;
        double rr = 0.01;
        for (int i = 0; i < 360 * 2; i++) {
            double theta = i * Math.PI / 180;
            double y = r * Math.cos(theta) * Math.cos(pos.getY());
            double z = r * Math.sin(theta) * Math.cos(pos.getY());
            double yy = rr * Math.cos(theta) * Math.sin(pos.getY());
            double zz = rr * Math.sin(theta) * Math.sin(pos.getY());
            rr += 0.00125;
            r += 0.0654;

            double xxx = z * Math.cos(t) - y * Math.sin(t);
            double zzz = z * Math.sin(t) + y * Math.cos(t);


            double xxxx = zz * Math.cos(t) - yy * Math.sin(t);
            double zzzz = zz * Math.sin(t) + yy * Math.cos(t);
            ParticleEffect effect = ParticleEffect.builder()
                    .type(ParticleTypes.SNOW_SHOVEL)
                    .build();
            ParticleEffect effect2 = ParticleEffect.builder()
                    .type(ParticleTypes.SMOKE_NORMAL)
                    .build();


            player.getLocation().getExtent().spawnParticles(effect, vector.clone().add(xxxx,o += 0.0125, zzzz));
            player.getLocation().getExtent().spawnParticles(effect2, vector.clone().add(xxx, o += 0.0879, zzz));
        }
    }
}
