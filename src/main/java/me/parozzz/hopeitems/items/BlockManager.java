/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import me.parozzz.hopeitems.HopeItems;
import me.parozzz.hopeitems.items.database.DatabaseManager;
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
    private static final Logger logger = Logger.getLogger(BlockManager.class.getSimpleName());
    
    private final DatabaseManager databaseManager;
    private final Map<Location, ItemCollection> blocks; 
    public BlockManager(final DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
        blocks = new HashMap<>();
    }
    
    public ItemCollection getBlockInfo(final Location l)
    {
        return blocks.get(l);
    }
    
    public void addBlock(final Block b, final ItemCollection collection)
    {
        this.addBlock(b.getLocation(), collection);
    }
    
    public void addBlock(final Location l, final ItemCollection collection)
    {
        blocks.put(l, collection);
        
        databaseManager.getBlockTable().addBlock(l, collection);
    }
    
    public ItemCollection removeBlock(final Block b)
    {
        return this.removeBlock(b.getLocation());
    }
    
    public @Nullable ItemCollection removeBlock(final Location l)
    {
        ItemCollection collection = blocks.remove(l);
        if(collection != null)
        {
            databaseManager.getBlockTable().removeBlock(l);
        }
        return collection;
    }
    
    public void loadAllBlocks()
    {
        databaseManager.getBlockTable().getAllBlocks().forEach((loc, collectionId) -> 
        {
            ItemCollection collection = ItemRegistry.getCollection(collectionId);
            if(collection == null)
            {
                logger.log(Level.WARNING, "An item named {0} cannot be found anymore while loading the block at x: {1}, y: {2}, z: {3}, world: {4}", new Object[] {
                    collectionId, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()
                });
                return;
            }
            this.blocks.put(loc, ItemRegistry.getCollection(collectionId));
        });
    }
    
    public boolean oldMethod()
    {
        return new File(JavaPlugin.getProvidingPlugin(BlockManager.class).getDataFolder(), "blocks.yml").exists();
    }
    
    public void convertFromFileToSql(final FileConfiguration data)
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
        
        this.blocks.forEach(databaseManager.getBlockTable()::addBlock);
    }
}

    
    /*
    public void saveBlocks(final FileConfiguration data)
    {
        data.set("blocks", blocks.entrySet().stream().map(e -> 
        {
            Map<String, Location> map = new LinkedHashMap<>();
            map.put(e.getValue().getId(), e.getKey());
            return map;
        }).collect(Collectors.toList()));
    }
    */