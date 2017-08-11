/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.manager;

import it.parozzz.hopeitems.Enum.CreeperAction;
import it.parozzz.hopeitems.HopeItems;
import it.parozzz.hopeitems.Value;
import it.parozzz.hopeitems.core.Utils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public class ExplosiveManager 
{

    private EntityType et;
    private String name;
    private boolean charged;
    private double health;
    
    private int power;
    private boolean fire=false;
    
    private EffectManager effect;
    
    private final Set<Type> action=new HashSet<>();
    private final Set<Meta> meta=new HashSet<>();
    public ExplosiveManager parse(final ConfigurationSection path)
    {
        if(path==null) { return this; }
        
        switch(path.getString("type").toLowerCase())
        {
            case "creeper": et=EntityType.CREEPER; break;
            case "tnt": et=EntityType.PRIMED_TNT; break;
            case "fireball": et=EntityType.FIREBALL; break;
            default: throw new IllegalArgumentException("You need to use either creeper or tnt in CustomExplosive");
        }
        
        name=Utils.color(path.getString("name",""));
        health=path.getDouble("health",-1);
        charged=path.getBoolean("charged",false);
        power=path.getInt("power",-1);
        
        path.getStringList("action").forEach(str -> 
        {
            if(!str.contains(":") && CreeperAction.getByStarting(str).equals(CreeperAction.FRIENDLY))
            {
                meta.add(new Friendly(HopeItems.getInstance()));
                return;
            }
            
            try
            {
                CreeperAction ca=CreeperAction.getByStarting(str.substring(0,str.indexOf(":")));
                str=str.substring(str.indexOf(":")+1);
                switch(ca)
                {
                    case ENDER:  action.add(new Ender(str)); break;
                    case STACKER: action.add(new Stacker(str)); break;
                    case POTION: action.add(new Potion(str)); break;
                    case FIRE:
                        action.add(new Fire(str));
                        fire=true;
                        break;
                    case TRANSMUTATION: action.add(new Transmutation(str)); break;
                    case SPAWNER: action.add(new Spawner(str)); break;
                    case THUNDER: action.add(new Thunder(str)); break;
                }
            }
            catch(IndexOutOfBoundsException ex) { throw new IllegalArgumentException(str+" is not valid!"); }
        });
        
        effect=new EffectManager().parseStringList(path.getStringList("effect"));
        
        return this;
    }
    
    /**
     * @return The new Prime Redius. -1 if not set.
     */
    public int getPower() { return power; }
    
    public boolean getFire() { return fire; }
    
    public void spawn(final Location l)
    {
        if(et==null) { return; }
        if(et==EntityType.FIREBALL) { l.add(l.add(0, 1, 0).getDirection().normalize().multiply(2)); }
        Entity explosive=l.getWorld().spawnEntity(l, et);
        
        if(explosive instanceof Creeper)
        {
            if(health!=-1) { Utils.setMaxHealth((Creeper)explosive, health); }
            ((Creeper)explosive).setPowered(charged);
        }
        
        if(!name.isEmpty()) { Utils.setName(explosive, name); }
        meta.forEach(meta -> meta.add(explosive));
        explosive.setMetadata(Value.CustomExplosiveMetadata, new FixedMetadataValue(HopeItems.getInstance(),this));
    }
    
    public void onExplode(final Location l)
    {
        action.forEach(type -> type.execute(l));
        effect.execute(l);
    }
    
    private class Ender implements Type
    {
        private final double range;
        public Ender(final String str) { range=Double.parseDouble(str); }
        
        @Override
        public void execute(Location l) 
        {
            l.getWorld().getNearbyEntities(l, range, range, range).stream().filter(ent -> ent instanceof Player).map(ent -> (Player)ent).forEach(p -> p.teleport(l));
        }
        @Override
        public CreeperAction getAction() 
        { 
            return CreeperAction.ENDER; 
        }
    }
    
    private class Stacker implements Type
    {
        private final int height;
        private final Material type;
        public Stacker(final String str) 
        { 
            String[] array=str.split(";");
            type=Material.valueOf(array[0].toUpperCase());
            height=Integer.parseInt(array[1]); 
        }
        
        @Override
        public void execute(Location l) 
        {
            l=l.clone();
            Integer height=this.height;
            do
            {
                l.getBlock().setType(type);
                l.add(0,1,0);
            }
            while(!l.getBlock().getType().isSolid() && --height!=0);
        }
        @Override
        public CreeperAction getAction() 
        { 
            return CreeperAction.ENDER; 
        }
    }
    
    private class Potion implements Type
    {
        private final PotionEffect pe;
        private final double range;
        public Potion(final String str)
        {
            String[] array=str.split(";");
            pe=new PotionEffect(PotionEffectType.getByName(array[0].toUpperCase()),Integer.valueOf(array[1])*20,Integer.valueOf(array[2])-1);
            range=Double.parseDouble(array[3]);
        }
        
        @Override
        public void execute(Location l) 
        {
            l.getWorld().getNearbyEntities(l, range, range, range).stream()
                    .filter(ent -> ent.getType()==EntityType.PLAYER)
                    .map(ent -> (Player)ent)
                    .forEach(p -> p.addPotionEffect(pe, true));
        }

        @Override
        public CreeperAction getAction()
        {
            return CreeperAction.POTION; 
        }
        
    }
    
    private class Fire implements Type
    {
        private final Integer tick;
        private final double range;
        public Fire(final String str) 
        {
            String[] array=str.split(";");
            tick=Integer.parseInt(array[0]);
            range=Double.parseDouble(array[1]);
        }
        @Override
        public void execute(Location l) 
        {
            l.getWorld().getNearbyEntities(l, range, range, range).stream()
                    .filter(ent -> ent.getType()==EntityType.PLAYER)
                    .map(ent -> (Player)ent)
                    .forEach(p -> p.setFireTicks(tick));
        }

        @Override
        public CreeperAction getAction() { return CreeperAction.FIRE; }
    }
    
    private class Spawner implements Type
    {
        private final EntityType et;
        private final int amount;
        public Spawner(final String str)
        {
            String[] array=str.split(";");
            et=EntityType.valueOf(array[0].toUpperCase());
            amount=Integer.parseInt(array[1]);
        }
        
        @Override
        public void execute(Location l) 
        {
            for(Integer j=0;j<amount;j++) 
            { 
                l.getWorld().spawnEntity(l, et); 
            } 
        }

        @Override
        public CreeperAction getAction() 
        { 
            return CreeperAction.TRANSMUTATION; 
        }
    
    }
    
    private class Transmutation implements Type 
    {
        private final Set<Material> from=new HashSet<>();
        private final Material to;
        private final int range;
        public Transmutation(final String str)
        {
            String[] array=str.split(";");
            from.addAll(Arrays.stream(array[0].split(",")).map(String::toUpperCase).map(Material::valueOf).collect(Collectors.toSet()));
            to=Material.valueOf(array[1].toUpperCase());
            range=Integer.parseInt(array[2]);
        }
        
        @Override
        public void execute(Location l) 
        { 
            for(Integer x=-range;x<range;x++)
            {
                for(Integer y=-range;y<range;y++)
                {
                    for(Integer z=-range;z<range;z++)
                    {
                        Location newLoc=l.clone().add(x,y,z);
                        if(from.contains(newLoc.getBlock().getType())) 
                        { 
                            newLoc.getBlock().setType(to); 
                        }
                    }
                }
            } 
        }

        @Override
        public CreeperAction getAction() 
        {
            return CreeperAction.TRANSMUTATION; 
        }
    }
    
    private class Thunder implements Type
    {
        private final boolean damage;
        private final double range;
        public Thunder(final String str)
        {
            String[] array=str.split(";");
            switch(array[0].toLowerCase())
            {
                case "damage": damage=true; break;
                case "nodamage": damage=false; break;
                default: throw new IllegalArgumentException("thunder:"+array[0]+"in creeper effect is not valid!");
            }
            range=Double.parseDouble(array[1]);
        }
        
        @Override
        public void execute(Location l) 
        {
            Stream<Player> stream=l.getWorld().getNearbyEntities(l, range, range, range).stream()
                    .filter(ent -> ent.getType() == EntityType.PLAYER)
                    .map(ent -> (Player)ent);
            if(damage) 
            { 
                stream.forEach(p -> p.getWorld().strikeLightning(p.getLocation())); 
            }
            else 
            {
                stream.forEach(p -> p.getWorld().strikeLightningEffect(p.getLocation())); 
            }
        }

        @Override
        public CreeperAction getAction() 
        {
            return CreeperAction.THUNDER;  
        }
        
    }    
    
    private interface Type
    {
        void execute(final Location l);
        CreeperAction getAction();
    }
    
    private class Friendly implements Meta
    {
        private final JavaPlugin pl;
        public Friendly(final JavaPlugin pl) 
        { 
            this.pl=pl; 
        }
        
        @Override
        public void add(Entity explosive) 
        { 
            explosive.setMetadata(Value.FriendlyMetadata, new FixedMetadataValue(pl,"Friendly"));
        }

        @Override
        public CreeperAction getAction() 
        { 
            return CreeperAction.FRIENDLY; 
        }
        
    }
    
    private interface Meta
    {
        void add(final Entity explosive);
        CreeperAction getAction();
    }
}
