/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.abilities.parser;

import java.util.function.BiConsumer;
import java.util.function.Function;
import me.parozzz.hopeitems.items.managers.AbstractRegistry;
import me.parozzz.reflex.configuration.MapArray;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public class MobAbilityRegistry extends AbstractRegistry<MobAbilitySpecificParser>
{
    public void addRegistered(final String key, final Function<MapArray, BiConsumer<LivingEntity, LivingEntity>> function)
    {
        super.addRegistered(key, new MobAbilitySpecificParser(function));
    }
}
