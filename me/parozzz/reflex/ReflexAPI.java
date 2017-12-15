/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.reflex.NMS.ReflectionUtil;
import me.parozzz.reflex.NMS.entity.EntityPlayer;
import me.parozzz.reflex.events.armor.ArmorHandler;
import me.parozzz.reflex.events.offhand.OffHandListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class ReflexAPI
{
    public enum Property
    {
        ENTITYPLAYER_LISTENER, ARMOREVENTS_LISTENER, OFFHANDEVENTS_LISTENER;
    }
    
    public static void enable(final Property... properties)
    {
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(ReflexAPI.class);
        Stream.of(properties).forEach(property -> 
        {
            try {
                switch(property)
                {
                    case ENTITYPLAYER_LISTENER:
                        Bukkit.getPluginManager().registerEvents((Listener)ReflectionUtil.getMethod(EntityPlayer.class, "getListener").invoke(null), plugin);
                        break;
                    case ARMOREVENTS_LISTENER:
                        Bukkit.getPluginManager().registerEvents(new ArmorHandler(), plugin);
                        break;
                    case OFFHANDEVENTS_LISTENER:
                        if(MCVersion.V1_9.isHigher())
                        {
                            Bukkit.getPluginManager().registerEvents(new OffHandListener(), plugin);
                        }
                        break;
                }
            }catch(final Exception ex){
                Logger.getLogger(ReflexAPI.class.getSimpleName()).log(Level.SEVERE, null, ex);
            }

        });
    }
}
