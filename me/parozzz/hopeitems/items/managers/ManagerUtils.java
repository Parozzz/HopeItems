/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.Debug;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.classes.MapArray;
import me.parozzz.hopeitems.utilities.placeholders.Placeholder;
import me.parozzz.hopeitems.utilities.reflection.API;
import me.parozzz.hopeitems.utilities.reflection.ParticleManager;
import me.parozzz.hopeitems.utilities.reflection.ParticleManager.ParticleEffect;
import me.parozzz.hopeitems.utilities.reflection.ParticleManager.ParticleEnum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public class ManagerUtils 
{
    public static Consumer<Location> createExplosion(final String[] t)
    {
        Object[] o=new Object[3];
        Stream.of(t).map(s -> s.split(":")).forEach(a -> 
        {
            switch(a[0].toLowerCase())
            {
                case "power":
                    o[0]=Float.valueOf(a[1]);
                    break;
                case "fire":
                    o[1]=Boolean.valueOf(a[1]);
                    break;
                case "damage":
                    o[2]=Boolean.valueOf(a[1]);
                    break;
            }
        });
        return l -> l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), (float)o[0], (boolean)o[1], (boolean)o[2]);
    }
    
    public static Sound getSound(final String[] t)
    {
        Sound sound=new Sound();
        Stream.of(t).map(s -> s.split(":")).forEach(a -> 
        {
            switch(a[0].toLowerCase())
            {
                case "name":
                    sound.setSound(a[1]);
                    break;
                case "volume":
                    sound.setVolume(Float.valueOf(a[1]));
                    break;
                case "pitch":
                    sound.setPitch(Float.valueOf(a[1]));
                    break;
            }
        });
        return sound;
    }
    
    public final static class Sound
    {
        public org.bukkit.Sound sound;
        public void setSound(final String s)
        {
            try { sound=org.bukkit.Sound.valueOf(s.toUpperCase()); }
            catch(final IllegalArgumentException ex) { throw new IllegalArgumentException("A sound named "+s+" does not exist"); } 
        }
        
        public float volume=1F;
        public void setVolume(final float f)
        {
            volume=f;
        }
        
        public float pitch=1F;
        public void setPitch(final float f)
        {
            pitch=f;
        }
        
        public void playSound(final Location l, final Player p)
        {
            p.playSound(l, sound, volume, pitch);
        }
        
        public void playSound(final Location l)
        {
            l.getWorld().playSound(l, sound, volume, pitch);
        }
    }
    
    public static Consumer<Location> spawnParticle(final String[] t)
    {
        Object o[]=new Object[3];
        Stream.of(t).map(s -> s.split(":")).forEach(a -> 
        {
            switch(a[0].toLowerCase())
            {
                case "name":
                    try { o[0]=ParticleManager.ParticleEnum.valueOf(a[1].toUpperCase()); }
                    catch(IllegalArgumentException ex) { throw new IllegalArgumentException("A particle named "+a[1]+" does not exist"); }
                    break;
                case "effect":
                    try { o[1]=ParticleManager.ParticleEffect.valueOf(a[1].toUpperCase()); }
                    catch(IllegalArgumentException ex) { throw new IllegalArgumentException("A particle effect named "+a[1]+" does not exit"); }
                    break;
                case "quantity":
                    o[2]=Integer.valueOf(a[1]);
                    break;
            }
        });
        return l -> API.getParticleManager().spawn(l.getWorld().getPlayers(), (ParticleManager.ParticleEffect)o[1], (ParticleManager.ParticleEnum)o[0], l, (int)o[2]);
    }
    
    public static BiConsumer<Player, Location> spawnPlayerParticle(final MapArray map)
    {
        Object o[]=new Object[3];
        
        ParticleEnum particle = Debug.validateEnum(map.getValue("name", Function.identity()), ParticleEnum.class);
        ParticleEffect effect = Debug.validateEnum(map.getValue("effect", Function.identity()), ParticleEffect.class);
        int quantity = map.getValue("quantity", Integer::valueOf);

        return (p, l) -> API.getParticleManager().spawn(p, effect, particle, l, quantity);
    }
    
    public static PotionEffect getPotionEffect(final MapArray map)
    {
        return new PotionEffect(map.getUpperValue("type", PotionEffectType::getByName), map.getValue("duration", Integer::valueOf)*20, map.getValue("level", Integer::valueOf));
    }
    
    public static Title getTitle(final MapArray map)
    {
        Title title=new Title();
        map.getKeys().forEach((key, value) -> 
        {
            switch(key)
            {
                case "title":
                    title.title=Utils.color(value);
                    break;
                case "subtitle":
                    title.subTitle=Utils.color(value);
                    break;
                case "in":
                    title.fadeIn=Integer.valueOf(value);
                    break;
                case "stay":
                    title.stay=Integer.valueOf(value);
                    break;
                case "out":
                    title.fadeOut=Integer.valueOf(value);
                    break;
            }
        });
        return title;
    }
    
    public static final class Title
    {
        public String title="";
        public String subTitle="";
        public int fadeIn=20;
        public int stay=60;
        public int fadeOut=20;
        
        public Consumer<Player> getConsumer()
        {
            Placeholder title=new Placeholder(this.title).checkLocation().checkPlayer();
            Placeholder subTitle=new Placeholder(this.subTitle).checkLocation().checkPlayer();
            return p -> API.getTitle().sendTitleAndSubTitle(title.parse(p, p.getLocation()), subTitle.parse(p, p.getLocation()), p, fadeIn, stay, fadeOut);
        }
        
        public Consumer<Location> getWorldConsumer()
        {
            Placeholder title=new Placeholder(this.title).checkLocation();
            Placeholder subTitle=new Placeholder(this.subTitle).checkLocation();
            return l -> l.getWorld().getPlayers().forEach(p -> API.getTitle().sendTitleAndSubTitle(title.parse(l), subTitle.parse(l), p, fadeIn, stay, fadeOut));
        }
    }
    
    public static Consumer<Player> teleportPlayer(final MapArray map)
    {
        Location l=new Location(map.getValue("world", Bukkit::getWorld), map.getValue("x", Integer::valueOf), map.getValue("y", Integer::valueOf), map.getValue("z", Integer::valueOf));
        return p -> p.teleport(l);
    }
            
    public static <T extends Number, H> Consumer<H> getNumberFunction(final String value, final Function<Double, T> convert, final BiConsumer<H, T> biConsumer)
    {
        if(value.contains("~"))
        {
            List<Double> list=Stream.of(value.split("~")).map(Double::valueOf).collect(Collectors.toList());
            
            double min=list.get(0);
            double max=list.get(1);
            
            return h -> biConsumer.accept(h, convert.apply(ThreadLocalRandom.current().nextDouble(min, max)));
        }
        else
        {
            T t=convert.apply(Double.valueOf(value));
            return h -> biConsumer.accept(h, t);
        }
    }
    
    public static <T extends Comparable> Predicate<T> getComparison(final String value, final Function<String, T> convert)
    {
        T t;
        if(value.contains("~"))
        {
            List<T> list=Stream.of(value.split("~")).map(convert).collect(Collectors.toList());
            
            T min=list.get(0);
            T max=list.get(1);
            return v -> v.compareTo(min)==1 && v.compareTo(max)==-1;
        }
        else if(value.startsWith(">"))
        {
            t=convert.apply(value.replace(">", ""));
            return v -> v.compareTo(t)==1;
        }
        else if(value.startsWith("<"))
        {
            t=convert.apply(value.replace("<", ""));
            return v -> v.compareTo(t)==-1;
        }
        else
        {
            t=convert.apply(value);
            return v -> v==t;
        }
    }
    
    public static Stream<String[]> splitArray(final String[] array)
    {
        return Stream.of(array).map(s -> s.split(":"));
    }
}
