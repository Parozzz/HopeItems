/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.conditions;

import java.util.function.Predicate;
import me.parozzz.hopeitems.items.managers.conditions.AbstractCondition;
import me.parozzz.hopeitems.items.managers.conditions.ConditionType;
import org.bukkit.Location;

/**
 *
 * @author Paros
 */
public class LocationCondition extends AbstractCondition<Location>
{
    
    public LocationCondition(final Predicate<Location> predicate, final String message) 
    {
        super(ConditionType.LOCATION, predicate, message);
    }
    
}
