/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.lucky.animations;

import java.util.List;
import me.parozzz.hopeitems.items.managers.lucky.LuckyReward;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public interface Animation 
{
    public void roll(List<LuckyReward> rewards, Player p);
}
