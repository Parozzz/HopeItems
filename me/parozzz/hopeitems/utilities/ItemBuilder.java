/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.ItemNBT;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author Paros
 */
public class ItemBuilder 
{
    private final ItemMeta meta;
    private final ItemStack item;
    public ItemBuilder(final Material type)
    {
        item=new ItemStack(type);
        meta=item.getItemMeta();
    }
    
    public ItemBuilder(final ItemStack item)
    {
        this.item=item;
        meta=item.getItemMeta();
    }
    
    public ItemBuilder amount(final int amount)
    {
        item.setAmount(amount);
        return this;
    }
    
    public ItemBuilder data(final short data)
    {
        item.setDurability(data);
        return this;
    }
    
    public ItemBuilder enchantment(final Enchantment ench, final int level)
    {
        item.addUnsafeEnchantment(ench, level);
        return this;
    }
    
    public ItemBuilder name(final String name)
    {
        meta.setDisplayName(name);
        return this;
    }
    
    public ItemBuilder lore(final String... lore)
    {
        meta.setLore(Stream.of(lore).collect(Collectors.toList()));
        return this;
    }
    
    public ItemBuilder lore(final List<String> lore)
    {
        meta.setLore(lore);
        return this;
    }
    
    public ItemBuilder flag(final ItemFlag... flags)
    {
        meta.addItemFlags(flags);
        return this;
    }
    
    public ItemBuilder unbreakable()
    {
        Utils.setUnbreakable(meta, true);
        return this;
    }
    
    public ItemBuilder copyDisplay(final ItemMeta toCopy)
    {
        if(toCopy.hasDisplayName())
        {
            meta.setDisplayName(toCopy.getDisplayName());
        }
        
        if(toCopy.hasLore())
        {
            meta.setLore(toCopy.getLore());
        }
        
        return this;
    }
    
    public ItemStack build()
    {
        item.setItemMeta(meta);
        return item;
    }
    
    public SkullBuilder toSkull()
    {
        item.setItemMeta(meta);
        return new SkullBuilder(item);
    }
    
    public EggBuilder toEgg(final EntityType et)
    {
        item.setItemMeta(meta);
        return new EggBuilder(item, et);
    }
    
    public PotionBuilder toPotion()
    {
        item.setItemMeta(meta);
        return new PotionBuilder(item);
    }
    
    public class PotionBuilder
    {
        private final PotionMeta meta;
        private final ItemStack potion;
        public PotionBuilder(final ItemStack potion)
        {
            meta=(PotionMeta)potion.getItemMeta();
            this.potion=potion;
        }
        
        public PotionBuilder addPotion(final PotionEffect pe)
        {
            meta.addCustomEffect(pe, true);
            return this;
        }
        
        public PotionBuilder setColor(final Color color)
        {
            meta.setColor(color);
            return this;
        }
        
        public ItemStack build()
        {
            potion.setItemMeta(meta);
            return potion;
        }
    }
    
    public class EggBuilder
    {
        private ItemMeta meta;
        private final ItemStack egg;
        public EggBuilder(final ItemStack egg, final EntityType et)
        {
            if(egg.getType()!=Material.MONSTER_EGG)
            {
                throw new IllegalArgumentException("The item in the builder is not a monster egg");
            }
            
            meta=egg.getItemMeta();
            
            if(MCVersion.V1_8.isEqual()) 
            {
                ItemStack item=new SpawnEgg(et).toItemStack(1);
                item.setItemMeta(meta);
                this.egg=item;
            }
            else if(MCVersion.contains(MCVersion.V1_10, MCVersion.V1_9)) 
            { 
                this.egg=ItemNBT.setSpawnedType(egg, et);
                meta=egg.getItemMeta();
            }
            else 
            {
                this.egg=egg;
                ((SpawnEggMeta)meta).setSpawnedType(et);   
            }
        }
        
        public ItemStack build()
        {
            egg.setItemMeta(meta);
            return item;
        }
    }
    
    public class SkullBuilder
    {
        private final SkullMeta meta;
        private final ItemStack skull;
        public SkullBuilder(final ItemStack skull) 
        {
            if(skull.getType()!=Material.SKULL_ITEM && skull.getDurability()!=3)
            {
                throw new IllegalArgumentException("The item in the builder is not a player head");
            }
            this.skull=skull;
            meta=(SkullMeta)item.getItemMeta();
        }
        
        public SkullBuilder owner(final String owner)
        {
            meta.setOwner(owner);
            return this;
        }
        
        public ItemStack build()
        {
            skull.setItemMeta(meta);
            return skull;
        }
        
    }
}
