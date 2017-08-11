/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Paros
 */
public class Language 
{
    private static final Map<String,String> language=new HashMap<>();
    public static void parse(final FileConfiguration c)
    {
        if(!c.contains("Language")){ throw new NullPointerException("Config does not contain Language section!"); }
        
        ConfigurationSection lPath = c.getConfigurationSection("Language");
        lPath.getKeys(false).forEach(s -> language.put(s, Utils.color(lPath.getString(s))));
    }
    
    public static Boolean sendMessage(final CommandSender cs, final String value)
    {
        String message=language.getOrDefault(value,"Value "+value+" not in the config!");
        if(message.isEmpty()){ return true; }
        
        cs.sendMessage(message);
        return true;
    }
    
    public static Boolean sendWithPermission(final CommandSender cs, final String value, final String permission)
    {
        String message=language.getOrDefault(value,"Value "+value+" not in the config!");
        if(message.isEmpty() || !cs.hasPermission(permission)){ return true; }
        
        cs.sendMessage(message);
        return true;
    }
    
    public static Boolean sendParsedMessage(final CommandSender cs, final String value, final String placeholder, final String replace)
    {
        String message=language.getOrDefault(value,"Value "+value+" not in the config!");
        if(message.isEmpty()){ return true; }
        
        cs.sendMessage(message.replace(placeholder, replace));
        return true;
    }
    
    public static Boolean sendMultipleMessage(final CommandSender cs,final String... array)
    {
        Arrays.stream(array).forEach(s -> sendMessage(cs,s));
        return true;
    }
    
    public static Boolean sendMultipleMessageWithPermission(final CommandSender cs, final String perm,final String... array)
    {
        if(!cs.hasPermission(perm)) { return true; }
        Arrays.stream(array).forEach(s -> sendMessage(cs,s));
        return true;
    }
    
    public static String getString(final String value) { return language.getOrDefault(value,"Value "+value+" not in the config!"); }
}
