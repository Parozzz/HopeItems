/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.manager;

import it.parozzz.hopeitems.Enum.When;
import it.parozzz.hopeitems.core.ItemBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class ItemManager 
{
    private final String name;
    public ItemManager(final String name)
    {
        this.name=name;
    }
    
    public String getName() { return name; }
    
    private ItemStack item;
    public ItemManager setItem(final ItemStack item)
    {
        this.item=item;
        return this;
    }
    public ItemStack getItem() { return item; }
    
    private final Set<String> command=new HashSet<>();
    public ItemManager addCommand(final String cmd)
    {
        command.add(cmd);
        return this;
    }
    
    public ItemManager addCommand(final Collection<String> cmd)
    {
        command.addAll(cmd);
        return this;
    }
    public Set<String> getCommands() { return command; }
    
    private boolean remove;
    public ItemManager setRemoveOnUse(final boolean remove) 
    {
        this.remove=remove;
        return this;
    }
    public boolean getRemoveOnUse() { return remove; }
    public void itemRemoval(final ItemStack item, final Player p) 
    { 
        if(remove) { p.getInventory().removeItem(new ItemBuilder(item.clone()).setAmount(1).build()); }
    }
    
    private final Set<When> when=new HashSet<>();
    public ItemManager setWhen(final When when) 
    { 
        this.when.add(when);
        return this;
    }
    public ItemManager setWhen(final Collection<When> when)
    {
        this.when.addAll(when);
        return this;
    }
    public Boolean canHappen(final When when) { return this.when.contains(when); }
    public Set<When> getWhen() { return when; }
    
    private EffectManager effect;
    public ItemManager setEffectManager(final EffectManager effect)
    {
        this.effect=effect;
        return this;
    }
    
    private PlayerManager player;
    public ItemManager setPlayerManager(final PlayerManager player)
    {
        this.player=player;
        return this;
    }
    
    private ConditionManager condition;
    public ItemManager setConditionManager(final ConditionManager condition)
    {
        this.condition=condition;
        return this;
    }
    
    private MobManager mob;
    public ItemManager setMobManager(final MobManager mob)
    {
        this.mob=mob;
        return this;
    }
    
    private LuckyManager lucky;
    public ItemManager setLuckyManager(final LuckyManager lucky)
    {
        this.lucky=lucky;
        return this;
    }
    
    private ExplosiveManager explosive;
    public ItemManager setCreeperManager(final ExplosiveManager explosive)
    {
        this.explosive=explosive;
        return this;
    }
    
    public void execute(final Location l, final boolean spawnMob, final boolean luckyRoll)
    { 
        execute(l, null, spawnMob, luckyRoll); 
    }
    
    public void execute(final Location l,final Player p,final boolean spawnMob, final boolean luckyRoll)
    {
        command.stream().map(cmd -> cmd.replace("%player%", p!=null?p.getName():"")).forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        effect.execute(l);
        
        if(spawnMob)
        {
            explosive.spawn(l);
            mob.summon(l);
        } 
        
        if(p!=null) 
        { 
            player.execute(p,l); 
            if(!luckyRoll)
            {
                lucky.execute(p);
            }
        }
    }
    
    public boolean check(final Player p) { return condition.check(p); }
    public boolean check(final Player p, PlayerInteractEvent e) { return condition.check(p,e); }
}
