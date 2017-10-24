/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection.nbt;

import java.lang.reflect.InvocationTargetException;
import me.parozzz.hopeitems.utilities.Debug;
import me.parozzz.hopeitems.utilities.reflection.API;
import me.parozzz.hopeitems.utilities.reflection.NBTTagManager.NBTType;

/**
 *
 * @author Paros
 */
public class NBTList implements Tags
{
    private final Object nbtList;
    public NBTList()
    {
        nbtList=Debug.validateConstructor(API.getNBT().getConstructor(NBTType.LIST));
    }

    public NBTList(final Object obj) 
    {
        nbtList=obj; 
    }

    public void addTag(final Tags nbt)
    {
        Debug.validateMethod(API.getNBT().listAddTo, nbtList, nbt.getNBTObject());
    }
    
    public NBTBase getTag(final NBTType type, final int i)
    {
        return new NBTBase(type, Debug.validateMethod(API.getNBT().listGetBase, nbtList, i));
    }
    
    public Object getTag(final int i)
    {
        return Debug.validateMethod(API.getNBT().listGetBase, nbtList, i);
    }
    
    public int size()
    {
        return (int)Debug.validateMethod(API.getNBT().listSize, nbtList);
    }
    
    public NBTType getListType()
    {
        return NBTType.getById((byte)Debug.validateMethod(API.getNBT().listGetType, nbtList));
    }

    @Override
    public String toString()
    {
        return nbtList.toString(); 
    }

    @Override
    public Object getNBTObject() 
    {
        return nbtList;
    }
}
