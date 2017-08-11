/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems.core;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class SoundManager 
{
        
    private final Sound sound;
    private final Float volume;
    private final Float pitch;
    
    public SoundManager(final String str)
    {
        String[] array=str.split(";");
        
        sound=Sound.valueOf(array[0].toUpperCase());
        volume=Float.parseFloat(array[1]);
        pitch=Float.parseFloat(array[2]);
    }
    
    public void playWorld(World w,Location l) { w.playSound(l, sound, volume, pitch); }
    public void playPlayer(final Player p) { p.playSound(p.getLocation(), sound, volume, pitch); }
}
