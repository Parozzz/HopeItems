/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.abilities;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import me.parozzz.hopeitems.items.managers.ManagerUtils;
import me.parozzz.hopeitems.items.managers.mobs.MobManager;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.NMS.packets.ParticlePacket;
import me.parozzz.reflex.NMS.packets.ParticlePacket.ParticleEnum;
import me.parozzz.reflex.classes.MapArray;
import me.parozzz.reflex.utilities.ParticleUtil;
import me.parozzz.reflex.utilities.ParticleUtil.ParticleEffect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public enum AbilityType 
{
    POTION{
        @Override
        public BiConsumer<LivingEntity, LivingEntity> getConsumer(final MapArray map)
        {
            PotionEffect pe=ManagerUtils.getPotionEffect(map);
            int chance=map.getValue("chance", Integer::valueOf);
            
            return (mob, other) -> 
            {
                if(ThreadLocalRandom.current().nextInt(101)<chance)
                {
                    other.addPotionEffect(pe, true);
                }
            };
        } 
    }, 
    FIRE{
        @Override
        public BiConsumer<LivingEntity, LivingEntity> getConsumer(final MapArray map)
        {
            int duration = map.getValue("duration", Integer::valueOf) * 20;
            int chance = map.getValue("chance", Integer::valueOf);
            
            return (mob, other) -> 
            {
                if(ThreadLocalRandom.current().nextInt(101)<chance)
                {
                    other.setFireTicks(duration);
                }
            };
        }
    }, 
    SPAWN{
        @Override
        public BiConsumer<LivingEntity, LivingEntity> getConsumer(final MapArray map)
        {
            EntityType et = Debug.validateEnum(map.getUpperValue("type", Function.identity()), EntityType.class);
            int quantity = map.getValue("quantity", Integer::valueOf);
            int chance = map.getValue("chance", Integer::valueOf);

            return (mob, other) -> 
            {
                if(ThreadLocalRandom.current().nextInt(101)<chance)
                {
                    IntStream.range(0, quantity).forEach(i -> mob.getWorld().spawnEntity(mob.getLocation(), et).setMetadata(MobManager.MINION, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(AbilityType.class), true)));
                }
            };
        } 
    },
    SNIPER{
        @Override
        public BiConsumer<LivingEntity, LivingEntity> getConsumer(final MapArray map)
        {
            Consumer<Arrow> randomDamage = ManagerUtils.getNumberFunction(map.getValue("damage", Function.identity()), Double::valueOf, (arrow, d) -> arrow.spigot().setDamage(d));
            int quantity = map.getValue("quantity", Integer::valueOf);
            int delay = map.getValue("delay", Integer::valueOf);
            int chance = map.getValue("chance", Integer::valueOf);
            
            return (mob, other) -> 
            {
                if(ThreadLocalRandom.current().nextInt(101)<chance)
                {
                    new BukkitRunnable()
                    {
                        private int count=quantity;
                        @Override
                        public void run() 
                        {
                            if(count--<=0 || mob.isDead() || !mob.isValid())
                            {
                                this.cancel();
                                return;
                            }

                            Arrow arrow=mob.launchProjectile(Arrow.class);
                            randomDamage.accept(arrow);
                            arrow.setVelocity(other.getLocation().getDirection().multiply(-2));
                            
                            ParticleUtil.playParticleEffect(mob.getEyeLocation(), ParticleEnum.CLOUD, ParticleEffect.CLOUD, 2);
                        }

                    }.runTaskTimer(JavaPlugin.getProvidingPlugin(AbilityType.class), 0L, delay);
                }
            };
        }
    },
    ENDER{
        @Override
        public BiConsumer<LivingEntity, LivingEntity> getConsumer(final MapArray map)
        {
            Consumer<LivingEntity> randomDamage=ManagerUtils.getNumberFunction(map.getValue("damage", Function.identity()), Function.identity(), (liv, d) -> liv.damage(d));
            int chance = map.getValue("chance", Integer::valueOf);
            
            return (mob, other) -> 
            {
                if(ThreadLocalRandom.current().nextInt(101)<chance)
                {
                    ParticleUtil.playParticleEffect(mob.getLocation(), ParticleEnum.PORTAL, ParticleEffect.CLOUD, 80);
                    mob.teleport(other);
                    randomDamage.accept(other);
                }
            };
        } 
    },
    SHIELD{
        @Override
        public BiConsumer<LivingEntity, LivingEntity> getConsumer(final MapArray map)
        {
            int duration = map.getValue("duration", Integer::valueOf);
            int chance = map.getValue("chance", Integer::valueOf);
            
            return (mob, other) -> 
            {
                if(ThreadLocalRandom.current().nextInt(101)<chance)
                {
                    mob.setNoDamageTicks(20);
                    new BukkitRunnable()
                    {
                        int count=duration*2;
                        @Override
                        public void run() 
                        {
                            if(count--<=0 || mob.isDead() || !mob.isValid())
                            {
                                this.cancel();
                                return;
                            }
                            mob.setNoDamageTicks(20);
                            ParticleUtil.playParticleEffect(mob.getEyeLocation(), ParticleEnum.FLAME, ParticleEffect.SPHERE, 2);
                        }
                    }.runTaskTimer(JavaPlugin.getProvidingPlugin(AbilityType.class), 0L, 10L);
                }
            };
        }
    };
    
    public BiConsumer<LivingEntity, LivingEntity> getConsumer(final MapArray map)
    {
        throw new UnsupportedOperationException();
    }
}
