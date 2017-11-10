/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopeitems.items.BlockManager;
import me.parozzz.hopeitems.items.ItemListener;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.mobs.MobManager;
import me.parozzz.hopeitems.shop.Shop;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class HopeItems extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        try
        {
            setupDependency();
            
            Configs.initConfig(Utils.fileStartup(new File(this.getDataFolder(), "config.yml")));
            Configs.initItems(Utils.fileStartup(new File(this.getDataFolder(), "items.yml")));
            
            MobManager.registerListener();
            ExplosiveManager.registerListener();
            LuckyManager.registerListener();
            
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
            
            BlockManager.getInstance().loadBlocks();
        }
        catch(final FileNotFoundException | UnsupportedEncodingException t)
        {
            Bukkit.getLogger().log(Level.SEVERE, "Problem loading the plugin. Disabling it", t);
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    public void reload() throws FileNotFoundException, UnsupportedEncodingException
    {
        BlockManager.getInstance().saveBlocks();
        
        Set<Recipe> recipes=new HashSet<>();
        Bukkit.recipeIterator().forEachRemaining(r -> 
        {
            if(!Configs.customRecipes.remove(r))
            {
                recipes.add(r);
            }
        });
        Bukkit.clearRecipes();
        
        recipes.forEach(Bukkit::addRecipe);
        
        Configs.initConfig(Utils.fileStartup(new File(this.getDataFolder(), "config.yml")));
        Configs.initItems(Utils.fileStartup(new File(this.getDataFolder(), "items.yml")));
        Shop.getInstance().loadConfig();
        BlockManager.getInstance().loadBlocks();
    }
    
    @Override
    public void onDisable()
    {
        BlockManager.getInstance().saveBlocks();
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
