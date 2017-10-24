/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.lucky.animations;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import me.parozzz.hopeitems.items.managers.lucky.LuckyReward;
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
        rewards.get(ThreadLocalRandom.current().nextInt(rewards.size())).getItems().forEach(info -> 
        {
            p.getInventory().addItem(info.getItem().parse(p, p.getLocation()));
            info.executeActionsAndSpawn(p.getLocation(), p);
        });
    }
    
}
