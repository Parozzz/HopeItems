/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.core;

import it.parozzz.hopeitems.core.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class CommandUtils 
{
    public static boolean isPlayer(final CommandSender cs)
    {
        if(cs instanceof Player) { return true; }
        Language.sendMessage(cs, "playerCommand");
        return false;
    }
    
    public static boolean isPlayerValid(final CommandSender cs, final String name) 
    { 
        if(Bukkit.getPlayer(name)!=null) { return true; }
        Language.sendMessage(cs, "playerNotFound");
        return false; 
    }
    
    public static boolean hasPermission(final CommandSender cs, final String perm)
    {
        if(cs.hasPermission(perm)) { return true; }
        Language.sendMessage(cs, "noPermission");
        return false;
    }
}