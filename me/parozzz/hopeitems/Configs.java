/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopeitems.ItemsCommand.CommandEnum;
import me.parozzz.hopeitems.ItemsCommand.CommandMessageEnum;
import me.parozzz.hopeitems.items.CustomItemUtil;
import me.parozzz.hopeitems.items.ItemCollection;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemInfo.ProjectileDamageType;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.hopeitems.items.ItemRegistry;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.shop.Shop.ShopMessage;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.MCVersion;
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

/**
 *
 * @author Paros
 */
public class Configs 
{
    private static final Logger logger = Logger.getLogger(Configs.class.getSimpleName());
    
    private enum RecipeType
    {
        FURNACE, SHAPED, SHAPELESS;
    }
    
    public static boolean overridePvpProtection;
    public static final Map<CommandEnum, String> helpMessages=new EnumMap(CommandEnum.class);
    public static final Map<CommandMessageEnum, String> otherMessages=new EnumMap(CommandMessageEnum.class);
    public static final Map<ShopMessage, String> shopMessages=new EnumMap(ShopMessage.class);
    protected static void initConfig(final FileConfiguration c)
    {
        helpMessages.clear();
        otherMessages.clear();
        shopMessages.clear();
        
        overridePvpProtection = c.getBoolean("overridePvpProtection", false);
        DebugLogger.getLogger().setDebug(c.getBoolean("debug", false));
        
        ConfigurationSection cmPath = c.getConfigurationSection("CommandMessages");
        
        cmPath.getConfigurationSection("Help").getValues(false).forEach((s,o) -> helpMessages.put(CommandEnum.valueOf(s.toUpperCase()), Util.cc((String)o)));
        cmPath.getConfigurationSection("Others").getValues(false).forEach((s,o) -> otherMessages.put(CommandMessageEnum.valueOf(s.toUpperCase()), Util.cc((String)o)));
        
        ConfigurationSection mPath = c.getConfigurationSection("Messages");
        
        mPath.getConfigurationSection("Shop").getValues(false).forEach((s, o) -> shopMessages.put(ShopMessage.valueOf(s.toUpperCase()), Util.cc((String)o)));
    }
    
    protected static void loadItems(final File itemsFolder, final boolean reload)
    {
        HopeItems hopeItems = HopeItems.getInstance();
        
        List<ItemCollection> collectionList = new LinkedList<>(); //Using a list for avoid item lost on reloading. Items won't be updated in a worst case.
        collectionList.addAll(loadFolderCollection(hopeItems, reload, itemsFolder));
        
        ItemRegistry.clearCollections();
        collectionList.forEach(ItemRegistry::addCollection);
    }
    
