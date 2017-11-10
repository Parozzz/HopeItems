/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection.nbt.item;

import me.parozzz.hopeitems.utilities.reflection.NBTTagManager.NBTType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.Debug;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.reflection.API;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTBase;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTCompound;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTList;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Paros
 */
public class ItemNBT implements Cloneable
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
        copyNMSStack = API.ReflectionUtils.getMethod(craftItemStack, "copyNMSStack", nmsItemStack, int.class);
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
    
    private static final Method copyNMSStack;
    private static Object copyNMS(final Object nmsItemStack)
    {
        return Debug.validateMethod(copyNMSStack, nmsItemStack, 1);
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
        id.setValue("id", NBTType.STRING, et.name());
        compound.setTag("EntityTag", id);
        
        return nbt.setTag(compound).getBukkitItem();
    }
    
    public static void setAdventureFlag(final NBTCompound compound, final AdventureTag tag ,final Material... where)
    {
        NBTList list=new NBTList();
        for(String str:Stream.of(where)
                .map(m -> m.name().toLowerCase())
                .map(str -> (MCVersion.V1_11.isHigher()?"minecraft:":"")+str)
                .toArray(String[]::new)) 
        {
            list.addTag(new NBTBase(NBTType.STRING, str));
        }
        
        compound.setTag(tag.getValue(), list);
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
    
    private ItemNBT(final Object nmsItemStack)
    {
        this.nmsItemStack = nmsItemStack;
    }
    
    public NBTCompound getTag()
    {
        if(!hasItemTag(nmsItemStack))
        {
            NBTCompound tag = new NBTCompound();
            setItemTag(nmsItemStack, tag.getNBTObject());
            return tag;
        }
        else
        {
            return new NBTCompound(getItemTag(nmsItemStack));
        }
    }
    
    public ItemNBT setTag(final NBTCompound compound)
    {
        setItemTag(nmsItemStack, compound.getNBTObject()); 
        return this;
    }
    
    public ItemStack getBukkitItem(final List<String> lore)
    {
        ItemStack item = (ItemStack)asBukkitCopy(nmsItemStack);
        
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    public ItemStack getBukkitItem()
    {
        return (ItemStack)asBukkitCopy(nmsItemStack);
    }
    
    public Object getNMSObject()
    {
        return nmsItemStack;
    }
    
    @Override
    public String toString() 
    {
        return this.getTag().toString(); 
    }
    
    @Override
    public ItemNBT clone()
    {
        return new ItemNBT(copyNMS(this.nmsItemStack));
    }
}
