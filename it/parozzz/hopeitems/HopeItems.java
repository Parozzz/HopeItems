/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems;

import it.parozzz.hopeitems.core.ActionBar;
import it.parozzz.hopeitems.core.Dependency;
import it.parozzz.hopeitems.core.FireworkManager;
import it.parozzz.hopeitems.core.Language;
import it.parozzz.hopeitems.core.OldVersionArmorstand;
import it.parozzz.hopeitems.core.Particle;
import it.parozzz.hopeitems.reflection.ReflectionUtils;
import it.parozzz.hopeitems.core.Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class HopeItems 
        extends JavaPlugin
{
    private static JavaPlugin pl;
    @Override
    public void onEnable()
    {
        pl=this; 
        
        ReflectionUtils.initialize();
        ActionBar.initialize();
        Particle.initialize();
        
        if(Dependency.setupEconomy()) { pl.getLogger().info(">> Hooked into Vault <<"); }
        if(Dependency.setupWorldGuard()) { pl.getLogger().info(">> Hooked into WorldGuard <<"); }  
        createFolder();
        try { cLoad(false); } 
        catch (IOException | InvalidConfigurationException ex) { ex.printStackTrace(); }
    }
    
    @Override
    public void onDisable()
    {
        Database.getEntities().forEach(ent -> ent.remove());
        Database.revertAll();
        Database.saveData(dataFolder);
        Database.newDatabase();
        pl=null;
    }
    
    private File itemFolder;
    private File dataFolder;
    private void createFolder()
    {
        File tutorialFolder=new File(pl.getDataFolder(),"Tutorials");
        if(!tutorialFolder.exists())
        {
            tutorialFolder.mkdir();
            saveResource("Tutorials"+File.separator+"ItemCreation.txt",true);
            saveResource("Tutorials"+File.separator+"Particle.txt",true);
            saveResource("Tutorials"+File.separator+"PlayerReward.txt",true);
            saveResource("Tutorials"+File.separator+"Conditions.txt",true);
            saveResource("Tutorials"+File.separator+"CustomExplosiveCreation.txt",true);
            saveResource("Tutorials"+File.separator+"MobCreation.txt",true);
            saveResource("Tutorials"+File.separator+"RemoveOnUse.txt",true);
            saveResource("Tutorials"+File.separator+"WorldReward.txt",true);
        }
        
        pl.getDataFolder().mkdir();
        (dataFolder=new File(pl.getDataFolder(),"datafolder")).mkdir();
        if(!(itemFolder=new File(pl.getDataFolder(),"item")).exists())
        {
            itemFolder.mkdir();
            if(Utils.bukkitVersion("1.8")) { pl.saveResource("item"+File.separator+"example1_8.yml", true); }
            else if(Utils.bukkitVersion("1.9","1.10")) { pl.saveResource("item"+File.separator+"example1_9.yml", true); }
            else { pl.saveResource("item"+File.separator+"example.yml", true); }
        }
    }
    
    public void cLoad(final Boolean reload) throws UnsupportedEncodingException, IOException, FileNotFoundException, InvalidConfigurationException
    {
        if(reload) 
        { 
            Database.saveData(dataFolder);
            HandlerList.unregisterAll(pl); 
        }
        Database.newDatabase();
        FileConfiguration c=Utils.fileStartup(pl, new File(pl.getDataFolder(),"config.yml"));
        
        Value.parse(c);
        Language.parse(c);
        
        Shop shop=null;
        if(c.getBoolean("Shop.enabled"))
        {
            shop=new Shop().parse(c.getConfigurationSection("Shop"));
            pl.getServer().getPluginManager().registerEvents(shop, pl);
        }
        
        ItemHandler items=new ItemHandler(shop);
        
        Arrays.stream(itemFolder.listFiles()).forEach(file -> 
        {
            try { items.parse(Utils.fileStartup(pl, file)); } 
            catch (IOException | InvalidConfigurationException ex) { ex.printStackTrace(); } 
        }); 
        items.initializeHandler();
        
        pl.getServer().getPluginManager().registerEvents(items, pl);
        pl.getCommand("hopeitems").setExecutor(new MainCommand(pl,this,shop));
        
        initializeCoreListeners();
        
        Database.loadData(dataFolder);
    }
    
    private void initializeCoreListeners()
    {
        if(Utils.bukkitVersion("1.11","1.12")) { pl.getServer().getPluginManager().registerEvents(new FireworkManager(), pl); }
        if(Utils.bukkitVersion("1.8")) { pl.getServer().getPluginManager().registerEvents(new OldVersionArmorstand(), pl); }
        pl.getServer().getPluginManager().registerEvents(new LuckyHandler(), pl);
    }
    
    public static JavaPlugin getInstance() { return pl; }
}
