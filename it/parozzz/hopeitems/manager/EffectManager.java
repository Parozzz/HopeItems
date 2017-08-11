/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.manager;

import it.parozzz.hopeitems.Enum.EffectType;
import it.parozzz.hopeitems.HopeItems;
import it.parozzz.hopeitems.core.ActionBar;
import it.parozzz.hopeitems.core.FireworkManager;
import it.parozzz.hopeitems.core.Particle;
import it.parozzz.hopeitems.core.Particle.ParticleEffect;
import it.parozzz.hopeitems.core.Particle.ParticleEnum;
import it.parozzz.hopeitems.core.Utils;
import it.parozzz.hopeitems.core.Utils.ColorEnum;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class EffectManager 
{

    private final Set<Type> effect=new HashSet<>();
    public EffectManager parseStringList(final List<String> list)
    {
        list.forEach(str -> 
        {
            try { 
                EffectType et=EffectType.getByStarting(str.substring(0, str.indexOf(":"))); 
                str=str.substring(str.indexOf(":")+1);
                switch(et)
                {
                    case THUNDER:  effect.add(new Thunder(str)); break;
                    case WORLDSOUND: effect.add(new WorldSound(str)); break;
                    case FIREWORK: effect.add(new SpawnFirework(str)); break;
                    case EXPLOSION: effect.add(new WorldExplosion(str)); break;
                    case WORLDMESSAGE: effect.add(new WorldMessage(str)); break;
                    case WORLDTITLE: effect.add(new WorldTitle(str)); break;
                    case WORLDACTIONBAR: effect.add(new WorldActionBar(str)); break;
                    case WORLDPARTICLE: effect.add(new WorldParticle(str)); break;
                }
            }
            catch(IndexOutOfBoundsException ex)  {  throw new IllegalArgumentException(str+" is not valid!"); }
        });
        return this;
    }
    
    public void execute(final Location l) { effect.stream().forEach(effect ->  effect.execute(l)); }
    
    private class Thunder implements Type
    {
        private final boolean damage;
        public Thunder(final String damage) 
        { 
            switch(damage.toLowerCase())
            {
                case "damage":
                    this.damage=true;
                    break;
                case "nodamage":
                    this.damage=false;
                    break;
                default:
                    throw new IllegalArgumentException("thunder:"+damage+"in effect is not valid!");
            }
        }
        
        @Override
        public void execute(Location l) 
        {
            if(damage) { l.getWorld().strikeLightning(l); }
            else { l.getWorld().strikeLightningEffect(l); }
        }
        
        @Override
        public EffectType getType() { return EffectType.THUNDER; }
    }
        
    private class WorldSound implements Type
    {
        private final Sound sound;
        private final float volume;
        private final float pitch;
        public WorldSound(final String str)
        {
            String[] array=str.split(";");
            sound=Sound.valueOf(array[0].toUpperCase());
            volume=Float.parseFloat(array[1]);
            pitch=Float.parseFloat(array[2]);
        }

        @Override
        public void execute(Location l) { l.getWorld().playSound(l, sound, volume, pitch); }

        @Override
        public EffectType getType()  { return EffectType.WORLDSOUND; }
    }
    
    private class SpawnFirework implements Type
    {
        private FireworkEffect fe;
        public SpawnFirework(final String str)
        {
            String[] array=str.split(";");
            fe = FireworkEffect.builder()
                    .with(FireworkEffect.Type.valueOf(array[0].toUpperCase()))
                    .withColor(Arrays.stream(array[1].split(",")).map(String::toUpperCase).map(ColorEnum::valueOf).map(ColorEnum::getBukkitColor).collect(Collectors.toList()))
                    .build();
        }

        @Override
        public EffectType getType() { return EffectType.FIREWORK; }

        @Override
        public void execute(Location l) { FireworkManager.spawn(HopeItems.getInstance(), l, fe); }
    }
    
    private class WorldMessage implements Type
    {
        private final String message;
        private double range=-1;
        public WorldMessage(final String str)
        {
            String[] array=str.split(";");
            message=Utils.color(array[0]);
            if(array.length==2) { range=Double.valueOf(array[1]); }
        }
        @Override
        public void execute(Location l) 
        {
            
            if(range==-1) { l.getWorld().getPlayers().forEach(p -> p.sendMessage(message)); }
            else {  l.getWorld().getNearbyEntities(l, range, range, range).stream()
                    .filter(ent -> ent instanceof Player)
                    .map(ent -> (Player)ent)
                    .forEach(p -> p.sendMessage(message));  }
        }
        @Override
        public EffectType getType() { return EffectType.WORLDMESSAGE; }
    }
    
    private class WorldTitle implements Type
    {
        private final String title;
        private final String subtitle;
        private double range=-1;
        public WorldTitle(final String str)
        {
            String[] array=str.split(";");
            title=Utils.color(array[0]);
            subtitle=Utils.color(array[1]);
            if(array.length==3) { range=Double.valueOf(array[2]); }
        }
        @Override
        public void execute(Location l) 
        {
            if(range==-1) { l.getWorld().getPlayers().forEach(p -> Utils.sendTitle(p, title, subtitle)); }
            else {  l.getWorld().getNearbyEntities(l, range, range, range).stream()
                    .filter(ent -> ent instanceof Player)
                    .map(ent -> (Player)ent)
                    .forEach(p -> Utils.sendTitle(p, title, subtitle)); }
        }
        @Override
        public EffectType getType() { return EffectType.WORLDTITLE; }
    }
    
    private class WorldActionBar implements Type
    {
        private final String message;
        private double range=-1;
        public WorldActionBar(final String str)
        {
            String[] array=str.split(";");
            message=Utils.color(array[0]);
            if(array.length==2) { range=Double.valueOf(array[1]); }
        }
        @Override
        public void execute(Location l) 
        {
            if(range==-1) { l.getWorld().getPlayers().forEach(p -> ActionBar.send(p, message)); }
            else {  l.getWorld().getNearbyEntities(l, range, range, range).stream()
                    .filter(ent -> ent instanceof Player)
                    .map(ent -> (Player)ent)
                    .forEach(p -> ActionBar.send(p, message)); }
        }
        @Override
        public EffectType getType() { return EffectType.WORLDACTIONBAR; }
    }
    
    private class WorldParticle implements Type
    {
        private final ParticleEnum pe;
        private final int count;
        public WorldParticle(final String str)
        {
            String[] array=str.split(";");
            pe=ParticleEnum.valueOf(array[0].toUpperCase());
            count=Integer.valueOf(array[1]);
        }
        
        @Override
        public void execute(Location l)  
        {
            Particle.spawn(Bukkit.getOnlinePlayers(),ParticleEffect.CLOUD, pe, l, count); 
        }
        @Override
        public EffectType getType() { return EffectType.WORLDPARTICLE; }  
    }
    
    private class WorldExplosion implements Type
    {
        private final int power;
        private final boolean destroy;
        public WorldExplosion(final String str)
        {
            String[] array=str.split(";");
            power=Integer.valueOf(array[0]);
            switch(array[1].toLowerCase())
            {
                case "destroy": destroy=true; break;
                case "nodestroy": destroy=false; break;
                default: throw new IllegalArgumentException("explosion: "+array[1]+"in effect is not valid!");
            }
        }
        
        @Override
        public void execute(Location l) { l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), power, false, destroy); }

        @Override
        public EffectType getType() { return EffectType.EXPLOSION; }
        
    }
    
    private interface Type{
        
        void execute(final Location l);
        EffectType getType();
    }
}
