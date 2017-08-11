/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class ItemDatabase 
{    
    private final Material id;
    public ItemDatabase(final Material id)
    {
        this.id=id;
    }
    
    public ItemDatabase()
    {
        this.id=null;
    }

    private final Map<ItemStack,Object> Item=new HashMap<>();
    private final Map<ItemStack,Object> Egg=new HashMap<>();
    private final Map<PotionItem,Object> Potion=new HashMap<>();
    private final Map<Object,ItemStack> nameMap=new HashMap<>();
    public ItemStack addItem(final Object key,ItemStack item)
    {
        item=item.clone();
        item.setAmount(1);
        switch(item.getType())
        {
            case POTION:
            case SPLASH_POTION:
            case LINGERING_POTION:
            case TIPPED_ARROW:
                PotionMeta pmeta=(PotionMeta)item.getItemMeta();
                Potion.put(new PotionItem(pmeta.hasDisplayName()?pmeta.getDisplayName():new String(),pmeta.hasLore()?pmeta.getLore():new ArrayList<>(),item.getType()), key);
                break;
            case MONSTER_EGG:
                Egg.put(item, key);
                break;
            default:
                Item.put(item, key);
                break;
        }
        nameMap.put(key, item);
        return item;
    }
    
    public void addPath(final ConfigurationSection path){ path.getKeys(false).forEach(name -> addItem(name,Utils.getItemByPath(id,path))); }
    
    public void addConfiguration(final FileConfiguration c){ c.getKeys(false).forEach(name -> addItem(name,Utils.getItemByPath(id,c.getConfigurationSection(name)))); }
    
    public void addMap(final Map<Object,ItemStack> map){ map.keySet().forEach(key -> addItem(key,map.get(key))); }
    
    public void addDatabase(final ItemDatabase id){ addMap(id.getKeyMap()); }
    
    /**
     * 
     * @param item - The item you want the configuration name to be found
     * @return - The item key if found, null othewise
     */
    public Object getKey(final ItemStack item)
    {
        item.setAmount(1);
        switch(item.getType())
        {
            case POTION:
            case SPLASH_POTION:
            case LINGERING_POTION:
            case TIPPED_ARROW:
                PotionMeta pmeta=(PotionMeta)item.getItemMeta();
                PotionItem potion=new PotionItem(pmeta.hasDisplayName()?pmeta.getDisplayName():new String(),pmeta.hasLore()?pmeta.getLore():new ArrayList<>(),item.getType());
                return Potion.entrySet().stream().filter(entry -> entry.getKey().isSimilar(potion)).findFirst().map(Map.Entry::getValue).orElse(null);
            case MONSTER_EGG:
                return Egg.entrySet().stream().filter(entry -> entry.getKey().isSimilar(item)).findFirst().map(Map.Entry::getValue).orElse(null);
            default:
                return Item.get(item);
        }
    }
    
    /**
     * 
     * @param value - The configuration key of the item
     * @return The item associated with the name, null otherwise
     */
    public ItemStack getItem(final Object value){ return nameMap.containsKey(value)?nameMap.get(value).clone():null; }
    
    public Map<Object,ItemStack> getKeyMap(){ return nameMap; }
    
    
    public Map<String,ItemStack> getStringKeyMap()
    {
        return nameMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue()));
    }
    
    private class PotionItem
    {
        private final String name;
        private final List<String> lore;
        private final Material material;
        public PotionItem(final String name, final List<String> lore, final Material material)
        {
            this.name=name;
            this.lore=lore;
            this.material=material;
        }
        
        private ItemStack item;
        public PotionItem setItem(final ItemStack item)
        {
            this.item=item;
            return this;
        }
        public ItemStack getItem() { return item; }
        
        public String getName() { return name; }
        public List<String> getLore() { return lore; }
        public Material getType() { return material; }
        
        public Boolean isSimilar(final Object o)
        {
            if(o.getClass()!=this.getClass()) { return false; }
            return ((PotionItem)o).getType().equals(material) && ((PotionItem)o).getName().equals(name) && ((PotionItem)o).getLore().equals(lore);
        }
    }
}
