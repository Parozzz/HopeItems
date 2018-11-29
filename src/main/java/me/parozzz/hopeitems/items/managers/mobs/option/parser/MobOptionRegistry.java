/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.option.parser;

import java.util.function.Consumer;
import java.util.function.Function;
import me.parozzz.hopeitems.items.managers.AbstractRegistry;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public class MobOptionRegistry extends AbstractRegistry<MobOptionSpecificParser>
{
    public void addRegistered(final String key, final Function<String, Consumer<LivingEntity>> function)
    {
        super.addRegistered(key, new MobOptionSpecificParser(function));
    }
}
