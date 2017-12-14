/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.placeholders;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Paros
 */
public class ItemPlaceholder 
{
    private final ItemStack item;
    
    private Placeholder displayName;
    private List<Placeholder> lore;
    
    public ItemPlaceholder(final ItemStack item)
    {
        this.item=item;
        
        ItemMeta meta=item.getItemMeta();
        
        if(meta.hasDisplayName())
        {
            displayName=new Placeholder(meta.getDisplayName()).checkPlayer().checkLocation();
        }
        
        if(meta.hasLore())
        {
            lore=meta.getLore().stream().map(Placeholder::new).map(Placeholder::checkPlayer).map(Placeholder::checkLocation).collect(Collectors.toList());
        }
    }
    
    public ItemStack getItem()
    {
        return item.clone();
    }
    
    public ItemStack parse(final Player p, final Location l)
    {
        ItemStack clone=item.clone();
        
        ItemMeta meta=clone.getItemMeta();
        Optional.ofNullable(displayName).ifPresent(holder -> meta.setDisplayName(holder.parse(p, l)));
        
        Optional.ofNullable(lore).ifPresent(lore -> meta.setLore(lore.stream().map(holder -> holder.parse(p, l)).collect(Collectors.toList())));
        clone.setItemMeta(meta);
        
        return clone;
    }
    
    
}
