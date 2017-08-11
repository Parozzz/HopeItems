/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.manager;

import it.parozzz.hopeitems.Enum.PlayerType;
import it.parozzz.hopeitems.core.ActionBar;
import it.parozzz.hopeitems.core.Dependency;
import it.parozzz.hopeitems.core.ItemBuilder;
import it.parozzz.hopeitems.core.Particle;
import it.parozzz.hopeitems.core.Particle.ParticleEffect;
import it.parozzz.hopeitems.core.Utils;
import it.parozzz.hopeitems.reflection.Packets;
import it.parozzz.hopeitems.reflection.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public class PlayerManager {

    private final Set<Type> player=new HashSet<>();
    public PlayerManager parseStringList(final List<String> list)
    {
        list.forEach(str -> 
        {
            PlayerType pt;
            if(!str.contains(":")) 
            {
                pt=PlayerType.getByStarting(str); 
            }
            else 
            { 
                pt=PlayerType.getByStarting(str.substring(0,str.indexOf(":"))); 
            }
            str=str.substring(str.indexOf(":")+1);
            switch(pt)
            {
                case HEAL: player.add(new Heal(str)); break;
                case DAMAGE: player.add(new Damage(str)); break;
                case FOOD: player.add(new Food(str)); break;
                case SATURATION: player.add(new Saturation(str)); break;
                case EXP: player.add(new Exp(str)); break;
                case LEVEL: player.add(new Level(str)); break;
                case FIRE: player.add(new Fire(str)); break;
                case POTION: player.add(new Potion(str)); break;
                case CURE: player.add(new Cure(str)); break;
                case SOUND: player.add(new PlayerSound(str)); break;
                case MESSAGE: player.add(new Message(str)); break;
                case ACTIONBAR: player.add(new PlayerActionBar(str)); break;
                case TITLE: player.add(new Title(str)); break;
                case MONEY: player.add(new Money(str)); break;
                case ITEM: player.add(new GiveItem(str)); break;
                case PARTICLE: player.add(new PlayerParticle(str)); break;
                case THUNDER: player.add(new PlayerThunder(str)); break;
                case TELEPORT: player.add(new PlayerTeleport()); break;
            }
        });
        return this;
    }
    
    public void execute(final Player p, final Location l) 
    {
        player.forEach(type -> type.execute(p,l)); 
    }
    
    private class Heal implements Type
    {
        private final double heal;
        public Heal(final String str){ heal=Double.parseDouble(str); }
        
        @Override
        public PlayerType getType() { return PlayerType.FOOD; }

        @Override
        public void execute(Player p,Location l) 
        {
            
            Double maxHealth=Utils.getMaxHealh(p);
            p.setHealth(p.getHealth()+heal>=maxHealth?maxHealth:p.getHealth()+heal);
        }
    }
    
    private class Damage implements Type
    {
        private final double damage;
        public Damage(final String str) { damage=Double.parseDouble(str); }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.FOOD; 
        }

        @Override
        public void execute(Player p,Location l) 
        {
            p.damage(damage); 
        }
    }
    
    private class Saturation implements Type
    {
        private final float saturation;
        public Saturation(final String str) 
        { 
            saturation=Float.parseFloat(str); 
        }
        
        @Override
        public PlayerType getType() 
        { 
            return PlayerType.FOOD; 
        }

        @Override
        public void execute(Player p,Location l) 
        {
            p.setSaturation(p.getSaturation()+saturation); 
        }
    }
    
     private class Food implements Type
    {
        private final int food;
        public Food(final String str) 
        {
            food=Integer.parseInt(str); 
        }
        
        @Override
        public PlayerType getType() 
        { 
            return PlayerType.FOOD; 
        }

        @Override
        public void execute(Player p,Location l) 
        {
            p.setFoodLevel(p.getFoodLevel()+food); 
        }
    }
     
    private class Exp implements Type
    {
        private final int exp;
        public Exp(final String str) 
        { 
            exp=Integer.parseInt(str); 
        }
        
        @Override
        public PlayerType getType() 
        { 
            return PlayerType.FOOD; 
        }

        @Override
        public void execute(Player p,Location l) 
        { 
            ((ExperienceOrb)p.getWorld().spawnEntity(p.getLocation(), EntityType.EXPERIENCE_ORB)).setExperience(exp); 
        }
    }
    
    private class Level implements Type
    {
        private final int level;
        public Level(final String str) 
        { 
            level=Integer.parseInt(str); 
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.FOOD; 
        }

        @Override
        public void execute(Player p,Location l) 
        { 
            p.setLevel(p.getLevel()+level); 
        }
    }
    
    private class Fire implements Type
    {
        private final int fire;
        public Fire(final String str) 
        {
            fire=Integer.parseInt(str); 
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.FOOD; 
        }

        @Override
        public void execute(Player p,Location l) 
        { 
            p.setFireTicks(fire); 
        }
    }
    
    private class Potion implements Type
    {
        private final PotionEffect pe;
        public Potion(final String str)
        {
            String[] array=str.split(";");
            pe=new PotionEffect(PotionEffectType.getByName(array[0].toUpperCase()),Integer.parseInt(array[1])*20,Integer.parseInt(array[2])-1);
        }

        @Override
        public void execute(Player p,Location l) 
        { 
            p.addPotionEffect(pe, true); 
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.POTION;
        }
    }   
    
    private class Cure implements Type
    {
        private boolean all=false;
        private PotionEffectType pet;
        public Cure(final String str)
        {
            if(str.equalsIgnoreCase("all")) { all=true; }
            else if(PotionEffectType.getByName(str.toUpperCase())==null) { Bukkit.getLogger().info("PotionEffectType wrong in cure setup"); }
            else { pet=PotionEffectType.getByName(str.toUpperCase()); }
        }
        
        @Override
        public void execute(Player p, Location l) 
        {
            if(all) { p.getActivePotionEffects().forEach(pe -> p.removePotionEffect(pe.getType())); }
            else { p.getActivePotionEffects().stream().filter(pe -> pe.getType()==pet).forEach(pe -> p.removePotionEffect(pe.getType())); }
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.CURE; 
        }
        
    }
    
    private class PlayerSound implements Type
    {
        private final Sound sound;
        private final float volume;
        private final float pitch;
        public PlayerSound(final String str)
        {
            String[] array=str.split(";");
            sound=Sound.valueOf(array[0].toUpperCase());
            volume=Float.parseFloat(array[1]);
            pitch=Float.parseFloat(array[2]);
        }
        
        @Override
        public void execute(Player p,Location l) 
        {
            p.playSound(l, sound, volume, pitch); 
        }

        @Override
        public PlayerType getType() 
        {
            return PlayerType.SOUND; 
        }
    }
    
    private class Message implements Type
    {
        private final String message;
        public Message(final String str) 
        {
            message=Utils.color(str); 
        }
        
        @Override
        public void execute(Player p,Location l) 
        { 
            p.sendMessage(message); 
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.MESSAGE; 
        }
    }
    
    private class PlayerActionBar implements Type
    {
        private final String message;
        public PlayerActionBar(final String str) 
        { 
            message=Utils.color(str); 
        }
       
        @Override
        public void execute(Player p,Location l) 
        {
            ActionBar.send(p, message); 
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.MESSAGE; 
        }
    }
    
    private class Title implements Type
    {
        private String title=new String();
        private String subtitle=new String();
        public Title(final String str) 
        { 
            String[] array=str.split(";");
            
            if(!array[0].isEmpty()) { title=Utils.color(array[0]); }
            if(array.length==2) { subtitle=Utils.color(array[1]); }
        }
        
        @Override
        public void execute(Player p,Location l)  
        { 
            Utils.sendTitle(p, title, subtitle); 
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.MESSAGE; 
        }
    }
    
    private class GiveItem implements Type
    {
        private final ItemStack item;
        public GiveItem(final String str)
        {
            String[] array=str.split(";");
            item=new ItemBuilder(Material.valueOf(array[0].toUpperCase()))
                    .setAmount(array.length>1?Integer.valueOf(array[1]):1)
                    .setName(array.length>2?Utils.color(array[2]):null)
                    .setLore(array.length>3?Arrays.stream(array).skip(3).map(Utils::color).collect(Collectors.toList()):null)
                    .build();
        }
        @Override
        public void execute(Player p,Location l) 
        {
            p.getInventory().addItem(item.clone()); 
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.ITEM; 
        }
        
    }
    
    private class Money implements Type
    {
        private final double money;
        public Money(final String str) 
        { 
            if(!Dependency.isEconomyHooked()) 
            {
                throw new UnknownDependencyException("Vault needed for give money features!");  
            }
            money=Double.valueOf(str); 
        }
        @Override
        public void execute(Player p,Location l) 
        { 
            
            if(money>0) 
            { 
                Dependency.getEconomy().depositPlayer(p, money); 
            }
            else if(money<0) 
            { 
                Dependency.getEconomy().withdrawPlayer(p, money); 
            }
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.ITEM; 
        }
        
    }
    
    private class PlayerParticle implements Type
    {
        private final it.parozzz.hopeitems.core.Particle.ParticleEnum pe;
        private final int count;
        public PlayerParticle(final String str)
        {
            String[] array=str.split(";");
            pe=it.parozzz.hopeitems.core.Particle.ParticleEnum.valueOf(array[0].toUpperCase());
            count=Integer.valueOf(array[1]);
        }
        
        @Override
        public void execute(Player p,Location l) 
        { 
            Particle.spawn(Bukkit.getOnlinePlayers(), ParticleEffect.CLOUD, pe, l, count); 
        }

        @Override
        public PlayerType getType() 
        {
            return PlayerType.PARTICLE; 
        }
    }
    
    private class PlayerThunder implements Type
    {
        private Constructor<?> PacketPlayOutSpawnEntityWeather;
        private Constructor<?> EntityLightning;
        private final boolean effect;
        public PlayerThunder(final String str)
        {
            switch(str.toLowerCase())
            {
                case "damage": effect=false; break;
                case "nodamage": effect=true; break;
                default: throw new IllegalArgumentException("Error in a player thunder definition!");
            }
            try 
            {
                EntityLightning=ReflectionUtils.getNMSClass("EntityLightning")
                        .getConstructor(ReflectionUtils.getNMSClass("World"),double.class,double.class,double.class,boolean.class);
                PacketPlayOutSpawnEntityWeather=ReflectionUtils.getNMSClass("PacketPlayOutSpawnEntityWeather")
                        .getConstructor(ReflectionUtils.getNMSClass("Entity"));
            } 
            catch (NoSuchMethodException | SecurityException ex) 
            { 
                ex.printStackTrace(); 
            }
        }
        
        @Override
        public void execute(Player p, Location l) 
        {
            try 
            {
                Object handle=Packets.getHandle(p);
                Packets.sendPacket(Packets.playerConnection(handle),
                        PacketPlayOutSpawnEntityWeather.newInstance(
                                EntityLightning.newInstance(Packets.getWorld(handle),l.getX(),l.getY(),l.getZ(),effect)));
                
                if(Utils.bukkitVersion("1.8")) { p.playSound(l, "AMBIENCE_THUNDER", 1F, 1F); } 
                else { p.playSound(l, Sound.ENTITY_LIGHTNING_IMPACT , 1F, 1F); }
                
            } 
            catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException ex)  
            {
                ex.printStackTrace(); 
            }
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.THUNDER; 
        }
    
    }
    
    private class PlayerTeleport implements Type
    {
        @Override
        public void execute(Player p, Location l) 
        {
            p.teleport(l); 
        }

        @Override
        public PlayerType getType() 
        { 
            return PlayerType.TELEPORT; 
        }
    }
    
    private interface Type
    {
        void execute(final Player p, final Location l);
        PlayerType getType();
    }
}
