/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.reflection;

import it.parozzz.hopeitems.core.Utils;
import static it.parozzz.hopeitems.reflection.ReflectionUtils.getVersion;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.entity.Player;
/**
 *
 * @author Paros
 */
public class Packets 
{
    private static Method handle;
    private static Method getWorld;
    private static Field playerConnection;
    private static Method serialize;
    private static Class<?> nmsChatSerializer;
    
    public static void initialize() 
            throws ClassNotFoundException, NoSuchFieldException
    {
        if (getVersion().contains("1_8")) 
        {
            if (getVersion().contains("R1")) 
            {
                nmsChatSerializer = ReflectionUtils.getNMSClass("ChatSerializer");
            } 
            else if (getVersion().contains("R2") || getVersion().contains("R3")) 
            {
                nmsChatSerializer = ReflectionUtils.getNMSClass("IChatBaseComponent$ChatSerializer");
            }
        } 
        else if (Utils.bukkitVersion("1.8","1.9","1.10","1.11","1.12")) 
        {
            nmsChatSerializer = ReflectionUtils.getNMSClass("IChatBaseComponent$ChatSerializer");
        }

        serialize=ReflectionUtils.getMethod(nmsChatSerializer, "a", String.class);

        Class<?> CraftPlayer=ReflectionUtils.getCraftbukkitClass("entity.CraftPlayer");
        handle=ReflectionUtils.getMethod(CraftPlayer,"getHandle",new Class[0]);

        Class<?> EntityPlayer=ReflectionUtils.getNMSClass("EntityPlayer");
        playerConnection=ReflectionUtils.getField(EntityPlayer, "playerConnection");
        getWorld=ReflectionUtils.getMethod(EntityPlayer, "getWorld", new Class[0]);
    }
    
    public static Object getHandle(final Player p) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return handle.invoke(p);
    }
    
    public static Object getWorld(final Object handle) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return getWorld.invoke(handle);
    }
    
    public static Object playerConnection(final Object handle) 
            throws IllegalArgumentException, IllegalAccessException
    {
        return playerConnection.get(handle);
    }
    
    public static Object getStringSerialized(final String str) 
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return serialize.invoke(nmsChatSerializer,"{\"text\":\""+str+"\"}");
    }
    
    public static void sendPacket(final Player p, final Object packet)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        
        sendPacket(Packets.playerConnection(Packets.getHandle(p)),packet);
    }
            
    public static void sendPacket(final Object playerConnection, final Object packet)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        ReflectionUtils.getMethod(playerConnection.getClass(), "sendPacket").invoke(playerConnection, packet);
    }
}
