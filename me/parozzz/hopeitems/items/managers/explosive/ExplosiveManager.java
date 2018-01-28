/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.explosive;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.configuration.ComplexMapList;
import me.parozzz.reflex.configuration.SimpleMapList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

/**
 *
 * @author Paros
 */
public class ExplosiveManager 
{
    private enum ExplosiveType
    {
        CREEPER(Creeper.class), 
        TNT(TNTPrimed.class), 
        FIREBALL(LargeFireball.class);
        
        private final Class<? extends Entity> clazz;
        private ExplosiveType(final Class<? extends Entity> clazz)
        {
            this.clazz=clazz;
        }
        
        public Class<? extends Entity> getSpawnClass()
        {
            return clazz;
        }
    }
    
    private final ExplosiveType et;
    
    private final BiConsumer<Entity, ExplosiveMetadata> option;
    
    private final int modifierRange;
    private BiConsumer<EntityExplodeEvent, Set<LivingEntity>> modifier;
    public ExplosiveManager(final ConfigurationSection path)
    {
        try { et=ExplosiveType.valueOf(path.getString("type").toUpperCase()); }
        catch(final IllegalArgumentException t) { throw new IllegalArgumentException("An explosive type named "+path.getString("type")+" does not exist"); }
        
        option = new SimpleMapList(path.getMapList("option")).getView().entrySet().stream()
                .map(e -> Debug.validateEnum(e.getKey(), ExplosiveOption.class).getConsumer(e.getValue().get(0)))
                .reduce(BiConsumer::andThen).orElse((ent, meta) -> {});
        
        modifierRange = path.getInt("modifierRange", -1);
        
        modifier = (e, set) -> {};
        new ComplexMapList(path.getMapList("modifier")).getView().entrySet().forEach(e -> 
        {
            ExplosionModifier modifierEnum = Debug.validateEnum(e.getKey(), ExplosionModifier.class);
            e.getValue().forEach(map -> modifier = modifier.andThen(modifierEnum.getConsumer(map)));
        });
    }
    
    public Entity spawn(final Location l, final ProjectileSource ps)
    {
        Entity ent = et == ExplosiveType.FIREBALL && ps != null ? ps.launchProjectile(LargeFireball.class) : l.getWorld().spawn(l, et.clazz);
        
        this.applyManager(ent);
        
        return ent;
    }
    
    private void applyManager(final Entity ent)
    {
        JavaPlugin plugin=JavaPlugin.getProvidingPlugin(ExplosiveManager.class);
        
        ExplosiveMetadata meta=new ExplosiveMetadata();
        option.accept(ent, meta);
        meta.modifierRange=modifierRange;
        meta.modifier=modifier;
        ent.setMetadata(ExplosiveMetadata.METADATA, new FixedMetadataValue(plugin, meta));
    }
    
    public static void registerListener()
    {
        Bukkit.getPluginManager().registerEvents(new Listener()
        {
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onEntityExplode(final EntityExplodeEvent e)
            {
                if(e.getEntity().hasMetadata(ExplosiveMetadata.METADATA))
                {
                    ExplosiveMetadata meta=(ExplosiveMetadata)e.getEntity().getMetadata(ExplosiveMetadata.METADATA).get(0).value();
                    
                    int range=meta.modifierRange;
                    Set<LivingEntity> set=meta.modifierRange!=-1? 
                            e.getLocation().getWorld().getNearbyEntities(e.getLocation(), range, range, range)
                            .stream()
                            .filter(LivingEntity.class::isInstance)
                            .map(LivingEntity.class::cast)
                            .collect(Collectors.toSet()) :
                            new HashSet<>();
                    
                    meta.modifier.accept(e, set);
                    
                    if(!meta.blockDamage)
                    {
                        e.blockList().clear();
                    }
                }
            }
            
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onExplosionPrime(final ExplosionPrimeEvent e)
            {
                if(e.getEntity().hasMetadata(ExplosiveMetadata.METADATA))
                {
                    ExplosiveMetadata meta=(ExplosiveMetadata)e.getEntity().getMetadata(ExplosiveMetadata.METADATA).get(0).value();
                    
                    e.setFire(meta.fire);
                    e.setRadius(meta.power);
                }
            }
        }, JavaPlugin.getProvidingPlugin(ExplosiveManager.class));
    }
}
