/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.listener;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import me.parozzz.hopeitems.Configs;
import me.parozzz.hopeitems.items.BlockManager;
import me.parozzz.hopeitems.items.HItem;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.hopeitems.items.listener.ListenerUtils.ProjectileType;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.classes.Task;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class ItemListener implements Listener
{
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onItemInteract(final PlayerInteractEvent e)
    {
        Optional.ofNullable(e.getClickedBlock())
                .map(Block::getLocation)
                .flatMap(l -> Optional.ofNullable(BlockManager.getInstance().getBlockInfo(l)))
                .filter(info -> info.execute(e.getClickedBlock().getLocation().add(0.5, 1, 0.5), e.getPlayer(), true) && info.removeOnUse)
                .ifPresent(info -> BlockManager.getInstance().removeBlock(e.getClickedBlock()));
        
        Optional.ofNullable(e.getItem()).ifPresent(item -> 
        {
            if(Utils.or(e.getAction(), Action.LEFT_CLICK_BLOCK, Action.RIGHT_CLICK_BLOCK) && e.isCancelled())
            {
                return;
            }
            
            getOptional(item).ifPresent(info -> 
            {
                if(info.hasWhen(When.INTERACT))
                {
                    e.setCancelled(true);

                    final Location l=Optional.ofNullable(e.getClickedBlock())
                            .map(Block::getLocation)
                            .map(temp -> temp.add(0.5, 1, 0.5))
                            .orElseGet(() -> e.getPlayer().getLocation());

                    info.executeWithItem(l, e.getPlayer(), item);
                }
            });
        });
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onEntityInteract(final PlayerInteractAtEntityEvent e)
    {
        getOptional(ListenerUtils.getUsedItem(e.getPlayer())).ifPresent(info -> e.setCancelled(true));
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onItemConsume(final PlayerItemConsumeEvent e)
    {
        getOptional(e.getItem(), When.CONSUME).ifPresent(info -> 
        {
            e.setCancelled(true);
            info.executeWithItem(e.getPlayer().getLocation(), e.getPlayer(), e.getItem());
        });
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onPotionSplash(final PotionSplashEvent e)
    {
        getOptional(e.getEntity().getItem(), When.SPLASH).ifPresent(info -> 
        {
            e.getAffectedEntities().stream()
                    .filter(LivingEntity.class::isInstance)
                    .forEach(ent -> info.execute(ent.getLocation(), ent instanceof Player ? (Player)ent : null, false));
            info.spawnMobs(e.getEntity().getLocation());
        });
    }
    
    private final static String PROJECTILE_METADATA="HopeItems.CustomPrjectile";
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onDispense(final BlockDispenseEvent e)
    {
        if(e.getBlock().getType()!=Material.DISPENSER)
        {
            return;
        }
        
        Dispenser d = (Dispenser)e.getBlock().getState();
        
        ItemStack used = Stream.of(d.getInventory().getContents()).filter(Objects::nonNull).findFirst().orElse(e.getItem());
        
        getOptional(used).ifPresent(info -> 
        {
            e.setCancelled(true);
            if(info.hasWhen(When.DISPENSE))
            {      
                info.execute(e.getBlock().getRelative(((DirectionalContainer)e.getBlock().getState().getData()).getFacing()).getLocation().add(0.5,0.5,0.5), null, true);
            }
            else if(info.hasProjectileWhens())
            {
                Projectile proj;
                switch(ProjectileType.getByMaterial(used.getType()))
                {
                    case ARROW:
                        proj = ListenerUtils.shootArrow(used, d.getBlockProjectileSource());
                        break;
                    case SNOW_BALL:
                        proj = d.getBlockProjectileSource().launchProjectile(Snowball.class);
                        break;
                    default:
                        return;
                }
                proj.setMetadata(PROJECTILE_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(ItemListener.class), info));
            }
            
            boolean arrow = ListenerUtils.isArrow(used.getType());
            if(!arrow && !info.removeOnUse)
            {
                return;
            }

            if(used.getAmount() == 1 && Stream.of(d.getInventory().getContents()).allMatch(Objects::isNull))
            {
                Task.scheduleSync(1L, () -> d.getInventory().clear());
            }
            else
            {
                if(arrow && MCVersion.V1_9.isHigher())
                {
                    Utils.decreaseItemStack(used, d.getInventory());
                    return;
                }

                if(info.removeOnUse)
                {
                    Utils.decreaseItemStack(used, d.getInventory());
                }
            }
        });
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onProjectileLaunch(final ProjectileLaunchEvent e)
    {
        if(e.getEntity().getShooter() instanceof Player)
        {
            Player p = (Player)e.getEntity().getShooter();
            switch(ProjectileType.getByEntityType(e.getEntityType()))
            {
                case SPLASH_POTION:
                    e.setCancelled(this.onPotionSpawn((ThrownPotion)e.getEntity(), When.SPLASH, p));
                    break;
                case LINGERING_POTION:
                    e.setCancelled(this.onPotionSpawn((ThrownPotion)e.getEntity(), When.LINGERING, p));
                    break;
                case ENDER_PEARL:
                case SNOW_BALL:
                    Optional.ofNullable(ListenerUtils.checkHands(p, Material.valueOf(e.getEntityType().name()))).ifPresent(item -> 
                    {
                        getOptional(item, When.PROJECTILEDAMAGE, When.PROJECTILEHIT).ifPresent(info -> 
                        {
                            e.getEntity().setMetadata(PROJECTILE_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(ItemListener.class), info));

                            if(p.getGameMode()!=GameMode.CREATIVE && !info.removeOnUse)
                            {
                                ItemStack toGive = item.clone();
                                toGive.setAmount(1);
                                ListenerUtils.giveDelayedItem(p, toGive);
                            }
                        });
                    });
                    break;
                case ARROW:
                    ListenerUtils.arrowPriority(p)
                            .filter(Objects::nonNull)
                            .filter(item ->  e.getEntityType().name().contains(item.getType().name()))
                            .findFirst().ifPresent(used -> 
                            {
                                getOptional(used, When.PROJECTILEDAMAGE, When.PROJECTILEHIT).ifPresent(info -> 
                                {
                                    if(!info.checkConditions(p.getLocation(), p) || info.hasCooldown(p))
                                    {
                                        e.setCancelled(true);
                                        return;
                                    }

                                    e.getEntity().setMetadata(PROJECTILE_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(ItemListener.class), info));

                                    if(p.getGameMode()!=GameMode.CREATIVE && MCVersion.V1_8.isEqual() && !info.removeOnUse)
                                    {
                                        ItemStack giveBack = used.clone();
                                        giveBack.setAmount(1);
                                        ListenerUtils.giveDelayedItem(p, used);
                                    }
                                });
                            });
                    break;
            }
        }
    }
    
    private boolean onPotionSpawn(final ThrownPotion tp, final When w, final Player p)
    {
        ItemStack thrown = tp.getItem().clone();
        return getOptional(thrown).map(info -> 
        {
            if(info.hasWhen(w))
            {
                if(!info.checkConditions(p.getLocation(), p) || info.hasCooldown(p))
                {
                    if(p.getGameMode() != GameMode.CREATIVE)
                    {
                        thrown.setAmount(1);
                        ListenerUtils.giveDelayedItem(p, thrown);
                    }
                    return true;
                }
                else if(p.getGameMode() !=GameMode.CREATIVE && !info.removeOnUse)
                {
                    thrown.setAmount(1);
                    ListenerUtils.giveDelayedItem(p, thrown);
                }
            }
            return false;
        }).orElse(false);
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onProjectileHit(final ProjectileHitEvent e)
    {
        if(e.getEntity().hasMetadata(PROJECTILE_METADATA))
        {
            ItemInfo info = ((ItemInfo)e.getEntity().getMetadata(PROJECTILE_METADATA).get(0).value());
            
            if(info.hasWhen(When.PROJECTILEDAMAGE))
            {
                Task.scheduleSync(1L, () -> 
                {
                    if(info.hasWhen(When.PROJECTILEHIT) && e.getEntity().hasMetadata(PROJECTILE_METADATA))
                    {
                        executeOnProjectileHit(e.getEntity(), info);
                    }
                }); 
            }
            else
            {
                executeOnProjectileHit(e.getEntity(), info);
            }
        }
    }
    
    private void executeOnProjectileHit(final Projectile pj, final ItemInfo info)
    {
        if(pj instanceof Arrow)
        {
            if(MCVersion.V1_8.isEqual() || info.removeOnUse)
            {
                pj.remove();
            }
        }
        
        info.execute(pj.getLocation(), pj.getShooter() instanceof Player ? (Player)pj.getShooter() : null, false);
        pj.removeMetadata(PROJECTILE_METADATA, JavaPlugin.getProvidingPlugin(ItemListener.class));
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onDamage(final EntityDamageByEntityEvent e)
    {
        if(e.getDamager().getType() == EntityType.PLAYER)
        {
            Player p = (Player)e.getDamager();
            Optional.ofNullable(Utils.getMainHand(p.getEquipment()))
                    .filter(item -> item.getType()!=Material.AIR)
                    .ifPresent(hand -> 
                    {
                        getOptional(hand, When.ATTACKSELF, When.ATTACKOTHER).ifPresent(info -> 
                        {
                            Location l = p.getLocation();
                            if(info.checkConditions(l, p) && !info.hasCooldown(p))
                            {
                                if(info.hasWhen(When.ATTACKSELF))
                                {
                                    info.execute(l, p, false);
                                }
                            
                                if(e.getEntityType() == EntityType.PLAYER && info.hasWhen(When.ATTACKOTHER))
                                {
                                    Player other = (Player)e.getEntity();
                                    info.execute(other.getLocation(), other, false);
                                }
                            }
                        });
                    });
        }
        else if(e.getDamager() instanceof Projectile && e.getDamager().hasMetadata(PROJECTILE_METADATA))
        {
            ItemInfo info = (ItemInfo)e.getDamager().getMetadata(PROJECTILE_METADATA).get(0).value();
            e.getDamager().removeMetadata(PROJECTILE_METADATA, JavaPlugin.getProvidingPlugin(ItemListener.class));

            info.execute(e.getDamager().getLocation(), e.getEntityType() == EntityType.PLAYER ? (Player)e.getEntity() : null, false);
            if(info.removeOnUse)
            {
                e.getDamager().remove();
            }
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onItemDrop(final PlayerDropItemEvent e)
    {
        getOptional(e.getItemDrop().getItemStack(), When.DROP, When.DROPONGROUND).ifPresent(info -> 
        {
            if(info.checkConditions(e.getPlayer().getLocation(), e.getPlayer()) && !info.hasCooldown(e.getPlayer()))
            {
                if(info.hasWhen(When.DROP))
                {
                    info.execute(e.getPlayer().getLocation(), e.getPlayer(), false);
                    
                    if(info.removeOnUse)
                    {
                        if(e.getItemDrop().getItemStack().getAmount()==1)
                        {
                            e.getItemDrop().remove();
                            return;
                        }
                        else
                        {
                            e.getItemDrop().getItemStack().setAmount(e.getItemDrop().getItemStack().getAmount()-1);
                        }
                    }
                }
                
                if(info.hasWhen(When.DROPONGROUND))
                {
                    Task.scheduleSyncTimer(1L, 1L, () -> 
                    {
                        if(e.getItemDrop().isOnGround())
                        {
                            info.execute(e.getItemDrop().getLocation(), e.getPlayer(), false);
                            if(info.removeOnUse)
                            {
                                if(e.getItemDrop().getItemStack().getAmount()==1)
                                {
                                    e.getItemDrop().remove();
                                }
                                else
                                {
                                    e.getItemDrop().getItemStack().setAmount(e.getItemDrop().getItemStack().getAmount()-1);
                                } 
                            }
                            return true;
                        }
                        
                        return !e.getItemDrop().isValid() || e.getItemDrop().isDead();
                    });
                }
            }
            else
            {
                e.setCancelled(true);
            }
        });
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onBlockPlace(final BlockPlaceEvent e)
    {
        getOptional(e.getItemInHand(), When.BLOCKINTERACT, When.BLOCKSTEP, When.BLOCKDESTROY)
                .ifPresent(info -> BlockManager.getInstance().addBlock(e.getBlockPlaced(), info));
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onBlockBreak(final BlockBreakEvent e)
    {
        Optional.ofNullable(BlockManager.getInstance().removeBlock(e.getBlock()))
                .ifPresent(info -> 
                {              
                    Location l = e.getBlock().getLocation().add(0.5, 1, 0.5);
                    if(info.hasWhen(When.BLOCKDESTROY))
                    {
                        if(info.execute(l, e.getPlayer(), true) && !info.removeOnUse)
                        {
                            this.cancelDrop(e);
                            l.getWorld().dropItemNaturally(l, info.getItem().parse(e.getPlayer(), e.getBlock().getLocation()));
                        }
                    }
                    else
                    {
                        l.getWorld().dropItemNaturally(l, info.getItem().parse(e.getPlayer(), e.getBlock().getLocation()));
                        this.cancelDrop(e);
                    }
                });
    }
    
    private void cancelDrop(final BlockBreakEvent e)
    {
        if(MCVersion.V1_12.isHigher())
        {
            e.setDropItems(false);
        }
        else
        {
            e.setCancelled(true);
            e.getBlock().setType(Material.AIR);
        } 
    }
    
    @EventHandler(ignoreCancelled=false,priority=EventPriority.HIGHEST)
    private void onCustomBlockExplosion(final EntityExplodeEvent e)
    {
        e.blockList().forEach(BlockManager.getInstance()::removeBlock);
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onMove(final PlayerMoveEvent e)
    {
        if(!e.getFrom().getBlock().equals(e.getTo().getBlock()))
        {
            Optional.ofNullable(BlockManager.getInstance().getBlockInfo(e.getTo().getBlock().getRelative(BlockFace.DOWN).getLocation()))
                    .filter(info -> info.hasWhen(When.BLOCKSTEP))
                    .ifPresent(info -> 
                    {
                        if(info.execute(e.getTo(), e.getPlayer(), true) && info.removeOnUse)
                        {
                            BlockManager.getInstance().removeBlock(e.getTo().getBlock().getLocation());
                        }
                    });
        }
    }
    
    public static void register1_9Listener()
    {
        Listener l=new Listener()
        {
            private final String LINGERING_METADATA="HopeItems.CustomLingeringPotion";
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onLingeringSplash(final LingeringPotionSplashEvent e)
            {
                getOptional(e.getEntity().getItem()).ifPresent(info -> 
                {
                    if(info.hasWhen(When.SPLASH))
                    {
                        info.execute(e.getEntity().getLocation(), e.getEntity().getShooter() instanceof Player ? (Player)e.getEntity().getShooter() : null, false);
                    }
                    
                    if(info.hasWhen(When.LINGERING))
                    {
                        e.getAreaEffectCloud().setMetadata(LINGERING_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(ItemListener.class), info));
                    }
                });
            }
            
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onArrowPickup(final PlayerPickupArrowEvent e)
            {
                if(e.getArrow().hasMetadata(PROJECTILE_METADATA))
                {
                    e.setCancelled(true);
                    e.getArrow().remove();
                    
                    ItemInfo info = (ItemInfo)e.getArrow().getMetadata(PROJECTILE_METADATA).get(0).value();
                    e.getPlayer().getInventory().addItem(info.getItem().parse(e.getPlayer(), e.getArrow().getLocation()));
                    e.getPlayer().updateInventory();
                }
            }
    
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onCloudApply(final AreaEffectCloudApplyEvent e)
            {
                if(e.getEntity().hasMetadata(LINGERING_METADATA))
                {
                    ItemInfo info=(ItemInfo)e.getEntity().getMetadata(LINGERING_METADATA).get(0).value();
                    
                    e.getAffectedEntities().stream()
                            .filter(Player.class::isInstance)
                            .map(Player.class::cast)
                            .forEach(p -> info.execute(p.getLocation(), p, false));
                }
            }
        };
        
        Bukkit.getPluginManager().registerEvents(l, JavaPlugin.getProvidingPlugin(ItemListener.class));
    }
    
    private static Optional<ItemInfo> getOptional(final ItemStack item, final When... w)
    {
        Optional<ItemInfo> op = Optional.ofNullable(item)
                .filter(Objects::nonNull)
                .map(HItem::new)
                .filter(HItem::isValid)
                .map(HItem::getStringId)
                .flatMap(id -> Optional.ofNullable(Configs.getItemInfo(id)))
                .filter(info -> w.length==0 || Stream.of(w).anyMatch(info::hasWhen));
        return op;
    }
}
