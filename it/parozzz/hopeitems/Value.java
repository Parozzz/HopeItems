/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems;

import it.parozzz.hopeitems.core.SoundManager;
import it.parozzz.hopeitems.core.Utils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class Value 
{
    public static List<Integer> luckySlot;
    public static int luckyFinalSlot;
    public static int luckyRolls;
    public static boolean luckyAnimationEnabled;
    public static String luckyGUIName;
    public static int luckyGUIRows;
    public static SoundManager luckySound;
    public static String luckyGUIMetadata="LuckyGUI";
    public static Map<Integer,ItemStack> luckyGUIDecoration=new HashMap<>();
    
    public static boolean useNBT;
    
    public static String FriendlyMetadata="FriendlyCreeper";
    public static String CustomExplosiveMetadata="CustomExplosive";
    public static String CustomProjectileMetadata="CustomArrows";
    public static String CustomAbilityMetadata="CustomAbilities";
    public static String CustomDropChestMetadata="CustomDropChest";
    public static String CustomMobMetadata="CustomMob";
    public static String CustomBlockMetadata="CustomBlock";
    public static String CustomLingeringMetadata="CustomLingering";
    
    public static int chestExpirationTime;
    public static String chestHologram;
    
    public static void parse(final FileConfiguration c)
    {
        useNBT=c.getBoolean("useNBTTag");
        chestExpirationTime=c.getInt("customMobChestDropExpirationTime");
        chestHologram=Utils.color(c.getString("customMobChestHologram"));
        
        ConfigurationSection lPath=c.getConfigurationSection("Lucky");
        luckyAnimationEnabled=lPath.getBoolean("animationEnabled");
        luckySlot=Stream.of(lPath.getString("rollSlot").split(";")).map(Integer::valueOf).collect(Collectors.toList());
        luckyFinalSlot=lPath.getInt("winSlot");
        luckyRolls=lPath.getInt("rolls");
        luckyGUIName=Utils.color(lPath.getString("GUIName"));
        luckyGUIRows=lPath.getInt("GUIRows");
        if(lPath.contains("sound")) { luckySound=new SoundManager(lPath.getString("sound")); }
        if(lPath.contains("GUI"))
        {
            luckyGUIDecoration.clear();
            ConfigurationSection gPath=lPath.getConfigurationSection("GUI");
            gPath.getKeys(false).stream().map(str -> gPath.getConfigurationSection(str)).forEach(path -> 
            {
                ItemStack item=Utils.getItemByPath(path);
                
                String[] array=path.getString("slot").split(";");
                Stream.of(array).map(Integer::valueOf).forEach(slot -> luckyGUIDecoration.put(slot, item));
            });
        }
    }
}
