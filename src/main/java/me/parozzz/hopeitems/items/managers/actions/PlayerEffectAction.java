/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions;

import me.parozzz.hopeitems.items.managers.actions.parsers.bi.AbstractBiAction;
import java.util.function.BiConsumer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class PlayerEffectAction extends AbstractBiAction<Player, Location>
{
    
    public PlayerEffectAction(final BiConsumer<Player, Location> consumer) 
    {
        super(ActionType.PLAYEREFFECT, consumer);
    }
    
}
