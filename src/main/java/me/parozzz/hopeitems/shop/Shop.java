/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.shop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopeitems.Configs;
import me.parozzz.hopeitems.HopeItems;
import me.parozzz.hopeitems.shop.ShopPage.ShopHolder;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class Shop 
{
    public enum ShopMessage
    {
        LOW_MONEY, ITEM_BOUGHT;
        
        public String getMessage()
        {
            return Configs.shopMessages.get(this);
        }
    }
    
    private static Shop instance;
    public static Shop getInstance()
    {
        return Optional.ofNullable(instance).orElseGet(() -> instance=new Shop());
    }
    
    private final Map<String, ShopPage> pages;
    private Shop()
    {
        pages=new HashMap<>();
    }
    
    public void loadConfig(final HopeItems hopeItems)
    {
        pages.clear();
        try {
            FileConfiguration c = Util.fileStartup(HopeItems.getInstance(), new File(hopeItems.getDataFolder(), "shop.yml"));
            
            c.getKeys(false).stream().map(c::getConfigurationSection).forEach(pPath -> 
            {
                String pageName = pPath.getName();
                
                ShopPage page = new ShopPage(pageName, pPath);
                pages.put(pageName.toLowerCase(), page);
            });
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ShopPage getPageByName(final String name)
    {
        return pages.get(name.toLowerCase());
    }
    
    public Set<String> pageNames()
    {
        return Collections.unmodifiableSet(pages.keySet());
    }
    
    public static Listener getListener()
    {
        return new Listener()
        {
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onInventoryClick(final InventoryClickEvent e)
            {
                if(e.getInventory().getHolder() instanceof ShopHolder)
                {
                    ((ShopHolder)e.getInventory().getHolder()).getShop().onEvent(e);
                }
            }
        };
    }
}
