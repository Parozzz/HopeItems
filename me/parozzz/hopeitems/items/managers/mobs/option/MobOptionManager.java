/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.option;

import java.util.LinkedList;
import java.util.List;
import me.parozzz.hopeitems.items.managers.IManager;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public class MobOptionManager implements IManager
{
    private final List<MobOption> options;
    public MobOptionManager()
    {
        options = new LinkedList<>();
    }
    
    public void addMobOption(final MobOption option)
    {
        this.options.add(option);
    }
    
    public void applyAll(final LivingEntity liv)
    {
        options.forEach(option -> option.applyOption(liv));
    }
}
