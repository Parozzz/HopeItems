/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.manager;

import it.parozzz.hopeitems.Database;
import it.parozzz.hopeitems.HopeItems;
import it.parozzz.hopeitems.Value;
import it.parozzz.hopeitems.core.ItemBuilder;
import it.parozzz.hopeitems.core.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public class LuckyManager 
{
    private final List<AnimationItem> reward=new ArrayList<>();
    public LuckyManager parse(final ConfigurationSection path)
    {
        if(path==null) { return this; }
        path.getKeys(false).stream().map(str -> path.getConfigurationSection(str)).forEach(newPath -> 
        {
            try {
                ItemManager im=new ItemManager(newPath.getName())
                        .setEffectManager(new EffectManager().parseStringList(newPath.getStringList("effect")))
                        .setPlayerManager(new PlayerManager().parseStringList(newPath.getStringList("player")))
                        .setConditionManager(new ConditionManager().parseStringList(newPath.getStringList("condition")))
                        .setMobManager(new MobManager().parse(newPath.contains("Mob")?newPath.getConfigurationSection("Mob"):null))
                        .setCreeperManager(new ExplosiveManager().parse(newPath.contains("CustomExplosive")?newPath.getConfigurationSection("CustomExplosive"):null))
                        .setWhen(newPath.contains("when")?newPath.getStringList("when").stream().map(String::toUpperCase).map(it.parozzz.hopeitems.Enum.When::valueOf).collect(Collectors.toSet()):Arrays.asList(it.parozzz.hopeitems.Enum.When.values()))
                        .addCommand(newPath.getStringList("command"));
                AnimationItem animation=
                        new AnimationItem(im,newPath.getInt("chance"),Value.luckyAnimationEnabled?newPath.getString("preview"):"",newPath.getString("custom"));
                for(Integer j=0;j<newPath.getInt("chance");j++) { reward.add(animation); }
            } 
            catch (InvalidConfigurationException ex) { ex.printStackTrace(); }
        });
        Collections.shuffle(reward);
        return this;
    }
    
    public void execute(final Player p) 
    { 
        if(reward.isEmpty()) { return; }
        
        Inventory i=Bukkit.createInventory(null, Value.luckyGUIRows*9, Value.luckyGUIName);
        Map<Integer,AnimationItem> items=new HashMap<>();
        Value.luckySlot.forEach(slot -> 
        {
            AnimationItem ai=reward.get(ThreadLocalRandom.current().nextInt(reward.size()));
            items.put(slot, ai);
            i.setItem(slot, ai.getPreview());
        });
        
        Value.luckyGUIDecoration.forEach((slot,item) -> i.setItem(slot, item));
        
        p.openInventory(i);
        
        Run runnable=new Run(p,items,i);
        p.setMetadata(Value.luckyGUIMetadata, new FixedMetadataValue(HopeItems.getInstance(),runnable));
        runnable.runTaskTimer(HopeItems.getInstance(), 3L, 3L);
    }

    public class Run extends BukkitRunnable
    {
        private final Player p;
        private final Map<Integer,AnimationItem> items;
        private final Inventory i;
        
        public Run(final Player p, final Map<Integer,AnimationItem> items, final Inventory i)
        {
            this.p=p;
            this.items=items;
            this.i=i;
        }
        
        public Run setRun(final int run) 
        {
            this.run=run; 
            return this;
        }
        
        public void end()
        {
            for(int j=run;j<Value.luckyRolls;j++) 
            {
                change(); 
            }
            reward();
            this.cancel();
        }
        
        private int run=0;     
        @Override
        public void run() 
        {
            run++;
            if(run==Value.luckyRolls/2)
            {
                this.cancel();
                p.setMetadata(Value.luckyGUIMetadata, new FixedMetadataValue(HopeItems.getInstance(),newRun(5L)));
                return;
            }
            else if(run==(int)(Value.luckyRolls/1.2))
            {
                this.cancel();
                p.setMetadata(Value.luckyGUIMetadata, new FixedMetadataValue(HopeItems.getInstance(),newRun(15L)));
                return;
            }
            else if(run==Value.luckyRolls)
            {
                reward();
                this.cancel();
                return;
            }
            
            sound();
            change();
        }
        
        private Run newRun(final long delay)
        {
            Run running=new Run(p,items,i).setRun(run);
            running.runTaskTimer(HopeItems.getInstance(), delay, delay);
            return running;
        }
        
        private void reward()
        {
            AnimationItem ai=items.get(Value.luckyFinalSlot);
            ai.getItemManager().execute(p.getLocation(), null, true, true);
            if(ai.getCustomName()!=null) 
            {
                p.getInventory().addItem(Database.getItem(ai.getCustomName()).clone()).values().forEach(item -> p.getWorld().dropItem(p.getLocation(), item)); 
            }
            p.removeMetadata(Value.luckyGUIMetadata, HopeItems.getInstance());
        }
        
        private void sound(){ if(Value.luckySound!=null) { Value.luckySound.playPlayer(p); } }
        
        private void change()
        {
            AnimationItem next=null;
            for(int slot:Value.luckySlot)
            {
                if(next==null)
                {
                    next=items.get(slot);
                    AnimationItem newValue=reward.get(ThreadLocalRandom.current().nextInt(reward.size()));
                    i.setItem(slot, newValue.getPreview());
                    items.replace(slot, next, newValue);
                    continue;
                }

                AnimationItem ai=next;
                next=items.get(slot);

                i.setItem(slot, ai.getPreview());
                items.put(slot, ai);
            }
        }
        
    }
    
    public class AnimationItem
    {
        private final String customName;
        private final ItemManager im;
        private final int chance;
        private ItemStack preview;
        
        public AnimationItem(final ItemManager im, final int chance, final String previewString, final String customName)
        {
            this.im=im;
            this.chance=chance;
            this.customName=customName;
            
            if(!previewString.isEmpty())
            {
                String[] array=previewString.split(";");
                preview=new ItemBuilder(Material.valueOf(array[0].toUpperCase()))
                        .setAmount(array.length>1?Integer.valueOf(array[1]):1)
                        .setName(array.length>2?Utils.color(array[2]):null)
                        .setLore(array.length>3?Arrays.stream(array).skip(3).map(Utils::color).collect(Collectors.toList()):null)
                        .build();
            }
        }
        
        public String getCustomName() { return customName; }
        public ItemManager getItemManager() { return im; }
        public int getChance() { return chance; }
        public ItemStack getPreview() { return preview; }
    }
}
