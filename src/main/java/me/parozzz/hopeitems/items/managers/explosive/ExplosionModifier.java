/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.explosive;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import me.parozzz.hopeitems.items.managers.ManagerUtils;
import me.parozzz.reflex.configuration.MapArray;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public enum ExplosionModifier 
{
    ENDER{
        @Override
        public BiConsumer<EntityExplodeEvent, Set<LivingEntity>> getConsumer(final MapArray map)
        {
            Consumer<LivingEntity> damage=ManagerUtils.getNumberFunction(map.getValue("damage", Function.identity()), Number::intValue, LivingEntity::damage);
            return (e, set) -> 
            {
                set.forEach(liv -> 
                {
                    liv.teleport(e.getEntity());
                    damage.accept(liv);
                });
            };
        }
    },
    STACKER{
        @Override
        public BiConsumer<EntityExplodeEvent, Set<LivingEntity>> getConsumer(final MapArray map)
        {
            final Material type=map.getUpperValue("type", Material::valueOf);
            final int height=map.getValue("height", Integer::valueOf);
            
            return (e, set) -> 
            {
                int count=height;
                
                Block b=e.getEntity().getLocation().getBlock();
                while(b.getRelative(BlockFace.UP).isEmpty() && count-->0)
                {
                    b.setType(type);
                    b = b.getRelative(BlockFace.UP);
                }
            };
        }
    },
    POTION{
        @Override
        public BiConsumer<EntityExplodeEvent, Set<LivingEntity>> getConsumer(final MapArray map)
        {
            PotionEffect pe=ManagerUtils.getPotionEffect(map);
            
            return (e, set) -> set.forEach(pe::apply);
        }
    },
    FIRE{
        @Override
        public BiConsumer<EntityExplodeEvent, Set<LivingEntity>> getConsumer(final MapArray map)
        {
            Consumer<LivingEntity> fire = ManagerUtils.getNumberFunction(map.getValue("duration"), Number::intValue, (liv, i) -> liv.setFireTicks(i));
            
            return (e, set) -> set.forEach(fire);
        }
    },
    TRANSMUTATION{
        @Override
        public BiConsumer<EntityExplodeEvent, Set<LivingEntity>> getConsumer(final MapArray map)
        {
            Set<Material> from=EnumSet.copyOf(Stream.of(map.getUpperValue("from", Function.identity()).split(";")).map(Material::valueOf).collect(Collectors.toSet()));
            Material to=map.getUpperValue("to", Material::valueOf);
            int range=map.getValue("range", Integer::valueOf);
            
            return (e, set) -> 
            {
                final Location l=e.getLocation();
                
                IntStream.range(l.getBlockX(), l.getBlockX()+range).forEach(x -> 
                {
                    IntStream.range(l.getBlockY(), l.getBlockY()+range).forEach(y -> 
                    {
                        IntStream.range(l.getBlockZ(), l.getBlockZ()+range).forEach(z -> 
                        {
                            Block b=l.getWorld().getBlockAt(x, y, z);
                            if(from.contains(b.getType()))
                            {
                                b.setType(to);
                            }
                        });
                    });
                });
            };
        }
    },
    SPAWN{
        @Override
        public BiConsumer<EntityExplodeEvent, Set<LivingEntity>> getConsumer(final MapArray map)
        {
            EntityType et=map.getUpperValue("type", EntityType::valueOf);
            int quantity=map.getValue("quantity", Integer::valueOf);
            int lifetime=map.getValue("lifetime", Integer::valueOf);
            
            if(lifetime==-1)
            {
                return (e, set) -> IntStream.range(0, quantity).forEach(i -> e.getLocation().getWorld().spawnEntity(e.getLocation(), et));
            }
            else
            {
                return (e, set) -> 
                {
                    Set<Entity> entities=new HashSet<>();
                    IntStream.range(0, quantity).forEach(i -> 
                    {
                        Entity ent=e.getLocation().getWorld().spawnEntity(e.getLocation(), et);
                        ent.setCustomName(ChatColor.WHITE.toString()+lifetime);
                        ent.setCustomNameVisible(true);
                        entities.add(ent);
                    });
                    
                    new BukkitRunnable()
                    {
                        private int duration=lifetime;
                        @Override
                        public void run() 
                        {
                            if(duration--<=0)
                            {
                                if(!entities.isEmpty())
                                {
                                    for(Iterator<Entity> it=entities.iterator(); it.hasNext(); )
                                    {
                                        Entity ent=it.next();
                                        if(!ent.getLocation().getChunk().isLoaded())
                                        {
                                            ent.getLocation().getChunk().load();
                                        }
                                        else
                                        {
                                            ent.remove();
                                            it.remove();
                                        }
                                    }
                                }
                                else
                                {
                                    this.cancel();
                                }
                                return;
                            }
                            
                            for(Iterator<Entity> it=entities.iterator(); it.hasNext(); )
                            {
                                Entity ent=it.next();
                                if(ent.isDead() || !ent.isValid())
                                {
                                    it.remove();
                                }
                                else
                                {
                                    ent.setCustomName(ChatColor.WHITE.toString()+duration);
                                }
                            }
                        }
                        
                    }.runTaskTimer(JavaPlugin.getProvidingPlugin(ExplosionModifier.class), 20L, 20L);
                };
            }
        }
    },
    THUNDER{
        @Override
        public BiConsumer<EntityExplodeEvent, Set<LivingEntity>> getConsumer(final MapArray map)
        {
            boolean damage=map.getValue("damage", Boolean::valueOf);
            
            return damage? (e, set) -> set.forEach(liv -> liv.getWorld().strikeLightning(liv.getLocation())):
                    (e, set) -> set.forEach(liv -> liv.getWorld().strikeLightningEffect(liv.getLocation()));
        }
    };
    
    public BiConsumer<EntityExplodeEvent, Set<LivingEntity>> getConsumer(final MapArray map)
    {
        throw new UnsupportedOperationException();
    }
}
