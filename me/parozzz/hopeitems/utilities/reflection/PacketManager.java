/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import me.parozzz.hopeitems.utilities.reflection.API.ReflectionUtils;
import org.bukkit.entity.Player;
/**
 *
 * @author Paros
 */
public final class PacketManager 
{
    private final Method handle;
    private final Method getWorld;
    private final Field playerConnection;
    protected PacketManager()
    {

        Class<?> CraftPlayer=ReflectionUtils.getCraftbukkitClass("entity.CraftPlayer");
        handle=ReflectionUtils.getMethod(CraftPlayer,"getHandle",new Class[0]);

        Class<?> EntityPlayer=ReflectionUtils.getNMSClass("EntityPlayer");
        playerConnection=ReflectionUtils.getField(EntityPlayer, "playerConnection");
        getWorld=ReflectionUtils.getMethod(EntityPlayer, "getWorld", new Class[0]);
    }
    
    public Object getHandle(final Player p) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return handle.invoke(p);
    }
    
    private Object getWorld(final Object handle) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return getWorld.invoke(handle);
    }
    
    private Object playerConnection(final Object handle) throws IllegalArgumentException, IllegalAccessException
    {
        return playerConnection.get(handle);
    }
    
    public void sendPacket(final Player p, final Object packet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Object connection=playerConnection(getHandle(p));
        ReflectionUtils.getMethod(connection.getClass(), "sendPacket").invoke(connection, packet);
    }
}
