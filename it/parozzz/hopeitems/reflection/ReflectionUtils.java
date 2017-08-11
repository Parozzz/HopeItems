/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.bukkit.Bukkit;
/**
 *
 * @author Paros
 */
public class ReflectionUtils 
{

    public static void initialize() 
    {      
        try 
        {
            NBT.initialize();
            Packets.initialize();
        } 
        catch (NoSuchMethodException | ClassNotFoundException | NoSuchFieldException ex) { ex.printStackTrace(); }
    }
    
    public static String getVersion() 
    {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1) + ".";
        return version;
    }
    
    public static Class<?> getNMSClass(final String className) 
    {
        try { return Class.forName("net.minecraft.server." + getVersion() + className); }
        catch (ClassNotFoundException e) { e.printStackTrace(); }
        return null;
    }
    
    public static Class<?> getCraftbukkitClass(final String path)
    {
        try { return Class.forName("org.bukkit.craftbukkit." + getVersion() + path); }
        catch (ClassNotFoundException e) { e.printStackTrace(); }
        return null;
    }
    
    public static Field getField(Class<?> clazz, String name) {
        try 
        {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } 
        catch (NoSuchFieldException | SecurityException ex) { ex.printStackTrace();}
        return null;
    }
    
    public static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        
        Method m=Arrays.stream(clazz.getMethods())
                .filter(mt -> mt.getName().equals(name))
                .filter(mt -> args.length == 0 || classListEqual(args, mt.getParameterTypes())).findFirst().orElse(null);
        if(m!=null) { m.setAccessible(true); } 
        return m;
    }
    
    private static boolean classListEqual(Class<?>[] l1, Class<?>[] l2) 
    {
        if (l1.length != l2.length) { return false; }
        for (int i = 0; i < l1.length; i++) { if (l1[i] != l2[i]) { return false; } }
        return true;
    }
}
