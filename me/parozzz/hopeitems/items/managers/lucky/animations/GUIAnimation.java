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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopeitems.items.managers.lucky.LuckyManager;
import me.parozzz.hopeitems.items.managers.lucky.LuckyReward;
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
    private final int roll;
    public GUIAnimation(final ConfigurationSection path)
    {
        i=Bukkit.createInventory(null, path.getInt("rows")*9, Utils.color(path.getString("title")));
        slots=Stream.of(path.getString("slot").split(",")).map(Integer::valueOf).collect(Collectors.toList());
        winSlot=path.getInt("winSlot");
        roll=path.getInt("roll");
        
        ConfigurationSection iPath=path.getConfigurationSection("Items");
        iPath.getKeys(false).stream().map(iPath::getConfigurationSection).forEach(sPath -> 
        {
            ItemStack item=Utils.getItemByPath(sPath);
            Stream.of(sPath.getName().split(",")).map(Integer::valueOf).forEach(slot -> i.setItem(slot, item));
        });
    }
    
    
    @Override
    public void roll(List<LuckyReward> rewards, Player p) 
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
        roll.runTaskTimer(plugin, 3L, 3L);
    }
    
    private class Roll extends BukkitRunnable implements AnimationRunnable
    {
        private final Player p;
        private final Map<Integer,LuckyReward> map;
        private final List<LuckyReward> rewards;
        private final Inventory i;
        
        public Roll(final Player p, final Map<Integer, LuckyReward> map, final Inventory i, final List<LuckyReward> rewards)
        {
            this.p=p;
            this.map=map;
            this.i=i;
            this.rewards=rewards;
        }
        
        private Roll setRun(final int run) 
        {
            this.run=run; 
            return this;
        }
        
        private boolean isEnded = false;
        @Override
        public void end()
        {
            isEnded = true;
            for(int j=run;j<roll;j++) 
            {
                change(); 
            }
            reward();
            this.cancel();
        }
        
        @Override
        public boolean isEnded()
        {
            return isEnded;
        }
        
        private int run=0;     
        @Override
        public void run() 
        {
            run++;
            if(run==roll/2)
            {
                this.cancel();
                p.setMetadata(LuckyManager.LUCKY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(GUIAnimation.class),newRun(5L)));
                return;
            }
            else if(run==(int)(roll/1.2))
            {
                this.cancel();
                p.setMetadata(LuckyManager.LUCKY_METADATA, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(GUIAnimation.class),newRun(15L)));
                return;
            }
            else if(run>=roll)
            {
                reward();
                this.cancel();
                return;
            }
            
            change();
        }
        
        private Roll newRun(final long delay)
        {
            Roll running=new Roll(p, map, i, rewards).setRun(run);
            running.runTaskTimer(JavaPlugin.getProvidingPlugin(GUIAnimation.class), delay, delay);
            return running;
        }
        
        private void reward()
        {
            isEnded = true;
            LuckyReward reward=map.get(winSlot);
            reward.getItems().forEach(info -> 
            {
                Location l = p.getLocation();
                
                p.getInventory().addItem(info.getItem().parse(p, l));
                info.executeActions(l, p);
                info.spawnMobs(l);
            });
        }
        
        private void change()
        {
            LuckyReward next=null;
            for(int slot:slots)
            {
                if(next==null)
                {
                    next=map.get(slot);
                    LuckyReward newValue=rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));
                    i.setItem(slot, newValue.getPreview());
                    map.replace(slot, next, newValue);
                    continue;
                }

                LuckyReward lr=next;
                next=map.get(slot);

                i.setItem(slot, lr.getPreview());
                map.put(slot, lr);
            }
        }
        
    }
    
}
