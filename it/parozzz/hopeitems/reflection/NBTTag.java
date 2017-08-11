/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.reflection;

import it.parozzz.hopeitems.core.Utils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagDouble;
import net.minecraft.server.v1_12_R1.NBTTagFloat;
import org.bukkit.Bukkit;
/**
 *
 * @author Paros
 */
public class NBTTag 
{
    
    public static enum NBTType
    {
        BYTE((byte)1, byte.class),
        SHORT((byte)2, short.class),
        INT((byte)3, int.class),
        LONG((byte)4, long.class),
        FLOAT((byte)5, float.class),
        DOUBLE((byte)6, double.class),
        BYTEARRAY((byte)7, byte[].class),
        STRING((byte)8, String.class),
        LIST((byte)9, NBTTag.listClass()),
        COMPOUND((byte)10, NBTTag.compoundClass()),
        INTARRAY((byte)11, int[].class),
        LONGARRAY((byte)12, long[].class);
        
        private final Class<?> type;
        private final byte id;
        private NBTType(final byte id,final Class<?> type) 
        { 
            this.id=id;
            this.type=type;
        }
        public byte getId() { return id; }
        public Class<?> getType() { return type; }
        
        public static NBTType getById(final byte id)
        {
            return Stream.of(NBTType.values()).filter(nbt -> nbt.getId()==id).findFirst().orElseGet(() -> null);
        }
    }
    
    /*
    ==================
    #### NBT BASE ####
    ==================
    */
    private static Class<?> NBTTagBase;
    public Class<?> nbtTagBaseClass() { return NBTTagBase; }
    
    private static Constructor<?> NBTTagString;
    private static Constructor<?> NBTTagDouble;
    private static Constructor<?> NBTTagShort;
    private static Constructor<?> NBTTagInt;
    private static Constructor<?> NBTTagLong;
    private static Constructor<?> NBTTagByte;
    
