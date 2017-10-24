/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import me.parozzz.hopeitems.items.managers.mobs.abilities.AbilityManager;
import me.parozzz.hopeitems.items.managers.mobs.drops.DropManager;
import me.parozzz.hopeitems.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
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
    
    private final EntityType et;
    private Consumer<LivingEntity> option= liv -> {};
    private Consumer<EntityEquipment> armor= liv -> {};
    private AbilityManager ability;
    private DropManager drop;
    public MobManager(final ConfigurationSection path)
    {
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
                ItemStack item=Utils.getItemByPath(sPath);
                float chance=((float)sPath.getDouble("chance", 0F))/100;
                
                Consumer<EntityEquipment> cns= equip -> Utils.addItem(equip, slot, item.clone(), chance);
                return cns;
            }).reduce(Consumer::andThen).orElseGet(() -> liv -> {});
        });
        
        Optional.ofNullable(path.getConfigurationSection("Ability")).ifPresent(aPath -> ability=new AbilityManager(aPath, this));
        Optional.ofNullable(path.getConfigurationSection("Drop")).ifPresent(dPath -> drop=new DropManager(this, dPath));
    }
    
    public boolean hasAbilities()
    {
        return ability!=null;
    }
    
    public boolean hasDrops()
    {
        return drop!=null;
    }
    
    public AbilityManager getAbilityManager()
    {
        return ability;
    }
    
    public LivingEntity spawnMob(final Location l)
    {
        LivingEntity liv=(LivingEntity)l.getWorld().spawnEntity(l, et);
        option.accept(liv);
        armor.accept(liv.getEquipment());
        if(this.hasAbilities())
        {
            liv.setMetadata(ABILITY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(MobManager.class), ability));
        }
        
        if(this.hasDrops())
        {
            liv.setMetadata(DROP_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(MobManager.class), drop));
        }
        return liv;
    }
    
    public static void registerListener()
    {
        Bukkit.getPluginManager().registerEvents(new Listener()
        {
            
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onEntityDamage(final EntityDamageByEntityEvent e)
            {
                if(e.getDamager() instanceof Arrow && e.getEntity().hasMetadata(ABILITY_METADATA))
                {
                    AbilityManager ability=(AbilityManager)e.getEntity().getMetadata(ABILITY_METADATA).get(0).value();
                    e.setCancelled(ability.resistArrows());
                    if(((Arrow)e.getDamager()).getShooter() instanceof LivingEntity)
                    {
                        ability.triggerPassiveAbility((LivingEntity) e.getEntity(), (LivingEntity) ((Arrow)e.getDamager()).getShooter());
                    }
                }
                else if(e.getDamager().getType().isAlive() && e.getEntity().getType().isAlive())
                {
                    if(e.getDamager().hasMetadata(ABILITY_METADATA))
                    {
                        ((AbilityManager)e.getDamager().getMetadata(ABILITY_METADATA).get(0).value())
                                .triggerDirectAbility((LivingEntity)e.getDamager(), (LivingEntity)e.getEntity());
                    }
                    else if(e.getEntity().hasMetadata(ABILITY_METADATA))
                    {
                        ((AbilityManager)e.getEntity().getMetadata(ABILITY_METADATA).get(0).value())
                                .triggerPassiveAbility((LivingEntity)e.getEntity(), (LivingEntity)e.getDamager());
                    }
                }
            }
            
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onEntityDeath(final EntityDeathEvent e)
            {
                if(e.getEntity().hasMetadata(DROP_METADATA))
                {
                    ((DropManager)e.getEntity().getMetadata(DROP_METADATA).get(0).value()).getRandomDrop().accept(e);
                }
            }
        }, JavaPlugin.getProvidingPlugin(MobManager.class));
    }
}
