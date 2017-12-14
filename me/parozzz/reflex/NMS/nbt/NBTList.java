/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.nbt;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.NMS.ReflectionUtil;

/**
 *
 * @author Paros
 */
public class NBTList extends NBTBase
{
    private static final Class<?> listClazz;
    private static final Constructor<?> constructor;
    
    private static final Method addToMethod;
    private static final Method getTypeMethod;
    private static final Method sizeMethod;
    private static Method getBaseMethod;
    static
    {
        listClazz = ReflectionUtil.getNMSClass("NBTTagList");
        constructor = ReflectionUtil.getConstructor(listClazz);
        
        addToMethod = ReflectionUtil.getMethod(listClazz, "add", NBTBase.getNMSClass());
        getTypeMethod = ReflectionUtil.getMethod(listClazz, MCVersion.V1_8.isEqual()? "f" : "g"); 
        sizeMethod = ReflectionUtil.getMethod(listClazz, "size");
        
        try { 
            getBaseMethod = ReflectionUtil.getMethod(listClazz, "i", int.class); 
        } catch(final NullPointerException t) {  
            
        }
    }
    
    public static Class<?> getNMSClass()
    {
        return listClazz;
    }
    
    public NBTList()
    {
        super(Debug.validateConstructor(constructor));
    }

    public NBTList(final Object nbt) 
    {
        super(nbt);
    }

    public void addTag(final NBTBase nbt)
    {
        Debug.validateMethod(addToMethod, super.nbtBase, nbt.getNMSObject());
    }
    
    public NBTBase getTag(final NBTType type, final int i)
    {
        return new NBTBase(type, Debug.validateMethod(getBaseMethod,  super.nbtBase, i));
    }
    
    public Object getTag(final int i)
    {
        return Debug.validateMethod(getBaseMethod,  super.nbtBase, i);
    }
    
    public int size()
    {
        return (int)Debug.validateMethod(sizeMethod,  super.nbtBase);
    }
    
    public NBTType getListType()
    {
        return NBTType.getById((byte)Debug.validateMethod(getTypeMethod,  super.nbtBase));
    }
    
    @Override
    public NBTList clone()
    {
        return (NBTList) super.clone();
    }
}
