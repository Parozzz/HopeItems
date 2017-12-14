/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions;

import java.util.function.BiConsumer;

/**
 *
 * @author Paros
 * @param <T>
 * @param <H>
 */
public class DoubleActionManager<T, H> implements ActionManager
{
    private BiConsumer<T, H> action;
    private final ActionType at;
    public DoubleActionManager(final ActionType at)
    {
        this.at=at;
        action = (t,h) -> {};
    }
    
    @Override
    public ActionType getType()
    {
        return at;
    }
    
    public void addAction(final BiConsumer<T, H> newAction)
    {
        action = action.andThen(newAction);
    }
    
    public void executeAction(final T t, final H h)
    {
        action.accept(t, h);
    }
}
