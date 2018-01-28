/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.cooldown.parser;

import java.util.function.Function;
import me.parozzz.hopeitems.items.managers.IParser;
import me.parozzz.hopeitems.items.managers.cooldown.PermissionedCooldown;
import me.parozzz.reflex.configuration.SimpleMapList;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Paros
 */
public class CooldownParser implements IParser
{

    @Override
    public void registerDefaultSpecificParsers() { }

    @Override
    public CooldownManager parse(ConfigurationSection path) 
    {
        long defaultCooldown = path.getLong("default") * 1000;
        String message = path.contains("message") ? Util.cc(path.getString("message")) : null;
        
        CooldownManager cooldownManager = new CooldownManager(defaultCooldown, message);
        
        new SimpleMapList(path.getMapList("permissions"))
                .getConvertedValues(Function.identity(), str -> Long.valueOf(str) * 1000, 0)
                .forEach((perm, time) -> 
                { 
                    PermissionedCooldown permCooldown = new PermissionedCooldown(perm, time);
                    cooldownManager.addPermissionedCooldown(permCooldown);
                });
        
        return cooldownManager;
    }
    
}
