/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.abilities;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import me.parozzz.hopeitems.items.managers.mobs.MobManager;
import me.parozzz.hopeitems.utilities.Debug;
import me.parozzz.hopeitems.utilities.classes.ComplexMapList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public class AbilityManager 
{
    private final int chance;
    private final MobManager mobManager;
    
    private BiConsumer<LivingEntity, LivingEntity> direct= (mob, damaged) -> {};
    private BiConsumer<LivingEntity, LivingEntity> passive= (mob, damaged) -> {};
    private final boolean resistArrow;
    public AbilityManager(final ConfigurationSection path, final MobManager mobManager)
    {
        this.mobManager=mobManager;
        
        chance=path.getInt("chance", 100);
        resistArrow=path.getBoolean("resistArrow", false);
        
        new ComplexMapList(path.getMapList("direct")).getMapArrays()
                .forEach((type, map) -> direct = direct.andThen(Debug.validateEnum(type, AbilityType.class).getConsumer(map)));
        
        new ComplexMapList(path.getMapList("passive")).getMapArrays()
                .forEach((type, map) -> passive = passive.andThen(Debug.validateEnum(type, AbilityType.class).getConsumer(map)));
    }
    
    public MobManager getMobManager()
    {
        return mobManager;
    }
    
    public boolean resistArrows()
    {
        return resistArrow;
    }
    
    public boolean triggerDirectAbility(final LivingEntity mob, final LivingEntity damaged)
    {
        if(ThreadLocalRandom.current().nextInt(101)<chance)
        {
            direct.accept(mob, damaged);
            return true;
        }
        return false;
    }
    
    public boolean triggerPassiveAbility(final LivingEntity mob, final LivingEntity damager)
    {
        if(ThreadLocalRandom.current().nextInt(101)<chance)
        {
            passive.accept(mob, damager);
            return true;
        }
        return false;
    }
}
