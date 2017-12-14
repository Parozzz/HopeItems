/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.packets;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.NMS.NMSWrapper;
import me.parozzz.reflex.NMS.ReflectionUtil;

/**
 *
 * @author Paros
 */
public class ChatPacket extends Packet
{
    public enum MessageType implements NMSWrapper
    {
        CHAT("CHAT", (byte)0), SYSTEM("SYSTEM", (byte)1), ACTIOBAR("GAME_INFO", (byte)2);
        
        private final Object nmsObject;
        private MessageType(final String enumName, final byte id)
        {
            nmsObject = MCVersion.V1_12.isHigher() ? Stream.of(getNMSClass().getEnumConstants()).filter(obj -> obj.toString().equals(enumName)).findFirst().get() : id;
        }

        @Override
        public Object getNMSObject() 
        {
            return nmsObject;
        }
        
        public static Class<?> getNMSClass()
        {
            return messageTypeClazz;
        }
    }
    
    private static final Class<?> messageTypeClazz = MCVersion.V1_12.isHigher() ? ReflectionUtil.getNMSClass("ChatMessageType") : byte.class;
    private static final Constructor<?> constructor;
    static
    {
        constructor = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutChat"), ReflectionUtil.getNMSClass("IChatBaseComponent"), MessageType.getNMSClass());
    }
    
    private final Object packet;
    public ChatPacket(final MessageType mt, final String str)
    {
        packet = Debug.validateConstructor(constructor, ReflectionUtil.getStringSerialized(str), mt.getNMSObject());
    }
    
    @Override
    public Object getNMSObject() 
    {
        return packet;
    }
    
}
