/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions.parsers.bi;

import java.util.function.BiConsumer;
import java.util.function.Function;
import me.parozzz.hopeitems.items.managers.AbstractRegistry;
import me.parozzz.hopeitems.items.managers.actions.ActionType;

/**
 *
 * @author Paros
 */
public class BiActionRegistry<T, H> extends AbstractRegistry<SpecificBiActionParser>
{
    private final ActionType at;
    public BiActionRegistry(final ActionType at)
    {
        this.at = at;
    }
    
    public void addRegistered(final String key, final Function<String, BiConsumer<T, H>> function)
    {
        this.addRegistered(key, new SpecificBiActionParser(at, function));
    }
}
