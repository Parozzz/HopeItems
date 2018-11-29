/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.abilities.parser;

import java.util.LinkedList;
import java.util.List;
import me.parozzz.hopeitems.items.managers.mobs.abilities.parser.MobAbilityParser;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import me.parozzz.hopeitems.items.managers.IManager;
import me.parozzz.hopeitems.items.managers.mobs.parsers.MobManager;
import me.parozzz.hopeitems.items.managers.mobs.abilities.MobAbility;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.configuration.ComplexMapList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public class AbilityManager implements IManager
{
    private final List<MobAbility> directAbilities;
    private final List<MobAbility> passiveAbilities;
    
    private final int chance;
    private final boolean resistArrow;
    public AbilityManager(final int chance, final boolean resistArrow)
    {
        this.chance = chance;
        this.resistArrow = resistArrow;
        directAbilities = new LinkedList<>();
        passiveAbilities = new LinkedList<>();
    }
    
    protected void addDirectAbility(final MobAbility ability)
    {
        directAbilities.add(ability);
    }
    
    protected void addPassiveAbility(final MobAbility ability)
    {
        passiveAbilities.add(ability);
    }
    
    public boolean resistArrows()
    {
        return resistArrow;
    }
    
    public boolean triggerDirectAbility(final LivingEntity mob, final LivingEntity damaged)
    {
        if(ThreadLocalRandom.current().nextInt(101) < chance)
        {
            directAbilities.stream().filter(MobAbility::calculateChance).forEach(ability -> ability.execute(mob, damaged));
            return true;
        }
        return false;
    }
    
    public boolean triggerPassiveAbility(final LivingEntity mob, final LivingEntity damager)
    {
        if(ThreadLocalRandom.current().nextInt(101) < chance)
        {
            passiveAbilities.stream().filter(MobAbility::calculateChance).forEach(ability -> ability.execute(mob, damager));
            return true;
        }
        return false;
    }
}
