/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions;

import me.parozzz.hopeitems.items.managers.actions.parsers.single.AbstractAction;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class PlayerAction extends AbstractAction<Player>
{
    
    public PlayerAction(final Consumer<Player> consumer) 
    {
        super(ActionType.PLAYER, consumer);
    }
    
}
