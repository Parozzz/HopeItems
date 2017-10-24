/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import me.parozzz.hopeitems.utilities.reflection.nbt.item.ItemNBT;
import me.parozzz.hopeitems.utilities.reflection.NBTTagManager.NBTType;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTCompound;
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
        this.item=item;
        
        nbt=new ItemNBT(item);
        tag=nbt.getTag();
    }
    
    public boolean isValid()
    {
        return tag.hasKey(ItemUtils.CUSTOM_NBT);
    }
    
    public String getStringId()
    {
        return tag.getKey(ItemUtils.CUSTOM_NBT, NBTType.STRING, String.class);
    }
    
    public ItemStack getItem()
    {
        return item;
    }
}
