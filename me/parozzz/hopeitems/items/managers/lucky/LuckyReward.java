/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.lucky;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import me.parozzz.hopeitems.Configs;
import me.parozzz.hopeitems.items.ItemCollection;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemRegistry;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class LuckyReward 
{
    private final ItemStack preview;
    private final Set<String> items;
    private final int chance;
    public LuckyReward(final ConfigurationSection path)
    {
        preview=Optional.ofNullable(path.getConfigurationSection("Preview")).map(ItemUtil::getItemByPath).orElse(null);
        items=path.getStringList("items").stream().collect(Collectors.toSet());
        chance=path.getInt("chance", 10);
    }
    
    public ItemStack getPreview()
    {
        return preview.clone();
    }
    
    public Set<ItemCollection> getItems()
    {
        return items.stream().map(ItemRegistry::getCollection).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    public int getChance()
    {
        return chance;
    }
    
}
