/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions.parsers.bi;

import java.util.function.BiConsumer;
import me.parozzz.hopeitems.items.managers.actions.ActionType;
import me.parozzz.hopeitems.items.managers.actions.IAction;

/**
 *
 * @author Paros
 * @param <T>
 * @param <H>
 */
public abstract class AbstractBiAction<T, H> implements IAction
{
    private final ActionType at;
    private final BiConsumer<T, H> consumer;
    public AbstractBiAction(final ActionType at, final BiConsumer<T, H> consumer)
    {
        this.at = at;
        this.consumer = consumer;
    }
    
    @Override
    public ActionType getType()
    {
        return at;
    }
    
    public void execute(final T t, final H h)
    {
        consumer.accept(t, h);
    }
}
