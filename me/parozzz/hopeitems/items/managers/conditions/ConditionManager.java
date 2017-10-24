/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.conditions;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Paros
 * @param <T>
 */
public class ConditionManager<T>
{
    private final Set<Condition<T>> conditions;
    private final ConditionType ct;
    public ConditionManager(final ConditionType ct)
    {
        conditions = new HashSet<>();
        this.ct=ct;
    }
    
    public ConditionType getType()
    {
        return ct;
    }
    
    public void addCondition(final Predicate<T> newCondition, final String message)
    {
        conditions.add(new Condition<>(newCondition , message));
    }
    
    public boolean testCondition(final T t)
    {
        return conditions.stream().allMatch(c -> c.test(t));
    }
    
    public boolean testCondition(final T t, final CommandSender cs)
    {
        return conditions.stream().allMatch(c -> c.test(t, cs));
    }
}
