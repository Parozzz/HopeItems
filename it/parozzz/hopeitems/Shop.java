/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems;

import it.parozzz.hopeitems.core.Dependency;
import it.parozzz.hopeitems.core.ItemBuilder;
import it.parozzz.hopeitems.core.ItemDatabase;
import it.parozzz.hopeitems.core.Language;
import it.parozzz.hopeitems.core.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class Shop 
        implements Listener
{
    
    @EventHandler(ignoreCancelled=false,priority=EventPriority.MONITOR)
    private void onClick(final InventoryClickEvent e)
    {
        if(GUIs.contains(e.getInventory()))
        {
            e.setCancelled(true);
            if(!e.getInventory().equals(e.getClickedInventory())) { return; }
            int index;
            switch(e.getSlot())
            {
                case 45:
                    index=GUIs.indexOf(e.getInventory());
                    if(index>=1) 
                    {
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().openInventory(GUIs.get(index-1));
                    }
                    break;
                case 53:
                    index=GUIs.indexOf(e.getInventory());
                    if(GUIs.size()>index+1) 
                    {
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().openInventory(GUIs.get(index+1));
                    }
                    break;
                default:
                    if(e.getCurrentItem().getType().equals(Material.AIR)) { return; }
                    GUIManager manager=(GUIManager)id.getKey(e.getCurrentItem());
                    this.buy((Player)e.getWhoClicked(), manager.getItem(), manager.getCost());
                    break;
            }
        }
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onSignChange(final SignChangeEvent e)
    {
        if(sign.containsKey(e.getLine(0))) 
        {
            if(!e.getPlayer().hasPermission(Permission.shop_sign)) 
            {           
                Language.sendMessage(e.getPlayer(), "noPermission");
                e.setCancelled(true);   
            }
            else { Language.sendMessage(e.getPlayer(), "onSignShopSetup"); }
        }
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onSignInteract(final PlayerInteractEvent e)
    {
        if(e.getClickedBlock().getState() instanceof Sign 
                && sign.containsKey(((Sign)e.getClickedBlock().getState()).getLine(0)) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) 
        {
            GUIManager manager=sign.get(((Sign)e.getClickedBlock().getState()).getLine(0));
            this.buy(e.getPlayer(), manager.getItem(), manager.getCost());
            e.setCancelled(true); 
        }
    }
    
    private final ItemDatabase id;
    public Shop()
    {  
        id=new ItemDatabase();
        
        if(!Dependency.isEconomyHooked()) 
        { 
            throw new UnknownDependencyException("Vault needed for shop features!");  
        }
    }
    
    private ItemStack nextItem;
    private ItemStack backItem;
    private String name;
    private String costLine;
    
    private final List<Inventory> GUIs=new ArrayList<>();
    
    public Shop parse(final ConfigurationSection path)
    {
        name=Utils.color(path.getString("guiTitle"));
        nextItem=Utils.getItemByPath(path.getConfigurationSection("guiNextItem"));
        backItem=Utils.getItemByPath(path.getConfigurationSection("guiBackItem"));
        costLine=Utils.color(path.getString("costLine"));
        
        GUIs.add(0, this.createNewInventory(1,null));
        
        return this;
    }
    
    private final Map<String,GUIManager> sign=new HashMap<>();
    public void addItem(final ItemStack key, final String sign, final Double cost)
    {
        ItemStack marker=new ItemBuilder(key.clone()).addLore(costLine.replace("%cost%", cost.toString())).build();
        GUIManager manager=new GUIManager(cost,key);
        if(sign!=null) 
        { 
            this.sign.put(sign, manager); 
        }
        id.addItem(manager, marker);
        for(Integer j=0;j<GUIs.size();j++)
        {
            if(!GUIs.get(j).addItem(marker).isEmpty() && j==GUIs.size()-1) { GUIs.add(this.createNewInventory(j+2,marker)); }
        }
        
    }
    
    public void openShop(final Player p) { p.openInventory(GUIs.get(0)); }
    
    private Inventory createNewInventory(final Integer page, final ItemStack item)
    {
        Inventory i=Bukkit.createInventory(null, 54, name.replace("%page%", page.toString()));
        i.setItem(45, backItem);
        i.setItem(53, nextItem);
        if(item!=null) { i.addItem(item); }
        return i;
    }
    
    private void buy(final Player p, final ItemStack item, final Double cost)
    {
        if(!Dependency.getEconomy().withdrawPlayer(p, cost).transactionSuccess()) 
        {  
            Language.sendParsedMessage(p, "notEnoughMoney", "%cost%",cost.toString());
            return;
        }
        
        p.sendMessage(Language.getString("onBuy").replace("%cost%", cost.toString()).replace("%balance%", Double.toString(Dependency.getEconomy().getBalance(p))));
        
        if(!p.getInventory().addItem(item).isEmpty())
        {
            Language.sendMessage(p, "inventoryFull");
            p.getLocation().getWorld().dropItem(p.getLocation(), item);
        }
    }
    
    private class GUIManager 
    {
        private final double cost;
        private final ItemStack item;
        public GUIManager(final double cost, final ItemStack item)
        {
            this.cost=cost;
            this.item=item;
        }

        public double getCost() { return cost; }
        public ItemStack getItem() { return item.clone(); }
    }
    
}
