/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.nbt;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import me.parozzz.reflex.NMS.ReflectionUtil;

/**
 *
 * @author Paros
 */
public enum NBTType 
{
    BYTE((byte)1, ReflectionUtil.getNMSClass("NBTTagByte"),  "setByte", new Class[] {String.class, byte.class}, "getByte", new Class[] {String.class}),
    SHORT((byte)2, ReflectionUtil.getNMSClass("NBTTagShort"),  "setShort", new Class[] {String.class, short.class}, "getShort", new Class[] {String.class}),
    INT((byte)3, ReflectionUtil.getNMSClass("NBTTagInt"),  "setInt", new Class[] {String.class, int.class}, "getInt", new Class[] {String.class}),
    LONG((byte)4, ReflectionUtil.getNMSClass("NBTTagLong"), "setLong", new Class[] {String.class, long.class}, "getLong", new Class[] {String.class}),
    FLOAT((byte)5, ReflectionUtil.getNMSClass("NBTTagFloat"), "setFloat", new Class[] {String.class, float.class}, "getFloat", new Class[] {String.class}),
    DOUBLE((byte)6, ReflectionUtil.getNMSClass("NBTTagDouble"), "setDouble", new Class[] {String.class, double.class}, "getDouble", new Class[] {String.class}),
    BYTEARRAY((byte)7, ReflectionUtil.getNMSClass("NBTTagByteArray"), "setByteArray", new Class[] {String.class, byte[].class}, "getByteArray", new Class[] {String.class}),
    STRING((byte)8, ReflectionUtil.getNMSClass("NBTTagString"), "setString", new Class[] {String.class, String.class}, "getString", new Class[] {String.class}),
    LIST((byte)9, NBTList.getNMSClass(), "set", new Class<?>[] {String.class, NBTBase.getNMSClass()}, "getList", new Class[] {String.class, int.class}),
    COMPOUND((byte)10, NBTCompound.getNMSClass(), "set", new Class<?>[] {String.class, NBTBase.getNMSClass()}, "getCompound", new Class[] {String.class}),
    INTARRAY((byte)11, ReflectionUtil.getNMSClass("NBTTagIntArray"), "setIntArray", new Class<?>[] {String.class, int[].class}, "getIntArray", new Class[] {String.class});
    //LONGARRAY((byte)12, long[].class);

    private final Class<?> clazz;
    private final byte id;
    
    private final Constructor<?> constructor;
    private final Method compoundSetter;
    private final Method compoundGetter;
    private NBTType(final byte id, final Class<?> clazz, final String compoundSetterName, final Class<?>[] compoundSetterArray, final String compoundGetterName, final Class<?>[] getterClasses) 
    { 
        this.id=id;
        this.clazz=clazz;
        
        constructor = ReflectionUtil.getConstructor(clazz);
        compoundSetter = ReflectionUtil.getMethod(NBTCompound.getNMSClass(), compoundSetterName, compoundSetterArray);
        compoundGetter = ReflectionUtil.getMethod(NBTCompound.getNMSClass(), compoundGetterName, getterClasses);
    }

    public byte getId() 
    {
        return id; 
    }
    
    public Constructor<?> getConstructor()
    {
        return constructor;
    }
    
    public Method getCompoundGetter()
    {
        return compoundGetter;
    }
    
    public Method getCompoundSetter()
    {
        return compoundSetter;
    }

    public Class<?> getObjectClass() 
    {
        return clazz; 
    }
    
    public static NBTType getById(final byte id)
    {
        switch(id)
        {
            case 1:
                return BYTE;
            case 2:
                return SHORT;
            case 3:
                return INT;
            case 4:
                return LONG;
            case 5:
                return FLOAT;
            case 6:
                return DOUBLE;
            case 7:
                return BYTEARRAY;
            case 8:
                return STRING;
            case 9:
                return LIST;
            case 10:
                return COMPOUND;
            case 11:
                return INTARRAY;
            default:
                return null;
        }
    }
}
