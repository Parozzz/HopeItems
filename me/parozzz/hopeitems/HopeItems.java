/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems;

import me.parozzz.hopeitems.hooks.Dependency;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import javax.annotation.Nullable;
import me.parozzz.hopeitems.items.BlockManager;
import me.parozzz.hopeitems.items.database.DatabaseManager;
import me.parozzz.hopeitems.items.listener.ItemListener;
import me.parozzz.hopeitems.items.managers.IParser;
import me.parozzz.hopeitems.items.managers.actions.parsers.ActionParser;
import me.parozzz.hopeitems.items.managers.conditions.parsers.ConditionParser;
import me.parozzz.hopeitems.items.managers.cooldown.parser.CooldownParser;
import me.parozzz.hopeitems.items.managers.explosive.ExplosiveManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.mobs.parsers.MobManager;
import me.parozzz.hopeitems.items.managers.mobs.parsers.MobParser;
import me.parozzz.hopeitems.shop.Shop;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.ReflexAPI;
import me.parozzz.reflex.ReflexAPI.Property;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
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
    
    private DatabaseManager databaseManager;
    private BlockManager blockManager;
    
    private ConditionParser conditionParser;
    private ActionParser actionParser;
    private MobParser mobParser;
    private CooldownParser cooldownParser;
    
    @Override
    public void onLoad()
    {
        this.getDataFolder().mkdir(); //Let me create the folder, otherwise the database initialization won't happend and will throw an error.
        
        databaseManager = new DatabaseManager(this);
        blockManager = new BlockManager(databaseManager);
        
        conditionParser = new ConditionParser();
        actionParser = new ActionParser();
        mobParser = new MobParser();
        cooldownParser = new CooldownParser();
    }
    
    public @Nullable DatabaseManager getDatabaseManager()
    {
        this.validate(databaseManager);
        return databaseManager;
    }
    
    public @Nullable ConditionParser getConditionParser()
    {
        this.validate(conditionParser);
        return conditionParser;
    }
    
    public @Nullable ActionParser getActionParser()
    {
        this.validate(actionParser);
        return actionParser;
    }
    
    public @Nullable MobParser getMobParser()
    {
        this.validate(mobParser);  
        return mobParser;
    }
    
    public @Nullable CooldownParser getCooldownParser()
    {
        this.validate(cooldownParser);  
        return cooldownParser;
    }
    
    private void validate(final Object obj)
    {
        if(obj == null)
        {
            throw new RuntimeException("Trying to access HopeItems class before loading");
        }
    }
    
    @Override
    public void onEnable()
    {
        ReflexAPI.getAPI().addProperty(Property.ENTITYPLAYER_LISTENER, Property.ARMOREVENTS_LISTENER);
        
        //Setting up dependencies (WorldGuard and Vault)
        if(Dependency.setupEconomy())
        {
            logger.log(Level.INFO, "Hooked into Vault");
        }
        
        if(Dependency.setupWorldGuard())
        {
            logger.log(Level.INFO, "Hooked into WorldGuard");
        }
        
        this.saveDefaultConfig();
        Dependency.setupClanHook(this.getConfig());
        //Register default parsers after setting up dependencies.
        Stream.of(conditionParser, actionParser, mobParser, conditionParser).forEach(IParser::registerDefaultSpecificParsers);
        
        //Loading up all the plugin features
        try {
            this.loadAllConfigs(false);
            if(getConfig().getBoolean("metric", true))
            {
                new MetricsLite(this);
            }
            
            Builder<Listener> listenerBuilder = Stream.builder();
            if(Bukkit.getServer().getVersion().contains("Paper") && MCVersion.V1_11.isHigher())
            {
                logger.log(Level.INFO, "Using PaperSpigot 1.11+ EntityRemoveFromWorldEvent");
                listenerBuilder.accept(MobManager.getPaperSpigotListener());
            }
            listenerBuilder.add(MobManager.getListener())
                    .add(ExplosiveManager.getListener())
                    .add(LuckyManager.getListener())
                    .add(new ItemListener(this, blockManager));
            Util.ifCheck(MCVersion.V1_9.isHigher(), () -> listenerBuilder.accept(ItemListener.get1_9Listener(this)));
            Util.ifCheck(Dependency.isEconomyHooked(), () -> listenerBuilder.add(Shop.getListener()));
            listenerBuilder.build().forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
            
            Util.ifCheck(MCVersion.V1_8.isEqual(), () -> Util.registerArmorStandInvicibleListener());
            
            new ItemsCommand().registerCommand(this.getCommand("items"));
            
            File dataFile = new File(this.getDataFolder(), "data.yml");
            if(dataFile.exists())
            {
                logger.log(Level.INFO, "Found data.yml file. Converting data from YAML file to SQLite");
                
                FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
                blockManager.convertFromFileToSql(data);
                MobManager.convertYamlToSql(data);
                
                dataFile.delete();
            }
            else
            {
                blockManager.loadAllBlocks();
                MobManager.loadAllMobs();
            }
        }
        catch(final Exception t) {
            logger.log(Level.SEVERE, "Problem loading the plugin. Disabling it", t);
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    public void loadAllConfigs(final boolean reload)
    {
        Configs.initConfig(this.getConfig());
        this.loadItems(reload);
        
        if(Dependency.isEconomyHooked())
        {  
            Shop.getInstance().loadConfig(this);
        }  
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
        HandlerList.unregisterAll(this);
    }
}
