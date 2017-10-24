/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions;

import java.util.function.Consumer;

/**
 *
 * @author Paros
 * @param <T>
 */
public class SingleActionManager<T> implements ActionManager
{
    private Consumer<T> action;
    private final ActionType at;
    public SingleActionManager(final ActionType at)
    {
        action= t -> {};
        this.at=at;
    }
    
    @Override
    public ActionType getType()
    {
        return at;
    }
    
    public void addAction(final Consumer<T> newAction)
    {
        action = action.andThen(newAction);
    }
    
    public void executeAction(final T t)
    {
        action.accept(t);
    }
}
