/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.hooks.clan;

import java.util.List;
import java.util.stream.Collectors;

import com.massivecraft.factions.entity.MPlayer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.massivecraft.factions.*;

/**
 *
 * @author Paros
 */
public class FactionUUIDHook implements ClanHook
{
    @Override
    public List<LivingEntity> getFilteredNearbyEntities(final Player player, final Location loc, final int range)
    {
        MPlayer fp = MPlayer.get(player);
        if(fp == null || !fp.hasFaction())
        {
            return loc.getWorld().getNearbyEntities(loc, range, range, range).stream()
                    .filter(LivingEntity.class::isInstance)
                    .map(LivingEntity.class::cast)
                    .collect(Collectors.toList());
        }

        return loc.getWorld().getNearbyEntities(loc, range, range, range).stream()
                .filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast)
                .filter(liv -> 
                {
                    if(liv.getType() != EntityType.PLAYER)
                    {
                        return true;
                    }

                    MPlayer localFp = MPlayer.get(liv);
                    return localFp == null || !localFp.hasFaction() || !localFp.getFaction().equals(fp.getFaction());
                }).collect(Collectors.toList());
    }

    @Override
    public List<Player> getNearbyEnemyPlayers(final Player player, final Location loc, final int range) 
    {
        return getFilteredNearbyEntities(player, loc, range).stream().filter(Player.class::isInstance).map(Player.class::cast).collect(Collectors.toList());
    }
    
    @Override
    public List<Player> getNearbyAlliedPlayers(final Player player, final Location loc, final int range)
    {
        MPlayer fp = MPlayer.get(player);
        if(fp == null || !fp.hasFaction())
        {
            return loc.getWorld().getNearbyEntities(loc, range, range, range).stream()
                    .filter(Player.class::isInstance)
                    .map(Player.class::cast)
                    .collect(Collectors.toList());
        }

        return loc.getWorld().getNearbyEntities(loc, range, range, range).stream()
                .filter(Player.class::isInstance).map(Player.class::cast)
                .filter(p -> 
                {
                    MPlayer localFp = MPlayer.get(p);
                    return localFp != null && localFp.hasFaction() && localFp.getFaction().equals(fp.getFaction());
                }).collect(Collectors.toList()); 
    }
    
    @Override
    public boolean sameClan(final Player p1, final Player p2) 
    {

        MPlayer fp1 = MPlayer.get(p1);
        if(fp1 == null || !fp1.hasFaction())
        {
            return false;
        }

        MPlayer fp2 = MPlayer.get(p2);
        if(fp2 == null || !fp2.hasFaction())
        {
            return false;
        }

        return fp1.getFaction().equals(fp2.getFaction());
    }
}
