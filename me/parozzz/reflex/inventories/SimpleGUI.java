/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.inventories;

import java.util.function.Consumer;
import me.parozzz.reflex.NMS.itemStack.ItemNBT;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class SimpleGUI extends GUI
{
    private static final String UID ="UID";
    public SimpleGUI(final String title, final int rows) 
    {
        super(title, rows);
    }
    
    public boolean addItem(final ItemStack item, final String uid, final Consumer<InventoryClickEvent> consumer)
    {
        if(i.firstEmpty() != -1)
        {
            setItem(i.firstEmpty(), item, uid, consumer);
            return true;
        }
        return false;
    }
    
    public void setItem(final int slot, final ItemStack item, final String uid, final Consumer<InventoryClickEvent> consumer)
    {
        ItemNBT nbt = new ItemNBT(item);
        nbt.getTag().setString(UID, uid);
        i.setItem(slot, nbt.getBukkitItem());
    }
    
    @Override
    protected void onClick(InventoryClickEvent e) 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
