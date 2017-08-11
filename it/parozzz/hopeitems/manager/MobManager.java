/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.manager;

import it.parozzz.hopeitems.Database;
import it.parozzz.hopeitems.Enum.PassiveType;
import it.parozzz.hopeitems.HopeItems;
import it.parozzz.hopeitems.Value;
import it.parozzz.hopeitems.core.ItemBuilder;
import it.parozzz.hopeitems.core.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author Paros
 */
public class MobManager 
{
    private EntityType et;
    private double health;
    private String name;
    
    private EntityType ride;
    
    private Abilities ability;
    
    private final List<DropManager> drops=new ArrayList<>();
    
    private final Set<PotionEffect> potion=new HashSet<>();
    public MobManager parse(final ConfigurationSection path) throws InvalidConfigurationException
    {
        if(path==null) { return this; }
        if(!path.contains("entity")) { throw new InvalidConfigurationException("entity cannot be null!"); }
        
        et=EntityType.valueOf(path.getString("entity").toUpperCase());
        if(path.contains("ride")) { ride=EntityType.valueOf(path.getString("ride").toUpperCase()); }
        health=path.getDouble("health",-1);
        name=Utils.color(path.getString("name",new String()));
        
        ability=new Abilities(path.getStringList("ability"),path.contains("triggerChance")?path.getDouble("triggerChance"):100D);
        
        potion.addAll(path.getStringList("potion").stream()
                .map(str -> str.split(":"))
                .map(array -> new PotionEffect(PotionEffectType.getByName(array[0].toUpperCase()),Integer.parseInt(array[1]),Integer.parseInt(array[2])))
                .collect(Collectors.toSet()));
        
        if(path.contains("Drop"))
        {
            ConfigurationSection dPath=path.getConfigurationSection("Drop");
            dPath.getKeys(false).stream().map(str -> dPath.getConfigurationSection(str)).forEach(mPath -> 
            {
                DropManager dm=new DropManager().parse(mPath);
                for(int j=0;j<dm.getChance();j++) { drops.add(dm); }
            });
            Collections.shuffle(drops);
        }
        
        if(path.contains("Equipment")) { this.parseEquip(path.getConfigurationSection("Equipment")); }         
        if(!Utils.bukkitVersion("1.8") && path.contains("Attribute")) { this.parseAttribute(path.getConfigurationSection("Attribute")); }
        return this;
    }
    
    public void summon(final Location l)
    {
        if(et==null) { return; }
        LivingEntity ent = (LivingEntity)l.getWorld().spawnEntity(l, et);
        if(health!=-1) {  Utils.setMaxHealth(ent, health); }
        if(!name.isEmpty()) { Utils.setName(ent, name); }
        
        if(ride!=null) 
        { 
            if(Utils.bukkitVersion("1.8","1.9","1.10")) { l.getWorld().spawnEntity(l, ride).setPassenger(ent); }
            else { l.getWorld().spawnEntity(l, ride).addPassenger(ent);  }
        }
        
        ability.addMetadata(ent);;
        equipment.forEach(equip -> equip.set(ent));
        ent.addPotionEffects(potion);
        
        attributes.forEach(ca -> ca.change(ent));
        ent.setMetadata(Value.CustomMobMetadata, new FixedMetadataValue(HopeItems.getInstance(),this));
        Database.addEntity(ent);
    }
    
    public void drop(final Location l, final List<ItemStack> drops) { drop(l, null, drops); }
    
    public void drop(final Location l, final Player p, final List<ItemStack> mobDrops)
    {
        if(!drops.isEmpty()) { drops.get(ThreadLocalRandom.current().nextInt(drops.size())).drop(l, p, mobDrops); }
    }
    
    private final Set<ChangeAttribute> attributes=new HashSet<>();
    private void parseAttribute(final ConfigurationSection path)
    {
        path.getKeys(false).forEach(attribute ->  attributes.add(new ChangeAttribute(Attribute.valueOf("GENERIC_"+attribute.toUpperCase()),path.getDouble(attribute))));
    }
    
