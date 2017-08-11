/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.core;

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
    private static Economy eco = null;
    public static boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) { eco = economyProvider.getProvider(); }
        return eco != null;
    }
    public static Boolean isEconomyHooked() { return eco!=null; }
    public static Economy getEconomy() { return eco; }
    
    
    private static WorldGuardPlugin wg = null;
    public static boolean setupWorldGuard() 
    {
        if(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard")!=null) { wg=(WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard"); }
        return wg!=null;
    }
    public static WorldGuardPlugin getWorldGuard() { return wg; }
    public static boolean isWorldGuardHooked() { return wg!=null; }
}
