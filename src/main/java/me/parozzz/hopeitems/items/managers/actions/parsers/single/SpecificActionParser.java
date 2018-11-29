/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions.parsers.single;

import java.util.function.Consumer;
import java.util.function.Function;
import me.parozzz.hopeitems.items.managers.ISpecificParser;
import me.parozzz.hopeitems.items.managers.actions.ActionType;
import me.parozzz.hopeitems.items.managers.actions.IAction;
import me.parozzz.hopeitems.items.managers.actions.MobAction;
import me.parozzz.hopeitems.items.managers.actions.PlayerAction;
import me.parozzz.hopeitems.items.managers.actions.WorldEffectAction;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class SpecificActionParser<T> implements ISpecificParser<String, IAction>
{
    private final ActionType at;
    private final Function<String, Consumer<T>> function;
    public SpecificActionParser(final ActionType at, final Function<String, Consumer<T>> function)
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
            case PLAYER:
                return new PlayerAction((Consumer<Player>)function.apply(p));
            case WORLDEFFECT:
                return new WorldEffectAction((Consumer<Location>)function.apply(p));
            case MOB:
                return new MobAction((Consumer<LivingEntity>)function.apply(p));
            default:
                throw new IllegalArgumentException("The selected action type is a double type action not single");
        }
    }
    
}
