/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.events.armor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import me.parozzz.reflex.events.armor.ArmorEquipEvent;
import me.parozzz.reflex.events.armor.ArmorUnequipEvent;
import me.parozzz.reflex.events.armor.ArmorUnequipEvent.Cause;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Material;
import static org.bukkit.Material.CHAINMAIL_BOOTS;
import static org.bukkit.Material.CHAINMAIL_CHESTPLATE;
import static org.bukkit.Material.CHAINMAIL_HELMET;
import static org.bukkit.Material.CHAINMAIL_LEGGINGS;
import static org.bukkit.Material.DIAMOND_BOOTS;
import static org.bukkit.Material.DIAMOND_CHESTPLATE;
import static org.bukkit.Material.DIAMOND_HELMET;
import static org.bukkit.Material.DIAMOND_LEGGINGS;
import static org.bukkit.Material.GOLD_BOOTS;
import static org.bukkit.Material.GOLD_CHESTPLATE;
import static org.bukkit.Material.GOLD_HELMET;
import static org.bukkit.Material.GOLD_LEGGINGS;
import static org.bukkit.Material.IRON_BOOTS;
import static org.bukkit.Material.IRON_CHESTPLATE;
import static org.bukkit.Material.IRON_HELMET;
import static org.bukkit.Material.IRON_LEGGINGS;
import static org.bukkit.Material.LEATHER_BOOTS;
import static org.bukkit.Material.LEATHER_CHESTPLATE;
import static org.bukkit.Material.LEATHER_HELMET;
import static org.bukkit.Material.LEATHER_LEGGINGS;
import static org.bukkit.Material.PUMPKIN;
import static org.bukkit.Material.SKULL_ITEM;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class ArmorHandler implements Listener
{
    private final static EnumMap<Material, Armor> armors = new EnumMap(Material.class);
    private final static Map<Integer, EquipmentSlot> slots = new HashMap<>();
    private final static Map<Integer, EquipmentSlot> rawSlots = new HashMap<>();
    static
    {
        slots.put(39, EquipmentSlot.HEAD); 
        slots.put(38, EquipmentSlot.CHEST); 
        slots.put(37, EquipmentSlot.LEGS); 
        slots.put(36, EquipmentSlot.FEET);
        
        rawSlots.put(5, EquipmentSlot.HEAD); 
        rawSlots.put(6, EquipmentSlot.CHEST); 
        rawSlots.put(7, EquipmentSlot.LEGS); 
        rawSlots.put(8, EquipmentSlot.FEET);
        
        Stream.of(LEATHER_HELMET, CHAINMAIL_HELMET, GOLD_HELMET, IRON_HELMET, DIAMOND_HELMET, SKULL_ITEM, PUMPKIN).forEach(m -> armors.put(m, new Armor(39, EquipmentSlot.HEAD)));
        Stream.of(LEATHER_CHESTPLATE, CHAINMAIL_CHESTPLATE, GOLD_CHESTPLATE, IRON_CHESTPLATE, DIAMOND_CHESTPLATE).forEach(m -> armors.put(m, new Armor(38, EquipmentSlot.CHEST)));
        Stream.of(LEATHER_LEGGINGS, CHAINMAIL_LEGGINGS, GOLD_LEGGINGS, IRON_LEGGINGS, DIAMOND_LEGGINGS).forEach(m -> armors.put(m, new Armor(37, EquipmentSlot.LEGS)));
        Stream.of(LEATHER_BOOTS, CHAINMAIL_BOOTS, GOLD_BOOTS, IRON_BOOTS, DIAMOND_BOOTS).forEach(m -> armors.put(m, new Armor(36, EquipmentSlot.FEET)));
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onPlayerArmorInteract(final PlayerInteractEvent e)
    {
        if(e.getAction()==Action.RIGHT_CLICK_AIR)
        {
            checkInteract(e);
        }
        else if(e.getAction()==Action.RIGHT_CLICK_BLOCK)
        {
            if(isContainer(e.getClickedBlock()) && !e.getPlayer().isSneaking())
            {
                return;
            }
            
            checkInteract(e);
        }
    }
    
    private void checkInteract(final PlayerInteractEvent e)
    {
        Optional.ofNullable(e.getItem())
                .map(ItemStack::getType)
                .filter(m -> m != Material.SKULL_ITEM)
                .flatMap(m -> Optional.ofNullable(armors.get(m)))
                .filter(armor -> getSlot(e.getPlayer().getEquipment(), armor.getEquipmentSlot()) == null)
                .ifPresent(armor -> 
                {
                    if(Util.callEvent(new ArmorEquipEvent(e.getPlayer(), e.getItem(), armor.getEquipmentSlot())).isCancelled())
                    {
                        e.setCancelled(true);
                    }
                });
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.LOWEST)
    private void onPlayerClickEquip(final InventoryClickEvent e)
    {/*
        Bukkit.getLogger().info("=====================");
        Bukkit.getLogger().info("InvType: "+(e.getInventory().getType()!=null?e.getInventory().getType().toString():""));
        Bukkit.getLogger().info("CurrentItem: "+(e.getCurrentItem()!=null?e.getCurrentItem().toString():""));
        Bukkit.getLogger().info("CursorItem: "+(e.getCursor()!=null?e.getCursor().toString():""));
        Bukkit.getLogger().info("SlotItem: "+(e.getInventory().getItem(e.getSlot())!=null?e.getInventory().getItem(e.getSlot()).toString():""));
        Bukkit.getLogger().info("RawSlotItem: "+(e.getInventory().getItem(e.getRawSlot())!=null?e.getInventory().getItem(e.getRawSlot()).toString():""));
        Bukkit.getLogger().info("HotbarSlotItem: "+(e.getInventory().getItem(e.getHotbarButton())!=null?e.getInventory().getItem(36+e.getHotbarButton()).toString():""));
        Bukkit.getLogger().info("PlayerCursorItem: "+(e.getWhoClicked().getItemOnCursor()!=null?e.getWhoClicked().getItemOnCursor().toString():""));
        Bukkit.getLogger().info("Action: "+(e.getAction()!=null?e.getAction().toString():""));
        Bukkit.getLogger().info("SlotType: "+(e.getSlotType()!=null?e.getSlotType().toString():""));
        Bukkit.getLogger().info("Click: "+(e.getClick()!=null?e.getClick().toString():""));
        Bukkit.getLogger().info("Slot: "+Integer.toString(e.getSlot()));
        Bukkit.getLogger().info("RawSlot: "+Integer.toString(e.getRawSlot()));
        Bukkit.getLogger().info("HotbarSlot: "+Integer.toString(e.getHotbarButton()));
        Bukkit.getLogger().info("=====================");
        */
        if(e.getInventory().getType()!=InventoryType.CRAFTING)
        {
            return;
        }
        
        switch(e.getSlotType())
        {
            case QUICKBAR:
            case CONTAINER:
                switch(e.getAction())
                {
                    case MOVE_TO_OTHER_INVENTORY:
                        if(e.getCurrentItem().getType()!=Material.AIR)
                        {
                            if(Util.or(e.getClick(), ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT))
                            {
                                Optional.ofNullable(armors.get(e.getCurrentItem().getType()))
                                        .filter(armor -> getSlot(e.getWhoClicked().getEquipment(), armor.getEquipmentSlot())==null)
                                        .ifPresent(armor -> 
                                        {
                                            if(Util.callEvent(new ArmorEquipEvent((Player)e.getWhoClicked(), e.getCurrentItem(), armor.getEquipmentSlot())).isCancelled())
                                            {
                                                e.setCancelled(true);
                                            }
                                        }); 
                            }
                        }
                        break;
                }
                break;
            case ARMOR:
                switch(e.getAction())
                {
                    case DROP_ONE_SLOT:
                    case DROP_ALL_SLOT:
                        if(Util.callEvent(new ArmorUnequipEvent((Player)e.getWhoClicked(), e.getCurrentItem(), slots.get(e.getSlot()), Cause.DROP)).isCancelled())
                        {
                            e.setCancelled(true);
                        }
                        break;
                    case MOVE_TO_OTHER_INVENTORY:
                    case PICKUP_ALL:
                    case PICKUP_HALF:
                    case PICKUP_ONE:
                        if(Util.callEvent(new ArmorUnequipEvent((Player)e.getWhoClicked(), e.getCurrentItem(), slots.get(e.getSlot()), Cause.NORMAL)).isCancelled())
                        {
                            e.setCancelled(true);
                        }
                        break;
                    case PLACE_ALL:
                    case PLACE_ONE:
                    case PLACE_SOME:
                        Optional.of(e.getCursor())
                                .map(ItemStack::getType)
                                .filter(m -> m!=Material.AIR)
                                .flatMap(m -> Optional.ofNullable(armors.get(m)))
                                .filter(armor -> armor.getSlot()==e.getSlot())
                                .ifPresent(armor -> 
                                {
                                    if(Util.callEvent(new ArmorEquipEvent((Player)e.getWhoClicked(), e.getCursor(), armor.getEquipmentSlot())).isCancelled())
                                    {
                                        e.setCancelled(true);
                                    }
                                });
                        break;
                    case SWAP_WITH_CURSOR:
                        if(e.getCursor().getType()==Material.AIR && e.getCurrentItem().getType()!=Material.AIR)
                        {
                            if(Util.callEvent(new ArmorUnequipEvent((Player)e.getWhoClicked(), e.getCurrentItem(), slots.get(e.getSlot()), Cause.NORMAL)).isCancelled())
                            {
                                e.setCancelled(true);
                            }
                        }
                        else
                        {
                            Cancellable un = Util.callEvent(new ArmorUnequipEvent((Player)e.getWhoClicked(), e.getCurrentItem(), slots.get(e.getSlot()), Cause.NORMAL));
                            Cancellable eq = Util.callEvent(new ArmorEquipEvent((Player)e.getWhoClicked(), e.getCursor(), slots.get(e.getSlot())));
                            if(un.isCancelled() || eq.isCancelled())
                            {
                                e.setCancelled(true);
                            }
                        }
                        break;
                    case HOTBAR_SWAP:                    
                        if(e.getCurrentItem().getType() != Material.AIR)
                        {
                            e.setCancelled(Util.callEvent(new ArmorUnequipEvent((Player)e.getWhoClicked(), e.getCurrentItem(), slots.get(e.getSlot()), Cause.NORMAL)).isCancelled());
                        }
                        
                        Optional.ofNullable(e.getWhoClicked().getInventory().getItem(e.getHotbarButton())).ifPresent(item -> 
                        {
                            Armor armor = armors.get(item.getType());
                            if(armor != null && slots.get(e.getSlot()) == armor.getEquipmentSlot())
                            {
                                e.setCancelled(Util.callEvent(new ArmorEquipEvent((Player)e.getWhoClicked(), item, armor.getEquipmentSlot())).isCancelled());
                            }
                        });
                }
        }
    }
    
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onInventoryDrag(final InventoryDragEvent e)
    {
        if(e.getInventory().getType()==InventoryType.CRAFTING)
        {
            for(Map.Entry<Integer, ItemStack> entry : e.getNewItems().entrySet()) 
            {   
                if(e.isCancelled())
                {
                    return;
                }
                
                Optional.ofNullable(rawSlots.get(entry.getKey())).ifPresent(slot -> 
                {
                    if(Util.callEvent(new ArmorEquipEvent((Player)e.getWhoClicked(), entry.getValue(), slot)).isCancelled())
                    {
                        e.setCancelled(true);
                    }
                });
            }
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onItemDamage(final PlayerItemDamageEvent e)
    {
        ItemStack[] armorArray = e.getPlayer().getInventory().getArmorContents();
        for(int i = 0; i < 4; i++)
        {
            ItemStack item = armorArray[i];
            if(item == null || !item.equals(e.getItem()))
            {
                continue;
            }
            
            EquipmentSlot slot = null;
            switch(i)
            {
                case 0: //Boots
                    slot = EquipmentSlot.FEET;
                    break;
                case 1: //Legs
                    slot = EquipmentSlot.LEGS;
                    break;
                case 2: //Chest
                    slot = EquipmentSlot.CHEST;
                    break;
                case 3: //Head
                    slot = EquipmentSlot.HEAD;
                    break;
            }
            
            if(e.getItem().getDurability() + e.getDamage() >= e.getItem().getType().getMaxDurability())
            {
                e.setCancelled(Util.callEvent(new ArmorUnequipEvent(e.getPlayer(), item, slot, Cause.BREAK)).isCancelled());
            }
            return;
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onPlayerDeath(final PlayerDeathEvent e)
    {
        if(!e.getKeepInventory())
        {
            slots.values().stream().forEach(slot -> 
            {
                ItemStack item=getSlot(e.getEntity().getEquipment(), slot);
                if(item!=null && Util.callEvent(new ArmorUnequipEvent(e.getEntity(), item, slot, Cause.DEATH)).isCancelled())
                {
                    e.getDrops().remove(item);
                    e.getEntity().setMetadata(slot.name(), new FixedMetadataValue(JavaPlugin.getProvidingPlugin(ArmorHandler.class), item));
                }
            });
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onPlayerRespawn(final PlayerRespawnEvent e)
    {
        slots.values().stream().filter(slot -> e.getPlayer().hasMetadata(slot.name())).forEach(slot -> 
        {
            setSlot(e.getPlayer().getInventory(), (ItemStack)e.getPlayer().getMetadata(slot.name()).get(0).value(), slot);
            e.getPlayer().removeMetadata(slot.name(), JavaPlugin.getProvidingPlugin(ArmorHandler.class));
        });
    }
    
    private ItemStack getSlot(final EntityEquipment equip, final EquipmentSlot slot)
    {
        switch(slot)
        {
            case HEAD:
                return equip.getHelmet();
            case CHEST:
                return equip.getChestplate();
            case LEGS:
                return equip.getLeggings();
            case FEET:
                return equip.getBoots();
            default:
                return null;
        }
    }
    
    public void setSlot(final PlayerInventory i, final ItemStack item, final EquipmentSlot slot)
    {
        switch(slot)
        {
            case HEAD:
                i.setHelmet(item);
                break;
            case CHEST:
                i.setChestplate(item);
                break;
            case LEGS:
                i.setLeggings(item);
                break;
            case FEET:
                i.setBoots(item);
                break;
        }
    }
    
    private static boolean isContainer(final Block b)
    {
        return b.getType() == Material.ANVIL || InventoryHolder.class.isInstance(b.getState());
    }
    
    private final static class Armor
    {
        private final int slot;
        private final EquipmentSlot eqSlot;
        public Armor(final int slot, final EquipmentSlot eqSlot)
        {
            this.slot=slot;
            this.eqSlot=eqSlot;
        }
        
        public int getSlot()
        {
            return slot;
        }
        
        public EquipmentSlot getEquipmentSlot()
        {
            return eqSlot;
        }
    }
}
