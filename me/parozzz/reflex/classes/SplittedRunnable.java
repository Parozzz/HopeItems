/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.classes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import me.parozzz.reflex.utilities.TaskUtil;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 * @param <T>
 */
public abstract class SplittedRunnable<T> extends BukkitRunnable
{
    private final int splitAmount;
    public SplittedRunnable(final int splitAmount)
    {
        this.splitAmount = splitAmount;
    }
    
    public abstract Collection<T> getCollection();
    
    public abstract Consumer<T> getConsumer();
    
    private final List<List<T>> splitted = new ArrayList<>();
    private int counter = -1;
    
    @Override
    public final void run() 
    {
        if(counter == -1 || counter++ == splitAmount - 1)
        {
            counter = 0;
            splitted.clear();
            Collection<T> collection = this.getCollection();
            if(!collection.isEmpty())
            {
                splitted.addAll(TaskUtil.splitCollection(collection, (collection.size() / splitAmount) + 1));
            }
        }
        
        Util.ifCheck(counter < splitted.size(), () -> splitted.get(counter).forEach(getConsumer()));
    }
    
}
