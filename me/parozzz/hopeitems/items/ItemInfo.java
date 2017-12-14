/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import me.parozzz.hopeitems.items.managers.conditions.ConditionManager;
import me.parozzz.hopeitems.items.managers.actions.ActionManager;
import me.parozzz.hopeitems.items.managers.actions.DoubleActionManager;
import me.parozzz.hopeitems.items.managers.actions.SingleActionManager;
import me.parozzz.hopeitems.items.managers.cooldown.CooldownManager;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.mobs.MobManager;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.Location;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

/**
 *
 * @author Paros
 */
public class ItemInfo 
{
    public enum When
    {
        LEFTINTERACT, RIGHTINTERACT, 
        CONSUME, SPLASH, LINGERING, DISPENSE, PROJECTILE,
        ARMOREQUIP, ARMORUNEQUIP,
        ATTACKSELF, ATTACKOTHER,
        DROP, DROPONGROUND,
        BLOCKINTERACT, BLOCKSTEP, BLOCKDESTROY, 
        NONE;
    }
    
    public boolean removeOnUse=false;
    
    private final ItemCollection collection;
    
    private final Set<When> whens;
    private final Set<ConditionManager> conditions;
    private final Set<ActionManager> actions;
    
    public ItemInfo(final ItemCollection collection, final Set<When> whens)
    {
        this.collection = collection;
        
        this.whens = EnumSet.copyOf(whens);
        conditions = new HashSet<>();
        actions = new HashSet<>();
    }
    
    public Set<When> getWhens()
    {
        return whens;
    }
    
    public ItemCollection getCollection()
    {
        return collection;
    }
    
    public void addConditionManager(final ConditionManager cm)
    {
        conditions.add(cm);
    }
    
    public void addActionManager(final ActionManager am)
    {
        actions.add(am);
    }
    
    private MobManager mobManager;
    public void setMobManager(final MobManager mm)
    {
        mobManager=mm;
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
        if(checkConditions(l, null))
        {
            spawnMobs(l, d.getBlockProjectileSource());
            executeActions(l, null);
            return true;
        }
        return false;
    }

    
    public boolean execute(final Location l, final Player p, final boolean conditions)
    {
        if(!conditions || (checkConditions(l, p) && !collection.hasCooldown(p)))
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
        });
    }
    
    public void executeActions(final Location l, final Player p)
    {
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
        });
        
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
