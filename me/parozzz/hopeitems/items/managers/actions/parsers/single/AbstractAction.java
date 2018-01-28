/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions.parsers.single;

import java.util.function.Consumer;
import me.parozzz.hopeitems.items.managers.actions.ActionType;
import me.parozzz.hopeitems.items.managers.actions.IAction;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 * @param <T>
 */
public abstract class AbstractAction<T> implements IAction
{
    private final ActionType at;
    private final Consumer<T> consumer;
    public AbstractAction(final ActionType at, final Consumer<T> consumer)
    {
        this.at = at;
        this.consumer = consumer;
    }
    
    @Override
    public ActionType getType()
    {
        return at;
    }
    
    public void execute(final T t)
    {
        consumer.accept(t);
    }
}
