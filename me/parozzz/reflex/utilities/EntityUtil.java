/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.utilities;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.utilities.HeadUtil.Headable;
import me.parozzz.reflex.utilities.HeadUtil.MobHead;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class EntityUtil 
{
    public enum CreatureType implements Headable
    {
        BAT,
        BLAZE,
        SPIDER, CAVE_SPIDER,
        CHICKEN,
        COW, MUSHROOM_COW,
        CREEPER,
        ENDER_DRAGON,
        ENDERMAN,
        ENDERMITE,
        GHAST,
        GUARDIAN,ELDER_GUARDIAN,
        HORSE, SKELETON_HORSE, ZOMBIE_HORSE, MULE, DONKEY, LLAMA,
        IRON_GOLEM, SNOWMAN,
        OCELOT,
        PARROT,
        PIG,
        POLAR_BEAR,
        RABBIT,
        SHEEP,
        SHULKER,
        SILVERFISH,
        SKELETON, WITHER_SKELETON, STRAY,
        SLIME, MAGMA_CUBE,
        SQUID,
        VILLAGER,
        VINDICATOR, EVOKER, ILLUSIONER,
        VEX,
        WITCH,
        WOLF,
        ZOMBIE, ZOMBIE_VILLAGER, PIG_ZOMBIE, HUSK, GIANT,
        WITHER,
        PLAYER;
        
        
        private static Function<LivingEntity, CreatureType> init()
        {
            if(MCVersion.V1_11.isHigher())
            {
                return ent -> CreatureType.valueOf(ent.getType().name());
            }
            
            return ent -> 
            {
                switch(ent.getType())
                {
                    case SKELETON:
                        switch(((Skeleton)ent).getSkeletonType())
                        {
                            case STRAY: 
                                return STRAY;
                            case WITHER: 
                                return WITHER_SKELETON;
                            default: 
                                return SKELETON;
                        }
                    case HORSE:
                        switch(((Horse)ent).getVariant())
                        {
                            case DONKEY:
                                return DONKEY;
                            case MULE: 
                                return MULE;
                            case LLAMA: 
                                return LLAMA;
                            case SKELETON_HORSE:
                                return SKELETON_HORSE;
                            case UNDEAD_HORSE: 
                                return ZOMBIE_HORSE;
                            default:
                                return HORSE;
                        }
                    case ZOMBIE:
                        Zombie z = (Zombie)ent;
                        return z.isVillager() ? (MCVersion.V1_9.isHigher() && z.getVillagerProfession() == Profession.HUSK ? HUSK : ZOMBIE_VILLAGER) : ZOMBIE;
                    case GUARDIAN: 
                        return ((Guardian)ent).isElder() ? CreatureType.ELDER_GUARDIAN : CreatureType.GUARDIAN;
                    default: 
                        return CreatureType.valueOf(ent.getType().name());
                }
            };
        }
        
        private static final Function<LivingEntity, CreatureType> FETCHER = init();
        public static CreatureType getByLivingEntity(final LivingEntity ent)
        {
            return FETCHER.apply(ent);
        }

        @Override
        public String getUrl() 
        {
            return MobHead.valueOf(name()).getUrl();
        }

        @Override
        public ItemStack getHead() 
        {
            return MobHead.valueOf(name()).getHead();
        }
    }
    
    private static Function<EntityEquipment, ItemStack> getHand;
    public static ItemStack getMainHand(final EntityEquipment equip)
    {
        return Optional.ofNullable(getHand)
                .orElseGet(() -> getHand = MCVersion.V1_8.isEqual()? eq -> eq.getItemInHand() : eq -> eq.getItemInMainHand())
                .apply(equip);
    }
    
    private static BiConsumer<EntityEquipment, ItemStack> setHand;
    public static void setMainHand(final EntityEquipment equip, final ItemStack item)
    {
        Optional.ofNullable(setHand)
                .orElseGet(() -> setHand = MCVersion.V1_8.isEqual()? (eq, i) -> eq.setItemInHand(i) : (eq, i) -> eq.setItemInMainHand(i))
                .accept(equip, item);
    }
    
    private static Function<LivingEntity, Double> getMaxHealth;
    public static double getMaxHealth(final LivingEntity ent)
    {
        return Optional.ofNullable(getMaxHealth)
                .orElseGet(() -> getMaxHealth = MCVersion.V1_8.isEqual()? e -> e.getMaxHealth() : e -> e.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
                .apply(ent);
    }
    
    private static BiConsumer<LivingEntity, Double> setMaxHealth;
    public static void setMaxHealth(final LivingEntity ent, final Double health)
    {
        Optional.ofNullable(setMaxHealth)
                .orElseGet(() -> setMaxHealth = MCVersion.V1_8.isEqual()? (e,h) -> e.setMaxHealth(h) : (e,h) -> e.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(h))
                .accept(ent, health);
        ent.setHealth(health.intValue());
    }
    
    public static void setExp(final Player p, final int exp)
    {
        p.setTotalExperience(0);
        p.setLevel(0);
        p.setExp(0);
        
        p.giveExp(exp);
    }
    
    public static void addItem(final EntityEquipment equip, final EquipmentSlot slot, final ItemStack item)
    {
        addItem(equip, slot, item, null);
    }
    
    public static void addItem(final EntityEquipment equip, final EquipmentSlot slot, final ItemStack item, final Float dropChance)
    {
        if(slot == null)
        {
            return;
        }
        
        switch(slot)
        {
            case HEAD:
                equip.setHelmet(item);
                if(dropChance!=null)
                {
                    equip.setHelmetDropChance(dropChance);
                }
                break;
            case CHEST:
                equip.setChestplate(item);
                if(dropChance!=null)
                {
                    equip.setChestplateDropChance(dropChance);
                }
                break;
            case LEGS:
                equip.setLeggings(item);
                if(dropChance!=null)
                {
                    equip.setLeggingsDropChance(dropChance);
                }
                break;
            case FEET:
                equip.setBoots(item);
                if(dropChance!=null)
                {
                    equip.setBootsDropChance(dropChance);
                }
                break;
            case HAND:
                setMainHand(equip, item);
                if(dropChance!=null)
                {
                    if(MCVersion.V1_8.isEqual())
                    {
                        equip.setItemInHandDropChance(dropChance);
                    }
                    else
                    {
                        equip.setItemInMainHandDropChance(dropChance);
                    }
                }
                break;
            case OFF_HAND:
                equip.setItemInOffHand(item);
                if(dropChance!=null)
                {
                    equip.setItemInOffHandDropChance(dropChance);
                }
                break;
        }
    }
}
