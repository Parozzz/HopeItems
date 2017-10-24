/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.bukkit.Bukkit;

/**
 *
 * @author Paros
 */
public class API 
{
    
    private static ActionBar actionBar;
    public static ActionBar getActionBar()
    {
        return Optional.ofNullable(actionBar).orElseGet(() -> actionBar=new ActionBar());
    }
    
    private static Title title;
    public static Title getTitle()
    {
        return Optional.ofNullable(title).orElseGet(() -> title=new Title());
    }
    
    private static PacketManager packets;
    public static PacketManager getPacketManager()
    {
        return Optional.ofNullable(packets).orElseGet(() -> packets=new PacketManager());
    }
    
    private static ParticleManager particles;
    public static ParticleManager getParticleManager()
    {
        return Optional.ofNullable(particles).orElseGet(() -> particles=new ParticleManager());
    }
    
    private static NBTTagManager nbt;
    public static NBTTagManager getNBT()
    {
        return Optional.ofNullable(nbt).orElseGet(() -> nbt=new NBTTagManager());
    }
    
    
    private static String version;
    public static String getVersion()
    {
        return Optional.ofNullable(version).orElseGet(() -> 
        {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            return version = name.substring(name.lastIndexOf('.') + 1) + ".";
        });
    }
    
    
    
    public static class ReflectionUtils
    {
        private static final Method serialize;
        private static final Class<?> nmsChatSerializer;
    
        static
        {
            if (getVersion().contains("1_8")) 
            {
                if (getVersion().contains("R1")) 
                {
                    nmsChatSerializer = getNMSClass("ChatSerializer");
                } 
                else //if (version.contains("R2") || version.contains("R3")) 
                {
                    nmsChatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
                }
            } 
            else 
            {
                nmsChatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
            }
            
            serialize=getMethod(nmsChatSerializer, "a", String.class);
        }
        
        public static Object getStringSerialized(final String str) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            return serialize.invoke(nmsChatSerializer,"{\"text\":\""+str+"\"}");
        }

        public static Class<?> getNMSClass(final String className)
        {
            try 
            {
                return Class.forName(new StringBuilder("net.minecraft.server.").append(getVersion()).append(className).toString());
            } 
            catch (ClassNotFoundException ex) 
            {
                Logger.getLogger(API.class.getSimpleName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        public static Class<?> getCraftbukkitClass(final String path)
        {
            try 
            { 
                return Class.forName(new StringBuilder("org.bukkit.craftbukkit.").append(getVersion()).append(path).toString());
            } 
            catch (ClassNotFoundException ex) 
            {
                Logger.getLogger(API.class.getSimpleName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }


        public static Field getField(Class<?> clazz, String name)
        {
            try 
            {
                return Optional.ofNullable(clazz.getDeclaredField(name)).map(field -> 
                {
                    field.setAccessible(true);
                    return field;
                })
                .orElseThrow(() -> new NullPointerException("Field "+name+" does not exist in class "+clazz.getName()));
            } 
            catch (NoSuchFieldException | SecurityException ex) 
            {
                Logger.getLogger(API.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... args) 
        {
            return Arrays.stream(clazz.getMethods())
                    .filter(mt -> mt.getName().equals(name))
                    .filter(mt -> args.length == 0 || classListEqual(args, mt.getParameterTypes()))
                    .findFirst().map(method -> 
                    { 
                        method.setAccessible(true);
                        return method;
                    }).orElseThrow(() -> new NullPointerException("Method "+name+" does not exist in class "+clazz.getSimpleName()));
        }

        public static boolean classListEqual(Class<?>[] l1, Class<?>[] l2) 
        {
            return l1.length == l2.length && IntStream.of(l1.length-1).allMatch(i -> l1[i] == l2[i]);
        } 
    }
}