    private static Object newObject(final Object value) 
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if(value instanceof Double) { return NBTTagDouble.newInstance(value); }
        else if(value instanceof String) { return NBTTagString.newInstance(value); }
        else if(value instanceof Integer) { return NBTTagInt.newInstance(value); }
        else if(value instanceof Short) { return NBTTagShort.newInstance(value); }
        else if(value instanceof Long) { return NBTTagLong.newInstance(value); }
        else if(value instanceof Byte) { return NBTTagByte.newInstance(value); }
        else { return null; }
    }
    
    /*
    ==================
    #### COMPOUND ####
    ==================
    */
    private static Class<?> NBTTagCompoundClass;
    public static Class<?> compoundClass() { return NBTTagCompoundClass; }
    
    private static Constructor<?> NBTTagCompound;
    private static Object newCompound() 
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException { return NBTTagCompound.newInstance(); }
    
    /*
    ==========================
    #### COMPOUND SETTERS ####
    ==========================
    */
    private static Method compoundSetNBT;
    private static void compoundSetNBT(final NBTCompound compound, final String compoundName, final Tags NBT) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    { 
        compoundSetNBT.invoke(compound.getNBTObject(), compoundName, NBT.getNBTObject()); 
    }
    
    private static Method compoundKeySet;
    private static Method compoundGetTypeByKey;
    
    private static Method compoundSetInt;
    private static Method compoundSetString;
    private static Method compoundSetDouble;
    private static Method compoundSetShort;
    private static Method compoundSetFloat;
    private static Method compoundSetLong;
    
    public static void compoundSet(final NBTCompound compound, final String key, final Object value) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if(value.getClass()==Double.class) { compoundSetDouble.invoke(compound.getNBTObject(), key, value); }
        else if(value.getClass()==String.class) { compoundSetString.invoke(compound.getNBTObject(), key, value); }
        else if(value.getClass()==Integer.class) { compoundSetInt.invoke(compound.getNBTObject(), key, value); }
        else if(value.getClass()==Short.class) { compoundSetShort.invoke(compound.getNBTObject(), key, value); }
        else if(value.getClass()==Float.class) { compoundSetFloat.invoke(compound.getNBTObject(), key, value); }
        else if(value.getClass()==Long.class) { compoundSetLong.invoke(compound.getNBTObject(), key, value); }
        else if(value instanceof Tags) { compoundSetNBT.invoke(compound.getNBTObject(), key, ((Tags)value).getNBTObject()); }
    }
    
    /*
    ==========================
    #### COMPOUND GETTERS ####
    ==========================
    */
    
    private static Method compoundGetString;
    private static Method compoundGetShort;
    private static Method compoundGetLong;
    private static Method compoundGetInt;
    private static Method compoundGetDouble;
    private static Method compoundGetFloat;
    public static <T> T compoundGet(final NBTCompound compound, final String key, final Class<T> type) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if(type==String.class) { return (T)compoundGetString.invoke(compound.getNBTObject(), key); }
        else if(type==double.class) { return (T)NBTTag.compoundGetDouble.invoke(compound.getNBTObject(), key); }
        else if(type==float.class) { return (T)NBTTag.compoundGetFloat.invoke(compound.getNBTObject(), key); }
        else if(type==short.class) { return (T)NBTTag.compoundGetShort.invoke(compound.getNBTObject(), key); }
        else if(type==int.class) { return (T)NBTTag.compoundGetInt.invoke(compound.getNBTObject(), key); }
        else if(type==long.class) { return (T)NBTTag.compoundGetLong.invoke(compound.getNBTObject(), key); }
        return null;
    }
    
    private static Method compoundHasKey;
    private static boolean compoundHasKey(final Object compound, final String key) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    { 
        return (boolean)compoundHasKey.invoke(compound, key); 
    }
    
    private static Method compoundHasKeyOfType;
    private static boolean compoundHasKeyOfType(final Object compound, final String key, final NBTType t) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    { 
        return (boolean)compoundHasKeyOfType.invoke(compound, key, t.getId()); 
    }
    
    private static Method compoundRemoveKey;
    private static void compoundRemoveKey(final Object compound, final String key) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {  
        compoundRemoveKey.invoke(compound, key); 
    }
    
    /*
    ==============
    #### LIST ####
    ==============
    */
    private static Class<?> nbtTagListClass;
    public static Class<?> listClass() { return nbtTagListClass; }
    
    private static Constructor<?> NBTTagList;
    public static Object newList() 
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        return NBTTagList.newInstance(); 
    }
    
    private static Method listAdd;
    private static void listAdd(final Object nbtList, final Tags nbt) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    { 
        listAdd.invoke(nbtList, nbt.getNBTObject()); 
    }
    
    private static Method listGetType;
    private static byte listGetType(final Object nbtList) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return (byte)listGetType.invoke(nbtList);
    }
    
    
    public static void initialize() throws NoSuchMethodException
    {  
        NBTTagBase=ReflectionUtils.getNMSClass("NBTBase");
        
        NBTTagString=ReflectionUtils.getNMSClass("NBTTagString").getConstructor(String.class);
        NBTTagDouble=ReflectionUtils.getNMSClass("NBTTagDouble").getConstructor(double.class);
        NBTTagInt=ReflectionUtils.getNMSClass("NBTTagInt").getConstructor(int.class);
        NBTTagLong=ReflectionUtils.getNMSClass("NBTTagLong").getConstructor(long.class);
        NBTTagByte=ReflectionUtils.getNMSClass("NBTTagByte").getConstructor(byte.class);
        NBTTagShort=ReflectionUtils.getNMSClass("NBTTagShort").getConstructor(short.class);
        
        
        nbtTagListClass=ReflectionUtils.getNMSClass("NBTTagList");
        
        NBTTagList=nbtTagListClass.getConstructor(new Class[0]);
        listAdd=ReflectionUtils.getMethod(nbtTagListClass, "add", NBTTagBase);
        
        if(Utils.bukkitVersion("1.8")) { listGetType=ReflectionUtils.getMethod(nbtTagListClass, "f"); }
        else { listGetType=ReflectionUtils.getMethod(nbtTagListClass, "g"); }
        
        NBTTagCompoundClass=ReflectionUtils.getNMSClass("NBTTagCompound");

        NBTTagCompound=NBTTagCompoundClass.getConstructor(new Class[0]);
        compoundSetNBT=ReflectionUtils.getMethod(NBTTagCompoundClass, "set", String.class, NBTTagBase);
        
        compoundGetString=ReflectionUtils.getMethod(NBTTagCompoundClass, "getString", String.class); //Getter
        compoundGetDouble=ReflectionUtils.getMethod(NBTTagCompoundClass, "getDouble", String.class);
        compoundGetInt=ReflectionUtils.getMethod(NBTTagCompoundClass, "getInt", String.class); //Getter
        compoundGetShort=ReflectionUtils.getMethod(NBTTagCompoundClass, "getShort", String.class); //Getter*/
        compoundGetLong=ReflectionUtils.getMethod(NBTTagCompoundClass, "getLong", String.class);
        compoundGetFloat=ReflectionUtils.getMethod(NBTTagCompoundClass, "getFloat", String.class);
        
        //NBTTagCompound c;
                
        compoundSetShort=ReflectionUtils.getMethod(NBTTagCompoundClass, "setShort", String.class,short.class); //Setter
        compoundSetInt=ReflectionUtils.getMethod(NBTTagCompoundClass, "setInt", String.class,int.class); //Setter
        compoundSetString=ReflectionUtils.getMethod(NBTTagCompoundClass, "setString", String.class,String.class); //Setter
        compoundSetDouble=ReflectionUtils.getMethod(NBTTagCompoundClass, "setDouble", String.class,double.class); //Setter
        compoundSetFloat=ReflectionUtils.getMethod(NBTTagCompoundClass, "setFloat", String.class,float.class);
        compoundSetLong=ReflectionUtils.getMethod(NBTTagCompoundClass, "setLong", String.class,long.class);
                
        compoundKeySet=ReflectionUtils.getMethod(NBTTagCompoundClass, "c");
        if(!Utils.bukkitVersion("1.8")) { compoundGetTypeByKey=ReflectionUtils.getMethod(NBTTagCompoundClass, "d",String.class); }
        else { compoundGetTypeByKey=ReflectionUtils.getMethod(NBTTagCompoundClass, "b",String.class); }
                
        compoundHasKey=ReflectionUtils.getMethod(NBTTagCompoundClass, "hasKey", String.class); //Checker
        compoundHasKeyOfType=ReflectionUtils.getMethod(NBTTagCompoundClass, "hasKeyOfType", String.class, int.class);
        compoundRemoveKey=ReflectionUtils.getMethod(NBTTagCompoundClass, "remove", String.class); //Removed
    }
    
    
    public static class NBTBase implements Tags
    {
        private final Object nbt;
        public NBTBase(final Object value) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
        { 
            nbt=NBTTag.newObject(value);
        }
        
        @Override
        public Object getNBTObject() { return nbt; }
        
        @Override
        public String toString(){ return nbt.toString(); }
    }
    
    public static class NBTList implements Tags
    {
        private final Object list;
        public NBTList() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            list=NBTTag.newList();
        }
        
        public NBTList(final Object list) { this.list=list; }
        
        @Override
        public Object getNBTObject() { return list; }
        
        public void addNBTBase(final NBTBase base) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            NBTTag.listAdd(list, base);
        }
        
        public void addNBTCompound(final NBTCompound compound) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            NBTTag.listAdd(list, compound);
        }
        
        public NBTType getListType() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            return NBTType.getById(NBTTag.listGetType(list));
        }
        
        @Override
        public String toString(){ return list.toString(); }
    }
    
    public static class NBTCompound implements Tags
    {
        private final Object compound;
        public NBTCompound() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            compound=NBTTag.newCompound();
        }
        
        public NBTCompound(final Object compound) { this.compound=compound; }
        
        @Override
        public Object getNBTObject() { return compound; }
        
        public void addValue(final String key, final Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            NBTTag.compoundSet(this, key, value);
        }
        
        public void addCompound(final String key, final NBTCompound nbt) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            NBTTag.compoundSetNBT(this, key, nbt);
        }
        
        public void addList(final String key, final NBTList nbt) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            NBTTag.compoundSetNBT(this, key, nbt);
        }
        
        public void addBase(final String key, final NBTBase nbt) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            NBTTag.compoundSetNBT(this, key, nbt);
        }
        
        public boolean hasKey(final String key) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
        {
            return (boolean)NBTTag.compoundHasKey(compound, key);
        }
        
        public NBTType getTypeByKey(final String key) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            return NBTType.getById((byte)compoundGetTypeByKey.invoke(compound, key));
        }
        
        public boolean hasKeyOfType(final String key, final NBTType type) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
        {
            return (boolean)NBTTag.compoundHasKeyOfType(compound, key, type);
        }
        
        public <T> T getKey(final String key, final Class<T> type) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            return (T)NBTTag.compoundGet(this, key, type);
        }
        
        public Set<String> keySet() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
        {
            return (Set<String>)NBTTag.compoundKeySet.invoke(compound);
        }
        
        @Override
        public String toString(){ return compound.toString(); }
    }
    
    private interface Tags
    {
        Object getNBTObject();
    }
}
