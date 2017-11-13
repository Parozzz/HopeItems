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
import me.parozzz.hopeitems.items.BlockManager;
import me.parozzz.hopeitems.items.listener.ItemListener;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.mobs.MobManager;
import me.parozzz.hopeitems.shop.Shop;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.Utils;
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
            
            FileConfiguration config = Utils.fileStartup(new File(this.getDataFolder(), "config.yml"));
            if(config.getBoolean("metric", true))
            {
                new MetricsLite(this);
            }
            Configs.initConfig(config);
            Configs.initItems(Utils.fileStartup(new File(this.getDataFolder(), "items.yml")), false);
            
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
            
            if(MCVersion.V1_8.isEqual()) 
            {
                Utils.registerArmorStandInvicibleListener();
            }
            else if(MCVersion.V1_11.isHigher())
            {
                Utils.registerFireworkDamageListener();
            }
            
            Bukkit.getPluginManager().registerEvents(new ItemListener(), this);
            if(MCVersion.V1_9.isHigher())
            {
                ItemListener.register1_9Listener();
            }
            
            this.getCommand("items").setExecutor(new ItemsCommand());
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
        
        Configs.initConfig(Utils.fileStartup(new File(this.getDataFolder(), "config.yml")));
        Configs.initItems(Utils.fileStartup(new File(this.getDataFolder(), "items.yml")), true);
        
        Shop.getInstance().loadConfig();
        BlockManager.getInstance().loadBlocks(data);
        MobManager.loadData(data);
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
