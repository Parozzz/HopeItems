/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import me.parozzz.hopeitems.ItemsCommand.CommandEnum;
import me.parozzz.hopeitems.ItemsCommand.CommandMessageEnum;
import me.parozzz.hopeitems.items.managers.actions.ActionType;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.hopeitems.items.ItemUtils;
import me.parozzz.hopeitems.items.managers.conditions.ConditionType;
import me.parozzz.hopeitems.items.managers.cooldown.CooldownManager;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.mobs.MobManager;
import me.parozzz.hopeitems.shop.Shop.ShopMessage;
import me.parozzz.hopeitems.utilities.Debug;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.classes.ComplexMapList;
import me.parozzz.hopeitems.utilities.classes.SimpleMapList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class Configs 
{
    private enum RecipeType
    {
        FURNACE, SHAPED, SHAPELESS;
    }
    
    public static final Map<CommandEnum, String> helpMessages=new EnumMap(CommandEnum.class);
    public static final Map<CommandMessageEnum, String> otherMessages=new EnumMap(CommandMessageEnum.class);
    public static final Map<ShopMessage, String> shopMessages=new EnumMap(ShopMessage.class);
    protected static void initConfig(final FileConfiguration c)
    {
        ConfigurationSection cmPath=c.getConfigurationSection("CommandMessages");
        
        cmPath.getConfigurationSection("Help").getValues(false).forEach((s,o) -> helpMessages.put(CommandEnum.valueOf(s.toUpperCase()), Utils.color((String)o)));
        cmPath.getConfigurationSection("Others").getValues(false).forEach((s,o) -> otherMessages.put(CommandMessageEnum.valueOf(s.toUpperCase()), Utils.color((String)o)));
        
        ConfigurationSection mPath=c.getConfigurationSection("Messages");
        
        mPath.getConfigurationSection("Shop").getValues(false).forEach((s, o) -> shopMessages.put(ShopMessage.valueOf(s.toUpperCase()), Utils.color((String)o)));
    }
    
    private static final Map<String, ItemInfo> items=new HashMap<>();
    protected static void initItems(final FileConfiguration c, final boolean reload)
    {
        c.getKeys(false).stream().map(c::getConfigurationSection).forEach(nPath -> 
        {
            String name=nPath.getName();
            ItemStack item=ItemUtils.addCustomTag(Utils.getItemByPath(nPath.getConfigurationSection("Item")), name);
            
            ItemInfo info=new ItemInfo(name, item);
            
            info.removeOnUse=nPath.getBoolean("removeOnUse", false);
            Optional.ofNullable(nPath.getConfigurationSection("Cooldown")).map(CooldownManager::new).ifPresent(info::setCooldown);
            nPath.getStringList("when").stream().map(String::toUpperCase).map(When::valueOf).forEach(info::addWhen);
            Optional.ofNullable(nPath.getConfigurationSection("Condition")).ifPresent(cPath -> 
            {
                cPath.getValues(false).forEach((condition, list) -> 
                {
                    ConditionType ct=Debug.validateEnum(condition, ConditionType.class);
                    
                    List<String[]> cList=((List<Map<?, ?>>)list).stream()
                            .map(Map::entrySet)
                            .flatMap(Set::stream)
                            .map(e -> new String[] { e.getKey().toString().toLowerCase() , e.getValue().toString() })
                            .collect(Collectors.toList());
                    
                    info.addConditionManager(ct.getConditionManager(new ComplexMapList((List<Map<?, ?>>)list)));
                });
            });
            
            Optional.ofNullable(nPath.getConfigurationSection("Action")).ifPresent(aPath -> 
            {
                aPath.getValues(false).forEach((action, list) -> 
                {
                    ActionType at=Debug.validateEnum(action, ActionType.class);
                    info.addActionManager(at.getActionManager(new SimpleMapList((List<Map<? , ?>>)list)));
                });
            });
            
            Optional.ofNullable(nPath.getConfigurationSection("Mob")).ifPresent(mPath -> info.setMobManager(new MobManager(info, mPath)));
            Optional.ofNullable(nPath.getConfigurationSection("Explosive")).ifPresent(ePath -> info.setExplosiveManager(new ExplosiveManager(ePath)));
            Optional.ofNullable(nPath.getConfigurationSection("Lucky")).ifPresent(lPath -> info.setLuckyManager(new LuckyManager(lPath)));
            items.put(name.toLowerCase(), info);
            
            Optional.ofNullable(nPath.getConfigurationSection("Crafting")).filter(path -> !reload).ifPresent(cPath -> 
            {
                Recipe r;
                switch(Debug.validateEnum(cPath.getString("type"), RecipeType.class))
                {
                    case SHAPED:
                        r= MCVersion.V1_12.isHigher() ? 
                                new ShapedRecipe(new NamespacedKey(JavaPlugin.getProvidingPlugin(Configs.class), name), item) : 
                                new ShapedRecipe(item);
                        
                        ((ShapedRecipe)r).shape(cPath.getStringList("shaped").stream().map(String::toUpperCase).toArray(String[]::new));
                        
                        cPath.getConfigurationSection("material").getValues(false).forEach((s, o) -> 
                        {
                            String[] itemData=o.toString().split(":");
                            ((ShapedRecipe)r).setIngredient(s.toUpperCase().charAt(0), new ItemStack(Material.valueOf(itemData[0].toUpperCase()), 1, Byte.valueOf(itemData[1])).getData());
                        });
                        break;
                    case SHAPELESS:
                        r=MCVersion.V1_12.isHigher() ? 
                                new ShapelessRecipe(new NamespacedKey(JavaPlugin.getProvidingPlugin(Configs.class), name), item) : 
                                new ShapelessRecipe(item);
                        
                        cPath.getStringList("material").forEach(str -> 
                        {
                            String[] itemData=str.split(":");
                            
                            ItemStack toUse=new ItemStack(Material.valueOf(itemData[0].toUpperCase()), 1, Byte.valueOf(itemData[1]));
                            int amount=Integer.valueOf(itemData[2]);
                            
                            ((ShapelessRecipe)r).addIngredient(amount, toUse.getData());
                        });
                        break;
                    case FURNACE:
                        String[] itemData=cPath.getString("source").split(":");
                        
                        ItemStack source=new ItemStack(Material.valueOf(itemData[0].toUpperCase()), 1, Byte.valueOf(itemData[1]));
                        r=new FurnaceRecipe(item, source.getData());
                        break;
                    default:
                        r=null;
                }
                Bukkit.addRecipe(r);
            });
        });
    }
    
    public static ItemInfo getItemInfo(final String name)
    {
        return items.get(name.toLowerCase());
    }
    
    public static Set<String> getItemNames()
    {
        return items.keySet();
    }
}