/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.conditions.parsers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import me.parozzz.hopeitems.items.managers.IManager;
import me.parozzz.hopeitems.items.managers.conditions.AbstractCondition;
import me.parozzz.hopeitems.items.managers.conditions.LocationCondition;
import me.parozzz.hopeitems.items.managers.conditions.PlayerCondition;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class ConditionManager implements IManager
{
    private final List<AbstractCondition> conditions;
    public ConditionManager()
    {
        conditions = new LinkedList<>();
    }
    
    protected void addCondition(final AbstractCondition condition)
    {
        conditions.add(condition);
    }
    
    public boolean testConditions(final Location l)
    {
        return testConditions(l, null);
    }
    
    public boolean testConditions(final Player p)
    {
        return testConditions(null, p);
    }
    
    public boolean testConditions(final Location l, final Player p)
    {
        return conditions.stream().allMatch(cond ->
        {
            switch(cond.getConditionType())
            {
                case PLAYER: //Check if the player is not null, test the condition, true otherwise (Condition is true if a null value is passed as parameter)
                    return Optional.ofNullable(p).map(localPlayer -> ((PlayerCondition)cond).test(localPlayer, p)).orElse(true);
                case LOCATION:
                    return Optional.ofNullable(l).map(localLoc -> ((LocationCondition)cond).test(localLoc, p)).orElse(true);
            }
            return true;
        });
    }
}
