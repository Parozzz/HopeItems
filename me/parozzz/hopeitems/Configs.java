/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems;

import java.io.File;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopeitems.ItemsCommand.CommandEnum;
import me.parozzz.hopeitems.ItemsCommand.CommandMessageEnum;
import me.parozzz.hopeitems.items.CustomItemUtil;
import me.parozzz.hopeitems.items.ItemCollection;
import me.parozzz.hopeitems.items.managers.actions.ActionType;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.hopeitems.items.ItemRegistry;
import me.parozzz.hopeitems.items.managers.conditions.ConditionType;
import me.parozzz.hopeitems.items.managers.cooldown.CooldownManager;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.mobs.MobManager;
import me.parozzz.hopeitems.shop.Shop.ShopMessage;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.classes.ComplexMapList;
import me.parozzz.reflex.classes.SimpleMapList;
import me.parozzz.reflex.utilities.ItemUtil;
import me.parozzz.reflex.utilities.Util;
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
        helpMessages.clear();
        otherMessages.clear();
        shopMessages.clear();
        
        ConfigurationSection cmPath=c.getConfigurationSection("CommandMessages");
        
        cmPath.getConfigurationSection("Help").getValues(false).forEach((s,o) -> helpMessages.put(CommandEnum.valueOf(s.toUpperCase()), Util.cc((String)o)));
        cmPath.getConfigurationSection("Others").getValues(false).forEach((s,o) -> otherMessages.put(CommandMessageEnum.valueOf(s.toUpperCase()), Util.cc((String)o)));
        
        ConfigurationSection mPath = c.getConfigurationSection("Messages");
        
        mPath.getConfigurationSection("Shop").getValues(false).forEach((s, o) -> shopMessages.put(ShopMessage.valueOf(s.toUpperCase()), Util.cc((String)o)));
    }
    
    private static final Map<String, ItemInfo> items=new HashMap<>();
    protected static void loadItems(final File[] files, final boolean reload)
    {
        items.clear();
        Stream.of(files).forEach(file -> 
        {
            String id = file.getName().replace(".yml", "");
            
            FileConfiguration config = Util.loadUTF(file);
            
            ItemStack item = ItemUtil.getItemByPath(config.getConfigurationSection("Item"));
            
            ItemCollection collection = new ItemCollection(id, item);
            collection.setEnchantable(config.getBoolean("enchantable", true));
            
            Optional.ofNullable(config.getConfigurationSection("Cooldown")).map(CooldownManager::new).ifPresent(collection::setCooldown);
            
            config.getKeys(false).stream().filter(key -> !Util.or(key.toLowerCase(), "enchantable", "item", "crafting", "cooldown")).map(config::getConfigurationSection).forEach(path -> 
            {
                String pathName = path.getName();
                Set<When> whens = pathName.equalsIgnoreCase("all") ? 
                        EnumSet.allOf(When.class) : 
                        Stream.of(pathName.split(",")).map(str -> Debug.validateEnum(str, When.class)).collect(Collectors.toSet());
                
                ItemInfo info = new ItemInfo(collection, whens);
                Util.ifCheck(path.contains("chance"), () -> info.setChance(path.getDouble("chance")));
                
                whens.forEach(w -> collection.setItemInfo(w, info));
                
                info.removeOnUse = path.getBoolean("removeOnUse", false);
                Optional.ofNullable(path.getConfigurationSection("Condition")).ifPresent(cPath -> 
                {
                    cPath.getValues(false).forEach((condition, list) -> 
                    {
                        info.addConditionManager(Debug.validateEnum(condition, ConditionType.class).getConditionManager(new ComplexMapList((List<Map<?, ?>>)list)));
                    });
                });

                Optional.ofNullable(path.getConfigurationSection("Action")).ifPresent(aPath -> 
                {
                    aPath.getValues(false).forEach((action, list) -> 
                    {
                        info.addActionManager(Debug.validateEnum(action, ActionType.class).getActionManager(new SimpleMapList((List<Map<? , ?>>)list)));
                    });
                });

                Optional.ofNullable(path.getConfigurationSection("Mob")).ifPresent(mPath -> info.setMobManager(new MobManager(info, mPath)));
                Optional.ofNullable(path.getConfigurationSection("Explosive")).ifPresent(ePath -> info.setExplosiveManager(new ExplosiveManager(ePath)));
                Optional.ofNullable(path.getConfigurationSection("Lucky")).ifPresent(lPath -> info.setLuckyManager(new LuckyManager(lPath)));
                
            });
            
            CustomItemUtil.addCustomTag(item, id);
            ItemRegistry.addCollection(collection);
            
            Optional.ofNullable(config.getConfigurationSection("Crafting")).filter(path -> !reload).ifPresent(path -> 
            {
                Recipe r;
                switch(Debug.validateEnum(path.getString("type"), RecipeType.class))
                {
                    case SHAPED:
                        r = MCVersion.V1_12.isHigher() ? 
                                new ShapedRecipe(new NamespacedKey(JavaPlugin.getProvidingPlugin(Configs.class), id), item) : 
                                new ShapedRecipe(item);
                        
                        ((ShapedRecipe)r).shape(path.getStringList("shape").stream().map(String::toUpperCase).toArray(String[]::new));
                        
                        path.getConfigurationSection("material").getValues(false).forEach((s, o) -> 
                        {
                            String[] itemData=o.toString().split(":");
                            ((ShapedRecipe)r).setIngredient(s.toUpperCase().charAt(0), new ItemStack(Material.valueOf(itemData[0].toUpperCase()), 1, Byte.valueOf(itemData[1])).getData());
                        });
                        break;
                    case SHAPELESS:
                        r=MCVersion.V1_12.isHigher() ? 
                                new ShapelessRecipe(new NamespacedKey(JavaPlugin.getProvidingPlugin(Configs.class), id), item) : 
                                new ShapelessRecipe(item);
                        
                        path.getStringList("material").forEach(str -> 
                        {
                            String[] itemData=str.split(":");
                            
                            ItemStack toUse=new ItemStack(Material.valueOf(itemData[0].toUpperCase()), 1, Byte.valueOf(itemData[1]));
                            int amount=Integer.valueOf(itemData[2]);
                            
                            ((ShapelessRecipe)r).addIngredient(amount, toUse.getData());
                        });
                        break;
                    case FURNACE:
                        String[] itemData = path.getString("source").split(":");
                        
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
}