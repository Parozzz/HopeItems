/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection.nbt.item;

import me.parozzz.hopeitems.utilities.reflection.NBTTagManager.NBTType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.Debug;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.reflection.API;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.AdventureTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTBase;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTCompound;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTList;
import org.bukkit.entity.EntityType;

/**
 *
 * @author Paros
 */
public class ItemNBT 
{
    static
    {
        Class<?> nmsItemStack=API.ReflectionUtils.getNMSClass("ItemStack");
        getItemTag=API.ReflectionUtils.getMethod(nmsItemStack, "getTag", new Class[0]);
        setItemTag=API.ReflectionUtils.getMethod(nmsItemStack, "setTag", NBTType.COMPOUND.getObjectClass());
        hasItemTag=API.ReflectionUtils.getMethod(nmsItemStack, "hasTag", new Class[0]);

        Class<?> craftItemStack=API.ReflectionUtils.getCraftbukkitClass("inventory.CraftItemStack");
        asNMSCopy=API.ReflectionUtils.getMethod(craftItemStack, "asNMSCopy", ItemStack.class);
        asBukkitCopy=API.ReflectionUtils.getMethod(craftItemStack, "asBukkitCopy", nmsItemStack);
    }
    
    private static final Method asNMSCopy;
    private static Object asNMSCopy(final ItemStack item)
    {
        return Debug.validateMethod(asNMSCopy, null, item);
    }
    
    private static final Method asBukkitCopy;
    private static Object asBukkitCopy(final Object nmsItemStack)
    {
        return Debug.validateMethod(asBukkitCopy, null, nmsItemStack);
    }
 
    private static final Method getItemTag;
    private static Object getItemTag(final Object nmsItemStack)
    {
        return Debug.validateMethod(getItemTag, nmsItemStack);
    }
    
    private static final Method setItemTag;
    private static Object setItemTag(final Object nmsItemStack, final Object compound)
    { 
        return Debug.validateMethod(setItemTag, nmsItemStack, compound);
    }
    
    private static final Method hasItemTag;
    private static boolean hasItemTag(final Object nmsItemStack)
    {
        return (boolean)Debug.validateMethod(hasItemTag, nmsItemStack);
    }
    
    public static ItemStack setSpawnedType(final ItemStack egg, final EntityType et)
    {
        ItemNBT nbt=new ItemNBT(egg);
        NBTCompound compound=nbt.getTag();
        
        NBTCompound id=new NBTCompound();
        id.addValue("id", NBTType.STRING, et.name());
        compound.addTag("EntityTag", id);
        
        return nbt.setTag(compound).getBukkitItem();
    }
    
    public static void setAdventureFlag(final NBTCompound compound, final AdventureTag tag ,final Material... where)
    {
        NBTList list=new NBTList();
        for(String str:Stream.of(where)
                .map(m -> m.name().toLowerCase())
                .map(str -> (Utils.bukkitVersion("1.11","1.12")?"minecraft:":"")+str)
                .toArray(String[]::new)) 
        {
            list.addTag(new NBTBase(NBTType.STRING, str));
        }
        
        compound.addTag(tag.getValue(), list);
    }
    
    public static <T> T getKey(final ItemStack item, final String key, final NBTType type, final Class<T> clazz)
    {
        try 
        {
            Object obj=getItemTag(asNMSCopy(item));
            return obj!=null? (T)API.getNBT().getCompoundGetter(type).invoke(obj, key) : null;
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) 
        {
            Logger.getLogger(ItemNBT.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    
    private final Object nmsItemStack;
    
    public ItemNBT(final Material type)
    {
        this(new ItemStack(type));
    }
    
    public ItemNBT(final ItemStack item)
    {
        nmsItemStack=asNMSCopy(item);
    }
    
    public NBTCompound getTag()
    {
        return hasItemTag(nmsItemStack)?new NBTCompound(getItemTag(nmsItemStack)):new NBTCompound();
    }
    
    public ItemNBT setTag(final NBTCompound compound)
    {
        setItemTag(nmsItemStack, compound.getNBTObject()); 
        return this;
    }
    
    public ItemStack getBukkitItem()
    {
        return (ItemStack)asBukkitCopy(nmsItemStack);
    }
    
    @Override
    public String toString() 
    {
        return this.getTag().toString(); 
    }
}