    private final Set<Equip> equipment=new HashSet<>();
    private void parseEquip(final ConfigurationSection path)
    {
        path.getKeys(false).stream().forEach(slot -> 
        {
            switch(EquipmentSlot.valueOf(slot.toUpperCase()))
            {
                case HAND:
                    equipment.add(new Hand(path.getConfigurationSection(slot)));
                    break;
                case HEAD:
                    equipment.add(new Head(path.getConfigurationSection(slot)));
                    break;
                case CHEST:
                    equipment.add(new Torso(path.getConfigurationSection(slot)));
                    break;
                case LEGS:
                    equipment.add(new Legs(path.getConfigurationSection(slot)));
                    break;
                case FEET:
                    equipment.add(new Boots(path.getConfigurationSection(slot)));
                    break;
                case OFF_HAND:
                    if(!Utils.bukkitVersion("1.8")) { equipment.add(new OffHand(path.getConfigurationSection(slot))); }
                    break;
            }
        });
    }
    
    private class ChangeAttribute
    {
        private final Attribute attribute;
        private final Double value;
        public ChangeAttribute(final Attribute attribute, final Double value)
        {
            this.attribute=attribute;
            this.value=value;
        }
        
        public void change(final LivingEntity ent) { ent.getAttribute(attribute).setBaseValue(value);  }
    }
    
    private class Head implements Equip
    {
        private final ItemStack item;
        private float chance=-1;
        public Head(final ConfigurationSection path)
        {
            item=Utils.getItemByPath(path);
            if(path.contains("chance")) { chance=Double.valueOf(path.getDouble("chance")).floatValue()/100; }
        }

        @Override
        public void set(LivingEntity ent) 
        { 
            ent.getEquipment().setHelmet(item);
            if(chance!=-1) { ent.getEquipment().setHelmetDropChance(chance); }
        }
        @Override
        public EquipmentSlot getSlot() { return EquipmentSlot.HEAD; }
    }
    private class Torso implements Equip
    {
        private final ItemStack item;
        private float chance=-1;
        public Torso(final ConfigurationSection path)
        {
            item=Utils.getItemByPath(path);
            if(path.contains("chance")) { chance=Double.valueOf(path.getDouble("chance")).floatValue()/100; }
        }

        @Override
        public void set(LivingEntity ent) 
        { 
            ent.getEquipment().setChestplate(item);
            if(chance!=-1) { ent.getEquipment().setChestplateDropChance(chance); }
        }
        @Override
        public EquipmentSlot getSlot() { return EquipmentSlot.CHEST; }
    }
    private class Legs implements Equip
    {
        private final ItemStack item;
        private float chance=-1;
        public Legs(final ConfigurationSection path)
        {
            item=Utils.getItemByPath(path);
            if(path.contains("chance")) { chance=Double.valueOf(path.getDouble("chance")).floatValue()/100; }
        }

        @Override
        public void set(LivingEntity ent) 
        { 
            ent.getEquipment().setLeggings(item);
            if(chance!=-1) { ent.getEquipment().setLeggingsDropChance(chance); }
        }
        @Override
        public EquipmentSlot getSlot() { return EquipmentSlot.LEGS; }
    }
    private class Boots implements Equip
    {
        private final ItemStack item;
        private float chance=-1;
        public Boots(final ConfigurationSection path)
        {   
            item=Utils.getItemByPath(path);
            if(path.contains("chance")) { chance=Double.valueOf(path.getDouble("chance")).floatValue()/100; }
        }

        @Override
        public void set(LivingEntity ent) 
        { 
            ent.getEquipment().setBoots(item);
            if(chance!=-1) { ent.getEquipment().setBootsDropChance(chance); }
        }
        @Override
        public EquipmentSlot getSlot() { return EquipmentSlot.FEET; }
    }
    private class Hand implements Equip
    {
        private final ItemStack item;
        private float chance=-1;
        public Hand(final ConfigurationSection path)
        {
            item=Utils.getItemByPath(path);
            if(path.contains("chance")) { chance=Double.valueOf(path.getDouble("chance")).floatValue()/100; }
        }

