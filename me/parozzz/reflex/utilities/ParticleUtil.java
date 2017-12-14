/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.utilities;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import me.parozzz.reflex.NMS.basics.NMSServer;
import me.parozzz.reflex.NMS.entity.EntityPlayer;
import me.parozzz.reflex.NMS.packets.ParticlePacket;
import me.parozzz.reflex.NMS.packets.ParticlePacket.ParticleEnum;
import me.parozzz.reflex.Sphere;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class ParticleUtil 
{
    public enum ParticleEffect
    {
        CLOUD, SPHERE, STATIC, EXPLOSION;
    }
    
    public static void playParticleEffect(final Location l, final ParticleEnum particle, final ParticleEffect effect, final int data, final Player... players)
    {
        Consumer<Stream<ParticlePacket>> sendPackets = players.length == 0 ? 
                sendPackets = stream -> stream.forEach(NMSServer.getServer()::sendAll) : 
                stream -> Stream.of(players).map(EntityPlayer::getNMSPlayer).map(EntityPlayer::getPlayerConnection).forEach(pc -> stream.forEach(pc::sendPacket));
        
        switch(effect)
        {
            case CLOUD:
                sendPackets.accept(IntStream.range(0, data).mapToObj(i -> new ParticlePacket(particle, l, (float)ThreadLocalRandom.current().nextDouble(2D), (float)ThreadLocalRandom.current().nextDouble(2D), (float)ThreadLocalRandom.current().nextDouble(2D), 0F, 10)));
                break;
            case SPHERE:
                sendPackets.accept(Sphere.generateSphere(l, data, false).stream().map(sphereLoc -> new ParticlePacket(particle, sphereLoc, 0F, data)));
                break;
           case STATIC:
                sendPackets.accept(Stream.of(new ParticlePacket(particle, l, 0F, data)));
                break;
            case EXPLOSION:
                sendPackets.accept(Stream.of(new ParticlePacket(particle, l, 2F, data)));
                break;
        }
    }
}
