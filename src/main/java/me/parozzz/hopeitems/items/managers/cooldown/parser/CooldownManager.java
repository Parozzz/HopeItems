/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.cooldown.parser;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import me.parozzz.hopeitems.items.managers.IManager;
import me.parozzz.hopeitems.items.managers.cooldown.Cooldown;
import me.parozzz.hopeitems.items.managers.cooldown.PermissionedCooldown;
import me.parozzz.reflex.configuration.SimpleMapList;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class CooldownManager implements IManager
{
    private final Map<UUID, Cooldown> cooldownMap;
    private final List<PermissionedCooldown> permissionCooldowns;
    
    private final long defaultCooldown;
    private final String message;
    public CooldownManager(final long defaultCooldown, final @Nullable String message)
    {
        this.defaultCooldown = defaultCooldown;
        this.message = message;
        
        cooldownMap = new HashMap<>();
        permissionCooldowns = new LinkedList<>();
    }
    
    protected void addPermissionedCooldown(final PermissionedCooldown permCooldown)
    {
        permissionCooldowns.add(permCooldown);
    }
    
    private long calculateTime(final Player p)
    {
        return permissionCooldowns.stream()
                .filter(permCool -> p.hasPermission(permCool.getPermission())).findFirst()
                .map(permCool -> permCool.getCooldownTime())
                .orElse(defaultCooldown);
    }
    
    public boolean hasCooldown(final Player p)
    {
        return Optional.ofNullable(cooldownMap.get(p.getUniqueId())).map(cool -> 
        {
            if(cool.hasExpired())
            {
                cool.reset();
                cool.setToWait(calculateTime(p));
                return false;
            }
            
            if(message != null)
            {
                p.sendMessage(message.replace("{time}", Util.longToTime(cool.getRemaining() / 1000)));
            }
            return true;
        }).orElseGet(() -> 
        {
            cooldownMap.put(p.getUniqueId(), new Cooldown(calculateTime(p)));
            return false;
        });
    }
}
