/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.drops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopeitems.Configs;
import me.parozzz.hopeitems.items.ItemCollection;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemRegistry;
import me.parozzz.hopeitems.items.managers.mobs.parsers.MobManager;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.configuration.MapArray;
import me.parozzz.reflex.placeholders.Placeholder;
import me.parozzz.reflex.utilities.ItemUtil;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public class DropManager 
{
    private final List<Consumer<EntityDeathEvent>> drops;
    public DropManager(final ConfigurationSection path)
    {
        drops=new ArrayList<>();
        
        path.getKeys(false).stream().map(path::getConfigurationSection).forEach(nPath -> 
        {
            Set<String> customItems=nPath.getStringList("customItems").stream().collect(Collectors.toSet());
            
            ConfigurationSection iPath=nPath.getConfigurationSection("Items");
            Set<ItemStack> items=iPath.getKeys(false).stream().map(iPath::getConfigurationSection).map(ItemUtil::getItemByPath).collect(Collectors.toSet());
            
            BiConsumer<Location, Player> commands=nPath.getStringList("command").stream().map(cmd -> 
            {
                Placeholder holder = new Placeholder(cmd).checkLocation().checkPlayer();

                BiConsumer<Location, Player> cns= (l, p) -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), holder.parse(p, l));
                return cns;
            }).reduce(BiConsumer::andThen).orElse((l, p) -> {});
            
            DropType dt;
            try { dt=DropType.valueOf(nPath.getString("type").toUpperCase()); }
            catch(final IllegalArgumentException t) { throw new IllegalArgumentException("Wrong drop type "+nPath.getString("type")); }
           
            Consumer<EntityDeathEvent> onDeathEvent=null;
            switch(dt)
            {
                case INVENTORY:
                    onDeathEvent= e->
                    {
                        Optional.ofNullable(e.getEntity().getKiller()).ifPresent(p -> 
                        {
                            commands.accept(e.getEntity().getLocation(), p);
                            
                            p.getInventory().addItem(Stream.concat(
                                    items.stream()
                                            .map(ItemStack::clone),
                                    customItems.stream()
                                            .map(ItemRegistry::getCollection)
                                            .filter(Objects::nonNull)
                                            .map(ItemCollection::getItem)
                                            .map(holder -> holder.parse(p, e.getEntity().getLocation()))).toArray(ItemStack[]::new)).values()      
                                    .forEach(extra -> p.getWorld().dropItem(p.getLocation(), extra));
                        });
                    };
                    break;
                case CHEST:
                    ChestPreview preview=new ChestPreview();
                    
                    MapArray map = new MapArray(nPath.getString("chestInfo"));
                    preview.type = Debug.validateEnum(map.getValue("id"), Material.class);
                    preview.name = map.getValue("name", Util::cc);
                    preview.duration = map.getValue("duration", Integer::valueOf);
                    
                    onDeathEvent= e->
                    {
                        Optional.ofNullable(e.getEntity().getKiller()).ifPresent(p -> 
                        {
                            commands.accept(e.getEntity().getLocation(), p);
                            
                            preview.spawnHologram(e.getEntity().getLocation().getBlock().getLocation().add(0.5, -1, 0.5));

                            JavaPlugin plugin=JavaPlugin.getProvidingPlugin(DropManager.class);
                            new BukkitRunnable()
                            {
                                final Location l=e.getEntity().getLocation();
                                final Chunk c=l.getChunk();
                                final MaterialData exBlockData=l.getBlock().getState().getData().clone();
                                @Override
                                public void run() 
                                {
                                    if(!c.isLoaded())
                                    {
                                        c.load();
                                        new BukkitRunnable()
                                        {
                                            @Override
                                            public void run() 
                                            {
                                                if(c.isLoaded())
                                                {
                                                    deleteChest(l.getBlock());
                                                }
                                            }
                                            
                                        }.runTaskTimer(plugin, 2L, 2L);
                                    }
                                    else
                                    {
                                        deleteChest(l.getBlock());
                                    }
                                }
                                
                                private void deleteChest(final Block b)
                                {
                                    b.setType(exBlockData.getItemType());
                                    
                                    BlockState state=b.getState();
                                    state.setData(exBlockData);
                                    state.update();
                                    
                                    b.getWorld().getNearbyEntities(b.getLocation(), 2L, 2L, 2L).stream()
                                            .filter(ArmorStand.class::isInstance)
                                            .map(ArmorStand.class::cast)
                                            .filter(as -> !as.isVisible())
                                            .forEach(ArmorStand::remove);
                                }
                                
                            }.runTaskLater(JavaPlugin.getProvidingPlugin(DropManager.class), preview.duration*20);
                            
                            Block b=e.getEntity().getLocation().getBlock();
                            b.setType(Material.CHEST);
                            ((Chest)b.getState()).getInventory().addItem(Stream.concat(
                                    items.stream()
                                            .map(ItemStack::clone),
                                    customItems.stream()
                                            .map(ItemRegistry::getCollection)
                                            .filter(Objects::nonNull)
                                            .map(ItemCollection::getItem)
                                            .map(holder -> holder.parse(p, e.getEntity().getLocation()))).toArray(ItemStack[]::new));
                        });
                    };
                    break;
                case DROP:
                    onDeathEvent= e->
                    {
                        Optional.ofNullable(e.getEntity().getKiller()).ifPresent(p -> 
                        {
                            commands.accept(e.getEntity().getLocation(), p);
                            
                            e.getDrops().addAll(Stream.concat(
                                    items.stream()
                                            .map(ItemStack::clone),
                                    customItems.stream()
                                            .map(ItemRegistry::getCollection)
                                            .filter(Objects::nonNull)
                                            .map(ItemCollection::getItem)
                                            .map(holder -> holder.parse(p, e.getEntity().getLocation()))).collect(Collectors.toList()));
                        });
                    };
                    break;
            }
            drops.add(onDeathEvent);
        });
        Collections.shuffle(drops);
    }
    
    public Consumer<EntityDeathEvent> getRandomDrop()
    {
        return drops.get(ThreadLocalRandom.current().nextInt(drops.size()));
    }
    
    private class ChestPreview
    {
        public Material type;
        public String name;
        public int duration;
        
        public ArmorStand spawnHologram(final Location l)
        {
            ArmorStand stand = Util.spawnHologram(l, name);
            stand.setHelmet(new ItemStack(type));
            stand.setMarker(false);
            return stand;
        }
    }
}
