/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.conditions.parsers;

import java.util.function.Function;
import java.util.function.Predicate;
import me.parozzz.hopeitems.items.managers.AbstractRegistry;
import me.parozzz.hopeitems.items.managers.conditions.ConditionType;

/**
 *
 * @author Paros
 * @param <T>
 */
public class ConditionRegistry<T> extends AbstractRegistry<SpecificConditionParser>
{
    private final ConditionType ct;
    public ConditionRegistry(final ConditionType ct)
    {
        this.ct = ct;
    }
    
    public void addRegistred(final String key, final Function<String, Predicate<T>> function)
    {
        this.addRegistered(key, new SpecificConditionParser(ct, function));
    }
}
