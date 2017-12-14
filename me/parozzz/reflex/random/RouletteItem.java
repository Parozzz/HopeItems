/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.random;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import me.parozzz.reflex.NMS.itemStack.ItemNBT;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class RouletteItem 
{
    private static boolean REGISTERED = false;
    public static void registerListener()
    {
        REGISTERED = true;
        Bukkit.getPluginManager().registerEvents(new Listener()
        {
            @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
            private void onInteract(final PlayerInteractEvent e)
            {
                Optional.ofNullable(e.getItem()).map(RouletteItem::new).filter(RouletteItem::isValid).ifPresent(random -> 
                {
                    if(e.getItem().getAmount() > 1)
                    {
                        ItemStack giveBack = e.getItem().clone();
                        giveBack.setAmount(giveBack.getAmount() - 1);
                        if(e.getPlayer().getInventory().firstEmpty() != -1)
                        {
                            e.getPlayer().getInventory().setItem(e.getPlayer().getInventory().firstEmpty(), giveBack);
                            e.getItem().setAmount(1);
                            random.start(e.getPlayer());
                        }
                    }
                    else
                    {
                        random.start(e.getPlayer());
                    }
                    
                    e.setCancelled(true);
                });
            }
        }, JavaPlugin.getProvidingPlugin(RouletteItem.class));
    }
    
    public static final String COMPOUND = JavaPlugin.getProvidingPlugin(RouletteItem.class).getName() + "Utilities.Random";
    public static final String UID = JavaPlugin.getProvidingPlugin(RouletteItem.class).getName() + "Utilities.UUID";
    
    private final ItemStack item;
    
    private final NBTCompound tag;
    public RouletteItem(final ItemStack item)
    {
        Util.ifCheck(!REGISTERED, () -> registerListener());
        
        this.item = item;
        tag = new ItemNBT(item).getTag();
    }
    
    public boolean isValid()
    {
        return tag.hasKey(COMPOUND);
    }
    
    public void start(final Player p)
    {
        NBTCompound compound = tag.getCompound(COMPOUND);
        
        UUID u = UUID.randomUUID();

        new RouletteInstance(p, u, item, compound.keySet().stream().map(compound::getCompound).flatMap(random -> 
        {
            ItemNBT nbt = new ItemNBT(random.getCompound(ITEM));
            nbt.getTag().setString(UID, u.toString());
            return Collections.nCopies(random.getInt(CHANCE), nbt.getBukkitItem()).stream();
        }).collect(Collectors.toList()));
    }
    
    private static final String CHANCE = "Chance";
    private static final String ITEM = "Item";
    
    public static ItemStack createRandom(final ItemStack item, final Map<ItemStack, Integer> map)
    {
        ItemNBT nbt = new ItemNBT(item);
        NBTCompound tag = nbt.getTag();
        
        NBTCompound randomCompound = new NBTCompound();
        int key = 0;
        for(Map.Entry<ItemStack, Integer> e : map.entrySet())
        {
            NBTCompound compound = new NBTCompound();
            compound.setInt(CHANCE, e.getValue());
            
            compound.setTag(ITEM,  new ItemNBT(e.getKey()).convertToNBT());
            
            randomCompound.setTag(Objects.toString(key++), compound);
        }
        
        //tag.setValue(UID, NBTType.STRING, u.toString());
        tag.setTag(COMPOUND, randomCompound);
        
        return nbt.getBukkitItem();
    }
}
