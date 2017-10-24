/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.conditions;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import me.parozzz.hopeitems.Dependency;
import me.parozzz.hopeitems.items.managers.ManagerUtils;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.classes.ComplexMapList;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public enum ConditionType 
{
    PLAYER(true){
        private final boolean tryE=true;
        @Override
        public ConditionManager getConditionManager(final ComplexMapList list)
        {
            ConditionManager<Player> player=new ConditionManager<>(this);
            list.getMapArrays().forEach((key, map) -> 
            {
                String message = map.getValue("message", Utils::color);
                String value = map.getValue("value", Function.identity());
                
                switch(key)
                {
                    case "permission":
                        player.addCondition(p -> p.hasPermission(value), message);
                        break;
                    case "food":
                        Optional.of(ManagerUtils.getComparison(value, Integer::valueOf))
                                .ifPresent(pred -> player.addCondition(p -> pred.test(p.getFoodLevel()), message));
                        break;
                    case "exp":
                        Optional.of(ManagerUtils.getComparison(value, Integer::valueOf))
                                .ifPresent(pred -> player.addCondition(p -> pred.test(p.getTotalExperience()), message));
                        break;
                    case "level":
                        Optional.of(ManagerUtils.getComparison(value, Integer::valueOf))
                                .ifPresent(pred -> player.addCondition(p -> pred.test(p.getLevel()), message));
                        break;
                    case "sneaking":
                        Optional.ofNullable(ManagerUtils.getComparison(value, Boolean::valueOf))
                                .ifPresent(pred -> player.addCondition(p -> pred.test(p.isSneaking()), message));
                        break;
                    case "gamemode":
                        Optional.ofNullable(ManagerUtils.getComparison(value.toUpperCase(), GameMode::valueOf))
                                .ifPresent(pred -> player.addCondition(p -> pred.test(p.getGameMode()), message));
                        break;
                    case "money":
                        if(Dependency.isEconomyHooked())
                        {
                            Optional.ofNullable(ManagerUtils.getComparison(value, Double::valueOf))
                                    .ifPresent(pred -> player.addCondition(p -> pred.test(Dependency.eco.getBalance(p)), message));
                        }
                        break;
                    default:
                        break;
                }
            });
            return player;
        }
    },
    LOCATION(false){
        @Override
        public ConditionManager getConditionManager(final ComplexMapList list)
        {
            ConditionManager<Location> location=new ConditionManager<>(this);
            list.getMapArrays().forEach((key, map) -> 
            {
                String message = map.getValue("message", Utils::color);
                String value = map.getValue("value", Function.identity());
                
                switch(key)
                {
                    case "world":
                        location.addCondition(l -> l.getWorld().getName().equals(value), message);
                        break;
                    case "ylevel":
                        Optional.of(ManagerUtils.getComparison(value, Integer::valueOf))
                                .ifPresent(pred -> location.addCondition(l -> pred.test(l.getBlockY()), message));
                        break;
                    case "worldguard":
                        if(Dependency.isWorldGuardHooked())
                        {
                            location.addCondition(l -> Dependency.wg.getRegionManager(l.getWorld()).getApplicableRegions(l).getRegions().stream().map(ProtectedRegion::getId).anyMatch(value::equals), message);
                        }
                        break;
                    default:
                        break;
                }
            });
            return location;
        }
    };
    
    private final boolean playerRelated;
    private ConditionType(final boolean playerRelated)
    {
        this.playerRelated=playerRelated;
    }
    
    public boolean isPlayerRelated()
    {
        return playerRelated;
    }
    
    public ConditionManager getConditionManager(final ComplexMapList list)
    {
        throw new UnsupportedOperationException();
    }
}
