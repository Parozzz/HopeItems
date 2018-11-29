/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions;

import java.util.function.Consumer;
import me.parozzz.hopeitems.items.managers.actions.parsers.single.AbstractAction;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public class MobAction extends AbstractAction<LivingEntity>
{
    
    public MobAction(final Consumer<LivingEntity> consumer) 
    {
        super(ActionType.MOB, consumer);
    }
    
}
