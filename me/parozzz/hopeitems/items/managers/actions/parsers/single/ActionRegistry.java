/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions.parsers.single;

import me.parozzz.hopeitems.items.managers.actions.parsers.single.SpecificActionParser;
import java.util.function.Consumer;
import java.util.function.Function;
import me.parozzz.hopeitems.items.managers.AbstractRegistry;
import me.parozzz.hopeitems.items.managers.actions.ActionType;

/**
 *
 * @author Paros
 * @param <T>
 */
public class ActionRegistry<T> extends AbstractRegistry<SpecificActionParser>
{
    private final ActionType at;
    public ActionRegistry(final ActionType at)
    {
        this.at = at;
    }
    
    public void addRegistered(final String key, final Function<String, Consumer<T>> function)
    {
        this.addRegistered(key, new SpecificActionParser(at, function));
    }
}
