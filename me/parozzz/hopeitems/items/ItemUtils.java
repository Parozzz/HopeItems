/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.logging.Level;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.ItemNBT;
import me.parozzz.hopeitems.utilities.reflection.NBTTagManager.NBTType;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTCompound;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class ItemUtils 
{
    protected static final String CUSTOM_NBT="HopeItem";
    public static ItemStack addCustomTag(final ItemStack item, final String name)
    {
        ItemNBT nbt=new ItemNBT(item);

        NBTCompound compound=nbt.getTag();
        compound.addValue(CUSTOM_NBT, NBTType.STRING, name);

        return nbt.setTag(compound).getBukkitItem();
    }
}
