/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.abilities.parser;

import me.parozzz.hopeitems.items.managers.mobs.abilities.MobAbility;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import me.parozzz.hopeitems.HopeItems;
import me.parozzz.hopeitems.items.managers.IParser;
import me.parozzz.hopeitems.items.managers.ManagerUtils;
import me.parozzz.hopeitems.items.managers.mobs.parsers.MobManager;
import me.parozzz.reflex.NMS.packets.ParticlePacket.ParticleEnum;
import me.parozzz.reflex.configuration.ComplexMapList;
import me.parozzz.reflex.utilities.ParticleUtil;
import me.parozzz.reflex.utilities.ParticleUtil.ParticleEffect;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public class MobAbilityParser implements IParser
{
    private static final Logger logger = Logger.getLogger(MobAbilityParser.class.getSimpleName());
    
    private final MobAbilityRegistry specificParsers;
    public MobAbilityParser()
    {
        specificParsers = new MobAbilityRegistry();
    }
    
    @Override
    public void registerDefaultSpecificParsers() 
    {
        specificParsers.addRegistered("potion", map -> 
        {
            PotionEffect pe = ManagerUtils.getPotionEffect(map);
            if(pe == null)
            {
                logger.log(Level.WARNING, "An error occoured while parsing a potion ability of a mob. Skipping.");
                return Util.EMPTY_BICONSUMER;
            }
            
            return (customMob, involved) -> involved.addPotionEffect(pe, true);
        });
        
        specificParsers.addRegistered("fire", map -> 
        {
            int duration = map.getValue("duration", Integer::valueOf) * 20;
            
            return (customMob, involved) -> involved.setFireTicks(duration);
        });
        
        specificParsers.addRegistered("spawn", map -> 
        {
            EntityType et;
            try {
                et = EntityType.valueOf(map.getUpperValue("type", Function.identity()));
            } catch(final IllegalArgumentException ex) {
                logger.log(Level.WARNING, "An entity named {0} does not exists in your currect minecraft version", map.getValue("type"));
                return Util.EMPTY_BICONSUMER;
            }
            
            int quantity = map.getValue("quantity", Integer::valueOf);
            
            return (customMob, involved) -> 
            {
                IntStream.range(0, quantity).forEach(i -> 
                {
                    Entity ent = customMob.getWorld().spawnEntity(customMob.getLocation(), et);
                    ent.setMetadata(MobManager.MINION, new FixedMetadataValue(HopeItems.getInstance(), true));
                });
            };
        });
        
        specificParsers.addRegistered("sniper", map -> 
        {
            Consumer<Arrow> randomDamage = ManagerUtils.getNumberFunction(map.getValue("damage", Function.identity()), Double::valueOf, (arrow, d) -> arrow.spigot().setDamage(d));
            int quantity = map.getValue("quantity", Integer::valueOf);
            int delay = map.getValue("delay", Integer::valueOf);
            
            return (customMob, involved) -> 
            {
                new BukkitRunnable()
                {
                    private int count=quantity;
                    @Override
                    public void run() 
                    {
                        if(count--<=0 || !customMob.isValid())
                        {
                            this.cancel();
                            return;
                        }

                        Arrow arrow = customMob.launchProjectile(Arrow.class);
                        randomDamage.accept(arrow);
                        arrow.setVelocity(involved.getLocation().getDirection().multiply(-2));

                        ParticleUtil.playParticleEffect(customMob.getEyeLocation(), ParticleEnum.CLOUD, ParticleEffect.CLOUD, 4);
                    }
                }.runTaskTimer(HopeItems.getInstance(), 0L, delay);
            };
        });
        
        specificParsers.addRegistered("ender", map -> 
        {
            Consumer<LivingEntity> randomDamage = ManagerUtils.getNumberFunction(map.getValue("damage", Function.identity()), Function.identity(), (liv, d) -> liv.damage(d));
            
            return (customMob, involved) -> 
            {
                ParticleUtil.playParticleEffect(customMob.getLocation(), ParticleEnum.PORTAL, ParticleEffect.CLOUD, 80);
                customMob.teleport(involved);
                randomDamage.accept(involved);
            };
        });
        
        specificParsers.addRegistered("shield", map -> 
        {
            int duration = map.getValue("duration", Integer::valueOf);
            
            return (customMob, involved) -> 
            {
                customMob.setNoDamageTicks(20);
                new BukkitRunnable()
                {
                    int count=duration*2;
                    @Override
                    public void run() 
                    {
                        if(count--<=0 || !customMob.isValid())
                        {
                            this.cancel();
                            return;
                        }
                        customMob.setNoDamageTicks(20);
                        ParticleUtil.playParticleEffect(customMob.getEyeLocation(), ParticleEnum.FLAME, ParticleEffect.SPHERE, 2);
                    }
                }.runTaskTimer(HopeItems.getInstance(), 0L, 10L);
            };
        });
    }

    @Override
    public AbilityManager parse(final ConfigurationSection path)
    {
        int chance = path.getInt("chance", 100);
        boolean resistArrow = path.getBoolean("resistArrow", false);
        
        AbilityManager abilityManager = new AbilityManager(chance, resistArrow);
        addAllAbilities(new ComplexMapList(path.getMapList("direct")), abilityManager, true);
        addAllAbilities(new ComplexMapList(path.getMapList("passive")), abilityManager, false);
        return abilityManager;
    }
    
    private void addAllAbilities(final ComplexMapList mapList, final AbilityManager abilityManager, final boolean direct)
    {
        mapList.forEach((type, list) -> 
        {
            MobAbilitySpecificParser parser = this.specificParsers.getRegistered(type.toLowerCase());
            if(parser == null)
            {
                logger.log(Level.WARNING, "A mob ability named {0} does not exists. Skipping.", type);
                return;
            }

            list.forEach(map -> 
            {
                MobAbility ability = parser.parse(map);
                if(ability == null)
                {
                    logger.log(Level.WARNING, "Error occoured while parse a {0} {1} mob ability", new Object[] { type, (direct ? "direct" : "passive")} );
                    return;
                }
                
                if(direct)
                {
                    abilityManager.addDirectAbility(ability);
                }
                else
                {
                    abilityManager.addPassiveAbility(ability);
                }
                
            });
        });
    }
    
}

/*
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
    
 */
