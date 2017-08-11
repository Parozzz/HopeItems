package it.parozzz.hopeitems.core;


import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Paros
 */
public class OldVersionArmorstand 
        implements Listener
{
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onManipulate(final PlayerArmorStandManipulateEvent e)
    {
       e.setCancelled(!e.getRightClicked().isVisible());
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onDamage(final EntityDamageByEntityEvent e)
    {
        e.setCancelled(e.getEntity() instanceof ArmorStand && !((ArmorStand)e.getEntity()).isVisible());
    }
}
