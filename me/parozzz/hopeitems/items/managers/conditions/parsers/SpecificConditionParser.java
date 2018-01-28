/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.conditions.parsers;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import me.parozzz.hopeitems.items.managers.ISpecificParser;
import me.parozzz.hopeitems.items.managers.conditions.AbstractCondition;
import me.parozzz.hopeitems.items.managers.conditions.ConditionType;
import me.parozzz.hopeitems.items.managers.conditions.LocationCondition;
import me.parozzz.hopeitems.items.managers.conditions.PlayerCondition;
import me.parozzz.reflex.configuration.MapArray;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 * @param <T>
 */
public class SpecificConditionParser<T> implements ISpecificParser<MapArray, AbstractCondition>
{
    private static final Logger logger = Logger.getLogger(SpecificConditionParser.class.getSimpleName());
    
    private final ConditionType ct;
    private final Function<String, Predicate<T>> function;
    public SpecificConditionParser(final ConditionType ct, final Function<String, Predicate<T>> function)
    {
        this.ct = ct;
        this.function = function;
    }
    
    public ConditionType getConditionType()
    {
        return ct;
    }
    
    @Override
    public @Nullable AbstractCondition parse(final MapArray map)
    {
        String message = map.hasKey("message") ? map.getValue("message", Util::cc) : null; //This can be null if no message is set.
        String value = map.getValue("value", Function.identity()); //This can't be null
        
        if(value == null)
        {
            logger.log(Level.WARNING, "A configuration error has been found while parsing a condition type {0}", ct.name());
            return null;
        }
        
        switch(ct)
        {
            case PLAYER:
                return new PlayerCondition((Predicate<Player>)function.apply(value), message);
            case LOCATION:
                return new LocationCondition((Predicate<Location>)function.apply(value), message);
            default:
                return null;
        }
    }
}
