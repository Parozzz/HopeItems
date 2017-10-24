/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection.nbt;

import java.lang.reflect.InvocationTargetException;
import me.parozzz.hopeitems.utilities.Debug;
import me.parozzz.hopeitems.utilities.reflection.API;
import me.parozzz.hopeitems.utilities.reflection.NBTTagManager;

/**
 *
 * @author Paros
 */
public class NBTBase implements Tags
{
    private final Object nbtBase;
    public NBTBase(final NBTTagManager.NBTType type, final Object value)
    { 
        nbtBase = Debug.validateConstructor(API.getNBT().getConstructor(type), value);
    }

    @Override
    public Object getNBTObject() 
    {
        return nbtBase; 
    }

    @Override
    public String toString()
    {
        return nbtBase.toString(); 
    }
}
