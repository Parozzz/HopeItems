/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.utilities;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

/**
 *
 * @author Paros
 */
public class InventoryUtil 
{
    /*
        PLAYER INVENTORY TESTING
        Bukkit.getLogger().info("=====================");
        Bukkit.getLogger().info("InvType: "+(e.getInventory().getType()!=null?e.getInventory().getType().toString():""));
        Bukkit.getLogger().info("CurrentItem: "+(e.getCurrentItem()!=null?e.getCurrentItem().toString():""));
        Bukkit.getLogger().info("CursorItem: "+(e.getCursor()!=null?e.getCursor().toString():""));
        Bukkit.getLogger().info("SlotItem: "+(e.getInventory().getItem(e.getSlot())!=null?e.getInventory().getItem(e.getSlot()).toString():""));
        Bukkit.getLogger().info("RawSlotItem: "+(e.getInventory().getItem(e.getRawSlot())!=null?e.getInventory().getItem(e.getRawSlot()).toString():""));
        Bukkit.getLogger().info("HotbarSlotItem: "+(e.getInventory().getItem(e.getHotbarButton())!=null?e.getInventory().getItem(e.getHotbarButton()).toString():""));
        Bukkit.getLogger().info("PlayerCursorItem: "+(e.getWhoClicked().getItemOnCursor()!=null?e.getWhoClicked().getItemOnCursor().toString():""));
        Bukkit.getLogger().info("Action: "+(e.getAction()!=null?e.getAction().toString():""));
        Bukkit.getLogger().info("SlotType: "+(e.getSlotType()!=null?e.getSlotType().toString():""));
        Bukkit.getLogger().info("Click: "+(e.getClick()!=null?e.getClick().toString():""));
        Bukkit.getLogger().info("Slot: "+Integer.toString(e.getSlot()));
        Bukkit.getLogger().info("RawSlot: "+Integer.toString(e.getRawSlot()));
        Bukkit.getLogger().info("HotbarSlot: "+Integer.toString(e.getHotbarButton()));
        Bukkit.getLogger().info("=====================");
    */
    
    public static Inventory clone(final Inventory i)
    {
        Inventory newInv;
        switch(i.getType())
        {
            case CHEST:
                newInv=Bukkit.createInventory(i.getHolder(), i.getSize(), i.getTitle());
                newInv.setContents(i.getContents());
                break;
            default:
                newInv = Bukkit.createInventory(i.getHolder(), i.getType(), i.getTitle());
                newInv.setContents(i.getContents());
                break;
        }
        return newInv;
    }
}
