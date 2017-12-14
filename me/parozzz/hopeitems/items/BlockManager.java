/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.parozzz.hopeitems.Configs;
import me.parozzz.hopeitems.HopeItems;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class BlockManager 
{
    private static BlockManager instance;
    public static BlockManager getInstance()
    {
        return Optional.ofNullable(instance).orElseGet(() -> instance=new BlockManager());
    }
    
    private final Map<Location, ItemCollection> blocks; 
    private BlockManager()
    {
        blocks = new HashMap<>();
    }
    
    public ItemCollection getBlockInfo(final Location l)
    {
        return blocks.get(l);
    }
    
    public void addBlock(final Block b, final ItemCollection collection)
    {
        blocks.put(b.getLocation(), collection);
    }
    
    public void addBlock(final Location l, final ItemCollection collection)
    {
        blocks.put(l, collection);
    }
    
    public ItemCollection removeBlock(final Block b)
    {
        return blocks.remove(b.getLocation());
    }
    
    public ItemCollection removeBlock(final Location l)
    {
        return blocks.remove(l);
    }
    
    public void saveBlocks(final FileConfiguration data)
    {
        data.set("blocks", blocks.entrySet().stream().map(e -> 
        {
            Map<String, Location> map = new LinkedHashMap<>();
            map.put(e.getValue().getId(), e.getKey());
            return map;
        }).collect(Collectors.toList()));
    }
    
    public boolean oldMethod()
    {
        return new File(JavaPlugin.getProvidingPlugin(BlockManager.class).getDataFolder(), "blocks.yml").exists();
    }
    
    public void loadBlocks(final FileConfiguration data)
    {
        Optional.of(new File(JavaPlugin.getProvidingPlugin(BlockManager.class).getDataFolder(), "blocks.yml")).filter(File::exists).ifPresent(File::delete);
        
        data.getMapList("blocks").stream().map(map -> (Map<String, Location>)map).map(Map::entrySet).flatMap(Set::stream).forEach(e -> 
        {
            Location l = e.getValue();
            Optional.ofNullable(ItemRegistry.getCollection(e.getKey())).map(collection -> 
            {
                addBlock(l, collection);
                return l;
            }).orElseGet(() -> 
            {
                Logger.getLogger(HopeItems.class.getSimpleName()).log(Level.WARNING, "An item named {0} is not found. Skipping block at x:{1} y:{2} z:{3}", new Object[]{e.getKey(), l.getBlockX(), l.getBlockY(), l.getBlockZ()});
                return null;
            });
        });
    }
}
