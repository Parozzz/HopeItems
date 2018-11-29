/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.hooks.clan;

import com.gmail.nossr50.api.PartyAPI;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class McMMOHook implements ClanHook
{
    @Override
    public List<Player> getNearbyEnemyPlayers(final Player player, final Location loc, final int range) 
    {
        return getFilteredNearbyEntities(player, loc, range).stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public boolean sameClan(Player p1, Player p2) 
    {
        return PartyAPI.inSameParty(p1, p2);
    }

    @Override
    public List<LivingEntity> getFilteredNearbyEntities(final Player player, final Location loc, final int range) 
    {
        return loc.getWorld().getNearbyEntities(loc, range, range, range).stream()
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(liv -> liv.getType() == EntityType.PLAYER ? !PartyAPI.inSameParty(player, (Player)liv) : true)
                .collect(Collectors.toList());
    }

    @Override
    public List<Player> getNearbyAlliedPlayers(Player player, Location loc, int range) 
    {
        return loc.getWorld().getNearbyEntities(loc, range, range, range).stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(localPlayer -> PartyAPI.inSameParty(player, localPlayer))
                .collect(Collectors.toList());
    }
}
