/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.reflection.API.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
/**
 *
 * @author Paros
 */
public class Title 
{
    private Object titleEnum;
    private Object subtitleEnum;
    
    private Constructor<?> PacketPlayOutTitleSerialized;
    private Constructor<?> PacketPlayOutTitleTimings;
    protected Title()
    {
        try
        {
            Class<?> enumClazz=ReflectionUtils.getNMSClass("PacketPlayOutTitle$EnumTitleAction");

            titleEnum=Stream.of(enumClazz.getEnumConstants()).filter(o -> o.toString().equals("TITLE")).findAny().get();
            subtitleEnum=Stream.of(enumClazz.getEnumConstants()).filter(o -> o.toString().equals("SUBTITLE")).findAny().get();

            Class<?> packetClazz=ReflectionUtils.getNMSClass("PacketPlayOutTitle");
            
            PacketPlayOutTitleSerialized=packetClazz.getConstructor(enumClazz, ReflectionUtils.getNMSClass("IChatBaseComponent"));
            PacketPlayOutTitleTimings=packetClazz.getConstructor(int.class, int.class, int.class);
        }
        catch(NoSuchMethodException | SecurityException t)
        {
            Logger.getLogger(Title.class.getSimpleName()).log(Level.SEVERE, null, t);
        }
    }
    
    public void sendTitle(final String title, final Player p, final int fadeIn, final int stay, final int fadeOut)
    {
        this.sendTitleAndSubTitle(title, "", p, fadeIn, stay, fadeOut);
    }
    
    public void sendSubTitle(final String subTitle, final Player p, final int fadeIn, final int stay, final int fadeOut)
    {
        this.sendTitleAndSubTitle("", subTitle, p, fadeIn, stay, fadeOut);
    }
    
    public void sendTitleAndSubTitle(final String title, final String subTitle, final Player p, final int fadeIn, final int stay, final int fadeOut)
    {
        try
        {
            if(!title.isEmpty())
            {
                Object packet=PacketPlayOutTitleSerialized.newInstance(titleEnum, ReflectionUtils.getStringSerialized(title));
                API.getPacketManager().sendPacket(p, packet);
            }
            
            if(!subTitle.isEmpty())
            {
                Object packet=PacketPlayOutTitleSerialized.newInstance(subtitleEnum, ReflectionUtils.getStringSerialized(subTitle));
                API.getPacketManager().sendPacket(p, packet);
            }
            
            API.getPacketManager().sendPacket(p, PacketPlayOutTitleTimings.newInstance(fadeIn, stay, fadeOut));
        }
        catch(IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException t)
        {
            Logger.getLogger(Title.class.getSimpleName()).log(Level.SEVERE, null, t);
        }
        
    }
}
