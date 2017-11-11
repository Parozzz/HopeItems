/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.classes;

import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Paros
 */
public class Task 
{
    public static BukkitTask scheduleSyncTimer(final long delay, final long timer, final Supplier<Boolean> runnable)
    {
        return new BukkitRunnable()
        {
            @Override
            public void run() 
            {
                if(runnable.get())
                {
                    this.cancel();
                }
            }
        }.runTaskTimer(JavaPlugin.getProvidingPlugin(Task.class), delay, timer);
    }
    
    public static BukkitTask scheduleSync(final long delay, final Runnable runnable)
    {
        return new BukkitRunnable()
        {
            @Override
            public void run() 
            {
                runnable.run();
            }
        }.runTaskLater(JavaPlugin.getProvidingPlugin(Task.class), delay);
    }
}
