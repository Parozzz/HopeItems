/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.classes;

import me.parozzz.reflex.Debug;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class SoundManager 
{
    private final Sound sound;
    private final float volume;
    private final float pitch;
    public SoundManager(final String sound, final float volume, final float pitch)
    {
        this(Debug.validateEnum(sound, Sound.class), volume, pitch);
    }

    public SoundManager(final Sound sound, final float volume, final float pitch)
    {
        this.sound=sound;
        this.volume=volume;
        this.pitch=pitch;
    }
    
    public SoundManager(final String toSplit, final String splitter)
    {
        String[] a = toSplit.split(splitter);
        
        sound = Debug.validateEnum(a[0], Sound.class);
        volume = Float.valueOf(a[1]);
        pitch = Float.valueOf(a[2]);
    }

    public void play(final Location l, final Player p)
    {
        p.playSound(l, sound, volume, pitch);
    }

    public void play(final Location l)
    {
        l.getWorld().playSound(l, sound, volume, pitch);
    }
}
