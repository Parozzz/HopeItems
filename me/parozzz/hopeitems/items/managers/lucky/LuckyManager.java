/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.lucky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import me.parozzz.hopeitems.items.managers.lucky.animations.Animation;
import me.parozzz.hopeitems.items.managers.lucky.animations.AnimationRunnable;
import me.parozzz.hopeitems.items.managers.lucky.animations.GUIAnimation;
import me.parozzz.hopeitems.items.managers.lucky.animations.NoAnimation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class LuckyManager 
{
    public static final String LUCKY_METADATA="RollingLucky";
    
    private enum AnimationType
    {
        NONE, GUI;
    }
    
    private final Animation animation;
    
    private final List<LuckyReward> rewards;
    public LuckyManager(final ConfigurationSection path)
    {
        rewards=new ArrayList<>();
        ConfigurationSection rPath=path.getConfigurationSection("Rewards");
        rPath.getKeys(false).stream()
                .map(rPath::getConfigurationSection)
                .map(LuckyReward::new)
                .forEach(reward -> IntStream.range(0, reward.getChance()).forEach(i -> rewards.add(reward)));
        Collections.shuffle(rewards);
        
        AnimationType type;
        try { type=AnimationType.valueOf(path.getString("animation").toUpperCase()); }
        catch(final IllegalArgumentException t) { throw new IllegalArgumentException("An animation called "+path.getString("animation")+" does not exist"); }
        
        switch(type)
        {
            case NONE:
                animation=new NoAnimation();
                break;
            case GUI:
                animation=new GUIAnimation(path.getConfigurationSection("Gui"));
                break;
            default:
                animation=null;
                break;
        }
    }
    
    
    public void roll(final Player p)
    {
        animation.roll(rewards, p);
    }
    
    public static void registerListener()
    {
        Listener l=new Listener()
        {
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onInventoryClick(final InventoryClickEvent e)
            {
                if(e.getWhoClicked().hasMetadata(LUCKY_METADATA))
                {
                    e.setCancelled(true);
                }
            }
            
            @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
            private void onInventoryClose(final InventoryCloseEvent e)
            {
                if(e.getPlayer().hasMetadata(LUCKY_METADATA))
                {
                    ((AnimationRunnable)e.getPlayer().getMetadata(LUCKY_METADATA).get(0).value()).end();
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(l, JavaPlugin.getProvidingPlugin(LuckyManager.class));
    }
}
