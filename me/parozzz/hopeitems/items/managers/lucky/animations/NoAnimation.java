/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.lucky.animations;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.hopeitems.items.managers.lucky.LuckyReward;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class NoAnimation implements Animation
{

    @Override
    public void roll(List<LuckyReward> rewards, Player p) 
    {
        rewards.get(ThreadLocalRandom.current().nextInt(rewards.size())).getItems().forEach(collection -> 
        {
            Location l = p.getLocation();
            
            p.getInventory().addItem(collection.getItem().parse(p, l));
            
            if(collection.hasWhen(When.NONE))
            {
                ItemInfo info = collection.getItemInfo(When.NONE);
                info.executeActions(l, p);
                info.spawnMobs(l, p);
            }
        });
    }
    
}
