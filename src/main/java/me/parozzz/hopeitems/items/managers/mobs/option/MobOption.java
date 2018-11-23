/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.option;

import java.util.function.Consumer;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public class MobOption 
{
    private final Consumer<LivingEntity> consumer;
    public MobOption(final Consumer<LivingEntity> consumer)
    {
        this.consumer = consumer;
    }
    
    public void applyOption(final LivingEntity liv)
    {
        consumer.accept(liv);
    }
}
