/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopeitems.Configs;
import me.parozzz.hopeitems.HopeItems;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
    
    private final File data;
    private final Map<Location, ItemInfo> blocks; 
    private BlockManager()
    {
        blocks=new HashMap<>();
        
        data=new File(JavaPlugin.getProvidingPlugin(BlockManager.class).getDataFolder(), "blocks.yml");
    }
    
    public ItemInfo getBlockInfo(final Location l)
    {
        return blocks.get(l);
    }
    
    public void addBlock(final Block b, final ItemInfo info)
    {
        blocks.put(b.getLocation(), info);
    }
    
    public void addBlock(final Location l, final ItemInfo info)
    {
        blocks.put(l, info);
    }
    
    public ItemInfo removeBlock(final Block b)
    {
        return blocks.remove(b.getLocation());
    }
    
    public ItemInfo removeBlock(final Location l)
    {
        return blocks.remove(l);
    }
    
    public void saveBlocks()
    {
        try 
        {
            data.createNewFile();
            
            FileConfiguration c=new YamlConfiguration();
            blocks.forEach((l, info) -> c.set(info.getName(), l));

            c.save(data);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(BlockManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadBlocks()
    {
        if(data.exists())
        {
            FileConfiguration c=YamlConfiguration.loadConfiguration(data);
            c.getKeys(false).forEach(s -> 
            {
                Location l=(Location)c.get(s);
                Optional.ofNullable(Configs.getItemInfo(s)).map(info -> 
                {
                    addBlock(l, info);
                    return null;
                }).orElseGet(() -> 
                {
                    Logger.getLogger(HopeItems.class.getSimpleName()).log(Level.WARNING, "An item named {0} is not found. Skipping block at x:{1} y:{2} z:{3}", new Object[]{s, l.getBlockX(), l.getBlockY(), l.getBlockZ()});
                    return null;
                });
                        
            });
        }
    }
}
