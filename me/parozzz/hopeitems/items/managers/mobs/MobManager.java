/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.parozzz.hopeitems.Configs;
import me.parozzz.hopeitems.HopeItems;
import me.parozzz.hopeitems.items.ItemCollection;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.hopeitems.items.ItemRegistry;
import me.parozzz.hopeitems.items.managers.mobs.abilities.AbilityManager;
import me.parozzz.hopeitems.items.managers.mobs.drops.DropManager;
import me.parozzz.reflex.utilities.EntityUtil;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class MobManager 
{
    private final static String ABILITY_METADATA="Hopeitems.ability";
    private final static String DROP_METADATA="Hopeitems.drops";
    
    private final ItemInfo info;
    private final EntityType et;
    private Consumer<LivingEntity> option= liv -> {};
    private Consumer<EntityEquipment> armor= liv -> {};
    private AbilityManager ability;
    private DropManager drop;
    
    public MobManager(final ItemInfo info, final ConfigurationSection path)
    {
        this.info=info;
        try { et=EntityType.valueOf(path.getString("type").toUpperCase()); }
        catch(final IllegalArgumentException t) { throw new IllegalArgumentException("Wrong entity type name"); }
        
        path.getMapList("option").stream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .map(e -> new String[] { e.getKey().toString().toUpperCase() , e.getValue().toString() })
                .forEach(a ->
                {
                    try { option = option.andThen(MobOption.valueOf(a[0]).getConsumer(a[1])); }
                    catch(final IllegalArgumentException t) { throw new IllegalArgumentException("A mob option named "+a[0]+" does not exist"); }
                });
        
        Optional.ofNullable(path.getConfigurationSection("Armor")).ifPresent(aPath -> 
        {
            armor=aPath.getKeys(false).stream().map(aPath::getConfigurationSection).map(sPath -> 
            {
                EquipmentSlot slot=EquipmentSlot.valueOf(sPath.getName().toUpperCase());
                ItemStack item = ItemUtil.getItemByPath(sPath);
                float chance=((float)sPath.getDouble("chance", 0F))/100;
                
                Consumer<EntityEquipment> cns= equip -> EntityUtil.addItem(equip, slot, item.clone(), chance);
                return cns;
            }).reduce(Consumer::andThen).orElseGet(() -> liv -> {});
        });
        
        Optional.ofNullable(path.getConfigurationSection("Ability")).ifPresent(aPath -> ability=new AbilityManager(aPath, this));
        Optional.ofNullable(path.getConfigurationSection("Drop")).ifPresent(dPath -> drop=new DropManager(this, dPath));
    }
    
    private boolean hasAbilities()
    {
        return ability!=null;
    }
    
    private boolean hasDrops()
    {
        return drop!=null;
    }
    
    public LivingEntity spawnMob(final Location l)
    {
        LivingEntity liv=(LivingEntity)l.getWorld().spawnEntity(l, et);
        liv.setRemoveWhenFarAway(false);
        
        option.accept(liv);
        armor.accept(liv.getEquipment());
        
        boolean flag = false;
        if(this.hasAbilities())
        {
            flag = true;
            liv.setMetadata(ABILITY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(MobManager.class), ability));
        }
        
        if(this.hasDrops())
        {
            flag = true;
            liv.setMetadata(DROP_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(MobManager.class), drop));
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
                            e.setCancelled(manager.ability.resistArrows());
                            if(((Arrow)e.getDamager()).getShooter() instanceof LivingEntity)
                            {
                                manager.ability.triggerPassiveAbility((LivingEntity) e.getEntity(), (LivingEntity) ((Arrow)e.getDamager()).getShooter());
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
                            .map(directManager -> directManager.ability.triggerDirectAbility(damager, damaged))
                            .orElseGet(() -> 
                            {
                                return Optional.ofNullable(customMobs.get(e.getEntity().getUniqueId()))
                                        .filter(MobManager::hasAbilities)
                                        .map(passiveManager -> passiveManager.ability
                                                .triggerPassiveAbility(damaged, damager))  
                                        .orElse(true);
                            });
                }
            }
            
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onEntityDeath(final EntityDeathEvent e)
            {
                Optional.ofNullable(customMobs.remove(e.getEntity().getUniqueId()))
                        .filter(MobManager::hasDrops)
                        .ifPresent(manager -> manager.drop.getRandomDrop().accept(e));
            }
        }, JavaPlugin.getProvidingPlugin(MobManager.class));
    }
}
