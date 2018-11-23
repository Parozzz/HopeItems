/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.hooks;

import me.parozzz.hopeitems.hooks.clan.ClanHook;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import me.parozzz.hopeitems.hooks.clan.FactionMCoreHook;
import me.parozzz.hopeitems.hooks.clan.FactionUUIDHook;
import me.parozzz.hopeitems.hooks.clan.McMMOHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author Paros
 */
public class Dependency 
{
    private final static Logger logger = Logger.getLogger(Dependency.class.getSimpleName());
    
    public static Economy economy = null;
    public static boolean setupEconomy()
    {
        if(Bukkit.getPluginManager().isPluginEnabled("Vault"))
        {
            RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null)
            {
                economy = economyProvider.getProvider(); 
            }
        }
        return economy != null;
    }
    
    public static Boolean isEconomyHooked() 
    {
        return economy != null; 
    }
    
    public static WorldGuardPlugin worldGuard;
    public static boolean setupWorldGuard() 
    {
        if(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) 
        {
            worldGuard= (WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard"); 
        }
        return worldGuard != null;
    }
    
    public static boolean isWorldGuardHooked() 
    {
        return worldGuard != null; 
    }
    
    private static ClanHook clanHook;
    public static boolean isClanHook()
    {
        return clanHook != null;
    }
    
    public static @Nullable ClanHook getClanHook()
    {
        return clanHook;
    }
    
    public static void setupClanHook(final FileConfiguration config)
    {
        PluginManager manager = Bukkit.getServer().getPluginManager();
        
        String priority = config.getString("clanProtectionPriority", "");
        if(priority.equalsIgnoreCase("mcmmo"))
        {
            mcMMOHook(manager);
        }
        else if(priority.equalsIgnoreCase("factions"))
        {
            factionsHook(manager);
        }
        else
        {
            mcMMOHook(manager);
            factionsHook(manager);
        }
    }
    
    private static void factionsHook(final PluginManager manager)
    {
        if(manager.getPlugin("Factions") != null)
        {
            try {
                Class.forName("com.massivecraft.factions.FPlayers");
                logger.log(Level.INFO, "[HopeItems] Hooked into FactionUUID");
                clanHook = new FactionUUIDHook();
            } catch (final ClassNotFoundException ex) {
            }
            
            try {
                Class.forName("com.massivecraft.factions.entity.MPlayer");
                logger.log(Level.INFO, "[HopeItems] Hooked into FactionMCore");
                clanHook = new FactionMCoreHook();
            } catch (final ClassNotFoundException ex) {
            }
        }
    }
    
    private static void mcMMOHook(final PluginManager manager)
    {
        if(manager.isPluginEnabled("mcMMO"))
        {
            logger.log(Level.INFO, "[HopeItems] Hooked into McMMO");
            clanHook = new McMMOHook();
        }
    }
}
