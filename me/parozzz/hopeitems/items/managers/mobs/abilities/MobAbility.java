/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.abilities;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public final class MobAbility 
{
    private final double chance;
    private final BiConsumer<LivingEntity, LivingEntity> consumer;
    public MobAbility(final double chance, final BiConsumer<LivingEntity, LivingEntity> consumer)
    {
        this.consumer = consumer;
        this.chance = chance;
    }
    
    public double getChance()
    {
        return chance;
    }
    
    public boolean calculateChance()
    {
        return ThreadLocalRandom.current().nextDouble(100) < chance;
    }
    
    public void execute(final LivingEntity customMob, final LivingEntity involved)
    {
        consumer.accept(customMob, involved);
    }
}
