/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.conditions;

import java.util.Optional;
import java.util.function.Predicate;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Paros
 */
public class Condition<T> 
{
    private final Predicate<T> predicate;
    private final String message;
    public Condition(final Predicate<T> predicate, final String message)
    {
        this.message = message;
        this.predicate = predicate;
    }
    
    public boolean test(final T t)
    {
        return predicate.test(t);
    }
    
    public boolean test(final T t, final CommandSender cs)
    {
        if(!predicate.test(t))
        {
            Optional.ofNullable(message).ifPresent(cs::sendMessage);
            return false;
        }
        return true;
    }
    
}
