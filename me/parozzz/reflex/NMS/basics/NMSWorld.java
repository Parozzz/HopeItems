/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.basics;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.NMS.NMSWrapper;
import me.parozzz.reflex.NMS.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 *
 * @author Paros
 */
public class NMSWorld implements NMSWrapper
{
    private static final Class<?> worldServerClass;
    private static final Method getHandle;
    static
    {
        worldServerClass = ReflectionUtil.getNMSClass("WorldServer");
        
        getHandle = ReflectionUtil.getMethod(worldServerClass, "getHandle");
    }
    
    public static Class<?> getNMSClass()
    {
        return worldServerClass;
    }
    
    private final static Map<World, NMSWorld> worlds = new HashMap<>();
    public static NMSWorld getNMSWorld(final String name)
    {
        World w = Bukkit.getWorld(name);
        if(w == null)
        {
            return null;
        }
        
        return worlds.computeIfAbsent(w, NMSWorld::new);
    }
    
    
    private final Object nmsWorld;
    private NMSWorld(final World w)
    {
        nmsWorld = Debug.validateMethod(getHandle, w);
    }
    
    @Override
    public Object getNMSObject() 
    {
        return nmsWorld;
    }
    
}
