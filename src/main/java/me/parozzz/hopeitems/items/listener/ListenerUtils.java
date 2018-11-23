/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.listener;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.utilities.TaskUtil;
import me.parozzz.reflex.utilities.Util;
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
        ARROW(Material.ARROW),
        TIPPED_ARROW(Util.ifCheck(MCVersion.V1_9.isHigher(), () -> Material.TIPPED_ARROW, () -> null)),
        SPECTRAL_ARROW(Util.ifCheck(MCVersion.V1_9.isHigher(), () -> Material.SPECTRAL_ARROW, () -> null)),
        SNOWBALL(Material.SNOWBALL),
        ENDER_PEARL(Material.ENDER_PEARL),
        SPLASH_POTION(Util.ifCheck(MCVersion.V1_9.isHigher(), () -> Material.SPLASH_POTION, () -> Material.POTION)),
        LINGERING_POTION(Util.ifCheck(MCVersion.V1_9.isHigher(), () -> Material.LINGERING_POTION, () -> null)),
        NONE(null);
        
        private final Material m;
        private ProjectileType(final @Nullable Material m)
        {
            this.m = m;
        }
        
        /**
         * The material associated to the Type
         * @return The materila associated, or null if that material doesn't exist in that version or is not a projectile type (Shouldn't be a problem though)
         */
        public @Nullable Material getMaterial()
        {
            return m;
        }
        
        
        public static ProjectileType getByMaterial(final Material m)
        {
            switch(m)
            {
                case ARROW:
                    return ARROW;
                case ENDER_PEARL:
                    return ENDER_PEARL;
                case SNOWBALL:
                    return SNOWBALL;
            }
            
            if(MCVersion.V1_9.isHigher())
            {
                switch(m)
                {
                    case TIPPED_ARROW:
                        return TIPPED_ARROW;
                    case SPECTRAL_ARROW:
                        return SPECTRAL_ARROW;
                    case LINGERING_POTION:
                        return LINGERING_POTION;
                }
            }
            
            return NONE;
        }
        
        public static ProjectileType getByEntityType(final EntityType et)
        {
            switch(et)
            {
                case ARROW: 
                    return ARROW;
                case SPLASH_POTION:
                    return SPLASH_POTION;
                case SNOWBALL:
                    return SNOWBALL;
                case ENDER_PEARL:
                    return ENDER_PEARL;
            }
            
            if(MCVersion.V1_9.isHigher())
            {
                switch(et)
                {
                    case TIPPED_ARROW:
                        return TIPPED_ARROW;
                    case SPECTRAL_ARROW:
                        return SPECTRAL_ARROW;
                    case LINGERING_POTION:
                        return LINGERING_POTION;
                }
            }
            
            return NONE;
        }
    }
    
    protected static Stream<ItemStack> arrowPriority(final Player p)
    {
        return (MCVersion.V1_8.isEqual() ? 
                Stream.of(p.getInventory().getContents()) : 
                Stream.concat(Stream.of(p.getInventory().getItemInOffHand()), Stream.of(p.getInventory().getContents()))).filter(Objects::nonNull);
    }
    
    protected static boolean isArrow(final Material m)
    {
        return MCVersion.V1_8.isEqual() ? m == Material.ARROW : Util.or(m, Material.ARROW, Material.TIPPED_ARROW, Material.SPECTRAL_ARROW);
    }
    
    protected static ItemStack checkHands(final Player p, final Material m)
    {
        return MCVersion.V1_8.isEqual() ?
                Optional.ofNullable(p.getItemInHand())
                        .filter(i -> i.getType() == m)
                        .orElse(null) :
                Optional.ofNullable(p.getInventory().getItemInMainHand())
                        .filter(i -> i.getType() == m)
                        .orElseGet(() -> Optional.ofNullable(p.getInventory().getItemInOffHand())
                                .filter(i -> i.getType() == m)
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
                        .filter(i -> i.getType() != Material.AIR)
                        .orElseGet(() -> Optional.ofNullable(p.getInventory().getItemInOffHand())
                                .filter(i -> i.getType() != Material.AIR)
                                .orElse(null));
    }
    
    protected static void giveDelayedItem(final Player p, final ItemStack item)
    {
        TaskUtil.scheduleSync(1L, () -> 
        {
            p.getInventory().addItem(item);
            p.updateInventory();
        });
    }
}
