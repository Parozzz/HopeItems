/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.cooldown;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.classes.SimpleMapList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class CooldownManager 
{
    private final Map<UUID, Cooldown> cooldownMap;
    private final Map<String, Long> permissionCooldowns;
    private final Long defaultCooldown;
    private final Predicate<Player> hasCooldown;
    public CooldownManager(final ConfigurationSection path)
    {
        cooldownMap = new HashMap<>();
        
        defaultCooldown = path.getLong("default") * 1000;
        final String message = Utils.color(path.getString("message"));
        
        permissionCooldowns = new LinkedHashMap<>();
        permissionCooldowns.putAll(new SimpleMapList(path.getMapList("permissions")).getConvertedValues(Function.identity(), str -> Long.valueOf(str) * 1000));
        
        hasCooldown = p -> 
        {
            return Optional.ofNullable(cooldownMap.get(p.getUniqueId())).map(cool -> 
            {
                if(cool.hasExpired())
                {
                    cool.reset();
                    cool.setToWait(calculateTime(p));
                    return false;
                }

                p.sendMessage(message.replace("{time}", Objects.toString(cool.getRemaining() / 1000)));
                return true;
            }).orElseGet(() -> 
            {
                cooldownMap.put(p.getUniqueId(), new Cooldown(calculateTime(p)));
                return false;
            });
        };
    }
    
    private long calculateTime(final Player p)
    {
        return permissionCooldowns.entrySet().stream().filter(e -> p.hasPermission(e.getKey())).findFirst().map(Map.Entry::getValue).orElse(defaultCooldown);
    }
    
    public boolean hasCooldown(final Player p)
    {
        return hasCooldown.test(p);
    }
}
