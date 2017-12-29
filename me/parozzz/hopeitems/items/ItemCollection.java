/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.hopeitems.items.managers.cooldown.CooldownManager;
import me.parozzz.reflex.placeholders.ItemPlaceholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class ItemCollection 
{
    private final ItemPlaceholder item;
    private final String id;
    private final Map<When, ItemInfo> infos;
    
    public ItemCollection(final String id, final ItemStack item)
    {
        infos = new EnumMap(When.class);
        this.item = new ItemPlaceholder(item);
        this.id = id;
    }
    
    public String getId()
    {
        return id;
    }
    
    public ItemPlaceholder getItem()
    {
        return item;
    }
    
    public ItemInfo getItemInfo(final When when)
    {
        return infos.get(when);
    }
    
    public void setItemInfo(final When when, final ItemInfo info)
    {
        infos.put(when, info);
    }
    
    public boolean hasWhen(final When when)
    {
        return infos.containsKey(when);
    }
    
    public boolean hasAnyWhen(final When... when)
    {
        return Stream.of(when).anyMatch(infos::containsKey);
    }
    
    private boolean enchantable = true;
    public void setEnchantable(final boolean bln)
    {
        enchantable = bln;
    }
    
    public boolean isEnchantable()
    {
        return enchantable;
    }
    
    private CooldownManager cooldown;
    public void setCooldown(final CooldownManager cooldown)
    {
        this.cooldown = cooldown;
    }
    
    public boolean hasCooldown(final Player p)
    {
        return Optional.ofNullable(cooldown).map(cool -> cool.hasCooldown(p)).orElse(false);
    }
}
