/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import me.parozzz.hopeitems.items.managers.conditions.ConditionManager;
import me.parozzz.hopeitems.items.managers.conditions.ConditionType;
import me.parozzz.hopeitems.items.managers.actions.ActionManager;
import me.parozzz.hopeitems.items.managers.actions.ActionType;
import me.parozzz.hopeitems.items.managers.actions.DoubleActionManager;
import me.parozzz.hopeitems.items.managers.actions.SingleActionManager;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.mobs.MobManager;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.placeholders.ItemPlaceholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class ItemInfo 
{
    public enum When
    {
        INTERACT, CONSUME, SPLASH, LINGERING, DISPENSE, ARROW, 
        ATTACKSELF, ATTACKOTHER,
        DROP, DROPONGROUND,
        BLOCKINTERACT, BLOCKSTEP, BLOCKDESTROY;
    }
    
    public boolean removeOnUse=false;
    
    private final String name;
    private final ItemPlaceholder item;
    
    private final Set<When> whens;
    private final Map<ConditionType, ConditionManager> conditions;
    private final Map<ActionType, ActionManager> actions;
    public ItemInfo(final String name, final ItemStack item)
    {
        this.name=name;
        this.item=new ItemPlaceholder(item);
        
        whens=EnumSet.noneOf(When.class);
        conditions=new EnumMap(ConditionType.class);
        actions=new EnumMap(ActionType.class);
    }
    
    public String getName()
    {
        return name;
    }
    
    private Predicate<UUID> hasCooldown= u -> false;
    public void setCooldown(final long cooldown)
    {
        Map<UUID, Long> cooldowns=new HashMap<>();
        hasCooldown = u -> 
                Optional.ofNullable(cooldowns.get(u)).map(l -> 
                {
                    if(System.currentTimeMillis()-l>cooldown)
                    {
                        cooldowns.replace(u, l, System.currentTimeMillis());
                        return false;
                    }

                    return true;
                }).orElseGet(() -> 
                {
                    cooldowns.put(u, System.currentTimeMillis());
                    return false;
                });
    }
    
    public boolean hasCooldown(final UUID u)
    {
        return hasCooldown.test(u);
    }
    
    public ItemPlaceholder getItem()
    {
        return item;
    }
    
    public void addConditionManager(final ConditionManager cm)
    {
        conditions.put(cm.getType(), cm);
    }
    
    public void addActionManager(final ActionManager am)
    {
        actions.put(am.getType(), am);
    }
    
    public void addWhen(final When w)
    {
        whens.add(w);
    }
    
    public boolean hasWhen(final When w)
    {
        return whens.contains(w);
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
    
    public boolean hasExplosive()
    {
        return explosiveManager!=null;
    }
    
    public ExplosiveManager getExplosiveManager()
    {
        return explosiveManager;
    }
    
    private LuckyManager luckyManager;
    public void setLuckyManager(final LuckyManager lm)
    {
        luckyManager=lm;
    }
    
    public boolean hasLucky()
    {
        return luckyManager!=null;
    }
    
    public LuckyManager getLuckyManager()
    {
        return luckyManager;
    }
    
    public Map<ConditionType, ConditionManager> getConditionMap()
    {
        return conditions;
    }
    
    public Map<ActionType, ActionManager> getActionMap()
    {
        return actions;
    }
    
    public void executeAll(final Location l, final Player p, final ItemStack item)
    {
        if(checkConditions(l, p) && !hasCooldown(p.getUniqueId()))
        {
            executeActionsAndSpawn(l, p);
            executeLucky(p);
            
            if(removeOnUse)
            {
                Utils.decreaseItemStack(item, p.getInventory());
            }
        }
    }
    
    public void executeActionsAndSpawn(final Location l, final Player p)
    {
        executeActions(l, p);
        spawnMobs(l);
    }
    
    public boolean checkConditions(final Location l, final Player p)
    {
        return this.getConditionMap().entrySet().stream().allMatch(e -> 
        {
            if(e.getKey().isPlayerRelated() && p==null)
            {
                return true;
            }
            
            switch(e.getKey())
            {
                case PLAYER:
                    ConditionManager<Player> player=(ConditionManager<Player>)e.getValue();
                    return player.testCondition(p, p);
                case LOCATION:
                    ConditionManager<Location> location=(ConditionManager<Location>)e.getValue();
                    return Optional.ofNullable(p).map(temp -> location.testCondition(l, temp)).orElseGet(() -> location.testCondition(l));
                default:
                    return false;
            }
        });
    }
    
    public void executeActions(final Location l, final Player p)
    {
        this.getActionMap().forEach((at , am) -> 
        {
            if(at.isPlayerRelated() && p==null)
            {
                return;
            }
            
            switch(at)
            {
                case PLAYER:
                    SingleActionManager<Player> player=(SingleActionManager<Player>)am;
                    player.executeAction(p);
                    break;
                case PLAYEREFFECT:
                    DoubleActionManager<Player, Location> playerEffect=(DoubleActionManager<Player, Location>)am;
                    playerEffect.executeAction(p, l);
                    break;
                case WORLDEFFECT:
                    SingleActionManager<Location> world=(SingleActionManager<Location>)am;
                    world.executeAction(l);
                    break;
            }
        });
        
    }
    
    public void executeLucky(final Player p)
    {
        if(hasLucky())
        {
            getLuckyManager().roll(p);
        }
    }
    
    public void spawnMobs(final Location l)
    {
        if(hasMob())
        {
            getMobManager().spawnMob(l);
        }

        if(hasExplosive())
        {
            getExplosiveManager().spawn(l);
        }
    }
}
