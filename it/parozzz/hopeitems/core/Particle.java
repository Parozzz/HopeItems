/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.core;

import it.parozzz.hopeitems.HopeItems;
import static it.parozzz.hopeitems.reflection.Packets.getHandle;
import static it.parozzz.hopeitems.reflection.Packets.playerConnection;
import static it.parozzz.hopeitems.reflection.Packets.sendPacket;
import it.parozzz.hopeitems.reflection.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
/**
 *
 * @author Paros
 */

public class Particle
{
    public static enum ParticleEnum 
    {
        EXPLODE_HUGE("EXPLOSION_HUGE"), 
        EXPLODE_LARGE("EXPLOSION_LARGE"),
        EXPLODE("EXPLOSION_NORMAL"),
        BLOCK_CRACK("BLOCK_CRACK"),  
        BLOCK_DUST("BLOCK_DUST"), 
        BUBBLE("WATER_BUBBLE"),
        CLOUD("CLOUD"),
        CRIT("CRIT"),
        CRIT_MAGIC("CRIT_MAGIC"),
        ENCHANT("ENCHANTMENT_TABLE"),
        ENDER("PORTAL"),
        FLAME("FLAME"),
        FIREWORKS("FIREWORKS_SPARK"),
        FOOTSTEP("FOOTSTEP"),
        HAPPY("VILLAGER_HAPPY"),
        HEART("HEART"),
        ITEM_CRACK("ITEM_CRACK"),
        ITEM_TAKE("ITEM_TAKE"),
        SPELL_AMBIENT("SPELL_MOB_AMBIENT"), 
        SPELL_MOB("SPELL_MOB"),
        SPELL_INSTANT("SPELL_INSTANT"),
        SPELL_WITCH("SPELL_WITCH"),
        SPELL("SPELL"), 
        LARGE_SMOKE("SMOKE_LARGE"),
        LAVA_SPARK("LAVA"),
        LAVA("DRIP_LAVA"),
        MOB_APPEARANCE("MOB_APPEARANCE"),
        NOTES("NOTE"),
        REDSTONE_DUST("REDSTONE"),
        SLIME("SLIME"),
        SMOKE("SMOKE_NORMAL"),
        SNOW("SNOW_SHOVEL"),
        SNOWBALL("SNOWBALL"),
        SUSPEND("SUSPENDED"),
        THUNDERCLOUD("VILLAGER_ANGRY"),
        TOWN_AURA("TOWN_AURA"),
        END("SUSPENDED_DEPTH"), 
        WATER_SPLASH("WATER_SPLASH"), 
        WATER("DRIP_WATER"), 
        WATER_DROP("WATER_DROP"), 
        WATER_WAKE("WATER_WAKE");

        private final Object nmsObject;
        private ParticleEnum(String name) 
        {  
            nmsObject = Arrays.stream(ReflectionUtils.getNMSClass("EnumParticle").getEnumConstants())
                    .filter(o -> o.toString().equals(name)).findFirst().orElseGet(() -> null);
        }
        
        public Object getNMSObject() 
        { 
            return nmsObject; 
        }
    }
    
    public static enum ParticleEffect
    {
        CLOUD,EXPLOSION;
    }
    
    private static Constructor<?> PacketPlayOutWorldParticles;
    public static void initialize()
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

    public static void spawn(final Player p, final ParticleEffect type, final ParticleEnum particle, final Location l, final int count) 
    {
        Particle.spawn(Arrays.asList(p), type, particle, l, count);
    }
    
    public static void spawn(final Collection<? extends Player> players, final ParticleEffect type, final ParticleEnum particle, final Location l, final int count) 
    {
        ParticlePacket particles=new ParticlePacket();

        switch(type)
        {
            case CLOUD:
                ThreadLocalRandom random=ThreadLocalRandom.current();
                for(int j=0;j<10;j++)
                {
                    particles.addPacket(particle, l, (float)random.nextDouble(1D),(float)random.nextDouble(1D),(float)random.nextDouble(1D), 0F, count);
                }
                break;
            case EXPLOSION:
                particles.addPacket(particle,l,0F,0F,0F,10F,count);
                break;
        }
       
        new BukkitRunnable() 
        {
            @Override
            public void run() 
            {
                players.forEach(p -> particles.spawn(p));
            }
        }.runTask(HopeItems.getInstance());
    }
    
    private static final class ParticlePacket
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
                Logger.getLogger(Particle.class.getName()).log(Level.SEVERE, null, ex);
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
                    sendPacket(playerConnection(getHandle(p)), o);
                } 
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) 
                {
                    Logger.getLogger(Particle.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }  
}