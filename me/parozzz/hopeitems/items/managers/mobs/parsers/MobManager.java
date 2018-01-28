/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs.parsers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.parozzz.hopeitems.HopeItems;
import me.parozzz.hopeitems.items.ItemCollection;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.hopeitems.items.ItemRegistry;
import me.parozzz.hopeitems.items.managers.IManager;
import me.parozzz.hopeitems.items.managers.mobs.MobEquipmentPart;
import me.parozzz.hopeitems.items.managers.mobs.abilities.parser.AbilityManager;
import me.parozzz.hopeitems.items.managers.mobs.drops.DropManager;
import me.parozzz.hopeitems.items.managers.mobs.option.MobOptionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class MobManager implements IManager
{
    private final static String ABILITY_METADATA = "Hopeitems.ability";
    private final static String DROP_METADATA = "Hopeitems.drops";
    
    private final EntityType et;
    private final List<MobEquipmentPart> equipmentMap;
    public MobManager(final EntityType et)
    {
        this.et = et;
        
        equipmentMap = new LinkedList<>();
    }
    
    private MobOptionManager optionManager;
    protected void setOptionManager(final MobOptionManager manager)
    {
        this.optionManager = manager;
    }
    
    private AbilityManager abilityManager;
    protected void setAbilityManager(final AbilityManager manager)
    {
        this.abilityManager = manager;
    }
    
    private DropManager dropManager;
    protected void setDropManager(final DropManager manager)
    {
        this.dropManager = manager;
    }
    
    protected void addEquipment(final MobEquipmentPart equipmentPart)
    {
        equipmentMap.add(equipmentPart);
    }
    
    private ItemInfo info;
    public void setItemInfo(final ItemInfo info)
    {
        this.info = info;
    }
    
    private boolean hasAbilities()
    {
        return abilityManager != null;
    }
    
    private boolean hasDrops()
    {
        return dropManager != null;
    }
    
    public LivingEntity spawnMob(final Location l)
    {
        LivingEntity liv = (LivingEntity)l.getWorld().spawnEntity(l, et);
        liv.setRemoveWhenFarAway(false);
        
        Optional.ofNullable(optionManager).ifPresent(manager -> manager.applyAll(liv));
        
        EntityEquipment equipment = liv.getEquipment();
        equipmentMap.forEach(part -> part.addToEquipment(equipment));
        
        boolean flag = false;
        if(this.hasAbilities())
        {
            flag = true;
            liv.setMetadata(ABILITY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(MobManager.class), abilityManager));
        }
        
        if(this.hasDrops())
        {
            flag = true;
            liv.setMetadata(DROP_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(MobManager.class), dropManager));
        }
        
        if(flag)
        {
            customMobs.put(liv.getUniqueId(), this);
        }
        return liv;
    }
    
    private final static Map<UUID, MobManager> customMobs = new HashMap<>();
    
    public static void saveData(final FileConfiguration data)
    {
        data.set("mobs", customMobs.entrySet().stream().map(e -> 
        {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("MOB_UID", e.getKey().toString());
            map.put("ITEM_ID", e.getValue().info.getCollection().getId());
            map.put("WHEN", e.getValue().info.getWhens().stream().findFirst().get().name());
            return map;
        }).collect(Collectors.toList()));
    }
    
    public static void loadData(final FileConfiguration data)
    {
        data.getMapList("mobs").stream()
                .map(map -> (Map<String, String>)map)
                .forEach(map -> 
                {
                    ItemCollection collection = ItemRegistry.getCollection(map.get("ITEM_ID"));
                    if(collection == null)
                    {
                        return;
                    }
                    
                    UUID u = UUID.fromString(map.get("MOB_UID"));
                    
                    Optional.ofNullable(collection.getItemInfo(When.valueOf(map.get("WHEN"))))
                            .filter(ItemInfo::hasMob)
                            .map(info -> 
                            {
                                customMobs.put(UUID.fromString(map.get("MOB_UID")), info.getMobManager());
                                return info;
                            })
                            .orElseGet(() -> 
                            {
                                Logger.getLogger(HopeItems.class.getSimpleName()).log(Level.SEVERE, "A configuration item named {0} does not exist. Skipping mob with uuid {1}", new Object[]{collection.getId(), u.toString()}); 
                                return null;
                            });
                });
    }
    
    public static final String MINION = "HopeItems.minion";
    
    public static void registerListener()
    {
        Bukkit.getPluginManager().registerEvents(new Listener()
        {
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onEntityTarget(final EntityTargetEvent e)
            {
                Optional.ofNullable(e.getTarget())
                        .map(target -> customMobs.get(target.getUniqueId()))
                        .filter(manager -> e.getEntity().hasMetadata(MINION))
                        .ifPresent(manager -> e.setCancelled(true));
            }
            
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onEntityDamage(final EntityDamageByEntityEvent e)
            {
                if(e.getDamager() instanceof Arrow && e.getEntity().hasMetadata(ABILITY_METADATA))
                {
                    Optional.ofNullable(customMobs.get(e.getEntity().getUniqueId())).ifPresent(manager -> 
                    {
                        if(manager.hasAbilities())
                        {
                            e.setCancelled(manager.abilityManager.resistArrows());
                            if(((Arrow)e.getDamager()).getShooter() instanceof LivingEntity)
                            {
                                manager.abilityManager.triggerPassiveAbility((LivingEntity) e.getEntity(), (LivingEntity) ((Arrow)e.getDamager()).getShooter());
                            }
                        }
                    });
                }
                else if(e.getDamager().getType().isAlive() && e.getEntity().getType().isAlive())
                {
                    LivingEntity damager = (LivingEntity)e.getDamager();
                    LivingEntity damaged = (LivingEntity)e.getEntity();
                    Optional.ofNullable(customMobs.get(e.getDamager().getUniqueId()))
                            .filter(MobManager::hasAbilities)
                            .map(directManager -> directManager.abilityManager.triggerDirectAbility(damager, damaged))
                            .orElseGet(() -> 
                            {
                                return Optional.ofNullable(customMobs.get(e.getEntity().getUniqueId()))
                                        .filter(MobManager::hasAbilities)
                                        .map(passiveManager -> passiveManager.abilityManager.triggerPassiveAbility(damaged, damager))  
                                        .orElse(true);
                            });
                }
            }
            
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onEntityDeath(final EntityDeathEvent e)
            {
                Optional.ofNullable(customMobs.remove(e.getEntity().getUniqueId()))
                        .filter(MobManager::hasDrops)
                        .ifPresent(manager -> manager.dropManager.getRandomDrop().accept(e));
            }
        }, JavaPlugin.getProvidingPlugin(MobManager.class));
    }
}
