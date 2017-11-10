/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.hopeitems.HopeItems;
import me.parozzz.hopeitems.items.managers.ManagerUtils;
import me.parozzz.hopeitems.utilities.Debug;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.classes.MapArray;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author Paros
 */
public enum MobOption 
{
    NAME{
        @Override
        public Consumer<LivingEntity> getConsumer(String value)
        {
            String name=Utils.color(value);
            return liv ->
            {
                liv.setCustomName(name);
                liv.setCustomNameVisible(true);
            };
        }
    }, 
    RIDE{
        @Override
        public Consumer<LivingEntity> getConsumer(final String value)
        {
            EntityType ride=EntityType.valueOf(value.toUpperCase());
            return MCVersion.V1_10.isLower() ? liv -> liv.setPassenger(liv.getWorld().spawnEntity(liv.getLocation(), ride)) : liv -> liv.addPassenger(liv.getWorld().spawnEntity(liv.getLocation(), ride));
        }
    },
    POTION{
        @Override
        public Consumer<LivingEntity> getConsumer(final String value)
        {
            PotionEffect pe=ManagerUtils.getPotionEffect(new MapArray(value));
            return liv -> liv.addPotionEffect(pe, true);
        }
    }, 
    HEALTH{
        @Override
        public Consumer<LivingEntity> getConsumer(final String value)
        {
            return ManagerUtils.getNumberFunction(value, Double::valueOf, Utils::setMaxHealth);
        }
    }, 
    ATTRIBUTE{
        @Override
        public Consumer<LivingEntity> getConsumer(final String value)
        {
            if(MCVersion.V1_8.isEqual())
            {
                Logger.getLogger(HopeItems.class.getSimpleName()).log(Level.WARNING, "[HopeItems] Mob attributes are not supported in MC1.8");
                return liv -> {};
            }
            
            MapArray map =new MapArray(value);
            
            Attribute attr = Debug.validateEnum("GENERIC_"+map.getUpperValue("type", Function.identity()), Attribute.class);
            String amount = map.getUpperValue("value", Function.identity());
            
            return ManagerUtils.getNumberFunction(amount, Double::valueOf, (liv, d) -> liv.getAttribute(attr).setBaseValue(d));
        }
    };
    
    public Consumer<LivingEntity> getConsumer(final String value)
    {
        throw new UnsupportedOperationException();
    }
}
