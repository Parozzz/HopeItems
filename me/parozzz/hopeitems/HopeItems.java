/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.hopeitems.items.BlockManager;
import me.parozzz.hopeitems.items.listener.ItemListener;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.mobs.MobManager;
import me.parozzz.hopeitems.shop.Shop;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.ReflexAPI;
import me.parozzz.reflex.ReflexAPI.Property;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class HopeItems extends JavaPlugin
{
    private File dataFile;
    @Override
    public void onEnable()
    {
        try
        {
            setupDependency();
            
            File configFile = new File(this.getDataFolder(), "config.yml");
            FileConfiguration config = Util.fileStartup(configFile);
            
            this.saveDefaultItems(configFile, config);
            
            if(config.getBoolean("metric", true))
            {
                new MetricsLite(this);
            }
            
            Configs.initConfig(config);
            loadItems(false);
            
            MobManager.registerListener();
            ExplosiveManager.registerListener();
            LuckyManager.registerListener();
            
            (dataFile = new File(this.getDataFolder(), "data.yml")).createNewFile();
            FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
            BlockManager.getInstance().loadBlocks(data);
            MobManager.loadData(data);
            
            if(Dependency.isEconomyHooked())
            {
                Shop.getInstance().loadConfig();
                Shop.registerListener();
            }
            
            Util.ifCheck(MCVersion.V1_8.isEqual(), () -> Util.registerArmorStandInvicibleListener());
            
            Bukkit.getPluginManager().registerEvents(new ItemListener(), this);
            Util.ifCheck(MCVersion.V1_9.isHigher(), () -> ItemListener.register1_9Listener());
            
            getCommand("items").setExecutor(new ItemsCommand());
        }
        catch(final IOException t)
        {
            Bukkit.getLogger().log(Level.SEVERE, "Problem loading the plugin. Disabling it", t);
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    public void reload() throws FileNotFoundException, UnsupportedEncodingException
    {
        FileConfiguration data = new YamlConfiguration();
        BlockManager.getInstance().saveBlocks(data);
        MobManager.saveData(data);
        try 
        {
            data.save(dataFile);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(HopeItems.class.getName()).log(Level.SEVERE, "[HopeItems] Something went wrong during data saving", ex);
        }
        
        Configs.initConfig(Util.fileStartup(new File(this.getDataFolder(), "config.yml")));
        this.loadItems(true);
        
        Shop.getInstance().loadConfig();
        BlockManager.getInstance().loadBlocks(data);
        MobManager.loadData(data);
    }
    
    public void loadItems(final boolean reload)
    {
        Configs.loadItems(new File(this.getDataFolder(), "items").listFiles(), reload);
    }
    
    public void saveDefaultItems(final File configFile, final FileConfiguration config)
    {
        new File(this.getDataFolder(), "items").mkdir();
        if(config.getBoolean("savedDefault", false))
        {
            return; 
        }
        
        Stream.of("ExampleItem", "ExplodingDiamond", "GiftHead", "ScammyEnderpearl", "SpawnSnowball", "TeleportArrow", "ThorPot")
                .forEach(defaultName -> this.saveResource("items/" + defaultName + ".yml", true));
        
                
        config.set("savedDefault", true);
        try {
            config.save(configFile);
        } catch (IOException ex) {
            Logger.getLogger(HopeItems.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void onDisable()
    {
        try 
        {
            FileConfiguration data = new YamlConfiguration();
            BlockManager.getInstance().saveBlocks(data);
            MobManager.saveData(data);
            data.save(dataFile);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(HopeItems.class.getName()).log(Level.SEVERE, "[HopeItems] Something went wrong during data saving", ex);
        }
        
        HandlerList.unregisterAll(this);
    }
    
    private void setupDependency()
    {
        Logger l=Logger.getLogger(HopeItems.class.getSimpleName());
        
        ReflexAPI.enable(Property.ENTITYPLAYER_LISTENER, Property.ARMOREVENTS_LISTENER, Property.OFFHANDEVENTS_LISTENER);
        
        if(Dependency.setupEconomy())
        {
            l.log(Level.INFO, "[HopeItems] Vault SoftDependency found");
        }
        
        if(Dependency.setupWorldGuard())
        {
            l.log(Level.INFO, "[HopeItems] WorldGuard SoftDependency found");
        }
        
    }
}
