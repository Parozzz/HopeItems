/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.core;

import static it.parozzz.hopeitems.reflection.Packets.getHandle;
import static it.parozzz.hopeitems.reflection.Packets.getStringSerialized;
import static it.parozzz.hopeitems.reflection.Packets.playerConnection;
import static it.parozzz.hopeitems.reflection.Packets.sendPacket;
import it.parozzz.hopeitems.reflection.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.bukkit.entity.Player;


public class ActionBar {
    private static Object nmsChatMessageType;
    private static Constructor<?> PacketPlayOutChat;
    public static void initialize() 
    {
        nmsChatMessageType = (byte)2;
        Class<?> ChatMessageType=byte.class;
        
        if(Utils.bukkitVersion("1.12"))
        {
            ChatMessageType=ReflectionUtils.getNMSClass("ChatMessageType");
            nmsChatMessageType= Arrays.stream(ChatMessageType.getEnumConstants())
                    .filter(o -> o.toString().equals("GAME_INFO")).findFirst().get();
        }
        
        try 
        {
            PacketPlayOutChat=ReflectionUtils.getNMSClass("PacketPlayOutChat")
                    .getConstructor(ReflectionUtils.getNMSClass("IChatBaseComponent"), ChatMessageType);
        }
        catch(NoSuchMethodException | SecurityException ex) { ex.printStackTrace(); }
    }
    
    public static void send(final Player p, final String message) 
    {
        try 
        {
            sendPacket(playerConnection(getHandle(p)), 
                    PacketPlayOutChat.newInstance(getStringSerialized(message), nmsChatMessageType));
        } 
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | SecurityException | InvocationTargetException ex)  
        { 
            ex.printStackTrace(); 
        }
    }
}