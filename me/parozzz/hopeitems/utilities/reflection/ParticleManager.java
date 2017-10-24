/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import me.parozzz.hopeitems.utilities.Sphere;
import me.parozzz.hopeitems.utilities.reflection.API.ReflectionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */

public class ParticleManager
{
    public static enum ParticleEnum 
    {
        //From 1.8
        BARRIER,
        EXPLOSION_HUGE, 
        EXPLOSION_LARGE,
        EXPOSION_NORMAL,
        BLOCK_CRACK,  
        BLOCK_DUST, 
        CLOUD,
        CRIT,
        CRIT_MAGIC,
        ENCHANTMENT_TABLE,
        PORTAL,
        FLAME,
        FIREWORKS_SPARK,
        FOOTSTEP,
        VILLAGER_HAPPY,
        VILLAGER_ANGRY,
        HEART,
        ITEM_CRACK,
        ITEM_TAKE,
        SPELL_MOB_AMBIENT, 
        SPELL_MOB,
        SPELL_INSTANT,
        SPELL_WITCH,
        SPELL, 
        SMOKE_LARGE,
        LAVA,
        DRIP_LAVA,
        MOB_APPEARANCE,
        NOTE,
        REDSTONE,
        SLIME,
        SMOKE_NORMAL,
        SNOW_SHOVEL,
        SNOWBALL,
        SUSPENDED,
        TOWN_AURA,
        SUSPENDED_DEPTH, 
        WATER_BUBBLE,
        WATER_SPLASH, 
        DRIP_WATER, 
        WATER_DROP, 
        WATER_WAKE,
        //Da 1.9
        DAMAGE_INDICATOR,
        DRAGON_BREATH,
        END_ROD,
        SWEEP_ATTACK,
        //Da 1.10
        FALLING_DUST,
        //Da 1.11
        SPIT,
        TOTEM;

        private final Object nmsObject;
        private ParticleEnum() 
        {  
            nmsObject = Arrays.stream(ReflectionUtils.getNMSClass("EnumParticle").getEnumConstants())
                    .filter(o -> o.toString().equals(name())).findFirst().orElseGet(() -> null);
        }
        
        public Object getNMSObject() 
        { 
            return nmsObject; 
        }
    }
    
    public static enum ParticleEffect
    {
        CLOUD, EXPLOSION, SPHERE, STATIC;
    }
    
    private Constructor<?> PacketPlayOutWorldParticles;
    protected ParticleManager()
    {
        try 
        {
            PacketPlayOutWorldParticles=ReflectionUtils.getNMSClass("PacketPlayOutWorldParticles")
                    .getConstructor(ReflectionUtils.getNMSClass("EnumParticle"),
                            boolean.class,
                            float.class,float.class,float.class,float.class,float.class,float.class,float.class,
                            int.class, int[].class);
        } 
        catch (NoSuchMethodException | SecurityException ex) 
        { 
            ex.printStackTrace();
        }
    }

    public void spawn(final Player p, final ParticleEffect type, final ParticleEnum particle, final Location l, final int count) 
    {
        spawn(Arrays.asList(p), type, particle, l, count);
    }
    
    public void spawn(final Collection<? extends Player> players, final ParticleEffect type, final ParticleEnum particle, final Location l, final int count) 
    {
        ParticlePacket particles=new ParticlePacket();

        switch(type)
        {
            case STATIC:
                particles.addPacket(particle, l, 0F, 0F, 0F, 0F, count);
                break;
            case CLOUD:
                ThreadLocalRandom random=ThreadLocalRandom.current();
                IntStream.of(10).forEach(i -> particles.addPacket(particle, l, (float)random.nextDouble(1D),(float)random.nextDouble(1D),(float)random.nextDouble(1D), 0F, count));
                break;
            case EXPLOSION:
                particles.addPacket(particle,l,0F,0F,0F,10F,count);
                break;
            case SPHERE:
                Sphere.generateSphere(l, count, false).forEach(sl -> particles.addPacket(particle, sl, 0F, 0F, 0F, 0, 1));
                break;
        }
        players.forEach(p -> particles.spawn(p));
    }
    
    private final class ParticlePacket
    {
        private final Set<Object> packets;
        public ParticlePacket()
        {
            packets=new HashSet<>();
        }
        
        public ParticlePacket(final ParticleEnum particle, final Location l, final float xoff, final float yoff, final float zoff, final float speed, final int count)
        {
            packets=new HashSet<>();
            addPacket(particle, l, xoff, yoff, zoff, speed, count);
        }
        
        /**
         * 
         * @param particle - The type of particle to be played in
         * @param l - The location of the particle
         * @param xoff - The X OffSet
         * @param yoff - The Y OffSet
         * @param zoff - The Z OffSet
         * @param speed - The Particles speed
         * @param count - The Particles count
         */
        public void addPacket(final ParticleEnum particle, final Location l, final float xoff, final float yoff, final float zoff, final float speed, final int count)
        {
            try 
            {
                packets.add(PacketPlayOutWorldParticles.newInstance(particle.getNMSObject(), true, (float)l.getX(), (float)l.getY(), (float)l.getZ(),
                        xoff,yoff,zoff,
                        speed,count,null));
            } 
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) 
            {
                Logger.getLogger(ParticleManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public Set<Object> getNMSObjects()
        {
            return packets;
        }
        
        public void spawn(final Player p)
        {
            packets.forEach(o -> 
            {
                try 
                {
                    API.getPacketManager().sendPacket(p, o);
                } 
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) 
                {
                    Logger.getLogger(ParticleManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }  
}