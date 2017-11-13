/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.listener;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.classes.Task;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.TippedArrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.projectiles.ProjectileSource;

/**
 *
 * @author Paros
 */
public class ListenerUtils 
{
    protected enum ProjectileType
    {
        ARROW, SNOW_BALL, ENDER_PEARL, SPLASH_POTION, LINGERING_POTION;
        
        public static ProjectileType getByMaterial(final Material m)
        {
            switch(m)
            {
                case ARROW:
                    return ProjectileType.ARROW;
                case ENDER_PEARL:
                    return ProjectileType.ENDER_PEARL;
                case SNOW_BALL:
                    return ProjectileType.SNOW_BALL;
            }
            
            if(MCVersion.V1_9.isHigher())
            {
                switch(m)
                {
                    case TIPPED_ARROW:
                    case SPECTRAL_ARROW:
                        return ProjectileType.ARROW;
                }
            }
            return null;
        }
        
        public static ProjectileType getByEntityType(final EntityType et)
        {
            switch(et)
            {
                case ARROW: 
                    return ProjectileType.ARROW;
                case SPLASH_POTION:
                    return ProjectileType.SPLASH_POTION;
                case SNOWBALL:
                    return ProjectileType.SNOW_BALL;
            }
            
            if(MCVersion.V1_9.isHigher())
            {
                switch(et)
                {
                    case TIPPED_ARROW:
                    case SPECTRAL_ARROW:
                        return ProjectileType.ARROW;
                    case LINGERING_POTION:
                        return ProjectileType.LINGERING_POTION;
                }
            }
            
            return null;
        }
    }
    
    protected static Stream<ItemStack> arrowPriority(final Player p)
    {
        return MCVersion.V1_8.isEqual() ? 
                Stream.of(p.getInventory().getContents()) : 
                Stream.concat(Stream.of(p.getInventory().getItemInOffHand()), Stream.of(p.getInventory().getContents()));
    }
    
    protected static boolean isArrow(final Material m)
    {
        return MCVersion.V1_8.isEqual() ? m == Material.ARROW : Utils.or(m, Material.ARROW, Material.TIPPED_ARROW, Material.SPECTRAL_ARROW);
    }
    
    protected static ItemStack checkHands(final Player p, final Material m)
    {
        return MCVersion.V1_8.isEqual() ?
                Optional.ofNullable(p.getItemInHand())
                .filter(i -> i.getType()==m)
                .orElse(null) :
                Optional.ofNullable(p.getInventory().getItemInMainHand())
                        .filter(i -> i.getType()==m)
                        .orElseGet(() -> Optional.ofNullable(p.getInventory().getItemInOffHand())
                                .filter(i -> i.getType()==m)
                                .orElse(null));
    }
    
    protected static Arrow shootArrow(final ItemStack item, final ProjectileSource source)
    {
        if(MCVersion.V1_8.isEqual())
        {
            return source.launchProjectile(Arrow.class);
        } 
        else
        {
            switch(item.getType())
            {
                case ARROW:
                    return source.launchProjectile(Arrow.class);
                case TIPPED_ARROW:
                    Arrow tipped = source.launchProjectile(TippedArrow.class);

                    PotionMeta meta=(PotionMeta)item.getItemMeta();
                    meta.getCustomEffects().forEach(pe -> ((TippedArrow)tipped).addCustomEffect(pe, true));
                    if(MCVersion.V1_11.isHigher() && meta.hasColor()) 
                    { 
                        ((TippedArrow)tipped).setColor(meta.getColor()); 
                    }

                    return tipped;
                case SPECTRAL_ARROW:
                    return source.launchProjectile(SpectralArrow.class);
                default:
                    return null;
            }
        }
    }
    
    protected static ItemStack getUsedItem(final Player p)
    {
        return MCVersion.V1_8.isEqual() ? p.getItemInHand() :
                Optional.ofNullable(p.getInventory().getItemInMainHand())
                        .filter(i -> i.getType()!=Material.AIR)
                        .orElseGet(() -> Optional.ofNullable(p.getInventory().getItemInOffHand())
                                .filter(i -> i.getType()!=Material.AIR)
                                .orElse(null));
    }
    
    protected static void giveDelayedItem(final Player p, final ItemStack item)
    {
        Task.scheduleSync(1L, () -> 
        {
            p.getInventory().addItem(item);
            p.updateInventory();
        });
    }
}
