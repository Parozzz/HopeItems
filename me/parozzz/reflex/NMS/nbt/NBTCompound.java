/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.nbt;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.NMS.ReflectionUtil;

/**
 *
 * @author Paros
 */
public class NBTCompound extends NBTBase
{
    private final static Class<?> compoundClazz;
    private final static Constructor<?> constructor;
    
    private final static Method parseStringMethod;
    
    private final static Method setNBTMethod;
    private final static Method keySetMethod;
    private final static Method getTypeByKeyMethod;
    private final static Method hasKeyMethod;
    private final static Method hasKeyOfTypeMethod;
    private final static Method removeKeyMethod;
    static
    {
        Class<?> mojangsonParserClazz = ReflectionUtil.getNMSClass("MojangsonParser");
        parseStringMethod = ReflectionUtil.getMethod(mojangsonParserClazz, "parse", String.class);
        
        compoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
        constructor = ReflectionUtil.getConstructor(compoundClazz);
        
        setNBTMethod = ReflectionUtil.getMethod(compoundClazz, "set", String.class, NBTBase.getNMSClass()); 
        keySetMethod = ReflectionUtil.getMethod(compoundClazz, "c");
        getTypeByKeyMethod = ReflectionUtil.getMethod(compoundClazz, MCVersion.V1_8.isEqual()? "b" : "d", String.class); 
        hasKeyMethod = ReflectionUtil.getMethod(compoundClazz, "hasKey", String.class); //Checker
        hasKeyOfTypeMethod = ReflectionUtil.getMethod(compoundClazz, "hasKeyOfType", String.class, int.class);
        removeKeyMethod = ReflectionUtil.getMethod(compoundClazz, "remove", String.class); //Removed
    }
    
    public static Class<?> getNMSClass()
    {
        return compoundClazz;
    }
    
    public NBTCompound()
    {
        super(Debug.validateConstructor(constructor));
    }

    public NBTCompound(final Object compound) 
    {
        super(compound);
    }

    public NBTCompound(final String str)
    {
        super(Debug.validateMethod(parseStringMethod, null, str));
    }
    
    private <T> T getKey(final String key, final NBTType type)
    {
        return (T) Debug.validateMethod(type.getCompoundGetter(), super.nbtBase, key);
    }
    
    private void setKey(final String key, final NBTType type, final Object value)
    { 
        Debug.validateMethod(type.getCompoundSetter(), super.nbtBase, key, value);
    }
    
    public NBTCompound setBoolean(final String key, final boolean value)
    {
        setByte(key, (byte)(value ? 1 : 0));
        return this;
    }
    
    public boolean getBoolean(final String key)
    {
        return getByte(key) != 0;
    }
    
    public NBTCompound setByte(final String key, final byte value)
    {
        setKey(key, NBTType.BYTE, value);
        return this;
    }
    
    public byte getByte(final String key)
    {
        return (byte) getKey(key, NBTType.BYTE);
    }
    
    public NBTCompound setByteArray(final String key, final byte[] value)
    {
        setKey(key, NBTType.BYTEARRAY, value);
        return this;
    }
    
    public byte[] getByteArray(final String key)
    {
        return (byte[]) getKey(key, NBTType.BYTEARRAY);
    }
    
    public NBTCompound setShort(final String key, final short value)
    {
        setKey(key, NBTType.SHORT, value);
        return this;
    }
    
    public short getShort(final String key)
    {
        return (short) getKey(key, NBTType.SHORT);
    }
    
    public NBTCompound setInt(final String key, final int value)
    {
        setKey(key, NBTType.INT, value);
        return this;
    }
    
    public int getInt(final String key)
    {
        return (int) getKey(key, NBTType.INT);
    }
    
    public NBTCompound setIntArray(final String key, final int[] value)
    {
        setKey(key, NBTType.INTARRAY, value);
        return this;
    }
    
    public int[] getIntArray(final String key)
    {
        return (int[]) getKey(key, NBTType.INTARRAY);
    }
    
    public NBTCompound setLong(final String key, final long value)
    {
        setKey(key, NBTType.LONG, value);
        return this;
    }
    
    public long getLong(final String key)
    {
        return (long) getKey(key, NBTType.LONG);
    }
    
    public NBTCompound setFloat(final String key, final float value)
    {
        setKey(key, NBTType.FLOAT, value);
        return this;
    }
    
    public float getFloat(final String key)
    {
        return (float) getKey(key, NBTType.FLOAT);
    }
    
    public NBTCompound setDouble(final String key, final double value)
    {
        setKey(key, NBTType.DOUBLE, value);
        return this;
    }
    
    public double getDouble(final String key)
    {
        return (double) getKey(key, NBTType.DOUBLE);
    }
    
    public NBTCompound setString(final String key, final String value)
    {
        setKey(key, NBTType.STRING, value);
        return this;
    }
    
    public String getString(final String key)
    {
        return (String) getKey(key, NBTType.STRING);
    }
    
    public NBTCompound getCompound(final String key)
    {
        return new NBTCompound(Debug.validateMethod(NBTType.COMPOUND.getCompoundGetter(), super.nbtBase, key));
    }
    
    public NBTList getList(final String key, final NBTType type)
    {
        return new NBTList(Debug.validateMethod(NBTType.LIST.getCompoundGetter(), super.nbtBase, key, (int)type.getId()));
    }
    
    public void removeKey(final String key)
    {
        Debug.validateMethod(removeKeyMethod, super.nbtBase, key);
    }
    
    public void setTag(final String key, final NBTBase nbt)
    {
        Debug.validateMethod(setNBTMethod, super.nbtBase, key, nbt.getNMSObject());
    }

    public boolean hasKey(final String key)
    {
        return (boolean)Debug.validateMethod(hasKeyMethod, super.nbtBase, key);
    }

    public NBTType getTypeByKey(final String key)
    {
        return NBTType.getById((byte)Debug.validateMethod(getTypeByKeyMethod, super.nbtBase, key));
    }

    public boolean hasKeyOfType(final String key, final NBTType type)
    {
        return (boolean)Debug.validateMethod(hasKeyOfTypeMethod, super.nbtBase, key, type.getId());
    }

    public Set<String> keySet()
    {
        return (Set<String>)Debug.validateMethod(keySetMethod, super.nbtBase);
    }
    
    @Override
    public NBTCompound clone()
    {
        return (NBTCompound) super.clone();
    }
}
