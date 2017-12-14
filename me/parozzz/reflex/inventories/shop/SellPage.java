/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.inventories.shop;

import java.util.Optional;
import me.parozzz.reflex.NMS.itemStack.ItemNBT;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.inventories.GUI;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class SellPage extends GUI
{
    private final static ItemStack[] contents = new ItemStack[54];
    static
    {
        ItemNBT nbt = new ItemNBT(new ItemStack(Material.THIN_GLASS));
        nbt.getTag().setBoolean("Panel", true);
        
        contents[1] = nbt.getBukkitItem();
        contents[9] = nbt.getBukkitItem();
        contents[10] = nbt.getBukkitItem();
    }
    
    private final ItemStack[] clearContents;
    private final ItemStack toCompare;
    private final long money;
    public SellPage(final String title, final ItemStack toSell, final long money) 
    {
        super(title, 6);
        
        this.money = money;
        i.setContents(contents);
        clearContents = i.getContents();
        
        toCompare = toSell.clone();
        this.setItem(toSell);
    }
    
    private void setItem(final ItemStack item)
    {
        ItemNBT nbt = new ItemNBT(item);
        nbt.getTag().setInt("Total", 0);
        i.setItem(0, nbt.getBukkitItem());
    }
    
    @Override
    protected void onClick(InventoryClickEvent e) 
    {
        Optional.ofNullable(e.getCurrentItem()).map(ItemNBT::new).ifPresent(nbt -> 
        {
            NBTCompound tag = nbt.getTag();
            if(!tag.hasKey("Panel"))
            {
                if(tag.hasKey("Total"))
                {
                    
                }
                else
                {
                    e.getWhoClicked().getInventory().addItem(e.getCurrentItem());
                    e.setCurrentItem(new ItemStack(Material.AIR));
                }
            }
        });
    }
    
    @Override
    protected void onBottomInventoryClick(final InventoryClickEvent e)
    {
        
    }
}
