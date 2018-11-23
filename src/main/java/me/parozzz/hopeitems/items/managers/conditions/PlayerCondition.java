/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.conditions;

import java.util.function.Predicate;
import me.parozzz.hopeitems.items.managers.conditions.AbstractCondition;
import me.parozzz.hopeitems.items.managers.conditions.ConditionType;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class PlayerCondition extends AbstractCondition<Player>
{
    
    public PlayerCondition(final Predicate<Player> predicate, final String message) 
    {
        super(ConditionType.PLAYER, predicate, message);
    }
    
}
