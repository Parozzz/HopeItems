/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection;

import me.parozzz.hopeitems.utilities.Utils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.reflection.API.ReflectionUtils;
/**
 *
 * @author Paros
 */
public class NBTTagManager 
{
    public enum NBTType
    {
        BYTE((byte)1, ReflectionUtils.getNMSClass("NBTTagByte"), byte.class, "getByte", "setByte", byte.class),
        SHORT((byte)2, ReflectionUtils.getNMSClass("NBTTagShort"), short.class, "getShort", "setShort", short.class),
        INT((byte)3, ReflectionUtils.getNMSClass("NBTTagInt"), int.class, "getInt", "setInt", int.class),
        LONG((byte)4, ReflectionUtils.getNMSClass("NBTTagLong"), long.class, "getLong", "setLong", long.class),
        FLOAT((byte)5, ReflectionUtils.getNMSClass("NBTTagFloat"), float.class, "getFloat", "setFloat", float.class),
        DOUBLE((byte)6, ReflectionUtils.getNMSClass("NBTTagDouble"), double.class, "getDouble", "setDouble", double.class),
        BYTEARRAY((byte)7, ReflectionUtils.getNMSClass("NBTTagByteArray"), byte[].class, "getByteArray", "setByteArray", byte[].class),
        STRING((byte)8, ReflectionUtils.getNMSClass("NBTTagString"), String.class, "getString", "setString", String.class),
        LIST((byte)9, ReflectionUtils.getNMSClass("NBTTagList"), ReflectionUtils.getNMSClass("NBTBase"), "getList", "set"),
        COMPOUND((byte)10, ReflectionUtils.getNMSClass("NBTTagCompound"), ReflectionUtils.getNMSClass("NBTBase"), "getCompound", "set"),
        INTARRAY((byte)11, ReflectionUtils.getNMSClass("NBTTagIntArray"), int[].class, "getIntArray", "setIntArray", int[].class);
        //LONGARRAY((byte)12, long[].class);
        
        private final Class<?> clazz;
        private final Class<?> primitiveClazz;
        private final byte id;
        private final String getterName;
        private final String setterName;
        private final Class[] requiredClasses;
        private NBTType(final byte id, final Class<?> clazz, final Class<?> primitive, final String getter, final String setter, Class... requiredClasses) 
        { 
            this.id=id;
            this.clazz=clazz;
            primitiveClazz=primitive;
            this.requiredClasses=requiredClasses;
            
            getterName=getter;
            setterName=setter;
        }
        
        public byte getId() 
        {
            return id; 
        }
        
        public Class<?> getObjectClass() 
        {
            return clazz; 
        }
        
        public Class<?> getPrimitiveClass()
        {
            return primitiveClazz;
        }
        
        public Class[] getRequiredClasses()
        {
            return requiredClasses;
        }
        
        public String getGetterName()
        {
            return getterName;
        }
        
        public String getSetterName()
        {
            return setterName;
        }
        
        private static Map<Byte, NBTType> ids;
        public static NBTType getById(final byte id)
        {
            return Optional.ofNullable(ids).orElseGet(() -> 
            {
                ids = new HashMap<>();
                Stream.of(NBTType.values()).forEach(type -> ids.put(type.getId(), type));
                return ids;
            }).get(id);
        }
        
        private static Map<Class<?>, NBTType> primitives;
        public static NBTType getByPrimitive(final Class<?> clazz)
        {
            return Optional.ofNullable(primitives).orElseGet(() -> 
            {
                primitives = new HashMap<>();
                Stream.of(NBTType.values()).forEach(type -> 
                {
                    primitives.put(type.getPrimitiveClass(), type);
                });
                return primitives;
            }).get(clazz);
        }
    }
    
    /*
    ==================
    #### NBT BASE ####
    ==================
    */
    public Class<?> baseClass;
    private final EnumMap<NBTType, Constructor<?>> constructors=new EnumMap<>(NBTType.class);
    public Constructor<?> getConstructor(final NBTType type)
    {
        return constructors.get(type);
    }
    
    /*
    ==================
    #### COMPOUND ####
    ==================
    */
    
    /*
    ==========================
    #### COMPOUND SETTERS ####
    ==========================
    */
    public Method compoundSetNBT;
    public Method compoundKeySet;
    public Method compoundGetTypeByKey;
    
    private final EnumMap<NBTType ,Method> compoundSetters=new EnumMap(NBTType.class);
    public Method getCompoundSetter(final NBTType type)
    {
        return compoundSetters.get(type);
    }
    
    /*
    ==========================
    #### COMPOUND GETTERS ####
    ==========================
    */    
    public Method compoundHasKey;
    public Method compoundHasKeyOfType;
    public Method compoundRemoveKey; //compoundRemoveKey.invoke(Object compound, String key); 
    
    private final EnumMap<NBTType ,Method> compoundGetters=new EnumMap<>(NBTType.class);
    public Method getCompoundGetter(final NBTType type)
    {
        return compoundGetters.get(type);
    }
    /*
    ==============
    #### LIST ####
    ==============
    */
    public Method listAddTo;
    public Method listGetType;
    public Method listSize;
    public Method listGetBase;
    
    protected NBTTagManager()
    {
        baseClass=ReflectionUtils.getNMSClass("NBTBase");
        
        Class<?> listClass=NBTType.LIST.getObjectClass();    
        listAddTo=ReflectionUtils.getMethod(listClass, "add", baseClass);
        listGetType=ReflectionUtils.getMethod(listClass, MCVersion.V1_8.isEqual()? "f" : "g"); 
        listSize=ReflectionUtils.getMethod(listClass, "size");
        try { listGetBase=ReflectionUtils.getMethod(listClass, "i", int.class); }
        catch(final NullPointerException t) {  }
        
        Class<?> compoundClass=NBTType.COMPOUND.getObjectClass();
        compoundSetNBT=ReflectionUtils.getMethod(compoundClass, "set", String.class, baseClass); 
        compoundKeySet=ReflectionUtils.getMethod(compoundClass, "c");
        compoundGetTypeByKey=ReflectionUtils.getMethod(compoundClass, MCVersion.V1_8.isEqual()? "b" : "d", String.class); 
        compoundHasKey=ReflectionUtils.getMethod(compoundClass, "hasKey", String.class); //Checker
        compoundHasKeyOfType=ReflectionUtils.getMethod(compoundClass, "hasKeyOfType", String.class, int.class);
        compoundRemoveKey=ReflectionUtils.getMethod(compoundClass, "remove", String.class); //Removed
            
        Stream.of(NBTType.values()).forEach(type -> 
        {
            try
            {
                constructors.put(type, type.getObjectClass().getConstructor(type.getRequiredClasses()));
                if(type==NBTType.LIST)
                {
                    compoundGetters.put(type, ReflectionUtils.getMethod(compoundClass, type.getGetterName(), String.class, int.class));
                }
                else
                {
                    compoundGetters.put(type, ReflectionUtils.getMethod(compoundClass, type.getGetterName(), String.class));
                }
                
                compoundSetters.put(type, ReflectionUtils.getMethod(compoundClass, type.getSetterName(), String.class, type.getPrimitiveClass()));
            }
            catch(NoSuchMethodException | SecurityException ex)
            {
                Logger.getLogger(NBTTagManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}
