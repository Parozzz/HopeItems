/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.core;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Paros
 */
public class ItemBuilder {
    
    private final ItemStack item;
    private final ItemMeta meta;
    public ItemBuilder(final Material material)
    {
        item=new ItemStack(material);
        meta=item.getItemMeta();
    }
    
    public ItemBuilder(final ItemStack item)
    {
        this.item=item;
        meta=item.getItemMeta();
    }
    
    public ItemBuilder setName(final String name)
    {
        if(name==null) { return this; }
        meta.setDisplayName(Utils.color(name));
        return this;
    }
    
    public ItemBuilder addLore(final String value)
    {
        if(value==null) { return this; }
        List<String> lore=meta.hasLore()?meta.getLore():new ArrayList<>();
        lore.add(Utils.color(value));
        meta.setLore(lore);
        return this;
    }
    
    public ItemBuilder setLore(final List<String> list)
    {
        if(list==null) { return this; }
        meta.setLore(list);
        return this;
    }
    
    public ItemBuilder addFlag(final ItemFlag... flag)
    {
        meta.addItemFlags(flag);
        return this;
    }
    
    public ItemBuilder addEnchantment(final Enchantment ench, final Integer level)
    {
        
        meta.addEnchant(ench, level, true);
        return this;
    }
    
    public ItemBuilder setData(final Short data)
    {
        item.setDurability(data);
        return this;
    }
    
    public ItemBuilder setAmount(final Integer amount)
    {
        item.setAmount(amount);
        return this;
    }
    
    public ItemBuilder setUnbreakable()
    {
        meta.setUnbreakable(true);
        return this;
    }
    
    public ItemStack build()
    {
        item.setItemMeta(meta);
        return item;
    }
}
