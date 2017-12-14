/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.classes.builders;

import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.utilities.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public class FireworkBuilder 
{
    private static final String FIREWORK_DATA = JavaPlugin.getProvidingPlugin(FireworkBuilder.class) + "Firework.NoDamage";
    static
    {
        if(MCVersion.V1_11.isHigher())
        {
            Bukkit.getServer().getPluginManager().registerEvents(new Listener()
            {
                @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
                private void onFireworkDamage(final EntityDamageByEntityEvent e)
                {
                    e.setCancelled(e.getDamager().getType()==EntityType.FIREWORK && e.getDamager().hasMetadata(FIREWORK_DATA));
                }
            }, JavaPlugin.getProvidingPlugin(FireworkBuilder.class));
        }    
    }
    
    private final FireworkEffect.Builder builder;
    public FireworkBuilder()
    {
        builder=FireworkEffect.builder();
    }

    public FireworkBuilder addColor(final Color... colors)
    {
        builder.withColor(colors);
        return this;
    }

    public FireworkBuilder addFadeColor(final Color... colors)
    {
        builder.withFade(colors);
        return this;
    }

    public FireworkBuilder setType(final org.bukkit.FireworkEffect.Type t)
    {
        builder.with(t);
        return this;
    }

    private FireworkEffect effect;
    public void spawn(final Location l, final int detonateTicks)
    {
        if(effect == null)
        {
            effect = builder.build();
        }
        
        Firework fw = (Firework)l.getWorld().spawnEntity(l, EntityType.FIREWORK);

        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(effect);
        fw.setFireworkMeta(meta);
        
        fw.setMetadata(FIREWORK_DATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(FireworkBuilder.class), true));
        
        TaskUtil.scheduleSync(detonateTicks, () -> fw.detonate());
    }
}
