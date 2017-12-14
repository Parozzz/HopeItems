/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.blocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.NMS.NMSWrapper;
import me.parozzz.reflex.NMS.ReflectionUtil;
/**
 *
 * @author Paros
 */
public class BlockPosition implements NMSWrapper
{
    private static final Class<?> blockPositionClazz;
    private static final Constructor<?> intBlockPosition;
    private static final Constructor<?> doubleBlockPosition;
    
    private static final Method getX;
    private static final Method getY;
    private static final Method getZ;
    static
    {
        blockPositionClazz = ReflectionUtil.getNMSClass("BlockPosition");
        intBlockPosition = ReflectionUtil.getConstructor(blockPositionClazz, int.class, int.class, int.class);
        doubleBlockPosition = ReflectionUtil.getConstructor(blockPositionClazz, double.class, double.class, double.class);
        
        Class<?> baseBlockPositionClass = ReflectionUtil.getNMSClass("BaseBlockPosition");
        
        getX = ReflectionUtil.getMethod(baseBlockPositionClass, "getX");
        getY = ReflectionUtil.getMethod(baseBlockPositionClass, "getY");
        getZ = ReflectionUtil.getMethod(baseBlockPositionClass, "getZ");
    }
    
    public static Class<?> getNMSClass()
    {
        return blockPositionClazz;
    }
    
    private final Object position;
    public BlockPosition(final int x, final int y, final int z)
    {
        position = Debug.validateConstructor(intBlockPosition, x, y, z);
    }
    
    public BlockPosition(final double x, final double y, final double z)
    {
        position = Debug.validateConstructor(doubleBlockPosition, x, y, z);
    }
    
    public int getX()
    {
        return (int)Debug.validateMethod(getX, position);
    }
    
    public int getY()
    {
        return (int)Debug.validateMethod(getY, position);
    }
    
    public int getZ()
    {
        return (int)Debug.validateMethod(getZ, position);
    }
    
    @Override
    public Object getNMSObject() 
    {
        return position;
    }
}
