/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import me.parozzz.reflex.MCVersion;

/**
 *
 * @author Paros
 */
public class ReflectionUtil 
{
    private static final Method serialize;
    private static final Class<?> nmsChatSerializer;

    static
    {
        if (MCVersion.nms().contains("1_8")) 
        {
            if (MCVersion.nms().contains("R1")) 
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

        serialize = getMethod(nmsChatSerializer, "a", String.class);
    }
    
    public static Object getStringSerialized(final String str)
    {
        try {
            return serialize.invoke(nmsChatSerializer,"{\"text\":\""+str+"\"}");
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ReflectionUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static Class<?> getNMSClass(final String className)
    {
        try  {
            return Class.forName(new StringBuilder("net.minecraft.server.").append(MCVersion.nms()).append(className).toString());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ReflectionUtil.class.getSimpleName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static Class<?> getCraftbukkitClass(final String path)
    {
        try  { 
            return Class.forName(new StringBuilder("org.bukkit.craftbukkit.").append(MCVersion.nms()).append(path).toString());
        }  catch (ClassNotFoundException ex)  {
            Logger.getLogger(ReflectionUtil.class.getSimpleName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static <T> Constructor<T> getConstructor(final Class<T> clazz, final Class<?>... types)
    {
        try {
            return clazz.getDeclaredConstructor(types);
        } catch (SecurityException | NoSuchMethodException ex) {
            Logger.getLogger(ReflectionUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static Field getField(final Class<?> clazz, final String name)
    {
        try  {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new NullPointerException("Field "+name+" does not exist in class "+clazz.getName());
        }
    }

    public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... args) 
    {
        return Arrays.stream(clazz.getDeclaredMethods())
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
