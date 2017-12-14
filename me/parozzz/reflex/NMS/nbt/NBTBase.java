/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.nbt;

import java.lang.reflect.Method;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.NMS.NMSWrapper;
import me.parozzz.reflex.NMS.ReflectionUtil;

/**
 *
 * @author Paros
 */
public class NBTBase implements Cloneable, NMSWrapper
{
    private final static Class<?> baseClazz;
    private final static Method cloneMethod;
    static
    {
        baseClazz = ReflectionUtil.getNMSClass("NBTBase");
        cloneMethod = ReflectionUtil.getMethod(baseClazz, "clone");
    }
    
    public static Class<?> getNMSClass()
    {
        return baseClazz;
    }
    
    protected final Object nbtBase;
    public NBTBase(final NBTType type, final Object value)
    { 
        nbtBase = Debug.validateConstructor(type.getConstructor(), value);
    }
    
    protected NBTBase(final Object nbtBase)
    {
        this.nbtBase = nbtBase;
    }
    
    @Override
    public Object getNMSObject() 
    {
        return nbtBase;
    }
    
    @Override
    public String toString()
    {
        return nbtBase.toString(); 
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        return nbtBase.equals(obj);
    }
    
    @Override
    public int hashCode()
    {
        return nbtBase.hashCode();
    }
    
    @Override
    public NBTBase clone()
    {
        return new NBTBase(Debug.validateMethod(cloneMethod, nbtBase));
    }
}
