/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.reflection;

import it.parozzz.hopeitems.core.Utils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class NBT 
{
    
    public static enum AdventureAction
    {
        CANPLACEON("CanPlaceOn"),
        CANDESTROY("CanDestroy");
        
        private final String value;
        private AdventureAction(final String str) { value=str; }
        public String getValue() { return value; }
    }
    
    private static Method asNMSCopy;
    public static Object asNMSCopy(final ItemStack item) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException { return asNMSCopy.invoke(null, item); }
    
    private static Method asBukkitCopy;
    public static Object asBukkitCopy(final Object nmsItemStack)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException { return asBukkitCopy.invoke(null, nmsItemStack); }
 
    private static Method getItemTag;
    public static Object getItemTag(final Object nmsItemStack)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException { return getItemTag.invoke(nmsItemStack); }
    
    private static Method setItemTag;
    public static Object setItemTag(final Object nmsItemStack, final Object compound) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException { return setItemTag.invoke(nmsItemStack, compound); }
    
    private static Method hasItemTag;
    public static boolean hasItemTag(final Object nmsItemStack)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException { return (boolean)hasItemTag.invoke(nmsItemStack); }
    
    public static void initialize() 
            throws NoSuchMethodException, ClassNotFoundException
    {
        NBTTag.initialize();
        
        Class<?> nmsItemStack=ReflectionUtils.getNMSClass("ItemStack");
        getItemTag=ReflectionUtils.getMethod(nmsItemStack, "getTag", new Class[0]);
        setItemTag=ReflectionUtils.getMethod(nmsItemStack, "setTag", NBTTag.compoundClass());
        hasItemTag=ReflectionUtils.getMethod(nmsItemStack, "hasTag", new Class[0]);

        Class<?> craftItemStack=ReflectionUtils.getCraftbukkitClass("inventory.CraftItemStack");
        asNMSCopy=ReflectionUtils.getMethod(craftItemStack, "asNMSCopy", ItemStack.class);
        asBukkitCopy=ReflectionUtils.getMethod(craftItemStack, "asBukkitCopy", nmsItemStack);
    }
    
    public static ItemStack setSpawnedType(final ItemStack egg, final EntityType et) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException
    {
        return new ItemNBT(egg).newCompound().addValueToNewCompound("id", et.name()).setCoumpound("EntityTag").buildItem();
    }
    
    public static ItemStack setAdventureFlag(final ItemStack item, final AdventureAction aa ,final Material... where) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException 
    {
        ItemNBT nbt=new ItemNBT(item);

        nbt.newList();
        for(String str:Stream.of(where)
                .map(m -> m.name().toLowerCase())
                .map(str -> (Utils.bukkitVersion("1.11","1.12")?"minecraft:":"")+str)
                .toArray(String[]::new)) { nbt.addValueToNewList(str); }
        nbt.addList(aa.getValue());

        return nbt.buildItem();
    }

    public static ItemStack changeAttribute(final ItemStack item, final double speedValue, final double damageValue) 
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        ItemNBT nbt=new ItemNBT(item);
        
        nbt.newList();

        Map<String,Object> speed=new HashMap<>();
        speed.put("AttributeName", "generic.attackSpeed");
        speed.put("Name", "generic.attackSpeed");
        speed.put("Amount", speedValue);
        speed.put("Operation", 0);
        speed.put("UUIDLeast", 894654);
        speed.put("UUIDMost", 2872);
        speed.put("Slot", "mainhand");
        nbt.addCoumpoundToNewList(speed);

        Map<String,Object> damage=new HashMap<>();
        damage.put("AttributeName", "generic.attackDamage");
        damage.put("Name", "generic.attackDamage");
        damage.put("Amount", damageValue);
        damage.put("Operation", 0);
        damage.put("UUIDLeast", 894654);
        damage.put("UUIDMost", 2872);
        damage.put("Slot", "mainhand");
        nbt.addCoumpoundToNewList(damage);

        nbt.addList("AttributeModifiers");
        return nbt.buildItem();
    }
}
