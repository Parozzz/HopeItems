/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems;

import it.parozzz.hopeitems.core.Particle;
import it.parozzz.hopeitems.core.Particle.ParticleEffect;
import it.parozzz.hopeitems.core.Particle.ParticleEnum;
import it.parozzz.hopeitems.manager.MobManager;
import it.parozzz.hopeitems.manager.MobManager.Abilities;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public class CustomMobHandler 
        implements Listener
{
    private final Set<LivingEntity> shielded=new HashSet<>();
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onCustomMobHit(final EntityDamageByEntityEvent e)
    {
        if(!(e.getEntity() instanceof LivingEntity) || !e.getEntity().hasMetadata(Value.CustomAbilityMetadata)) { return; }
        
        Abilities ab=(Abilities)e.getEntity().getMetadata(Value.CustomAbilityMetadata).get(0).value();
        
        if(ThreadLocalRandom.current().nextDouble(101D)>ab.getChance()) { return; }
        
        if(e.getDamager() instanceof Projectile) 
        { 
            if(((Projectile)e.getDamager()).getShooter() instanceof Player) { ab.execute((Player)((Projectile)e.getDamager()).getShooter(), (LivingEntity)e.getEntity()); }
            if(ab.doesDeflect()) 
            { 
                e.setCancelled(true);
                return;
            } 
        }
        
        e.setCancelled(shielded.contains((LivingEntity)e.getEntity()));
        if(e.isCancelled()) { return; }
        else if(ab.doesShield() && ThreadLocalRandom.current().nextDouble(101D)<=ab.getChance())
        {
            shielded.add((LivingEntity)e.getEntity());
            new BukkitRunnable()
            {
                Integer count=0;
                @Override
                public void run() 
                { 
                    count+=10;
                    if(count>=ab.getShieldDuration() || e.getEntity().isDead() || !e.getEntity().isValid())
                    {
                        shielded.remove((LivingEntity)e.getEntity()); 
                        this.cancel();
                        return;
                    }
                    Particle.spawn(Bukkit.getOnlinePlayers(), ParticleEffect.CLOUD, ParticleEnum.CLOUD, e.getEntity().getLocation(), 3);
                }
            }.runTaskTimer(HopeItems.getInstance(), 10L,10L);
        }
        
        if(e.getDamager() instanceof Player) { ab.execute((Player)e.getDamager(), (LivingEntity)e.getEntity()); }
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onMobDeath(final EntityDeathEvent e)
    {
        if(e.getEntity().hasMetadata(Value.CustomAbilityMetadata)) 
        { 
            shielded.remove(e.getEntity());
            Database.removeEntity(e.getEntity());;
        }
        
        if(e.getEntity().hasMetadata(Value.CustomMobMetadata))
        {
            ((MobManager)e.getEntity().getMetadata(Value.CustomMobMetadata).get(0).value()).drop(e.getEntity().getLocation(), e.getEntity().getKiller(), e.getDrops());
            e.getDrops().clear();
        }
    }
}
