/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.parsers;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import me.parozzz.hopeitems.items.managers.IParser;
import me.parozzz.hopeitems.items.managers.mobs.MobEquipmentPart;
import me.parozzz.hopeitems.items.managers.mobs.abilities.parser.AbilityManager;
import me.parozzz.hopeitems.items.managers.mobs.abilities.parser.MobAbilityParser;
import me.parozzz.hopeitems.items.managers.mobs.drops.DropManager;
import me.parozzz.hopeitems.items.managers.mobs.option.MobOptionManager;
import me.parozzz.hopeitems.items.managers.mobs.option.parser.MobOptionParser;
import me.parozzz.reflex.configuration.SimpleMapList;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class MobParser implements IParser
{
    private final static Logger logger = Logger.getLogger(MobParser.class.getSimpleName());
    
    private final MobAbilityParser abilityParser;
    private final MobOptionParser mobOptionParser;
    public MobParser()
    {
        abilityParser = new MobAbilityParser();
        mobOptionParser = new MobOptionParser();
    }
    
    @Override
    public void registerDefaultSpecificParsers() 
    {
        abilityParser.registerDefaultSpecificParsers();
        mobOptionParser.registerDefaultSpecificParsers();
    }

    @Override
    public @Nullable MobManager parse(ConfigurationSection path) 
    {
        EntityType et;
        try {
            et = EntityType.valueOf(path.getString("type").toUpperCase());
            if(!et.isAlive() || !et.isSpawnable())
            {
                logger.log(Level.WARNING, "The entity named {0} is not a mob or is not spawnable. Skipping Mob Manager.");
                return null;
            }
        } catch(final IllegalArgumentException ex) {
            logger.log(Level.WARNING, "En entity named {0} does not exist. Skipping Mob Manager.");
            return null;
        }
        
        MobManager mobManager = new MobManager(et);
        
        if(path.contains("option"))
        {
            if(!path.isList("option"))
            {
                logger.log(Level.WARNING, "Wrong formatting of a Mob Option List. Skipping.");
            }
            else
            {
                MobOptionManager optionManager = mobOptionParser.parse(new SimpleMapList(path.getMapList("option")));
                mobManager.setOptionManager(optionManager);
            }
        }

        
        if(path.contains("Armor"))
        {
            if(!path.isConfigurationSection("Armor"))
            {
                logger.log(Level.WARNING, "Wrong formatting of Mob Armor Section. Skipping.");
            }
            else
            {
                ConfigurationSection armorSubPath = path.getConfigurationSection("Armor");
                armorSubPath.getKeys(false).stream().map(armorSubPath::getConfigurationSection).forEach(localSubPath -> 
                {
                    String name = localSubPath.getName();
                    
                    EquipmentSlot slot;
                    try {
                        slot = EquipmentSlot.valueOf(name.toUpperCase());
                    } catch(final IllegalArgumentException ex) {
                        logger.log(Level.WARNING, "An equipment slot named {0} does not exist. Skipping.", name);
                        return;
                    }

                    ItemStack itemStack = ItemUtil.getItemByPath(localSubPath);
                    if(itemStack == null)
                    {
                        logger.log(Level.WARNING, "An error occoured while parsing a Mob Armor ItemStack. Skipping.");
                        return;
                    }
                    float chance = ((float)localSubPath.getDouble("chance", 0F)) / 100F;

                    MobEquipmentPart equipmentPart = new MobEquipmentPart(slot, itemStack, chance);
                    mobManager.addEquipment(equipmentPart);
                });
            }
        }

        
        if(path.contains("Ability"))
        {
            if(!path.isConfigurationSection("Ability"))
            {
                logger.log(Level.WARNING, "Wrong formatting of Mob Ability Section. Skipping.");
            }
            else
            {
                AbilityManager abilityManager = abilityParser.parse(path.getConfigurationSection("Ability"));
                mobManager.setAbilityManager(abilityManager);
            }
        }
        
        if(path.contains("Drop"))
        {
            if(!path.isConfigurationSection("Drop"))
            {
                logger.log(Level.WARNING, "Wrong formatting of Mob Drop Section. Skipping.");
            }
            else
            {
                DropManager dropManager = new DropManager(path.getConfigurationSection("Drop"));
                mobManager.setDropManager(dropManager);
            }
        }
        
        return mobManager;
    }
   
    
}
