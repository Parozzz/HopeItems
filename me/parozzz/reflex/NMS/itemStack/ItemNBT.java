/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.itemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.NMS.NMSWrapper;
import me.parozzz.reflex.NMS.ReflectionUtil;
import me.parozzz.reflex.NMS.nbt.NBTBase;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.NMS.nbt.NBTList;
import me.parozzz.reflex.NMS.nbt.NBTType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.ItemMeta;
/**
 *
 * @author Paros
 */
public final class ItemNBT implements Cloneable, NMSWrapper
{
    private static final Method getTag;
    private static final Method setTag;
    private static final Method hasTag;
    
    private static final Method asNMSCopy;
    private static final Method asBukkitCopy;
    private static final Method copyNMSStack;
    static
    {
        Class<?> nmsItemStack = ReflectionUtil.getNMSClass("ItemStack");
        getTag = ReflectionUtil.getMethod(nmsItemStack, "getTag");
        setTag = ReflectionUtil.getMethod(nmsItemStack, "setTag", NBTCompound.getNMSClass());
        hasTag = ReflectionUtil.getMethod(nmsItemStack, "hasTag");
                
        Class<?> craftItemStack = ReflectionUtil.getCraftbukkitClass("inventory.CraftItemStack");
        asNMSCopy = ReflectionUtil.getMethod(craftItemStack, "asNMSCopy", ItemStack.class);
        asBukkitCopy = ReflectionUtil.getMethod(craftItemStack, "asBukkitCopy", nmsItemStack);
        copyNMSStack = ReflectionUtil.getMethod(craftItemStack, "copyNMSStack", nmsItemStack, int.class);
    }
 
    public static ItemStack setSpawnedType(final ItemStack egg, final EntityType et)
    {
        ItemNBT nbt=new ItemNBT(egg);
        NBTCompound compound=nbt.getTag();
        
        NBTCompound id=new NBTCompound();
        id.setString("id", et.name());
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
    
    private final Object nmsItemStack;
    public ItemNBT(final Material type)
    {
        this(new ItemStack(type));
    }
    
    public ItemNBT(final NBTCompound nbt)
    {
        ItemStack item = new ItemStack(Material.valueOf(nbt.getString("id")), nbt.getInt("amount"), nbt.getShort("data"));
        nmsItemStack = Debug.validateMethod(asNMSCopy, null, item);
        this.setTag(nbt.getCompound("tag"));
    }
    
    public ItemNBT(final String str)
    {
        this(new NBTCompound(str));
    }
    
    public ItemNBT(final ItemStack item)
    {
        nmsItemStack = Debug.validateMethod(asNMSCopy, null, item);
    }
    
    public NBTCompound convertToNBT()
    {
        NBTCompound nbt = new NBTCompound();
        ItemStack item = this.getBukkitItem();
        
        nbt.setString("id", item.getType().name())
                .setInt("amount", item.getAmount())
                .setShort("data", item.getDurability())
                .setTag("tag", getTag());
        
        return nbt;
    }
    
    private ItemNBT(final Object nmsItemStack)
    {
        this.nmsItemStack = nmsItemStack;
    }
    
    public NBTCompound getTag()
    {
        if(!(boolean)Debug.validateMethod(hasTag, nmsItemStack))
        {
            NBTCompound tag = new NBTCompound();
            Debug.validateMethod(setTag, nmsItemStack, tag.getNMSObject());
            return tag;
        }
        else
        {
            return new NBTCompound(Debug.validateMethod(getTag, nmsItemStack));
        }
    }
    
    public ItemNBT setTag(final NBTCompound compound)
    {
        Debug.validateMethod(setTag, nmsItemStack, compound.getNMSObject());
        return this;
    }
    
    public ItemStack getBukkitItem(final List<String> lore)
    {
        ItemStack item = getBukkitItem();
        
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    public ItemStack getBukkitItem()
    {
        return (ItemStack)Debug.validateMethod(asBukkitCopy, null, nmsItemStack);
    }
    
    @Override
    public Object getNMSObject()
    {
        return nmsItemStack;
    }
    
    @Override
    public String toString() 
    {
        return this.convertToNBT().toString(); 
    }
    
    @Override
    public ItemNBT clone()
    {
        return new ItemNBT(Debug.validateMethod(copyNMSStack, null, nmsItemStack, 1));
    }
}
