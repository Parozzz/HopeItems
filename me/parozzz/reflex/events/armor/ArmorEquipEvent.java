/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.events.armor;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class ArmorEquipEvent extends Event implements Cancellable
{
    private final Player p;
    private final EquipmentSlot slot;
    private final ItemStack item;
    public ArmorEquipEvent(final Player p, final ItemStack item, final EquipmentSlot slot)
    {
        this.p=p;
        this.slot=slot;
        this.item=item;
    }
    
    public Player getPlayer()
    {
        return p;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
    
    public EquipmentSlot getSlot()
    {
        return slot;
    }
    
    private static final HandlerList handler=new HandlerList();
    @Override
    public HandlerList getHandlers() 
    {
        return handler;
    }
    
    public static HandlerList getHandlerList()
    {
        return handler;
    }
    
    private boolean cancelled=false;
    @Override
    public boolean isCancelled() 
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean bln) 
    {
        cancelled=bln;
    }
}