        @Override
        public void set(LivingEntity ent) 
        { 
            if(Utils.bukkitVersion("1.8"))
            {
                ent.getEquipment().setItemInHand(item);
                if(chance!=-1) { ent.getEquipment().setItemInHandDropChance(chance); }
            }
            else
            {
                ent.getEquipment().setItemInMainHand(item);
                if(chance!=-1) { ent.getEquipment().setItemInMainHandDropChance(chance); }
            }
        }
        @Override
        public EquipmentSlot getSlot() { return EquipmentSlot.HAND; }
    }
    private class OffHand implements Equip
    {
        private final ItemStack item;
        private float chance=-1;
        public OffHand(final ConfigurationSection path)
        {
            if(Utils.bukkitVersion("1.8")) {  }
            
            item=Utils.getItemByPath(path);
            if(path.contains("chance")) { chance=Double.valueOf(path.getDouble("chance")).floatValue()/100; }
        }

        @Override
        public void set(LivingEntity ent) 
        { 
            ent.getEquipment().setItemInOffHand(item);
            if(chance!=-1) { ent.getEquipment().setItemInOffHandDropChance(chance); }
        }
        @Override
        public EquipmentSlot getSlot() { return EquipmentSlot.OFF_HAND; }
    }
    
    private interface Equip{
        void set(final LivingEntity ent);
        EquipmentSlot getSlot();
    }
    
    private enum DropType
    {
        CHEST,PLAYER,DROP;
    }
    
    public class DropManager
    {
        private DropType type;
        private int chance;
        private String name;
        
        private Material preview;
        private String previewName;
        
        private final Map<String,Integer> custom=new HashMap<>();
        private final Set<ItemStack> items=new HashSet<>();
        private final Set<String> command=new HashSet<>();
        public DropManager parse(final ConfigurationSection path)
        {
            this.name=path.getName();
            type=DropType.valueOf(path.getString("type").toUpperCase());
            
            if(type==DropType.CHEST && path.contains("preview")) 
            { 
                String[] array=path.getString("preview").split(";");
                preview=Material.valueOf(array[0].toUpperCase()); 
                previewName=Utils.color(array[1]);
            }
            
            custom.putAll(path.getStringList("custom").stream().map(str -> str.split(";")).collect(Collectors.toMap(array -> array[0], array -> Integer.parseInt(array[1]))));

            path.getStringList("item").stream().map(str -> str.split(";")).forEach(array -> 
            {
                items.add(new ItemBuilder(Material.valueOf(array[0].toUpperCase()))
                        .setAmount(array.length>1?Integer.valueOf(array[1]):1)
                        .setName(array.length>2?Utils.color(array[2]):null)
                        .setLore(array.length>3?Arrays.stream(array).skip(3).map(Utils::color).collect(Collectors.toList()):null)
                        .build());
            });

            command.addAll(path.getStringList("command"));
            chance=path.getInt("chance");
            return this;
        }
        
        public int getChance() { return chance; } 
        
