/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.packets;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.NMS.NMSWrapper;
import me.parozzz.reflex.NMS.ReflectionUtil;

/**
 *
 * @author Paros
 */
public class TitlePacket extends Packet
{
    public enum TitleType implements NMSWrapper
    {
        TITLE, SUBTITLE, CLEAR;
        
        private final Object enumTitle;
        private TitleType()
        {
            enumTitle = Stream.of(enumClazz.getEnumConstants()).filter(obj -> obj.toString().equals(name())).findFirst().orElse(null);
        }
        
        @Override
        public Object getNMSObject() 
        {
            return enumTitle;
        }
    }
    
    private final static Class<?> enumClazz;
    private final static Constructor<?> constructor;
    static
    {
        enumClazz = ReflectionUtil.getNMSClass("PacketPlayOutTitle$EnumTitleAction");

        Class<?> packetClazz = ReflectionUtil.getNMSClass("PacketPlayOutTitle");
        constructor = ReflectionUtil.getConstructor(packetClazz, enumClazz, ReflectionUtil.getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
    }
    
    private final Object packet;
    public TitlePacket(final TitleType type, final String str, final int fadeIn, final int stay, final int fadeOut)
    {
        packet = Debug.validateConstructor(constructor, type.getNMSObject(), ReflectionUtil.getStringSerialized(str), fadeIn, stay, fadeOut);
    }
    
    @Override
    public Object getNMSObject() 
    {
        return packet;
    }
    
}
