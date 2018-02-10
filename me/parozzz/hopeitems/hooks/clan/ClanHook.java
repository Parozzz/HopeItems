/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.hooks.clan;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public interface ClanHook 
{
    public List<LivingEntity> getFilteredNearbyEntities(final Player player, final Location loc, final int range);
    public List<Player> getNearbyEnemyPlayers(final Player player, final Location loc, final int range);
    public boolean sameClan(final Player firstPlayer, final Player secondPlayer);
    public List<Player> getNearbyAlliedPlayers(final Player player, final Location loc, final int range);
}
