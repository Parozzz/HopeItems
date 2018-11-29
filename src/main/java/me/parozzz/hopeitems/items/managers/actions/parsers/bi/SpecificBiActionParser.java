/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions.parsers.bi;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import me.parozzz.hopeitems.items.managers.ISpecificParser;
import me.parozzz.hopeitems.items.managers.actions.ActionType;
import me.parozzz.hopeitems.items.managers.actions.IAction;
import me.parozzz.hopeitems.items.managers.actions.PlayerAction;
import me.parozzz.hopeitems.items.managers.actions.PlayerEffectAction;
import me.parozzz.hopeitems.items.managers.actions.WorldEffectAction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class SpecificBiActionParser<T, H> implements ISpecificParser<String, IAction>
{
    private final ActionType at;
    private final Function<String, BiConsumer<T, H>> function;
    public SpecificBiActionParser(final ActionType at, final Function<String, BiConsumer<T, H>> function)
    {
        this.at = at;
        this.function = function;
    }
    
    public ActionType getActionType()
    {
        return at;
    }

    @Override
    public IAction parse(final String p) 
    {
        switch(at)
        {
            case PLAYEREFFECT:
                return new PlayerEffectAction((BiConsumer<Player, Location>)function.apply(p));
            default:
                throw new IllegalArgumentException("The selected action type is a single type action not double");
        }
    }
    
}