    private static List<ItemCollection> loadFolderCollection(final HopeItems hopeItems, final boolean reload, final File folder)
    {
        List<ItemCollection> collectionList = new ArrayList<>();
        Stream.of(folder.listFiles()).forEach(file -> 
        {
            if(file.isDirectory())
            {
                collectionList.addAll(loadFolderCollection(hopeItems, reload, file));
                return;
            }
            
            String id = file.getName().replace(".yml", "");
            FileConfiguration config = Util.loadUTF(file);
            if(config == null)
            {
                logger.log(Level.SEVERE, "An error occoured while parsing YAML file for {0}. Skipping.", id);
                return;
            }
            
            if(!config.contains("Item"))
            {
                logger.log(Level.WARNING, "The Item for {0} could not be found. Skipping.", id);
                return;
            }
            else if(!config.isConfigurationSection("Item"))
            {
                logger.log(Level.WARNING, "The Item for {0} is using a wrong format (Not a section). Skipping.", id);
                return;
            }
            
            ItemStack itemStack = ItemUtil.getItemByPath(config.getConfigurationSection("Item"));
            if(itemStack == null)
            {
                logger.log(Level.WARNING, "An error occoured while parsing the ItemStack of {0}. Skipping.", id);
                return;
            }
            
            ItemCollection collection = new ItemCollection(id, itemStack);
            collection.setEnchantable(config.getBoolean("enchantable", true));
            
            Optional.ofNullable(config.getConfigurationSection("Cooldown"))
                    .map(hopeItems.getCooldownParser()::parse)
                    .ifPresent(collection::setCooldown);
            
            config.getKeys(false).stream().filter(key -> !Util.or(key.toLowerCase(), "enchantable", "item", "crafting", "cooldown")).map(config::getConfigurationSection).forEach(path -> 
            {
                String pathName = path.getName();
                Set<When> whens = pathName.equalsIgnoreCase("all") ? 
                        EnumSet.allOf(When.class) : 
                        Stream.of(pathName.split(",")).map(str -> Debug.validateEnum(str, When.class)).collect(Collectors.toSet());
                
                ItemInfo info = new ItemInfo(collection, whens);
                Util.ifCheck(path.contains("chance"), () -> info.setChance(path.getDouble("chance")));
                if(path.contains("projectileDamageType"))
                {
                    ProjectileDamageType projectileDamageType;
                    try {
                        projectileDamageType = ProjectileDamageType.valueOf(path.getString("projectileDamageType").toUpperCase());
                        info.setProjectileDamageType(projectileDamageType);
                    } catch(final IllegalArgumentException ex) {
                        logger.log(Level.WARNING, "The projectileDamageType named {0} does not exist. Skipping.", path.getString("projectileDamageType"));
                    }
                }
                
                whens.forEach(w -> collection.setItemInfo(w, info));
                
                info.removeOnUse = path.getBoolean("removeOnUse", false);
                
                Optional.ofNullable(path.getConfigurationSection("Condition"))
                        .map(hopeItems.getConditionParser()::parse)
                        .ifPresent(info::setConditionManager);

                Optional.ofNullable(path.getConfigurationSection("Action"))
                        .map(hopeItems.getActionParser()::parse)
                        .ifPresent(info::setActionManager);

                Optional.ofNullable(path.getConfigurationSection("Mob"))
                        .map(hopeItems.getMobParser()::parse)
                        .ifPresent(info::setMobManager);
                
                Optional.ofNullable(path.getConfigurationSection("Explosive")).ifPresent(ePath -> info.setExplosiveManager(new ExplosiveManager(ePath)));
                Optional.ofNullable(path.getConfigurationSection("Lucky")).ifPresent(lPath -> info.setLuckyManager(new LuckyManager(lPath)));
                
            });
            
            CustomItemUtil.addCustomTag(itemStack, id);
            collectionList.add(collection);
            
            Optional.ofNullable(config.getConfigurationSection("Crafting")).filter(path -> !reload).ifPresent(path -> 
            {
                if(!path.contains("type") || !path.isString("type"))
                {
                    logger.log(Level.WARNING, "The recipe type is not set in {0}. Skipping.", id);
                    return;
                }
                
                RecipeType type;
                try {
                    type = RecipeType.valueOf(path.getString("type").toUpperCase());
                } catch(final IllegalArgumentException ex) {
                    logger.log(Level.WARNING, "A recipe type named {0} does not exists. Skipping.", path.get("type"));
                    return;
                }
                
                try {
                    Recipe r;
                    switch(type)
                    {
                        case SHAPED:
                            r = MCVersion.V1_12.isHigher() 
                                    ? new ShapedRecipe(new NamespacedKey(hopeItems, id), itemStack) 
                                    : new ShapedRecipe(itemStack);

                            ((ShapedRecipe)r).shape(path.getStringList("shape").stream().map(String::toUpperCase).toArray(String[]::new));

                            path.getConfigurationSection("material").getValues(false).forEach((s, o) -> 
                            {
                                String[] itemData=o.toString().split(":");
                                ((ShapedRecipe)r).setIngredient(s.toUpperCase().charAt(0), new ItemStack(Material.valueOf(itemData[0].toUpperCase()), 1, Byte.valueOf(itemData[1])).getData());
                            });
                            break;
                        case SHAPELESS:
                            r = MCVersion.V1_12.isHigher() 
                                    ?  new ShapelessRecipe(new NamespacedKey(hopeItems, id), itemStack) 
                                    :  new ShapelessRecipe(itemStack);

                            path.getStringList("material").forEach(str -> 
                            {
                                String[] itemData=str.split(":");

                                ItemStack toUse = new ItemStack(Material.valueOf(itemData[0].toUpperCase()), 1, Byte.valueOf(itemData[1]));
                                int amount = Integer.valueOf(itemData[2]);

                                ((ShapelessRecipe)r).addIngredient(amount, toUse.getData());
                            });
                            break;
                        case FURNACE:
                            String[] itemData = path.getString("source").split(":");

                            ItemStack source = new ItemStack(Material.valueOf(itemData[0].toUpperCase()), 1, Byte.valueOf(itemData[1]));
                            r = new FurnaceRecipe(itemStack, source.getData());
                            break;
                        default:
                            r = null;
                    }
                    Bukkit.addRecipe(r); 
                } catch(final Exception ex) {
                    logger.log(Level.WARNING, "An error occoured while loading recipes for " + id + ". Skipping.", ex);
                }
            });
        });
        return collectionList;
    }
}