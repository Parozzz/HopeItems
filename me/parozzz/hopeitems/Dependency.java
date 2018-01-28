/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author Paros
 */
public class Dependency 
{
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
}
