/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions.parsers;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import me.parozzz.hopeitems.items.managers.IManager;
import me.parozzz.hopeitems.items.managers.actions.IAction;
import me.parozzz.hopeitems.items.managers.actions.MobAction;
import me.parozzz.hopeitems.items.managers.actions.PlayerAction;
import me.parozzz.hopeitems.items.managers.actions.PlayerEffectAction;
import me.parozzz.hopeitems.items.managers.actions.WorldEffectAction;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class ActionManager implements IManager
{
    private final List<IAction> actions; 
    public ActionManager()
    {
        actions = new LinkedList<>();
    }
    
    public void addAction(final IAction action)
    {
        actions.add(action);
    }
    
    public void executeAll(final Player p)
    {
        executeAll(p, p, null);
    }
    
    public void executeAll(final LivingEntity ent)
    {
        executeAll(null, ent, null);
    }
    
    public void executeAll(final Location l)
    {
        executeAll(null, null, l);
    }
    
    public void executeAll(final Player p, final LivingEntity ent, final Location l)
    {
        actions.forEach(action -> 
        {
            switch(action.getType())
            {
                case PLAYER:
                    Optional.ofNullable(p).ifPresent(((PlayerAction)action)::execute);
                    break;
                case MOB:
                    Optional.ofNullable(ent).ifPresent(((MobAction)action)::execute);
                    break;
                case PLAYEREFFECT:
                    if(p == null || l == null)
                    {
                        return;
                    }
                    ((PlayerEffectAction)action).execute(p, l);
                    break;
                case WORLDEFFECT:
                    Optional.ofNullable(l).ifPresent(((WorldEffectAction)action)::execute);
                    break;
            }
        });
    }
}
