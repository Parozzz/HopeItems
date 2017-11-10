/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopeitems.utilities.reflection.API.ReflectionUtils;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public final class PacketManager 
{
    private final Method handle;
    private final Field playerConnection;
    protected PacketManager()
    {
        Class<?> CraftPlayer=ReflectionUtils.getCraftbukkitClass("entity.CraftPlayer");
        handle=ReflectionUtils.getMethod(CraftPlayer,"getHandle",new Class[0]);

        Class<?> EntityPlayer=ReflectionUtils.getNMSClass("EntityPlayer");
        playerConnection=ReflectionUtils.getField(EntityPlayer, "playerConnection");
    }
    
    public Object getHandle(final Player p) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return handle.invoke(p);
    }
    
    private Object playerConnection(final Object handle) throws IllegalArgumentException, IllegalAccessException
    {
        return playerConnection.get(handle);
    }
    
    private Method sendPacket;
    public void sendPacket(final Player p, final Object packet)
    {
        try
        {
            Object connection=playerConnection(getHandle(p));
            Optional.ofNullable(sendPacket).orElseGet(() -> sendPacket = ReflectionUtils.getMethod(connection.getClass(), "sendPacket")).invoke(connection, packet);  
        }
        catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException t)
        {
            Logger.getLogger(PacketManager.class.getSimpleName()).log(Level.SEVERE, null, t);
        }

    }
}
