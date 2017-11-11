/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.lucky.animations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyReward;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public class GUIAnimation implements Animation
{
    private Inventory i;
    
    private final List<Integer> slots;
    private final int winSlot;
    private final int rolls;
    
    public GUIAnimation(final ConfigurationSection path)
    {
        i=Bukkit.createInventory(null, path.getInt("rows")*9, Utils.color(path.getString("title")));
        
        slots=Stream.of(path.getString("slot").split(",")).map(Integer::valueOf).collect(Collectors.toList());
        winSlot=path.getInt("winSlot");
        rolls=path.getInt("roll");
        
        ConfigurationSection iPath=path.getConfigurationSection("Items");
        iPath.getKeys(false).stream().map(iPath::getConfigurationSection).forEach(sPath -> 
        {
            ItemStack item=Utils.getItemByPath(sPath);
            Stream.of(sPath.getName().split(",")).map(Integer::valueOf).forEach(slot -> i.setItem(slot, item));
        });
    }
    
    
    @Override
    public void roll(final List<LuckyReward> rewards, final Player p) 
    {
        Inventory i = Utils.cloneChestInventory(this.i);
        
        Map<Integer, LuckyReward> map=new HashMap<>();
        slots.forEach(slot -> 
        {
            LuckyReward reward=rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));
            i.setItem(slot, reward.getPreview());
            map.put(slot, reward);
        });
        
        p.openInventory(i);
        
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(GUIAnimation.class);
        
        Roll roll=new Roll(p, map, i, rewards);
        p.setMetadata(LuckyManager.LUCKY_METADATA, new FixedMetadataValue(plugin, roll));
        roll.runTaskTimer(plugin, 2L, 2L);
    }
    
    private class Roll extends BukkitRunnable implements AnimationRunnable
    {
        private final Player p;
        private final Map<Integer,LuckyReward> map;
        private final List<LuckyReward> rewards;
        private final Inventory i;
        
        public Roll(final Player p, final Map<Integer, LuckyReward> map, final Inventory i, final List<LuckyReward> rewards)
        {
            this.p = p;
            this.map = map;
            this.i = i;
            this.rewards = rewards;
        }
        
        @Override
        public void end()
        {
            IntStream.range(0, rolls).forEach(i -> change());
            reward();
            this.cancel();
        }
        
        private int run=0;     
        @Override
        public void run() 
        {
            run++;
            if(run == (int)(rolls/3))
            {
                this.cancel();
                p.setMetadata(LuckyManager.LUCKY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(GUIAnimation.class),newRun(3L)));
                return; 
            }
            else if(run == (int)(rolls/2.7))
            {
                this.cancel();
                p.setMetadata(LuckyManager.LUCKY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(GUIAnimation.class),newRun(4L)));
                return;
            }
            else if(run == (int)(rolls/2.3))
            {
                this.cancel();
                p.setMetadata(LuckyManager.LUCKY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(GUIAnimation.class),newRun(5L)));
                return;
            }
            else if(run == (int)(rolls/2))
            {
                this.cancel();
                p.setMetadata(LuckyManager.LUCKY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(GUIAnimation.class),newRun(6L)));
                return;
            }
            else if(run == (int)(rolls/1.7))
            {
                this.cancel();
                p.setMetadata(LuckyManager.LUCKY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(GUIAnimation.class),newRun(7L)));
                return;
            }
            else if(run == (int)(rolls/1.3))
            {
                this.cancel();
                p.setMetadata(LuckyManager.LUCKY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(GUIAnimation.class),newRun(8L)));
                return;
            }
            else if(run == (int) (rolls/1.1))
            {
                this.cancel();
                p.setMetadata(LuckyManager.LUCKY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(GUIAnimation.class),newRun(9L)));
                return;
            }
            else if(run>=rolls)
            {
                reward();
                this.cancel();
                return;
            }
            
            change();
        }
        
        private final Sound sound = Sound.valueOf(MCVersion.V1_8.isEqual() ? "ORB_PICKUP" : "ENTITY_EXPERIENCE_ORB_PICKUP");
        private void change()
        {
            p.playSound(p.getLocation(), sound, 0.3F, 1F);
            
            LuckyReward nextReward=null;
            for(int slot:slots)
            {
                if(nextReward==null)
                {
                    nextReward=map.get(slot);
                    
                    LuckyReward firstValue=rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));
                    i.setItem(slot, firstValue.getPreview());
                    map.replace(slot, nextReward, firstValue);
                    
                    continue;
                }

                i.setItem(slot, nextReward.getPreview());
                nextReward=map.replace(slot, nextReward);
            }
        }
        
        private Roll newRun(final long delay)
        {
            Roll running=new Roll(p, map, i, rewards);
            running.run = run;
            running.runTaskTimer(JavaPlugin.getProvidingPlugin(GUIAnimation.class), delay, delay);
            return running;
        }
        
        private boolean isEnded = false;
        private void reward()
        {
            isEnded = true;
            LuckyReward reward=map.get(winSlot);
            reward.getItems().forEach(info -> 
            {
                Location l = p.getLocation();
                p.getInventory().addItem(info.getItem().parse(p, l));
                info.execute(l, p, false);
            });
        }
        
        @Override
        public boolean isEnded()
        {
            return isEnded;
        }
        
    }
    
}
