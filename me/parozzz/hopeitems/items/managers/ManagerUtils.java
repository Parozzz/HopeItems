/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import me.parozzz.reflex.*;
import me.parozzz.reflex.NMS.basics.NMSServer;
import me.parozzz.reflex.NMS.entity.EntityPlayer;
import me.parozzz.reflex.NMS.packets.ParticlePacket.ParticleEnum;
import me.parozzz.reflex.NMS.packets.TitlePacket;
import me.parozzz.reflex.NMS.packets.TitlePacket.TitleType;
import me.parozzz.reflex.configuration.MapArray;
import me.parozzz.reflex.classes.SoundManager;
import me.parozzz.reflex.placeholders.Placeholder;
import me.parozzz.reflex.utilities.ParticleUtil;
import me.parozzz.reflex.utilities.ParticleUtil.ParticleEffect;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public class ManagerUtils 
{
    private static final Logger logger = Logger.getLogger(ManagerUtils.class.getSimpleName());
    
    public static Consumer<Location> createExplosion(final MapArray map)
    {
        float power = map.getValue("power", Float::valueOf);
        boolean fire = map.getUpperValue("fire", Boolean::valueOf);
        boolean damage = map.getUpperValue("damage", Boolean::valueOf);
        
        return l -> l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), power, fire, damage);
    }
    
    public static SoundManager getSound(final MapArray map)
    {
        return new SoundManager(map.getValue("name"), map.getValue("volume", Float::valueOf), map.getValue("pitch", Float::valueOf));
    }
    
    public static Consumer<Location> spawnParticle(final MapArray t)
    {
        ParticleEnum particle = Debug.validateEnum(t.getValue("name"), ParticleEnum.class);
        ParticleEffect effect = Debug.validateEnum(t.getValue("effect"), ParticleEffect.class);
        int quantity = t.getValue("quantity", Integer::valueOf);
        
        return l -> ParticleUtil.playParticleEffect(l, particle, effect, quantity);
    }
    
    public static BiConsumer<Player, Location> spawnPlayerParticle(final MapArray map)
    {
        ParticleEnum particle = Debug.validateEnum(map.getValue("name", Function.identity()), ParticleEnum.class);
        ParticleEffect effect = Debug.validateEnum(map.getValue("effect", Function.identity()), ParticleEffect.class);
        int quantity = map.getValue("quantity", Integer::valueOf);

        return (p, l) -> ParticleUtil.playParticleEffect(l, particle, effect, quantity, p);
    }
    
    public static PotionEffect getPotionEffect(final MapArray map)
    {
        PotionEffectType pet = map.getUpperValue("type", PotionEffectType::getByName);
        if(pet == null)
        {
            logger.log(Level.WARNING, "A potion effect named {0} does not exist", map.getValue("type"));
            return null;
        }
        return new PotionEffect(pet, map.getValue("duration", Integer::valueOf)*20, map.getValue("level", Integer::valueOf));
    }
    
    public static Consumer<Location> spawnMobs(final MapArray map)
    {
        EntityType et;
        try {
            et = EntityType.valueOf(map.getValue("type").toUpperCase());
        } catch(final IllegalArgumentException ex) {
            logger.log(Level.WARNING, "An entity type named {0} does not exist in your current version.", map.getValue("type"));
            return l -> {};
        }
        
        int amount = map.getValue("amount", Integer::valueOf);
        return l -> IntStream.range(0, amount).forEach(x -> l.getWorld().spawnEntity(l, et));
    }
    
    public static Title getTitle(final MapArray map)
    {
        Title title=new Title();
        
        title.fadeIn = map.getValue("in", Integer::valueOf);
        title.stay = map.getValue("stay", Integer::valueOf);
        title.fadeOut = map.getValue("out", Integer::valueOf);
        
        title.title = map.hasKey("title") ? new Placeholder(map.getValue("title", Util::cc)).checkLocation().checkPlayer() : null;
        title.subTitle = map.hasKey("subtitle") ? new Placeholder(map.getValue("subtitle", Util::cc)).checkLocation().checkPlayer() : null;
        
        return title;
    }
    
    public static final class Title
    {
        protected Placeholder title;
        protected Placeholder subTitle;
        
        protected int fadeIn;
        protected int stay;
        protected int fadeOut;
        
        public Consumer<Player> getConsumer()
        {
            return p -> 
            {
                EntityPlayer ep = EntityPlayer.getNMSPlayer(p);
                
                Location l = p.getLocation();
                ep.getPlayerConnection().sendPacket(Optional.ofNullable(title)
                        .map(holder -> new TitlePacket(TitleType.TITLE, holder.parse(p, l), fadeIn, stay, fadeOut))
                        .orElseGet(() -> new TitlePacket(TitleType.TITLE, "", fadeIn, stay, fadeOut)));
                
                Optional.ofNullable(subTitle).ifPresent(holder -> ep.getPlayerConnection().sendPacket(new TitlePacket(TitleType.SUBTITLE, holder.parse(p, l), fadeIn, stay, fadeOut)));
            };
        }
        
        public Consumer<Location> getWorldConsumer()
        {
            return l -> 
            {
                NMSServer.getServer().sendAll(Optional.ofNullable(title)
                        .map(holder -> new TitlePacket(TitleType.TITLE, holder.parse(l), fadeIn, stay, fadeOut))
                        .orElseGet(() -> new TitlePacket(TitleType.TITLE, "", fadeIn, stay, fadeOut)));
                Optional.ofNullable(subTitle).ifPresent(holder -> NMSServer.getServer().sendAll(new TitlePacket(TitleType.SUBTITLE, holder.parse(l), fadeIn, stay, fadeOut)));
            };
        }
    }
    
    public static Consumer<Player> teleportPlayer(final MapArray map)
    {
        Location l = new Location(map.getValue("world", Bukkit::getWorld), map.getValue("x", Integer::valueOf), map.getValue("y", Integer::valueOf), map.getValue("z", Integer::valueOf));
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

}
