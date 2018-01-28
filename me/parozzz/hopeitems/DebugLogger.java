/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 *
 * @author Paros
 */
public class DebugLogger 
{
    private static DebugLogger loggerInstance;
    public static DebugLogger getLogger()
    {
        return Optional.ofNullable(loggerInstance).orElseGet(() -> loggerInstance = new DebugLogger());
    }
    
    private DebugLogger() {}
    
    private boolean debug = false;
    protected void setDebug(final boolean debug)
    {
        this.debug = debug;
    }
    
    public void info(final String msg, final Class<?> clazz)
    {
        if(!debug)
        {
            return;
        }
        
        Logger logger = Logger.getLogger(clazz.getSimpleName());
        logger.log(Level.INFO, getDebuggedString(msg));
    }
    
    public void warn(final String msg, final Class<?> clazz)
    {
        if(!debug)
        {
            return;
        }
        
        Logger logger = Logger.getLogger(clazz.getSimpleName());
        logger.log(Level.WARNING, getDebuggedString(msg));
    }
    
    public void warn(final Exception ex, final Class<?> clazz)
    {
        warn(null, ex, clazz);
    }
    
    public void warn(final String msg, final Exception ex, final Class<?> clazz)
    {
        if(!debug)
        {
            return;
        }
                
        Logger logger = Logger.getLogger(clazz.getSimpleName());
        logger.log(Level.WARNING, getDebuggedString(msg), ex);
    }
    
    private @Nullable String getDebuggedString(final @Nullable String str)
    {
        if(str == null)
        {
            return null;
        }
        return "[HopeItems Debug] " + str; 
    }
}
