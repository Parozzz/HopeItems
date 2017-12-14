/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.Set;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.reflex.NMS.itemStack.ItemNBT;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class CustomItemUtil 
{
    protected static final String CUSTOM_NBT = "HopeItem";
    
    protected static final String ID = "Id";
    
    public static void addCustomTag(final ItemStack item, final String name)
    {
        ItemNBT nbt = new ItemNBT(item);

        NBTCompound customCompound = new NBTCompound();
        customCompound.setString(ID, name);
        nbt.getTag().setTag(CUSTOM_NBT, customCompound);
        
        item.setItemMeta(nbt.getBukkitItem().getItemMeta());
    }
}
