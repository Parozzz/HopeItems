/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.events;

import it.parozzz.hopeitems.manager.ItemManager;
import org.bukkit.block.Dispenser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class BlockDispenseCustomItemEvent 
        extends Event
        implements Cancellable
{
    private final Dispenser dispenser;
    private final ItemStack item;
    private final ItemManager im;
    public BlockDispenseCustomItemEvent(final Dispenser dispenser, final ItemStack item, final ItemManager im)
    {
        this.dispenser=dispenser;
        this.item=item;
        this.im=im;
    }
    
    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
    
    private boolean cancelled=false;
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean bln) { cancelled=bln; }
    
    public Dispenser getDispenser() { return dispenser; }
    public ItemStack getItem() { return item; }
    public ItemManager getItemManager() { return im; }
    
}
