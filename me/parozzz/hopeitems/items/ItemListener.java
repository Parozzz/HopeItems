/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import me.parozzz.hopeitems.Configs;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public class ItemListener implements Listener
{
    private final Function<Player, Stream<ItemStack>> arrowPriority;
    private final Function<Player, ItemStack> itemEntityInteract;
    public ItemListener()
    {
        arrowPriority = MCVersion.V1_8.isEqual() ?
                p -> Stream.of(p.getInventory().getContents()) :
                p -> Stream.concat(Stream.of(p.getInventory().getItemInOffHand()), Stream.of(p.getInventory().getContents()));
        
        itemEntityInteract = MCVersion.V1_8.isEqual() ?
                p -> p.getItemInHand() :
                p ->
                {
                    switch(p.getMainHand())
                    {
                        case LEFT:
                            return p.getInventory().getItemInOffHand();
                        case RIGHT:
                            return p.getInventory().getItemInMainHand();
                        default:
                            return null;
                    }
                };
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onItemInteract(final PlayerInteractEvent e)
    {
        Optional.ofNullable(e.getClickedBlock())
                .map(Block::getLocation)
                .flatMap(l -> Optional.ofNullable(BlockManager.getInstance().getBlockInfo(l)))
                .ifPresent(info -> 
                {
                    if(info.removeOnUse)
                    {
                        BlockManager.getInstance().removeBlock(e.getClickedBlock());
                    }
                    
                    info.execute(e.getClickedBlock().getLocation().add(0.5, 1, 0.5), e.getPlayer());
                });
        
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
                else if(Utils.or(e.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK) && item.getType().name().contains("POTION") && 
                        (info.hasWhen(When.SPLASH) || info.hasWhen(When.LINGERING)))
                {
                    if(!info.removeOnUse)
                    {
                        e.setCancelled(true);
                        e.getPlayer().launchProjectile(ThrownPotion.class).setItem(item);
                    }
                }
            });
        });
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onEntityInteract(final PlayerInteractAtEntityEvent e)
    {
        getOptional(itemEntityInteract.apply(e.getPlayer())).ifPresent(info -> e.setCancelled(true));
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
        getOptional(e.getEntity().getItem(), When.CONSUME).ifPresent(info -> 
        {
            e.getAffectedEntities().stream().filter(Player.class::isInstance).map(Player.class::cast).forEach(p -> info.execute(p.getLocation(), p));
            info.spawnMobs(e.getEntity().getLocation());
        });
    }
    
    private final static String ARROW_METADATA="CustomArrow";
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onDispense(final BlockDispenseEvent e)
    {
        if(e.getBlock().getType()!=Material.DISPENSER)
        {
            return;
        }
        
        ItemStack used=Stream.of(((Dispenser)e.getBlock().getState()).getInventory().getContents())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(e.getItem());
        
        getOptional(used).ifPresent(info -> 
        {
            e.setCancelled(true);
            if(info.hasWhen(When.DISPENSE))
            {      
                info.execute(e.getBlock().getRelative(((DirectionalContainer)e.getBlock().getState().getData()).getFacing()).getLocation().add(0.5,0.5,0.5), null);
            }
            else if(used.getType().name().contains("ARROW") && info.hasWhen(When.ARROW))
            {
                spawnArrow(((Dispenser)e.getBlock().getState()).getBlockProjectileSource(), used)
                        .setMetadata(ARROW_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(ItemListener.class), info));
            }
            
            if(info.hasWhen(When.ARROW) || info.removeOnUse)
            {
                if(Stream.of(((Dispenser)e.getBlock().getState()).getInventory().getContents()).allMatch(Objects::isNull))
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run() 
                        {
                            ((Dispenser)e.getBlock().getState()).getInventory().clear();
                        }
                    }.runTaskLater(JavaPlugin.getProvidingPlugin(ItemListener.class), 1L);
                }
                else
                {
                    this.decreaseArrow(info, used, ((Dispenser)e.getBlock().getState()).getInventory());
                }
            }
        });
    }
    
    private final Set<UUID> shooters=new HashSet<>();
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onArrowLaunch(final ProjectileLaunchEvent e)
    {
        if(e.getEntity() instanceof Arrow && e.getEntity().getShooter() instanceof Player)
        {
            Player p = (Player)e.getEntity().getShooter();
            if(shooters.remove(p.getUniqueId()))
            {
                return;
            }
                        
            e.setCancelled(true);
            
            shooters.add(p.getUniqueId());
            
            new BukkitRunnable()
            {
                @Override
                public void run() 
                {
                    arrowPriority.apply(p).filter(Objects::nonNull)
                            .filter(item -> item.getType().name().contains("ARROW"))
                            .findFirst().ifPresent(used -> 
                            {
                                Arrow arrow=spawnArrow(p, used);
                                getOptional(used, When.ARROW).ifPresent(info -> 
                                {
                                    arrow.setMetadata(ARROW_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(ItemListener.class), info));
                                    
                                    decreaseArrow(info, used, p.getInventory());
                                });
                                
                                arrow.setCritical(((Arrow)e.getEntity()).isCritical());
                                arrow.setVelocity(e.getEntity().getVelocity());
                            });
                }

            }.runTaskLater(JavaPlugin.getProvidingPlugin(ItemListener.class), 1L);
        }
    }
    
    private void decreaseArrow(final ItemInfo info, final ItemStack item, final Inventory i)
    {
        if(info.hasWhen(When.ARROW) && MCVersion.V1_9.isHigher())
        {
            Utils.decreaseItemStack(item, i);
            return;
        }

        if(info.removeOnUse)
        {
            Utils.decreaseItemStack(item, i);
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onProjectileHit(final ProjectileHitEvent e)
    {
        if(e.getEntity().hasMetadata(ARROW_METADATA))
        {
            ItemInfo info = ((ItemInfo)e.getEntity().getMetadata(ARROW_METADATA).get(0).value());
            info.execute(e.getEntity().getLocation(), e.getEntity().getShooter() instanceof Player ? (Player)e.getEntity().getShooter() : null);
            
            if(info.removeOnUse)
            {
                e.getEntity().remove();
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
                    info.execute(e.getPlayer().getLocation(), e.getPlayer());
                    
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
                }
                
                if(info.hasWhen(When.DROPONGROUND))
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run() 
                        {
                            if(!e.getItemDrop().isValid() || e.getItemDrop().isDead())
                            {
                                this.cancel();
                            }
                            else if(e.getItemDrop().isOnGround())
                            {
                                this.cancel();
                                
                                info.execute(e.getItemDrop().getLocation(), e.getPlayer());
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
                            }
                        }
                    
                    }.runTaskTimer(JavaPlugin.getProvidingPlugin(ItemListener.class), 2L, 1L);
                }
            }
            else
            {
                e.setCancelled(true);
            }
        });
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onDamage(final EntityDamageByEntityEvent e)
    {
        if(e.getDamager().getType()==EntityType.PLAYER)
        {
            Player p = (Player)e.getDamager();
            Optional.ofNullable(Utils.getMainHand(p.getEquipment()))
                    .filter(item -> item.getType()!=Material.AIR)
                    .ifPresent(hand -> 
                    {
                        getOptional(hand, When.ATTACKSELF, When.ATTACKOTHER).ifPresent(info -> 
                        {
                            if(info.checkConditions(p.getLocation(), p) && !info.hasCooldown(p))
                            {
                                if(info.hasWhen(When.ATTACKSELF))
                                {
                                    info.execute(p.getLocation(), p);
                                }
                            
                                if(e.getEntityType() == EntityType.PLAYER && info.hasWhen(When.ATTACKOTHER))
                                {
                                    Player other = (Player)e.getEntity();
                                    info.execute(other.getLocation(), other);
                                }
                            }
                            
                            
                        });
                    });
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onBlockPlace(final BlockPlaceEvent e)
    {
        getOptional(e.getItemInHand(), When.BLOCKINTERACT, When.BLOCKSTEP, When.BLOCKDESTROY).ifPresent(info -> 
        {
            BlockManager.getInstance().addBlock(e.getBlockPlaced(), info);
        });
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onBlockBreak(final BlockBreakEvent e)
    {
        Optional.ofNullable(BlockManager.getInstance().removeBlock(e.getBlock()))
                .filter(info -> info.hasWhen(When.BLOCKDESTROY))
                .ifPresent(info -> 
                {
                    Location l = e.getBlock().getLocation().add(0.5, 1, 0.5);
                    info.execute(l, e.getPlayer());
                    
                    if(!info.removeOnUse)
                    {
                        l.getWorld().dropItemNaturally(l, info.getItem().parse(e.getPlayer(), e.getBlock().getLocation()));
                    }
                });
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
                        if(info.removeOnUse)
                        {
                            BlockManager.getInstance().removeBlock(e.getTo().getBlock().getLocation());
                        }
                        
                        info.execute(e.getTo(), e.getPlayer());
                    });
        }
    }
    
    private Arrow spawnArrow(final ProjectileSource source, final ItemStack item)
    {
        switch(item.getType())
        {
            case ARROW:
                return source.launchProjectile(Arrow.class);
            case TIPPED_ARROW:
                Arrow arrow=source.launchProjectile(TippedArrow.class);

                PotionMeta meta=(PotionMeta)item.getItemMeta();
                
                meta.getCustomEffects().forEach(pe -> ((TippedArrow)arrow).addCustomEffect(pe, true));
                if(MCVersion.V1_11.isHigher() && meta.hasColor()) 
                { 
                    ((TippedArrow)arrow).setColor(meta.getColor()); 
                }
                return arrow;
            case SPECTRAL_ARROW:
                return source.launchProjectile(SpectralArrow.class);
            default:
                return null;
        }
    }
    
    public static void register1_9Listener()
    {
        Listener l=new Listener()
        {
            private final String LINGERING_METADATA="CustomLingeringPotion";
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onLingeringSplash(final LingeringPotionSplashEvent e)
            {
                getOptional(e.getEntity().getItem()).ifPresent(info -> 
                {
                    if(info.hasWhen(When.SPLASH))
                    {
                        Player p =  e.getEntity().getShooter() instanceof Player ? (Player)e.getEntity().getShooter() : null;
                        
                        info.execute(e.getEntity().getLocation(), p);
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
                if(e.getArrow().hasMetadata(ARROW_METADATA))
                {
                    e.setCancelled(true);
                    e.getArrow().remove();

                    e.getPlayer().getInventory()
                            .addItem(((ItemInfo)e.getArrow().getMetadata(ARROW_METADATA).get(0).value()).getItem().parse(e.getPlayer(), e.getArrow().getLocation()));
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
                            .forEach(p -> 
                            {
                                info.executeActions(p.getLocation(), p);
                                info.executeLucky(p);
                            });
                }
            }
        };
        
        Bukkit.getPluginManager().registerEvents(l, JavaPlugin.getProvidingPlugin(ItemListener.class));
    }
    
    private static Optional<ItemInfo> getOptional(final ItemStack item, final When... w)
    {
        return Optional.ofNullable(item)
                .filter(Objects::nonNull)
                .map(HItem::new)
                .filter(HItem::isValid)
                .map(HItem::getStringId)
                .flatMap(id -> Optional.ofNullable(Configs.getItemInfo(id)))
                .filter(info -> w.length==0 || Stream.of(w).anyMatch(info::hasWhen));
    }
}
