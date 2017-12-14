/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.explosive;

import java.util.function.BiConsumer;
import me.parozzz.reflex.utilities.EntityUtil;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public enum ExplosiveOption 
{
    HEALTH{
        @Override
        public BiConsumer<Entity, ExplosiveMetadata> getConsumer(final String value)
        {
            double health=Double.valueOf(value);
            
            return (ent, meta) -> 
            {
                if(ent instanceof LivingEntity)
                {
                    EntityUtil.setMaxHealth((LivingEntity)ent, health);
                }
            };
        }
    }, 
    FRIENDLY{
        @Override
        public BiConsumer<Entity, ExplosiveMetadata> getConsumer(final String value)
        {
            return (ent, meta) -> 
            {
                if(ent.getType()==EntityType.CREEPER)
                {
                    ent.setMetadata(ExplosiveMetadata.FRIENDLY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(ExplosiveOption.class), true));
                }
            };
        }
    },
    CHARGED{
        @Override
        public BiConsumer<Entity, ExplosiveMetadata> getConsumer(final String value)
        {
            boolean charged=Boolean.valueOf(value);
            
            return (ent, meta) -> 
            {
                if(ent instanceof Creeper)
                {
                    ((Creeper)ent).setPowered(charged);
                }
            };
        }
    }, 
    NAME{
        @Override
        public BiConsumer<Entity, ExplosiveMetadata> getConsumer(final String value)
        {
            String name = Util.cc(value);
            
            return (ent, meta) -> 
            {
                ent.setCustomName(name);
                ent.setCustomNameVisible(true);
            };
        }
    }, 
    FIRE{
        @Override
        public BiConsumer<Entity, ExplosiveMetadata> getConsumer(final String value)
        {
            boolean fire=Boolean.valueOf(value);
            
            return (ent, meta) -> meta.fire=fire;
        }
    }, 
    POWER{
        @Override
        public BiConsumer<Entity, ExplosiveMetadata> getConsumer(final String value)
        {
            float power=Float.valueOf(value);
            
            return (ent, meta) -> meta.power=power;
        }
    },
    BLOCKDAMAGE{
        @Override
        public BiConsumer<Entity, ExplosiveMetadata> getConsumer(final String value)
        {
            boolean blockDamage=Boolean.valueOf(value);
            
            return (ent, meta) -> meta.blockDamage=blockDamage;
        }
    };
    
    public BiConsumer<Entity, ExplosiveMetadata> getConsumer(final String value)
    {
        throw new UnsupportedOperationException();
    }
}
