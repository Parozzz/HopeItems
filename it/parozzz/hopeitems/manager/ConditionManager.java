/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.manager;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.parozzz.hopeitems.Enum.ConditionType;
import it.parozzz.hopeitems.HopeItems;
import it.parozzz.hopeitems.core.Dependency;
import it.parozzz.hopeitems.core.Language;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class ConditionManager {

    private final Set<Type> condition=new HashSet<>();
    private final Set<Interact> interact=new HashSet<>();
    
    private CheckCooldown cooldown;
    public ConditionManager parseStringList(final List<String> list)
    {
        list.forEach(str -> 
        {
            try { 
                ConditionType ct=ConditionType.getByStarting(str.substring(0, str.indexOf(":"))); 
                str=str.substring(str.indexOf(":")+1);
                switch(ct)
                {
                    case WORLD: condition.add(new CheckWorld(str)); break;
                    case PERMISSION:  condition.add(new CheckPermission(str)); break;
                    case COOLDOWN: cooldown=new CheckCooldown(str); break;
                    case WORLDGUARD: condition.add(new CheckWorldGuard(str)); break;
                    case LEVEL: condition.add(new CheckLevel(str)); break;
                    case ACTION: interact.add(new EventAction(str)); break;
                }
            }
            catch(IndexOutOfBoundsException ex)  {  throw new IllegalArgumentException(str+" is not valid!"); }
        });
        return this;
    }
    
    public boolean check(final Player p, final PlayerInteractEvent e)
    {
        if(!condition.stream().allMatch(t -> t.check(p)) || !interact.stream().allMatch(i -> i.checkEvent(e))) { return false; }
        return cooldown==null || cooldown.check(p); 
    }
    
    public boolean check(final Player p) 
    {
        if(!condition.stream().allMatch(t -> t.check(p))) { return false; }
        return cooldown==null || cooldown.check(p); 
    }
    
    private class CheckWorld implements Type
    {
        private final World world;
        public CheckWorld(final String str) 
        {
            if((world=Bukkit.getWorld(str))==null){ throw new NullPointerException("World "+str+" does not exist!"); } 
        }
        
        @Override
        public boolean check(Player p) 
        { 
            return p.getWorld().equals(world)?true:!Language.sendParsedMessage(p, "conditionWrongWorld","%world%",world.getName()); 
        }
    
        @Override
        public ConditionType getType() { return ConditionType.WORLD; }
    }
    
    private class CheckPermission implements Type
    {
        private final String permission;
        public CheckPermission(final String str) { permission=str; }
        
        @Override
        public boolean check(Player p) 
        { 
            return p.hasPermission(permission)?true:!Language.sendMessage(p, "noPermission"); 
        }
    
        @Override
        public ConditionType getType() { return ConditionType.WORLD; }
    }
    
    private class CheckCooldown implements Type
    {
        private final long cooldown;
        public CheckCooldown(final String str) { cooldown=Long.parseLong(str); }
        private final Map<UUID,Long> player=new HashMap<>();
        
        @Override
        public boolean check(Player p) 
        {
            Long time=player.get(p.getUniqueId());
            if(time==null || System.currentTimeMillis()/1000 - time >= cooldown)
            {
                player.put(p.getUniqueId(), System.currentTimeMillis()/1000);
                return true;
            }
            else
            {
                Language.sendParsedMessage(p, "conditionCooldown", "%time%", Long.toString(cooldown+time-System.currentTimeMillis()/1000));
                return false;
            }
        }

        @Override
        public ConditionType getType() { return ConditionType.COOLDOWN; }
        
    }
    
    private class CheckLevel implements Type
    {
        private final int level;
        public CheckLevel(final String str) { level=Integer.parseInt(str); }
        
        @Override
        public boolean check(Player p) 
        {
            return p.getLevel()>=level?true:!Language.sendParsedMessage(p, "conditionWrongLevel", "%level%", Integer.toString(level));
        }

        @Override
        public ConditionType getType() { return ConditionType.LEVEL; }
    }
    
    private class CheckWorldGuard implements Type
    {

        private final String region;
        public CheckWorldGuard(final String str)
        {
            if(!Dependency.isWorldGuardHooked()) { throw new UnknownDependencyException("WorldGuard needed for worldguard condition!"); }
            region=str;
        }
        
        @Override
        public boolean check(Player p) 
        {
            Set<ProtectedRegion> set=Dependency.getWorldGuard().getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation()).getRegions();
            if((set.isEmpty() && region.equalsIgnoreCase("global")) || set.stream().anyMatch(region -> region.getId().equalsIgnoreCase(this.region))) { return true; }
            return !Language.sendParsedMessage(p, "conditionWrongRegion","%region%",region);
        }

        @Override
        public ConditionType getType() { return ConditionType.WORLDGUARD; }
    }
    
    interface Type
    {
        boolean check(final Player p);
        ConditionType getType();
    }
    
    private class EventAction implements Interact
    {
        private final Action action;
        public EventAction(final String str)
        {
            action=Action.valueOf(str.toUpperCase());
        }
        
        @Override
        public boolean checkEvent(final PlayerInteractEvent e) 
        {
            return e.getAction()==action;
        }

        @Override
        public ConditionType getType() { return ConditionType.ACTION; }
    }
    
    interface Interact
    {
        boolean checkEvent(final PlayerInteractEvent e);
        ConditionType getType();
    }
}
