/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.hooks.clan;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.parozzz.reflex.Debug;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class FactionMCoreHook implements ClanHook
{
    private static Method get;
    private static Method hasFaction;
    private static Method getFactionName;
    static
    {
        try {
            Class<?> mPlayerClazz = Class.forName("com.massivecraft.factions.entity.MPlayer");
            get = mPlayerClazz.getDeclaredMethod("get", Object.class);
            hasFaction = mPlayerClazz.getDeclaredMethod("hasFaction");
            getFactionName = mPlayerClazz.getDeclaredMethod("getFactionName");
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(FactionMCoreHook.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public List<LivingEntity> getFilteredNearbyEntities(final Player player, final Location loc, final int range) 
    {
        Object mcCorePlayer = get(player);
        if(mcCorePlayer == null || !hasFaction(mcCorePlayer))
        {
            return loc.getWorld().getNearbyEntities(loc, range, range, range).stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast).collect(Collectors.toList());
        }
        
        return loc.getWorld().getNearbyEntities(loc, range, range, range).stream()
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(liv -> 
                {
                    if(liv.getType() != EntityType.PLAYER)
                    {
                        return true;
                    }
                    
                    Object localMcCorePlayer = get((Player)liv);
                    return localMcCorePlayer == null || !hasFaction(localMcCorePlayer) || !getFactionName(mcCorePlayer).equals(getFactionName(localMcCorePlayer));
                }).collect(Collectors.toList());
    }
    
    private Object get(final Player p)
    {
        return Debug.validateMethod(get, null, p);
    }
    
    private boolean hasFaction(final Object mp)
    {
        return (boolean)Debug.validateMethod(hasFaction, mp);
    }
    
    private String getFactionName(final Object mp)
    {
        return (String)Debug.validateMethod(getFactionName, mp);
    }
    
    @Override
    public List<Player> getNearbyEnemyPlayers(final Player player, final Location loc, final int range) 
    {
        return getFilteredNearbyEntities(player, loc, range).stream().filter(Player.class::isInstance).map(Player.class::cast).collect(Collectors.toList());
    }

    @Override
    public boolean sameClan(final Player p1, final Player p2) 
    {
        Object localMcCorePlayer1 = get(p1);
        if(localMcCorePlayer1 == null || !hasFaction(localMcCorePlayer1))
        {
            return false;
        }
        
        Object localMcCorePlayer2 = get(p2);
        if(localMcCorePlayer2 == null || !hasFaction(localMcCorePlayer2))
        {
            return false;
        }
        
        return getFactionName(localMcCorePlayer1).equals(getFactionName(localMcCorePlayer2));
    }

    @Override
    public List<Player> getNearbyAlliedPlayers(Player player, Location loc, int range) 
    {
        Object mcCorePlayer = get(player);
        if(mcCorePlayer == null || !hasFaction(mcCorePlayer))
        {
            return loc.getWorld().getNearbyEntities(loc, range, range, range).stream()
                    .filter(Player.class::isInstance)
                    .map(Player.class::cast)
                    .collect(Collectors.toList());
        }
        
        return loc.getWorld().getNearbyEntities(loc, range, range, range).stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(localPlayer -> 
                {
                    Object localMcCorePlayer = get(localPlayer);
                    return localMcCorePlayer != null && hasFaction(localMcCorePlayer) && getFactionName(mcCorePlayer).equals(getFactionName(localMcCorePlayer));
                }).collect(Collectors.toList());
    }
    
}