        public void drop(final Location l, final Player p, final List<ItemStack> mobDrops)
        {
            l.add(0, 1, 0);
            DropType temp=type;
            if((temp == DropType.CHEST && !Utils.or(l.getBlock().getType(), Material.AIR, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA, Material.GRASS, Material.LONG_GRASS))
                    || (temp==DropType.PLAYER && p==null))
            {
                Bukkit.getLogger().info("OK");
                temp=DropType.DROP;
            }
            
            if(p!=null) { command.stream().map(str -> str.replace("%player%", p.getName())).forEach(str -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str)); }
            switch(temp)
            {
                case DROP:
                    custom.forEach((str,amount) -> l.getWorld().dropItem(l, new ItemBuilder(Database.getItem(str).clone()).setAmount(amount).build()));
                    items.stream().map(item -> item.clone()).forEach(item -> l.getWorld().dropItem(l, item));
                    mobDrops.forEach(item -> l.getWorld().dropItem(l, item));
                    break;
                case PLAYER:
                    p.getInventory().addItem(custom.entrySet().stream()
                            .map(entry -> new ItemBuilder(Database.getItem(entry.getKey()).clone()).setAmount(entry.getValue()).build())
                            .toArray(ItemStack[]::new)).values().forEach(item -> l.getWorld().dropItem(l, item));
                    p.getInventory().addItem(items.stream().map(item -> item.clone()).toArray(ItemStack[]::new)).values().forEach(item -> l.getWorld().dropItem(l, item));
                    p.getInventory().addItem(mobDrops.toArray(new ItemStack[mobDrops.size()]));
                    break;
                case CHEST:
                    ArmorStand hologram=Utils.spawnHologram(l.getBlock().getLocation().add(0.5, 0.75,0.5),Value.chestHologram);
                    if(Value.chestExpirationTime!=-1)
                    {
                        if(Value.chestHologram.isEmpty()) { hologram.setCustomNameVisible(false); }
                        else { hologram.setCustomName(Value.chestHologram.replace("%expire%", Integer.toString(Value.chestExpirationTime))); }
                        
                        Item dropped=Utils.spawnFloatingItem(l.getBlock().getLocation().add(0.5, 1.25, 0.5), previewName, preview);
                        
                        new BukkitRunnable()
                        {
                            RevertBlock revert=Database.addRevert(l.getBlock(), new RevertBlock(l.getBlock().getState().getData(),l.getBlock().getType()));
                            int time=0;
                            @Override
                            public void run() 
                            {
                                if(time++>=Value.chestExpirationTime)
                                {
                                    revert.revert(l.getBlock());
                                    Database.removeRevert(l.getBlock());
                                    
                                    if(dropped!=null) { dropped.remove(); }
                                    hologram.remove();
                                    
                                    this.cancel();
                                    return;
                                }
                                hologram.setCustomName(Value.chestHologram.replace("%expire%", Integer.toString(Value.chestExpirationTime-time)));
                            }  
                        }.runTaskTimer(HopeItems.getInstance(), 20L, 20L);
                    }
                    else { hologram.setCustomNameVisible(false); }
                    
                    l.getBlock().setType(Material.CHEST);
                    
                    Chest chest=(Chest)l.getBlock().getState();
                    chest.setCustomName(name);
                    chest.getBlockInventory().addItem(custom.entrySet().stream()
                            .map(entry -> new ItemBuilder(Database.getItem(entry.getKey()).clone()).setAmount(entry.getValue()).build())
                            .toArray(ItemStack[]::new));
                    chest.getBlockInventory().addItem(items.stream().map(item -> item.clone()).toArray(ItemStack[]::new));
                    chest.getBlockInventory().addItem(mobDrops.toArray(new ItemStack[mobDrops.size()]));
                    l.getBlock().setMetadata(Value.CustomDropChestMetadata, new FixedMetadataValue(HopeItems.getInstance(),this));
                    break;
            }
        }
    }
         
    public class RevertBlock
    {
        private final MaterialData data;
        private final Material type;
        public RevertBlock(final MaterialData data, final Material type)
        {
            this.data=data;
            this.type=type;
        }

        public void revert(final Block b)
        {
            b.removeMetadata(Value.CustomDropChestMetadata, HopeItems.getInstance());
            b.setType(type);

            BlockState state=b.getState();
            state.setData(data);
            state.update(true);
        }
    }
        
    public class Abilities
    {
        private final Map<PassiveType,Passive> single;
        private final Set<Passive> passives;
        private final double triggerChance;
        public Abilities(final List<String> list, double triggerChance)
        {
            this.triggerChance=triggerChance;
            
            passives=new HashSet<>();
            single=new HashMap<>();
            list.forEach(str -> 
            {
                try 
                { 
                    PassiveType pt;
                    if(!str.contains(":")) { pt=PassiveType.getByStarting(str); }
                    else { pt=PassiveType.getByStarting( str.substring(0, str.indexOf(":"))); }
                    str=str.substring(str.indexOf(":")+1);
                    
                    Passive passive;
                    switch(pt)
                    {
                        case POTION: passive=new Potion(str); break;
                        case DEFLECT:  single.put(pt, new Deflect()); return;
                        case SHIELD:   single.put(pt, new Shield(str)); return;
                        case PUSH:  
                            if(str.isEmpty()) { single.put(pt, new Push()); }
                            else { single.put(pt, new Push(str)); }
                            return;
                        case DAMAGE:  passive=new Damage(str); break;
                        case LIGHTER: passive=new Lighter(str); break;
                        case SNIPER: passive=new Sniper(str); break;
                        case SPAWN:  passive=new Spawn(str); break;
                        default: return;
                    }
                    passives.add(passive);
                }
                catch(IndexOutOfBoundsException ex)  {  throw new IllegalArgumentException(str+" is not valid!"); }
            });
        }
        
        public void addMetadata(final LivingEntity mob) 
        { 
            if(!passives.isEmpty()) 
            { 
                mob.setMetadata(Value.CustomAbilityMetadata, new FixedMetadataValue(HopeItems.getInstance(),this)); 
            } 
        }
        
        public boolean doesDeflect() 
        { 
            return single.containsKey(PassiveType.DEFLECT); 
        }
        
        public boolean doesShield()  
        { 
            return single.containsKey(PassiveType.SHIELD); 
        }
        
        public int getShieldDuration() 
        {
            return ((Shield)single.get(PassiveType.SHIELD)).getDuration();  
        }
        
        public double getShieldChance() 
        { 
            return ((Shield)single.get(PassiveType.SHIELD)).getChance(); 
        }
        
        public double getChance() { return triggerChance; }
        
        public void execute(final Player p, final LivingEntity mob) { passives.forEach(passive -> passive.execute(p, mob)); }
        
        private class Potion implements Passive
        {
            private final PotionEffect pe;
            private final double chance;
            public Potion(final String str)
            {
                String[] array=str.split(";");
                pe=new PotionEffect(PotionEffectType.getByName(array[0].toUpperCase()),Integer.parseInt(array[1])*20,Integer.parseInt(array[2]));
                
                chance=array.length==4?Double.parseDouble(array[3]):100D;
            }

            @Override
            public void execute(Player p,LivingEntity mob) { if(ThreadLocalRandom.current().nextDouble(101D)<chance) { p.addPotionEffect(pe, true); } }

            @Override
            public PassiveType getType() { return PassiveType.POTION; }

        }
        
        private class Lighter implements Passive
        {
            private final int ticks;
            private final double chance;
            public Lighter(final String str)
            {
                String[] array=str.split(";");
                
                ticks=Integer.valueOf(array[0])*20;
                chance=array.length==2?Double.parseDouble(array[1]):100D;
            }
            
            @Override
            public void execute(Player p, LivingEntity mob) { if(ThreadLocalRandom.current().nextDouble(101D)<chance) { p.setFireTicks(ticks); } }

            @Override
            public PassiveType getType() { return PassiveType.LIGHTER; }
            
        }
        
        private class Spawn implements Passive
        {
            private final int quantity;
            private final EntityType type;
            private final double chance;
            public Spawn(final String str) 
            { 
                String[] array=str.split(";");
                type=EntityType.valueOf(array[0].toUpperCase());
                quantity=Integer.valueOf(array[1]); 
                
                chance=array.length==3?Double.parseDouble(array[2]):100D;
            }
            
            @Override
            public void execute(Player p, LivingEntity mob) 
            {
                if(ThreadLocalRandom.current().nextDouble(101D)<chance)
                {
                    for(int j=0;j<quantity;j++) 
                    { 
                        mob.getLocation().getWorld().spawnEntity(mob.getLocation(), type); 
                    }
                }
            }

            @Override
            public PassiveType getType() { return PassiveType.SPAWN; } 
        }
        
        private class Sniper implements Passive
        {
            private final double damage;
            private final int quantity;
            private final double chance;
            public Sniper(final String str)
            {
                String[] array=str.split(";");
                
                damage=Double.parseDouble(array[0]);
                quantity=Integer.parseInt(array[1]);
                chance=array.length==3?Double.parseDouble(array[2]):100D;
            }
            
            @Override
            public void execute(Player p, LivingEntity mob) 
            {
                if(ThreadLocalRandom.current().nextDouble(101D)>=chance) { return; }
                new BukkitRunnable()
                {
                    int counter=0;
                    @Override
                    public void run() 
                    {
                        if(counter++>=quantity) { this.cancel(); }
                        else
                        {
                            Arrow arrow=((ProjectileSource)mob).launchProjectile(Arrow.class);
                            arrow.spigot().setDamage(damage);
                            arrow.setVelocity(p.getLocation().toVector().subtract(mob.getLocation().toVector()).multiply(2D));
                        }
                    }
                }.runTaskTimer(HopeItems.getInstance(), 0L, 5L);
            }

            @Override
            public PassiveType getType() { return PassiveType.SNIPER; }
            
        }
        
        private class Push implements Passive
        {
            private final double chance;
            public Push() { chance=100D; }
            public Push(final String str) { chance=Double.parseDouble(str); }
            
            @Override
            public void execute(Player p,LivingEntity mob) 
            {
                if(ThreadLocalRandom.current().nextDouble(101D)>=chance) { return; }
                
                              //Get the location from the target player - Get the location from the source player
                double deltaX = p.getLocation().getX()         -          mob.getLocation().getX();//Get X Delta
                double deltaZ = p.getLocation().getZ()         -          mob.getLocation().getZ();//Get Z delta

                Vector vec = new Vector(deltaX, 0, deltaZ);//Create new vector
                vec.normalize();//Normalize it so we don't shoot the player into oblivion
                vec.multiply(5 / (Math.sqrt(Math.pow(deltaX, 2.0) + Math.pow(deltaZ, 2.0))));//Use a bit of trig to get 'h' then divide the max power by 'h' to get a fall off effect
                vec.setY(2 / (Math.sqrt(Math.pow(deltaX, 2.0) + Math.pow(deltaZ, 2.0))));//use a lower number so the effect is less intense.
                p.setVelocity(vec);
            }
            @Override
            public PassiveType getType() { return PassiveType.PUSH; }
        }
        
        private class Shield implements Passive
        {
            private final int duration;
            private final double chance;
            public Shield(final String str) 
            { 
                String[] array=str.split(";");
                
                duration=Integer.parseInt(array[0])*20;  
                chance=array.length==2?Double.parseDouble(array[1]):100D;
            }
            
            public int getDuration() { return duration; }
            public double getChance() { return chance; }
            
            @Override
            public void execute(Player p,LivingEntity mob) {}
            @Override
            public PassiveType getType() { return PassiveType.SHIELD; }  
        }
        
        private class Deflect implements Passive
        {
            @Override
            public void execute(Player p,LivingEntity mob) { }
            @Override
            public PassiveType getType() { return PassiveType.DEFLECT; }
        }
        
        private class Damage implements Passive
        {
            private final double damage;
            private final double chance;
            public Damage(final String str) 
            { 
                String[] array=str.split(";");
                
                damage=Double.parseDouble(array[0]);
                chance=array.length==2?Double.parseDouble(array[1]):100D;
            }
            @Override
            public void execute(Player p, LivingEntity mob) { if(ThreadLocalRandom.current().nextDouble(101D)>chance) { p.damage(damage); } }

            @Override
            public PassiveType getType() { return PassiveType.DAMAGE; }
        }
    }
    
    private interface Passive
    {
        void execute(Player p, LivingEntity mob);
        PassiveType getType();
    }
}
