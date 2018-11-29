/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.abilities.parser;

import me.parozzz.hopeitems.items.managers.mobs.abilities.MobAbility;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import me.parozzz.hopeitems.items.managers.ISpecificParser;
import me.parozzz.reflex.configuration.MapArray;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public class MobAbilitySpecificParser implements ISpecificParser<MapArray, MobAbility>
{
    private static final Logger logger = Logger.getLogger(MobAbilitySpecificParser.class.getSimpleName());
    
    private final Function<MapArray, BiConsumer<LivingEntity, LivingEntity>> function;
    public MobAbilitySpecificParser(final Function<MapArray, BiConsumer<LivingEntity, LivingEntity>> function)
    {
        this.function = function;
    }
            
            
    @Override
    public @Nullable MobAbility parse(final MapArray map) 
    {
        if(!map.hasKey("chance"))
        {
            logger.log(Level.WARNING, "A mob ability chance has not been set");
            return null;
        }
        
        double chance = map.getValue("chance", Double::valueOf);
        
        MobAbility mobAbility = new MobAbility(chance, function.apply(map));
        return mobAbility;
    }
    
}
