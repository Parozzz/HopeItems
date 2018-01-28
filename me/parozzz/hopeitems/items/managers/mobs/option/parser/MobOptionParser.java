/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.option.parser;

import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopeitems.items.managers.IManager;
import me.parozzz.hopeitems.items.managers.IParser;
import me.parozzz.hopeitems.items.managers.ManagerUtils;
import me.parozzz.hopeitems.items.managers.mobs.option.MobOption;
import me.parozzz.hopeitems.items.managers.mobs.option.MobOptionManager;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.configuration.MapArray;
import me.parozzz.reflex.configuration.SimpleMapList;
import me.parozzz.reflex.utilities.EntityUtil;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author Paros
 */
public class MobOptionParser
{
    private final static Logger logger = Logger.getLogger(MobOptionParser.class.getSimpleName());
    
    private final MobOptionRegistry registry;
    public MobOptionParser()
    {
        registry = new MobOptionRegistry();
    }
    
    public void registerDefaultSpecificParsers() 
    {
        registry.addRegistered("name", value -> 
        {
            String name = Util.cc(value);
            return liv -> 
            {
                liv.setCustomName(name);
                liv.setCustomNameVisible(true);
            };
        });
        
        registry.addRegistered("ride", value -> 
        {
            EntityType rideType;
            try {
                rideType = EntityType.valueOf(value.toUpperCase());
                if(!rideType.isAlive() || !rideType.isSpawnable())
                {
                    logger.log(Level.WARNING, "An entity named {0} is not valid or not spawnable. Skipping Mob Option.");
                    return Util.EMPTY_CONSUMER;
                }
            } catch(final IllegalArgumentException ex) {
                logger.log(Level.WARNING, "An entity named {0} does not exist in your current version. Skipping Mob Option.");
                return Util.EMPTY_CONSUMER;
            }
            
            return MCVersion.V1_10.isLower() 
                    ? liv -> liv.setPassenger(liv.getWorld().spawnEntity(liv.getLocation(), rideType)) 
                    : liv -> liv.addPassenger(liv.getWorld().spawnEntity(liv.getLocation(), rideType));
        });
        
        registry.addRegistered("potion", value -> 
        {
            PotionEffect pe = ManagerUtils.getPotionEffect(new MapArray(value));
            if(pe == null)
            {
                logger.log(Level.WARNING, "An error occoured while parser potion mobOption. Skipping.");
                return Util.EMPTY_CONSUMER;
            }
            return liv -> liv.addPotionEffect(pe, true);
        });
        
        registry.addRegistered("health", value -> ManagerUtils.getNumberFunction(value, Double::valueOf, EntityUtil::setMaxHealth));
        registry.addRegistered("attribute", value -> 
        {
            if(MCVersion.V1_8.isEqual())
            {
                logger.log(Level.WARNING, "Mob attributes are not supported in MC1.8. Skipping Mob Option");
                return Util.EMPTY_CONSUMER;
            }
            
            MapArray map = new MapArray(value);
            
            Attribute attr;
            try {
                attr = Attribute.valueOf("GENERIC_" + map.getUpperValue("type", Function.identity()));
            } catch(final IllegalArgumentException ex) {
                logger.log(Level.WARNING, "A mob attribute named {0} does not exist. Skipping Mob Option.", map.getValue("type"));
                return Util.EMPTY_CONSUMER;
            }
            
            String amount = map.getUpperValue("value", Function.identity());
            return ManagerUtils.getNumberFunction(amount, Double::valueOf, (liv, d) -> liv.getAttribute(attr).setBaseValue(d));
        });
    }

    public MobOptionManager parse(final SimpleMapList mapList) 
    {
        MobOptionManager mobOptionManager = new MobOptionManager();
        mapList.forEach((key, list) -> 
        {
            MobOptionSpecificParser parser = registry.getRegistered(key);
            if(parser == null)
            {
                logger.log(Level.WARNING, "A mob option named {0} does not exist. Skipping.", key);
                return;
            }
            
            list.forEach(value -> 
            {
                MobOption option = parser.parse(value);
                if(option == null)
                {
                    logger.log(Level.WARNING, "An error occoured parsing a mob option named {0}. Skipping.", key);
                    return;
                }
                mobOptionManager.addMobOption(option);
            });
        });
        return mobOptionManager;
    }
    
}