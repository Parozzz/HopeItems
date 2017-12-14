/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.inventories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public abstract class GUI
{
    static
    {
        Bukkit.getPluginManager().registerEvents(new Listener()
        {
            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            private void onInventoryOpen(final InventoryOpenEvent e)
            {
                if(e.getInventory().getHolder() instanceof GUIHolder)
                {
                    GUI gui = ((GUIHolder)e.getInventory().getHolder()).getGUI();
                    Util.ifCheck(!gui.changed.contains(e.getPlayer().getUniqueId()), () -> gui.onOpen.accept(e));
                }
            }
            
            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            private void onInventoryClick(final InventoryClickEvent e)
            {
                if(e.getInventory().getHolder() instanceof GUIHolder)
                {
                    e.setCancelled(true);
                    if(e.getInventory().equals(e.getClickedInventory()))
                    {
                        Optional.ofNullable(e.getCurrentItem()).ifPresent(item -> ((GUIHolder)e.getInventory().getHolder()).getGUI().onClick(e));
                    }
                    else if(e.getClickedInventory() != null)
                    {
                        Optional.ofNullable(e.getCurrentItem()).ifPresent(item -> ((GUIHolder)e.getInventory().getHolder()).getGUI().onBottomInventoryClick(e));
                    }
                    
                }
            }
            
            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            private void onInventoryClose(final InventoryCloseEvent e)
            {
                if(e.getInventory().getHolder() instanceof GUIHolder)
                {
                    GUI gui = ((GUIHolder)e.getInventory().getHolder()).getGUI();
                    Util.ifCheck(!gui.changed.contains(e.getPlayer().getUniqueId()), () -> gui.onClose.accept(e));
                }
            }
            
        }, JavaPlugin.getProvidingPlugin(GUI.class));
    }
    
    private final InventoryHolder holder = new GUIHolder()
    {
        @Override
        public Inventory getInventory() 
        {
            return i;
        }

        @Override
        public GUI getGUI() 
        {
            return instance;
        }
        
    };
    
    private final GUI instance;
    protected Inventory i;
    
    private final Set<UUID> changed; 
    public GUI(final String title, final int rows)
    {
        changed = new HashSet<>();
        
        instance = this;
        i = Bukkit.createInventory(holder, rows * 9, Util.cc(title));
    }
    
    public final void setTitle(final String title)
    {
        List<HumanEntity> viewers = new ArrayList<>(i.getViewers());
        changed.addAll(viewers.stream().map(HumanEntity::getUniqueId).collect(Collectors.toSet()));
        
        Inventory newInventory = Bukkit.createInventory(holder, i.getSize(), Util.cc(title));
        newInventory.setContents(i.getContents());
        viewers.forEach(he -> he.openInventory(newInventory));
        
        changed.clear();
        i = newInventory;
    }
    
    private Consumer<InventoryOpenEvent> onOpen = e -> {};
    public final GUI onOpen(final Consumer<InventoryOpenEvent> consumer)
    {
        onOpen = consumer;
        return this;
    }
    
    private Consumer<InventoryCloseEvent> onClose = e -> {};
    public final GUI onClose(final Consumer<InventoryCloseEvent> consumer)
    {
        onClose = consumer;
        return this;
    }
    
    protected abstract void onClick(final InventoryClickEvent e);
    
    protected void onBottomInventoryClick(final InventoryClickEvent e)
    {
        
    }
    
    public final void open(final HumanEntity he)
    {
        he.openInventory(i);
    }
    
    private abstract class GUIHolder implements InventoryHolder
    {
        public abstract GUI getGUI();

        @Override
        public abstract Inventory getInventory();
    }
}
