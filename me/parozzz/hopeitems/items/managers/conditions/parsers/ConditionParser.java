/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.conditions.parsers;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import me.parozzz.hopeitems.hooks.Dependency;
import me.parozzz.hopeitems.items.managers.IParser;
import me.parozzz.hopeitems.items.managers.ISpecificParser;
import me.parozzz.hopeitems.items.managers.ManagerUtils;
import me.parozzz.hopeitems.items.managers.conditions.AbstractCondition;
import me.parozzz.hopeitems.items.managers.conditions.ConditionType;
import me.parozzz.reflex.configuration.ComplexMapList;
import me.parozzz.reflex.configuration.MapArray;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public class ConditionParser implements IParser
{
    private static final Logger logger = Logger.getLogger(ConditionParser.class.getSimpleName());
    
    private final Map<ConditionType, ConditionRegistry> specificParserMap;
    public ConditionParser()
    {
        specificParserMap = new EnumMap(ConditionType.class);
    }

    @Override
    public void registerDefaultSpecificParsers() 
    {
        Stream.of(ConditionType.values()).forEach(ct -> 
        {
            switch(ct)
            {
                case LOCATION:
                    LocationRegistry locationRegistry = new LocationRegistry();
                    this.specificParserMap.put(ct, locationRegistry);
                    
                    locationRegistry.addRegistred("world", value -> loc -> loc.getWorld().getName().equalsIgnoreCase(value));
                    locationRegistry.addRegistred("ylevel", value -> loc -> ManagerUtils.getComparison(value, Integer::valueOf).test(loc.getBlockY()));
                    if(Dependency.isWorldGuardHooked())
                    {
                        locationRegistry.addRegistred("worldguard", value -> 
                                loc -> Dependency.worldGuard.getRegionManager(loc.getWorld()).getApplicableRegions(loc).getRegions().stream()
                                            .map(ProtectedRegion::getId)
                                            .anyMatch(value::equalsIgnoreCase));
                    }
                    break;
                case PLAYER:
                    PlayerRegistry playerRegistry = new PlayerRegistry();
                    this.specificParserMap.put(ct, playerRegistry);
                    
                    playerRegistry.addRegistred("permission", value -> p -> p.hasPermission(value));
                    playerRegistry.addRegistred("food", value -> this.getPredicate(value, Player::getFoodLevel, Integer::valueOf));
                    playerRegistry.addRegistred("exp", value -> this.getPredicate(value, Player::getTotalExperience, Integer::valueOf));
                    playerRegistry.addRegistred("level", value -> this.getPredicate(value, Player::getLevel, Integer::valueOf));
                    playerRegistry.addRegistred("sneaking", value -> this.getPredicate(value, Player::isSneaking, Boolean::valueOf));
                    playerRegistry.addRegistred("sprinting", value -> this.getPredicate(value, Player::isSprinting, Boolean::valueOf));
                    playerRegistry.addRegistred("gamemode", value -> this.getPredicate(value.toUpperCase(), Player::getGameMode, GameMode::valueOf));
                    playerRegistry.addRegistred("haspotion", value -> 
                    {
                        PotionEffectType pet = PotionEffectType.getByName(value.toUpperCase());
                        if(pet == null)
                        {
                            logger.log(Level.WARNING, "Error parser haspotion condition. A potion effect type named {0} does not exist. Skipping.");
                            return p -> true;
                        }
                        return p -> p.hasPotionEffect(pet);
                    });
                    if(Dependency.isEconomyHooked())
                    {
                        playerRegistry.addRegistred("money", value -> this.getPredicate(value, Dependency.economy::getBalance, Double::valueOf));
                    }
                    break;
            }
        });
    }
    
    private <A extends Comparable, B> Predicate<B> getPredicate(final String value, final Function<B, A> function, final Function<String, A> convertingFunction)
    {
        try {
            Predicate<A> localPredicate = ManagerUtils.getComparison(value, convertingFunction);
            return b -> localPredicate.test(function.apply(b));
        } catch(final Exception ex) {
            logger.log(Level.WARNING, "An error occoured.", ex);
            return b -> true; //Return an always try predicate in case of error to avoid an npe and the condition manager to still work.
        }
        
    }

    @Override
    public @Nullable ConditionManager parse(final ConfigurationSection path) 
    {
        ConditionManager manager = new ConditionManager();
        for(String key : path.getKeys(false))
        {
            ConditionType type;
            try {
                type = ConditionType.valueOf(key.toUpperCase());
            } catch(final IllegalArgumentException ex) {
                logger.log(Level.WARNING, "The condition type {0} does not exist. Skipping type.", key);
                continue;
            }
            
            if(!path.isList(key))
            {
                logger.log(Level.WARNING, "Wrong format of condition {0}. Skipping type.", key);
                continue;
            }
            
            ConditionRegistry registry = specificParserMap.get(type);
            for(Map.Entry<String, List<MapArray>> entry : new ComplexMapList(path.getMapList(key)).getView().entrySet())
            {
                String specificKey = entry.getKey();
                List<MapArray> localList = entry.getValue();
                
                SpecificConditionParser parser = (SpecificConditionParser)registry.getRegistered(specificKey);
                if(parser == null)
                {
                    logger.log(Level.WARNING, "The condition subType {0} does not exist. Skipping.", specificKey);
                }
                else if(!localList.isEmpty())
                {
                    AbstractCondition condition = parser.parse(localList.get(0));
                    if(condition == null)
                    {
                        logger.log(Level.WARNING, "There has been a problem loading the Condition subType {0}", specificKey);
                        continue;
                    }
                    manager.addCondition(condition);
                }
            }
        }
        
        return manager;
    }

    
    /*
    @Override
    public void addSpecificParser(final ConditionType e, final String key, final ISpecificParser<?, AbstractCondition> specificParser) 
    {
        specificParserMap.get(e).addRegistered(key, specificParser);
    }*/
    
    
    private class LocationRegistry extends ConditionRegistry<Location>
    {
        
        public LocationRegistry() 
        {
            super(ConditionType.LOCATION);
        }
        
    }
    
    private class PlayerRegistry extends ConditionRegistry<Player>
    {
        
        public PlayerRegistry() 
        {
            super(ConditionType.PLAYER);
        }
        
    }
}
