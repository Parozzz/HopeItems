/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.packets;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.NMS.NMSWrapper;
import me.parozzz.reflex.NMS.ReflectionUtil;
import org.bukkit.Location;

/**
 *
 * @author Paros
 */
public class ParticlePacket extends Packet
{
    private final static Class<?> particleEnumClazz;
    private final static Constructor<?> constructor;
    static
    {
        particleEnumClazz = ReflectionUtil.getNMSClass("EnumParticle");
        
        constructor = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutWorldParticles"),
                particleEnumClazz, boolean.class,
                float.class, float.class, float.class, float.class, float.class, float.class, float.class,
                int.class, int[].class);
    }

    public static enum ParticleEnum implements NMSWrapper
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
            nmsObject = Arrays.stream(particleEnumClazz.getEnumConstants())
                    .filter(obj -> obj.toString().equals(name()))
                    .findFirst()
                    .orElse(null);
        }
        
        @Override
        public Object getNMSObject() 
        { 
            return nmsObject; 
        }
    }
    
    private final Object packet;
    public ParticlePacket(final ParticleEnum particle, final Location l, final float xOffset, final float yOffset, final float zOffset, final float speed, final int amount)
    {
        packet = Debug.validateConstructor(constructor, particle.getNMSObject(), true, (float)l.getX(), (float)l.getY(), (float)l.getZ(), xOffset, yOffset, zOffset, speed, amount, null);
    }
    
    public ParticlePacket(final ParticleEnum particle, final Location l, final float speed, final int amount)
    {
        packet = Debug.validateConstructor(constructor, particle.getNMSObject(), true, (float)l.getX(), (float)l.getY(), (float)l.getZ(), 0F, 0F, 0F, speed, amount, null);
    }
    
    @Override
    public Object getNMSObject() 
    {
        return packet;
    }
    
}
