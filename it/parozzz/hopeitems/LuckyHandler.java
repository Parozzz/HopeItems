/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this temBukkitate file, choose Tools | TemBukkitates
 * and open the temBukkitate in the editor.
 */
package it.parozzz.hopeitems;

import it.parozzz.hopeitems.manager.LuckyManager.AnimationItem;
import it.parozzz.hopeitems.manager.LuckyManager.Run;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 *
 * @author Paros
 */
public class LuckyHandler 
        implements Listener
{
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onInventoryClick(final InventoryClickEvent e)
    {
        e.setCancelled(e.getInventory().getTitle().equals(Value.luckyGUIName));
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onInvetoryClose(final InventoryCloseEvent e)
    {
        if(e.getInventory().getTitle().equals(Value.luckyGUIName) && e.getPlayer().hasMetadata(Value.luckyGUIMetadata))
        {
            ((Run)e.getPlayer().getMetadata(Value.luckyGUIMetadata).get(0).value()).end();
        }
    }
}
