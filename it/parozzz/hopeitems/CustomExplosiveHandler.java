/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems;

import it.parozzz.hopeitems.manager.ExplosiveManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class CustomExplosiveHandler 
        implements Listener
{
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onCreeperTarget(final EntityTargetLivingEntityEvent e)
    {
        e.setCancelled(e.getEntity().hasMetadata(Value.FriendlyMetadata) && e.getTarget().getType()==EntityType.PLAYER);
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onExplosiveExplode(final EntityExplodeEvent e)
    {
        if(e.getEntity().hasMetadata(Value.CustomExplosiveMetadata)) 
        { 
            ((ExplosiveManager)e.getEntity().getMetadata(Value.CustomExplosiveMetadata).get(0).value()).onExplode(e.getEntity().getLocation());
        }
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onExplosivePrime(final ExplosionPrimeEvent e)
    {
        if(e.getEntity().hasMetadata(Value.CustomExplosiveMetadata)) 
        { 
            ExplosiveManager cm=(ExplosiveManager)e.getEntity().getMetadata(Value.CustomExplosiveMetadata).get(0).value();
            e.setRadius(cm.getPower()!=-1?cm.getPower():e.getRadius());
            e.setFire(cm.getFire());
        }
    }
}
