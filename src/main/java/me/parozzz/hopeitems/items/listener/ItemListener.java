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
import me.parozzz.hopeitems.DebugLogger;
import me.parozzz.hopeitems.HopeItems;
import me.parozzz.hopeitems.items.BlockManager;
import me.parozzz.hopeitems.items.CustomItemUtil;
import me.parozzz.hopeitems.items.ItemCollection;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemInfo.ProjectileDamageType;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.hopeitems.items.listener.ListenerUtils.ProjectileType;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.events.armor.ArmorEquipEvent;
import me.parozzz.reflex.events.armor.ArmorUnequipEvent;
import me.parozzz.reflex.utilities.EntityUtil;
import me.parozzz.reflex.utilities.ItemUtil;
import me.parozzz.reflex.utilities.TaskUtil;
import me.parozzz.reflex.utilities.Util;
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
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.metadata.FixedMetadataValue;

/**
 *
 * @author Paros
 */
public class ItemListener implements Listener
{   
    private final HopeItems hopeItems;
    private final BlockManager blockManager;
    public ItemListener(final HopeItems hopeItems, final BlockManager blockManager)
    {
        this.hopeItems = hopeItems;
        this.blockManager = blockManager;
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onItemInteract(final PlayerInteractEvent e)
    {
        Optional.ofNullable(e.getClickedBlock())
                .map(Block::getLocation)
                .flatMap(l -> Optional.ofNullable(blockManager.getBlockInfo(l)))
                .map(collection -> collection.getItemInfo(When.BLOCKINTERACT))
                .filter(Objects::nonNull)
                .filter(info -> info.execute(e.getClickedBlock().getLocation().add(0.5, 1, 0.5), e.getPlayer(), true) && info.removeOnUse)
                .ifPresent(info -> blockManager.removeBlock(e.getClickedBlock()));
        
        if(e.getAction() == Action.PHYSICAL)
        {
            return;
        }
        
        Optional.ofNullable(e.getItem()).ifPresent(item -> 
        {
            if(Util.or(e.getAction(), Action.LEFT_CLICK_BLOCK, Action.RIGHT_CLICK_BLOCK) && e.isCancelled())
            {
                return;
            }
            
            When when = Util.or(e.getAction(), Action.LEFT_CLICK_AIR , Action.LEFT_CLICK_BLOCK) ? When.LEFTINTERACT : When.RIGHTINTERACT;
            
            getOptional(item, when).ifPresent(collection -> 
            {
                e.setCancelled(true);

                final Location l = Optional.ofNullable(e.getClickedBlock())
                        .map(Block::getLocation)
                        .map(temp -> temp.add(0.5, 1, 0.5))
                        .orElseGet(() -> e.getPlayer().getLocation());
                
                e.setCancelled(collection.getItemInfo(when).executeWithItem(l, e.getPlayer(), item));
            });
        });
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onArmorEquip(final ArmorEquipEvent e)
    {
        getOptional(e.getItem(), When.ARMOREQUIP)
                .ifPresent(collection -> e.setCancelled(!collection.getItemInfo(When.ARMOREQUIP).execute(e.getPlayer().getLocation(), e.getPlayer(), true)));
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onArmorUnequip(final ArmorUnequipEvent e)
    {
        getOptional(e.getItem(), When.ARMORUNEQUIP)
                .ifPresent(collection -> e.setCancelled(!collection.getItemInfo(When.ARMORUNEQUIP).execute(e.getPlayer().getLocation(), e.getPlayer(), true)));
    }
    
    @EventHandler(ignoreCancelled = false, priority=EventPriority.HIGHEST)
    private void onEntityInteract(final PlayerInteractAtEntityEvent e)
    {
        getOptional(ListenerUtils.getUsedItem(e.getPlayer())).ifPresent(info -> e.setCancelled(true));
    }
    
    @EventHandler(ignoreCancelled = true, priority=EventPriority.HIGHEST)
    private void onItemConsume(final PlayerItemConsumeEvent e)
    {
        getOptional(e.getItem(), When.CONSUME)
                .ifPresent(collection -> e.setCancelled(collection.getItemInfo(When.CONSUME).executeWithItem(e.getPlayer().getLocation(), e.getPlayer(), e.getItem())));
    }
    
    @EventHandler(ignoreCancelled = false, priority=EventPriority.HIGHEST)
    private void onPotionSplash(final PotionSplashEvent e)
    {
        getOptional(e.getEntity().getItem(), When.SPLASH).ifPresent(collection -> 
        {
            ItemInfo info = collection.getItemInfo(When.SPLASH);
            e.getAffectedEntities().forEach(liv -> 
            {
                if(liv.getType() == EntityType.PLAYER)
                {
                    info.execute(liv.getLocation(), (Player)liv, false);
                }
                else
                {
                    info.execute(liv.getLocation(), liv);
                }
            });
            
            info.spawnMobs(e.getEntity().getLocation(), null);
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
        
        getOptional(used).ifPresent(collection -> 
        {
            e.setCancelled(true);
            if(collection.hasWhen(When.DISPENSE))
            {      
                ItemInfo info = collection.getItemInfo(When.DISPENSE);
                removeFromDispenser(d, used, info);
                
                BlockFace face = ((DirectionalContainer)e.getBlock().getState().getData()).getFacing();
                info.execute(e.getBlock().getRelative(face).getLocation().add(0.5, 0.5, 0.5), d);
            }
            else if(collection.hasWhen(When.PROJECTILE))
            {
                ItemInfo info = collection.getItemInfo(When.PROJECTILE);
                
                Projectile proj;
                switch(ProjectileType.getByMaterial(used.getType()))
                {
                    case ARROW:
                        proj = ListenerUtils.shootArrow(used, d.getBlockProjectileSource());
                        break;
                    case SNOWBALL:
                        proj = d.getBlockProjectileSource().launchProjectile(Snowball.class);
                        break;
                    default:
                        return;
                }
                
                removeFromDispenser(d, used, info);
                proj.setMetadata(PROJECTILE_METADATA, new FixedMetadataValue(hopeItems, info));
            }
        });
    }
    
    private void removeFromDispenser(final Dispenser d,final ItemStack used, final ItemInfo info)
    {
        boolean arrow = ListenerUtils.isArrow(used.getType());
        if((MCVersion.V1_9.isHigher() && arrow) || !info.removeOnUse)
        {
            return;
        }

        if(Stream.of(d.getInventory().getContents()).allMatch(Objects::isNull))
        {
            TaskUtil.scheduleSync(1L, () -> d.getInventory().clear());
        }
        else if(used.getAmount() == 1)
        {
            TaskUtil.scheduleSync(1L, () -> removeFromDispenser(d, used, info, arrow));
        }
        else
        {
            removeFromDispenser(d, used, info, arrow);
        }
    }
    
    private void removeFromDispenser(final Dispenser d, final ItemStack item, final ItemInfo info, final boolean arrow)
    {
        if(arrow && MCVersion.V1_9.isHigher())
        {
            ItemUtil.decreaseItemStack(item, d.getInventory());
            return;
        }

        if(info.removeOnUse)
        {
            ItemUtil.decreaseItemStack(item, d.getInventory());
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onProjectileLaunch(final ProjectileLaunchEvent e)
    {
        if(e.getEntity().getShooter() instanceof Player)
        {
            Player p = (Player)e.getEntity().getShooter();
            ProjectileType projectileType = ProjectileType.getByEntityType(e.getEntityType());
            if(projectileType.getMaterial() == null)
            {
                DebugLogger.getLogger().warn("Null material in a projectileType", ItemListener.class);
                return;
            }
            
            switch(projectileType)
            {
                case SPLASH_POTION:
                    e.setCancelled(this.onPotionSpawn((ThrownPotion)e.getEntity(), When.SPLASH, p));
                    break;
                case LINGERING_POTION: //Shouldn't be a problem because i am using my own enum, so no version dependency.
                    e.setCancelled(this.onPotionSpawn((ThrownPotion)e.getEntity(), When.LINGERING, p));
                    break;
                case ENDER_PEARL:
                case SNOWBALL:
                    Optional.ofNullable(ListenerUtils.checkHands(p, projectileType.getMaterial()))
                            .map(item -> onProjectileSpawn(item, p, e.getEntity()))
                            .ifPresent(e::setCancelled);
                    break;
                case ARROW:
                case TIPPED_ARROW:
                case SPECTRAL_ARROW:
                    ListenerUtils.arrowPriority(p)
                            .filter(item -> item.getType() == projectileType.getMaterial())
                            .findFirst()
                            .map(used -> onProjectileSpawn(used, p, e.getEntity()))
                            .ifPresent(e::setCancelled);
                    break;
            }
        }
    }
    
    private boolean onProjectileSpawn(final ItemStack item, final Player p, final Projectile entity)
    {
        Optional<ItemCollection> optional = ItemListener.getOptional(item, When.PROJECTILE);
        if(optional.isPresent())
        {
            ItemCollection collection = optional.get();
            ItemInfo info = collection.getItemInfo(When.PROJECTILE);
            if(!info.checkConditions(p.getLocation(), p) || collection.hasCooldown(p))
            {
                return true; //So the event is cancelled
            }

            entity.setMetadata(PROJECTILE_METADATA, new FixedMetadataValue(hopeItems, info));

            if(p.getGameMode() != GameMode.CREATIVE && !info.removeOnUse)
            {
                ItemStack toGive = item.clone();
                toGive.setAmount(1);
                ListenerUtils.giveDelayedItem(p, toGive);
            }
        }
        return false; //No need to cancel the event is the item is not custom or everything went fine
    }
    
    private boolean onPotionSpawn(final ThrownPotion tp, final When w, final Player p)
    {
        ItemStack thrown = tp.getItem().clone();
        return getOptional(thrown, w).map(collection -> 
        {
            ItemInfo info = collection.getItemInfo(w);
            
            if(!info.checkConditions(p.getLocation(), p) || collection.hasCooldown(p))
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
                
            return false;
        }).orElse(false);
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onProjectileHit(final ProjectileHitEvent e)
    {
        if(e.getEntity().hasMetadata(PROJECTILE_METADATA))
        {
            ItemInfo info = ((ItemInfo)e.getEntity().getMetadata(PROJECTILE_METADATA).get(0).value());
            
            ProjectileDamageType damageType = info.getProjectileDamageType();
            if(damageType == ProjectileDamageType.DAMAGED)
            {
                DebugLogger.getLogger().info("Skipping projectile hit event. Damage Type only damaged", ItemListener.class);
                return;
            }
            
            Projectile pj = e.getEntity();
            //Wait for the EntityDamageByEntityEvent to proc to see if the arrow has hit an entity. 
            TaskUtil.scheduleSync(1L, () -> 
            {
                if(damageType == ProjectileDamageType.SHOOTER || damageType == ProjectileDamageType.BOTH || 
                        (damageType == ProjectileDamageType.MUTUAL && pj.hasMetadata(PROJECTILE_METADATA)))
                {
                    if(pj instanceof Arrow && (MCVersion.V1_8.isEqual() || info.removeOnUse))
                    {
                        pj.remove();
                    }

                    info.execute(pj.getLocation(), pj.getShooter() instanceof Player ? (Player)pj.getShooter() : null, false);
                    pj.removeMetadata(PROJECTILE_METADATA, hopeItems);
                }
            }); 
        }
    }
    
    @EventHandler(ignoreCancelled = false, priority=EventPriority.HIGHEST)
    private void onDamage(final EntityDamageByEntityEvent e)
    {
        if(e.isCancelled() && e.getEntityType() == EntityType.PLAYER && e.getDamager().getType() == EntityType.PLAYER && !Configs.overridePvpProtection)
        {
            return;
        }
                                    
        if(e.getDamager().getType() == EntityType.PLAYER)
        {
            Player p = (Player)e.getDamager();
            Optional.ofNullable(EntityUtil.getMainHand(p.getEquipment()))
                    .filter(item -> item.getType()!=Material.AIR)
                    .flatMap(hand -> getOptional(hand, When.ATTACKSELF, When.ATTACKOTHER))
                    .ifPresent(collection -> 
                    {
                        if(collection.hasCooldown(p))
                        {
                            return;
                        }
                        
                        if(collection.hasWhen(When.ATTACKSELF))
                        {
                            ItemInfo info = collection.getItemInfo(When.ATTACKSELF);
                            Location loc = p.getLocation();
                            if(info.checkConditions(loc, p))
                            {
                                info.execute(loc, p, false);
                            }
                        }
                        
                        if(e.getEntityType() == EntityType.PLAYER && collection.hasWhen(When.ATTACKOTHER))
                        {
                            ItemInfo info = collection.getItemInfo(When.ATTACKOTHER);

                            Location otherLoc = e.getEntity().getLocation();
                            if(info.checkConditions(otherLoc, p))
                            {
                                info.execute(otherLoc, (Player)e.getEntity(), false);
                            }
                        }
                    });
        }
        else if(e.getDamager() instanceof Projectile && e.getDamager().hasMetadata(PROJECTILE_METADATA))
        {
            ItemInfo info = (ItemInfo)e.getDamager().getMetadata(PROJECTILE_METADATA).get(0).value();
            
            ProjectileDamageType damageType = info.getProjectileDamageType();
            DebugLogger.getLogger().info("Custom projectile entity damage event. ProjectileDamageType " + damageType.name(), ItemListener.class);
            switch(damageType)
            {
                case SHOOTER:
                    return;
                case MUTUAL: //Remove the metadata in case of mutual, so the delayed task in projectile hit event knows has hit someone.
                    e.getDamager().removeMetadata(PROJECTILE_METADATA, hopeItems);
                    break;
                case DAMAGED: //This cases, the damaged will receive the effects anyway.
                case BOTH:
            }
            
            if(e.getEntityType() == EntityType.PLAYER)
            {
                DebugLogger.getLogger().info("Custom projectile player hit", ItemListener.class);
                info.execute(e.getDamager().getLocation(), (Player)e.getEntity(), false);
            }
            else if(e.getEntityType().isAlive())
            {
                DebugLogger.getLogger().info("Custom projectile livingEntity hit", ItemListener.class);
                info.execute(e.getDamager().getLocation(), (LivingEntity)e.getEntity());
            }
            
            if(info.removeOnUse)
            {
                e.getDamager().remove();
            }
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onItemDrop(final PlayerDropItemEvent e)
    {
        getOptional(e.getItemDrop().getItemStack(), When.DROP, When.DROPONGROUND).ifPresent(collection -> 
        {
            if(collection.hasCooldown(e.getPlayer()))
            {
                e.setCancelled(true);
                return;
            }
                                
            if(collection.hasWhen(When.DROP))
            {
                ItemInfo info = collection.getItemInfo(When.DROP);
                if(info.checkConditions(e.getPlayer().getLocation(), e.getPlayer()))
                {
                    info.execute(e.getPlayer().getLocation(), e.getPlayer(), false);

                    if(info.removeOnUse)
                    {
                        if(e.getItemDrop().getItemStack().getAmount() == 1)
                        {
                            e.getItemDrop().remove();
                            return;
                        }
                        else
                        {
                            e.getItemDrop().getItemStack().setAmount(e.getItemDrop().getItemStack().getAmount() - 1);
                        }
                    }
                }
            }
                
            if(collection.hasWhen(When.DROPONGROUND))
            {
                TaskUtil.scheduleSyncTimer(1L, 1L, () -> 
                {
                    if(e.getItemDrop().isOnGround())
                    {
                        ItemInfo info = collection.getItemInfo(When.DROPONGROUND);
                        info.execute(e.getItemDrop().getLocation(), e.getPlayer(), false);
                        if(info.removeOnUse)
                        {
                            Util.ifCheck(e.getItemDrop().getItemStack().getAmount() == 1, 
                                    () -> e.getItemDrop().remove(), 
                                    () -> e.getItemDrop().getItemStack().setAmount(e.getItemDrop().getItemStack().getAmount() - 1));
                        }
                        
                        return true;
                    }

                    return !e.getItemDrop().isValid() || e.getItemDrop().isDead();
                });
            }
        });
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onBlockPlace(final BlockPlaceEvent e)
    {
        getOptional(e.getItemInHand()).ifPresent(collection -> 
        {
            if(collection.hasAnyWhen(When.BLOCKINTERACT, When.BLOCKSTEP, When.BLOCKDESTROY))
            {
                blockManager.addBlock(e.getBlockPlaced(), collection);
            }
            else
            {
                e.setCancelled(true);
            } 
        });
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onBlockBreak(final BlockBreakEvent e)
    {
        ItemStack hand = EntityUtil.getMainHand(e.getPlayer().getEquipment());
        getOptional(hand, When.MINE).ifPresent(collection -> collection.getItemInfo(When.MINE).executeWithItem(e.getBlock().getLocation(), e.getPlayer(), hand));
        
        Optional.ofNullable(blockManager.removeBlock(e.getBlock())).ifPresent(collection -> 
        {              
            if(collection.hasWhen(When.BLOCKDESTROY))
            {
                ItemInfo info = collection.getItemInfo(When.BLOCKDESTROY);
                
                Location l = e.getBlock().getLocation().add(0.5, 1, 0.5);
                if(info.execute(l, e.getPlayer(), true) && !info.removeOnUse)
                {
                    this.cancelDrop(e);
                    l.getWorld().dropItemNaturally(l, collection.getItem().parse(e.getPlayer(), e.getBlock().getLocation()));
                }
            }
            else
            {
                Location l = e.getBlock().getLocation();
                l.getWorld().dropItemNaturally(l, collection.getItem().parse(e.getPlayer(), l));
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
        e.blockList().forEach(blockManager::removeBlock);
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onMove(final PlayerMoveEvent e)
    {
        if(!e.getFrom().getBlock().equals(e.getTo().getBlock()))
        {
            Optional.ofNullable(blockManager.getBlockInfo(e.getTo().getBlock().getRelative(BlockFace.DOWN).getLocation()))
                    .filter(collection -> collection.hasWhen(When.BLOCKSTEP))
                    .map(collection -> collection.getItemInfo(When.BLOCKSTEP))
                    .filter(Objects::nonNull)
                    .ifPresent(info -> 
                    {
                        if(info.execute(e.getTo(), e.getPlayer(), true) && info.removeOnUse)
                        {
                            blockManager.removeBlock(e.getTo().getBlock().getLocation());
                        }
                    });
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onEnchant(final PrepareItemEnchantEvent e)
    {
        getOptional(e.getItem()).filter(collection -> !collection.isEnchantable()).ifPresent(collection -> e.setCancelled(true));
    }
            
                
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onAnvilClick(final InventoryClickEvent e)
    {
        if(e.getInventory().getType() == InventoryType.ANVIL)
        {
            ItemStack zero = e.getInventory().getItem(0);
            getOptional(zero).ifPresent(collection -> 
            {
                switch(e.getAction())
                {
                    case PLACE_ONE:
                    case PLACE_ALL:
                        if(e.getInventory().equals(e.getClickedInventory()) && e.getSlot() == 1)
                        {
                            e.setCancelled(checkEnchantments(zero, e.getCursor()));
                        }
                        break;
                    case MOVE_TO_OTHER_INVENTORY:
                        if(!e.getInventory().equals(e.getClickedInventory()))
                        {
                            e.setCancelled(checkEnchantments(zero, e.getCurrentItem()));
                        }
                        break;
                    case HOTBAR_SWAP:
                        if(e.getInventory().equals(e.getClickedInventory()))
                        {
                            Optional.ofNullable(e.getWhoClicked().getInventory().getItem(e.getHotbarButton())).ifPresent(item -> 
                            {
                                e.setCancelled(checkEnchantments(zero, item));
                            });
                        }
                        break;
                }
            });
        }
    }
            
    private boolean checkEnchantments(final ItemStack zero, final ItemStack one)
    {
        return one.getType() == Material.ENCHANTED_BOOK || 
                (zero.getType() == one.getType() && one.getEnchantments().entrySet().stream().anyMatch(en -> zero.getEnchantmentLevel(en.getKey()) <= en.getValue()));
    }
    
    public static Listener get1_9Listener(final HopeItems hopeItems)
    {
        return new Listener()
        {
            private final String LINGERING_METADATA="HopeItems.CustomLingeringPotion";
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onLingeringSplash(final LingeringPotionSplashEvent e)
            {
                getOptional(e.getEntity().getItem(), When.SPLASH, When.LINGERING).ifPresent(collection -> 
                {
                    if(collection.hasWhen(When.SPLASH))
                    {
                        collection.getItemInfo(When.SPLASH).execute(e.getEntity().getLocation(), e.getEntity().getShooter() instanceof Player ? (Player)e.getEntity().getShooter() : null, false);
                    }
                    
                    if(collection.hasWhen(When.LINGERING))
                    {
                        e.getAreaEffectCloud().setMetadata(LINGERING_METADATA, new FixedMetadataValue(hopeItems, collection.getItemInfo(When.LINGERING)));
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
                    e.getPlayer().getInventory().addItem(info.getCollection().getItem().parse(e.getPlayer(), e.getArrow().getLocation()));
                    e.getPlayer().updateInventory();
                }
            }
    
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onCloudApply(final AreaEffectCloudApplyEvent e)
            {
                if(e.getEntity().hasMetadata(LINGERING_METADATA))
                {
                    ItemInfo info=(ItemInfo)e.getEntity().getMetadata(LINGERING_METADATA).get(0).value();
                    
                    e.getAffectedEntities().forEach(liv -> 
                    {
                        if(liv.getType() == EntityType.PLAYER)
                        {
                            info.execute(liv.getLocation(), (Player)liv, false);
                        }
                        else
                        {
                            info.execute(liv.getLocation(), liv);
                        }
                    });
                }
            }
        };
    }
    
    private static Optional<ItemCollection> getOptional(final ItemStack item, final When... w)
    {
        return item == null || item.getType() == Material.AIR 
                ? Optional.empty()
                : Optional.ofNullable(CustomItemUtil.getItemCollection(item)).filter(collection -> w.length == 0 || collection.hasAnyWhen(w));
    }
}
