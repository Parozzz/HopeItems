/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.Set;
import javax.annotation.Nullable;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.reflex.NMS.itemStack.NMSStack;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import org.bukkit.Material;
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
        NMSStackCompound tag = new NMSStackCompound(item);

        NBTCompound customCompound = new NBTCompound();
        customCompound.setString(ID, name);
        tag.setTag(CUSTOM_NBT, customCompound);
        
        item.setItemMeta(tag.getItemStack().getItemMeta());
    }
    
    public static @Nullable String getItemCollectionId(final @Nullable ItemStack itemStack)
    {
        if(itemStack == null || itemStack.getType() == Material.AIR)
        {
            return null;
        }
        
        NBTCompound tag = new NMSStackCompound(itemStack);
        if(!tag.hasKey(CustomItemUtil.CUSTOM_NBT))
        {
            return null;
        }
        
        return tag.getCompound(CustomItemUtil.CUSTOM_NBT).getString(CustomItemUtil.ID);
    }
    
    public static @Nullable ItemCollection getItemCollection(final @Nullable ItemStack itemStack)
    {
        return ItemRegistry.getCollection(getItemCollectionId(itemStack));
    }
    
    public static boolean hasCustomTag(final ItemStack item)
    {
        return new NMSStack(item).getTag().hasKey(CUSTOM_NBT);
    }
}
