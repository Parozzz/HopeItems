/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import me.parozzz.hopeitems.items.BlockManager;
import me.parozzz.hopeitems.items.listener.ItemListener;
import me.parozzz.hopeitems.items.managers.IParser;
import me.parozzz.hopeitems.items.managers.actions.parsers.ActionParser;
import me.parozzz.hopeitems.items.managers.conditions.parsers.ConditionParser;
import me.parozzz.hopeitems.items.managers.cooldown.parser.CooldownParser;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.mobs.parsers.MobManager;
import me.parozzz.hopeitems.items.managers.mobs.abilities.parser.MobAbilityParser;
import me.parozzz.hopeitems.items.managers.mobs.parsers.MobParser;
import me.parozzz.hopeitems.shop.Shop;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.ReflexAPI;
import me.parozzz.reflex.ReflexAPI.Property;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class HopeItems extends JavaPlugin
{
    private static final Logger logger = Logger.getLogger(HopeItems.class.getSimpleName());
    public static HopeItems getInstance()
    {
        return JavaPlugin.getPlugin(HopeItems.class);
    }
    
    
    private ConditionParser conditionParser;
    private ActionParser actionParser;
    private MobParser mobParser;
    private CooldownParser cooldownParser;
    @Override
    public void onLoad()
    {
        conditionParser = new ConditionParser();
        actionParser = new ActionParser();
        mobParser = new MobParser();
        cooldownParser = new CooldownParser();
        
        if(Dependency.setupEconomy())
        {
            logger.log(Level.INFO, "Hooked into Vault");
        }
        
        if(Dependency.setupWorldGuard())
        {
            logger.log(Level.INFO, "Hooked into WorldGuard");
        }
        
        Stream.of(conditionParser, actionParser, mobParser, conditionParser).forEach(IParser::registerDefaultSpecificParsers);
    }
    
    public @Nullable ConditionParser getConditionParser()
    {
        this.validateParser(conditionParser);
        return conditionParser;
    }
    
    public @Nullable ActionParser getActionParser()
    {
        this.validateParser(actionParser);
        return actionParser;
    }
    
    public @Nullable MobParser getMobParser()
    {
        this.validateParser(mobParser);  
        return mobParser;
    }
    
    public @Nullable CooldownParser getCooldownParser()
    {
        this.validateParser(cooldownParser);  
        return cooldownParser;
    }
    
    private void validateParser(final IParser parser)
    {
        if(parser == null)
        {
            throw new RuntimeException("Trying to access HopeItems class before loading");
        }
    }
    private final File dataFile = new File(this.getDataFolder(), "data.yml");
    @Override
    public void onEnable()
    {
        ReflexAPI.getAPI().addProperty(Property.ENTITYPLAYER_LISTENER, Property.ARMOREVENTS_LISTENER);
        
        try {
            this.saveDefaultConfig();
            if(getConfig().getBoolean("metric", true))
            {
                new MetricsLite(this);
            }
            
            Configs.initConfig(getConfig());
            loadItems(false);
            
            MobManager.registerListener();
            ExplosiveManager.registerListener();
            LuckyManager.registerListener();
            
            dataFile.createNewFile();
            FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
            BlockManager.getInstance().loadBlocks(data);
            MobManager.loadData(data);
            
            if(Dependency.isEconomyHooked())
            {
                Shop.getInstance().loadConfig();
                Shop.registerListener();
            }
            
            Util.ifCheck(MCVersion.V1_8.isEqual(), () -> Util.registerArmorStandInvicibleListener());
            
            Bukkit.getPluginManager().registerEvents(new ItemListener(this), this);
            Util.ifCheck(MCVersion.V1_9.isHigher(), () -> ItemListener.register1_9Listener(this));
            
            ItemsCommand itemsCommand = new ItemsCommand();
            PluginCommand command = this.getCommand("items");
            command.setExecutor(itemsCommand);
            command.setTabCompleter(itemsCommand.getTabCompleter());
            
        }
        catch(final Exception t) {
            logger.log(Level.SEVERE, "Problem loading the plugin. Disabling it", t);
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    public void reloadConfigurations()
    {
        FileConfiguration data = new YamlConfiguration();
        BlockManager.getInstance().saveBlocks(data);
        MobManager.saveData(data);
        try  {
            data.save(dataFile);
        } 
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Something went wrong during data saving", ex);
        }
        
        this.reloadConfig();
        Configs.initConfig(this.getConfig());
        this.loadItems(true);
        
        Shop.getInstance().loadConfig();
        BlockManager.getInstance().loadBlocks(data);
        MobManager.loadData(data);
    }
    
    @Override
    public void saveDefaultConfig()
    {
        super.saveDefaultConfig();
        
        new File(this.getDataFolder(), "items").mkdir();
        if(getConfig().getBoolean("savedDefault", false))
        {
            return; 
        }
        
        Stream.of("ExampleItem", "ExplodingDiamond", "GiftHead", "ScammyEnderpearl", "HelmetJump", "HastePick", "PoisonousSnowball", "SpawnSnowball", "TeleportArrow", "ThorPot")
                .forEach(defaultName -> this.saveResource("items/" + defaultName + ".yml", true));
          
        getConfig().set("savedDefault", true);
        this.saveConfig();
    }
    
    public void loadItems(final boolean reload)
    {
        Configs.loadItems(new File(this.getDataFolder(), "items"), reload);
    }
    
    @Override
    public void onDisable()
    {
        try {
            FileConfiguration data = new YamlConfiguration();
            BlockManager.getInstance().saveBlocks(data);
            MobManager.saveData(data);
            data.save(dataFile);
        } 
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Something went wrong during data saving on disable", ex);
        }
        
        HandlerList.unregisterAll(this);
    }
}
