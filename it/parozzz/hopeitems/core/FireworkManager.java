/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.core;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public final class FireworkManager implements Listener
{
    @EventHandler(ignoreCancelled=true,priority=EventPriority.MONITOR)
    private void onFireworkDamage(EntityDamageByEntityEvent e)
    {
        e.setCancelled(e.getDamager() instanceof Firework 
                && e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) 
                && e.getDamager().hasMetadata("FireworkDamage"));
    }
    
    public static void spawn(final JavaPlugin pl, final Location l, final FireworkEffect fe)
    {
        Firework fw=(Firework)l.getWorld().spawnEntity(l, EntityType.FIREWORK);
        
        FireworkMeta meta=fw.getFireworkMeta();
        meta.addEffect(fe);
        fw.setFireworkMeta(meta);
        
        fw.setMetadata("FireworkDamage", new FixedMetadataValue(pl,"Damage"));
        
        new BukkitRunnable()
        {
            @Override
            public void run() 
            {
                fw.detonate();
            }
        }.runTaskLater(pl, 5);
    }
}
