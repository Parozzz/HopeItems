/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.option.parser;

import java.util.function.Consumer;
import java.util.function.Function;
import me.parozzz.hopeitems.items.managers.ISpecificParser;
import me.parozzz.hopeitems.items.managers.mobs.option.MobOption;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public class MobOptionSpecificParser implements ISpecificParser<String, MobOption>
{
    private final Function<String, Consumer<LivingEntity>> function;
    public MobOptionSpecificParser(final Function<String, Consumer<LivingEntity>> function)
    {
        this.function = function;
    }
    
    @Override
    public MobOption parse(final String p) 
    {
        return new MobOption(function.apply(p));
    }
    
}
