/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection.nbt;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import me.parozzz.hopeitems.utilities.Debug;
import me.parozzz.hopeitems.utilities.reflection.API;
import me.parozzz.hopeitems.utilities.reflection.NBTTagManager.NBTType;

/**
 *
 * @author Paros
 */
public class NBTCompound implements Tags, Cloneable
{
    private final Object nbtCompound;
    public NBTCompound()
    {
        nbtCompound = Debug.validateConstructor(API.getNBT().getConstructor(NBTType.COMPOUND));
    }

    public NBTCompound(final Object compound) 
    {
        this.nbtCompound=compound; 
    }

    public <T> T getKey(final String key, final Class<T> clazz)
    {
        return (T) Debug.validateMethod(API.getNBT().getCompoundGetter(NBTType.getByPrimitive(clazz)), nbtCompound, key);
    }
    
    public NBTCompound getCompound(final String key)
    {
        return new NBTCompound(Debug.validateMethod(API.getNBT().getCompoundGetter(NBTType.COMPOUND), nbtCompound, key));
    }
    
    public NBTList getList(final String key, final NBTType type)
    {
        return new NBTList(Debug.validateMethod(API.getNBT().getCompoundGetter(NBTType.LIST), nbtCompound, key, (int)type.getId()));
    }
    
    public void removeKey(final String key)
    {
        Debug.validateMethod(API.getNBT().compoundRemoveKey, nbtCompound, key);
    }

    public void setValue(final String key, final NBTType type, final Object value)
    { 
        Debug.validateMethod(API.getNBT().getCompoundSetter(type), nbtCompound, key, value);
    }
    
    public void setTag(final String key, final Tags nbt)
    {
        Debug.validateMethod(API.getNBT().compoundSetNBT, nbtCompound, key, nbt.getNBTObject());
    }

    public boolean hasKey(final String key)
    {
        return (boolean)Debug.validateMethod(API.getNBT().compoundHasKey, nbtCompound, key);
    }

    public NBTType getTypeByKey(final String key)
    {
        return NBTType.getById((byte)Debug.validateMethod(API.getNBT().compoundGetTypeByKey, nbtCompound, key));
    }

    public boolean hasKeyOfType(final String key, final NBTType type)
    {
        return (boolean)Debug.validateMethod(API.getNBT().compoundHasKeyOfType, nbtCompound, key, type.getId());
    }

    public Set<String> keySet()
    {
        return (Set<String>)Debug.validateMethod(API.getNBT().compoundKeySet, nbtCompound);
    }
    
    @Override
    public Object getNBTObject() 
    {
        return nbtCompound; 
    }

    @Override
    public String toString()
    {
        return nbtCompound.toString(); 
    }
}
