/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.events.offhand;

import java.util.Optional;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
/**
 *
 * @author Paros
 */
public class OffHandListener implements Listener
{ 
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onSwap(final PlayerSwapHandItemsEvent e)
    {
        Optional.ofNullable(e.getMainHandItem())
                .filter(item -> item.getType()!=Material.AIR)
                .ifPresent(item -> callUnequip(e.getPlayer(), item));
        
        Optional.ofNullable(e.getOffHandItem())
                .filter(item -> item.getType()!=Material.AIR)
                .ifPresent(item -> callEquip(e.getPlayer(), item));
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onInventoryDrag(final InventoryDragEvent e)
    {
        Optional.ofNullable(e.getNewItems().get(45)).filter(item -> item.getType()==Material.AIR).ifPresent(item -> callEquip((Player)e.getWhoClicked(), item));
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onInventoryClick(final InventoryClickEvent e)
    {
        switch(e.getInventory().getType())
        {
            case CRAFTING:
                if(e.getWhoClicked().getGameMode() != GameMode.CREATIVE)
                {
                    this.onSurvivalClick(e);
                }
                else
                {
                    this.onCreativeClick(e);
                }
                break;
        }
    }
    
    private void onSurvivalClick(final InventoryClickEvent e)
    {
        if(e.getInventory().getType() != InventoryType.CRAFTING)
        {
            return;
        }
        
        switch(e.getSlotType())
        {
            case QUICKBAR:
                if(e.getSlot() != 40)
                {
                    if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && e.getCurrentItem().getType() == Material.SHIELD && !Optional.ofNullable(e.getWhoClicked().getInventory().getItemInOffHand()).filter(i -> i.getType()!=Material.AIR).isPresent())
                    {
                        callEquip((Player)e.getWhoClicked(), e.getCurrentItem());
                    }
                    return;
                }
                
                switch(e.getAction())
                {
                    case HOTBAR_SWAP:
                        callUnequip((Player)e.getWhoClicked(), e.getCurrentItem());
                        callEquip((Player)e.getWhoClicked(), e.getWhoClicked().getInventory().getItem(e.getHotbarButton()));
                        break;
                    case SWAP_WITH_CURSOR:
                        callUnequip((Player)e.getWhoClicked(), e.getCurrentItem());
                    case PLACE_ALL:
                    case PLACE_ONE:
                        if(!e.getCursor().isSimilar(e.getCurrentItem()))
                        {
                            callEquip((Player)e.getWhoClicked(), e.getCursor());
                        }
                        break;
                    case PICKUP_ONE:
                    case PICKUP_HALF:
                    case DROP_ONE_SLOT:
                        if(e.getCurrentItem().getAmount() == 1)
                        {
                            callUnequip((Player)e.getWhoClicked(), e.getCurrentItem());
                        }
                        break;
                    case PICKUP_ALL:
                    case MOVE_TO_OTHER_INVENTORY:
                    case DROP_ALL_SLOT:
                        callUnequip((Player)e.getWhoClicked(), e.getCurrentItem());
                        break;
                }
                break;
            case CONTAINER:
                if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && e.getCurrentItem().getType() == Material.SHIELD && !Optional.ofNullable(e.getWhoClicked().getInventory().getItemInOffHand()).filter(i -> i.getType()!=Material.AIR).isPresent())
                {
                    callEquip((Player)e.getWhoClicked(), e.getCurrentItem());
                }
                else if(e.getClick() == ClickType.DOUBLE_CLICK && e.getCursor() != null && e.getCursor().isSimilar(e.getWhoClicked().getInventory().getItemInOffHand()))
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run() 
                        {
                            if(e.getWhoClicked().getInventory().getItemInOffHand().getType() == Material.AIR)
                            {
                                callUnequip((Player)e.getWhoClicked(), e.getCursor());
                            }
                        }  
                    }.runTaskLater(JavaPlugin.getProvidingPlugin(OffHandListener.class), 1L);
                }
                break;
        } 
    }
    
    private void onCreativeClick(final InventoryClickEvent e)
    {
        //To - DO
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onPlayerDeath(final PlayerDeathEvent e)
    {
        Optional.ofNullable(e.getEntity().getInventory().getItemInOffHand())
                .filter(item -> item.getType() != Material.AIR)
                .ifPresent(item -> this.callUnequip(e.getEntity(), item));
    }
    
    /*
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onPlayerJoin(final PlayerJoinEvent e)
    {
        Optional.ofNullable(e.getPlayer().getInventory().getItemInOffHand())
                .filter(item -> item.getType() != Material.AIR)
                .ifPresent(item -> CharmUtils.addCharm(e.getPlayer(), item));
    }*/
    
    private boolean callUnequip(final Player p, final ItemStack item)
    {
        return item != null && item.getType() != Material.AIR && Util.callEvent(new OffHandUnequipEvent(p, item)).isCancelled();
    }
    
    private boolean callEquip(final Player p, final ItemStack item)
    {
        return item != null && item.getType() != Material.AIR && Util.callEvent(new OffHandEquipEvent(p, item)).isCancelled();
    }
}
