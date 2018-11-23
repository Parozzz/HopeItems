/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopeitems.items.managers.conditions.parsers.ConditionManager;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.mobs.parsers.MobManager;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.Location;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import me.parozzz.hopeitems.items.managers.actions.parsers.ActionManager;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Paros
 */
public class ItemInfo 
{
    private static final Logger logger = Logger.getLogger(ItemInfo.class.getSimpleName());
    
    public enum ProjectileDamageType
    {
        SHOOTER, DAMAGED, MUTUAL, BOTH;
    }
    
    public enum When
    {
        LEFTINTERACT, RIGHTINTERACT, MINE,
        CONSUME, SPLASH, LINGERING, DISPENSE, PROJECTILE, PROJECTILE_DAMAGE,
        ARMOREQUIP, ARMORUNEQUIP,
        ATTACKSELF, ATTACKOTHER,
        DROP, DROPONGROUND,
        BLOCKINTERACT, BLOCKSTEP, BLOCKDESTROY, 
        NONE;
    }
    
    public boolean removeOnUse = false;
    
    private final ItemCollection collection;
    
    private final Set<When> whens;
    
    private ConditionManager conditionManager;
    private ActionManager actionManager;
    
    public ItemInfo(final ItemCollection collection, final Set<When> whens)
    {
        this.collection = collection;
        this.whens = EnumSet.copyOf(whens);
    }
    
    public Set<When> getWhens()
    {
        return whens;
    }
    
    public ItemCollection getCollection()
    {
        return collection;
    }
    
    public void setConditionManager(final ConditionManager cm)
    {
        this.conditionManager = cm;
    }
    
    public void setActionManager(final ActionManager am)
    {
        this.actionManager = am;
    }
    
    private double chance = -1D;
    public void setChance(final double chance)
    {
        this.chance = chance;
    }
    
    private boolean calculateChance()
    {
        return chance == -1 ? true : ThreadLocalRandom.current().nextDouble(100D) < chance;
    }
    
    private MobManager mobManager;
    public void setMobManager(final MobManager mm)
    {
        mobManager = mm;
        mm.setItemInfo(this);
    }
    
    public boolean hasMob()
    {
        return mobManager!=null;
    }
    
    public MobManager getMobManager()
    {
        return mobManager;
    }
    
    private ExplosiveManager explosiveManager;
    public void setExplosiveManager(final ExplosiveManager em)
    {
        explosiveManager=em;
    }
    
    private LuckyManager luckyManager;
    public void setLuckyManager(final LuckyManager lm)
    {
        luckyManager=lm;
    }
    
    private ProjectileDamageType projectileDamageType = ProjectileDamageType.MUTUAL;
    public void setProjectileDamageType(final ProjectileDamageType type)
    {
        this.projectileDamageType = type;
    }
    
    public ProjectileDamageType getProjectileDamageType()
    {
        return projectileDamageType;
    }
    
    public boolean executeWithItem(final Location l, final Player p, final ItemStack item)
    {
        if(execute(l, p, true) && removeOnUse)
        {
            ItemUtil.decreaseItemStack(item, p.getInventory());
            p.updateInventory();
            return true;
        }
        
        return false;
    }
    
    public boolean execute(final Location l, final Dispenser d)
    {
        if(calculateChance() && checkConditions(l, null))
        {
            spawnMobs(l, d.getBlockProjectileSource());
            executeActions(l, null);
            return true;
        }
        return false;
    }
    
    public boolean execute(final Location l, final LivingEntity liv)
    {
        if(calculateChance() && checkConditions(l, null))
        {
            spawnMobs(l, liv);
            executeActions(l, liv);
            return true;
        }
        return false;
    }
    
    public boolean execute(final Location l, final Player p, final boolean conditions)
    {
        if(!conditions || (calculateChance() && (checkConditions(l, p) && !collection.hasCooldown(p))))
        {
            spawnMobs(l, p);
            executeActions(l, p);
            if(p != null)
            {
                executeLucky(p);
            }
            
            return true;
        }
        return false;
    }
    
    public boolean checkConditions(final Location l, final Player p)
    {
        return Optional.ofNullable(conditionManager).map(localManager -> localManager.testConditions(l, p)).orElse(true);
    }
    
    public void executeActions(final Location l, final Player p)
    {
        Optional.ofNullable(actionManager).ifPresent(localManager -> localManager.executeAll(p, p, l));
    }
    
    public void executeActions(final Location l, final LivingEntity ent)
    {
        Optional.ofNullable(actionManager).ifPresent(localManager -> localManager.executeAll(null, ent, l));
    }
    
    public void executeLucky(final Player p)
    {
        Optional.ofNullable(luckyManager).ifPresent(lm -> lm.roll(p));
    }
    
    public void spawnMobs(final Location l, final ProjectileSource ps)
    {
        Optional.ofNullable(mobManager).ifPresent(mm -> mm.spawnMob(l));
        Optional.ofNullable(explosiveManager).ifPresent(em -> em.spawn(l, ps));
    }
}

/*
return conditions.stream().allMatch(condition -> 
{
    if(condition.getType().isPlayerRelated() && p==null)
    {
        return true;
    }

    switch(condition.getType())
    {
        case PLAYER:
            return ((ConditionManager<Player>)condition).testCondition(p, p);
        case LOCATION:
            ConditionManager<Location> location = (ConditionManager<Location>)condition;
            return Optional.ofNullable(p).map(temp -> location.testCondition(l, temp)).orElseGet(() -> location.testCondition(l));
        default:
            return false;
    }
});*/

/*
actions.forEach(action -> 
{
    if(action.getType().isPlayerRelated() && p == null)
    {
        return;
    }

    switch(action.getType())
    {
        case PLAYER:
            ((SingleActionManager<Player>)action).executeAction(p);
            break;
        case PLAYEREFFECT:
            ((DoubleActionManager<Player, Location>)action).executeAction(p, l);
            break;
        case WORLDEFFECT:
            ((SingleActionManager<Location>)action).executeAction(l);
            break;
    }
});*/
        