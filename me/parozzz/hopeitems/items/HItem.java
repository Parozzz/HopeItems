/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.stream.Stream;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.reflex.NMS.itemStack.ItemNBT;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class HItem 
{
    private final ItemNBT nbt;
    private final NBTCompound tag;
    
    private final ItemStack item;
    public HItem(final ItemStack item)
    {
        this.item = item;
        
        nbt = new ItemNBT(item);
        tag = nbt.getTag();
    }
    
    public boolean isValid()
    {
        return tag.hasKey(CustomItemUtil.CUSTOM_NBT);
    }
    
    public String getStringId()
    {
        return tag.getCompound(CustomItemUtil.CUSTOM_NBT).getString(CustomItemUtil.ID);
    }
    
    public ItemStack getItem()
    {
        return item;
    }
}
