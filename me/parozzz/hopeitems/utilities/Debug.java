/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paros
 */
public class Debug 
{
    public static boolean debugMode=false;
    
    public static <T extends Enum> T validateEnum(final String name, final Class<T> en)
    {
        return Optional.ofNullable(name).map(String::toUpperCase).map(s -> 
        {
            try 
            {
                return (T)Enum.valueOf(en, s); 
            }
            catch(final IllegalArgumentException t)
            {
                throw new IllegalArgumentException("Wrong format type for enum "+en.getSimpleName()+". "+name+" does not exist in this enum");
            }
        }).orElseThrow(() -> new IllegalArgumentException("Null value passed to "+en.getSimpleName()+ " for enum validation"));
    }
    
    public static Object validateMethod(final Method m, final Object o, final Object... arguments)
    {
        try 
        { 
            return m.invoke(o, arguments); 
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) 
        {
            Logger.getLogger(Debug.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static Object validateConstructor(final Constructor<?> c, final Object... arguments)
    {
        try 
        {
            return c.newInstance(arguments); 
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) 
        {
            Logger.getLogger(Debug.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
