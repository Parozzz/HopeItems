/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.placeholders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class Placeholder 
{
    private enum PlayerPlaceholders
    {
        PLAYERNAME("{player}", (p, str) -> str.replace("{player}", p.getName())), 
        KILLS("{playerkills}", (p, str) -> str.replace("{playerkills}", p.getStatistic(Statistic.PLAYER_KILLS))), 
        DEATHS("{death}", (p, str) -> str.replace("{death}", p.getStatistic(Statistic.DEATHS)));
        
        private final String holder;
        private final BiConsumer<Player, MutableString> biConsumer;
        private PlayerPlaceholders(final String holder, final BiConsumer<Player, MutableString> biConsumer)
        {
            this.holder=holder;
            this.biConsumer=biConsumer;
        }
        
        public BiConsumer<Player, MutableString> getBiConsumer()
        {
            return biConsumer;
        }
        
        @Override
        public String toString()
        {
            return holder;
        }
    }
    
    private enum LocationPlaceholders
    {
        X("{x}", (l, str) -> str.replace("{x}", l.getBlockX())), 
        Y("{y}", (l, str) -> str.replace("{y}", l.getBlockY())), 
        Z("{z}", (l, str) -> str.replace("{z}", l.getBlockZ())), 
        WORLD("{world}", (l, str) -> str.replace("{world}", l.getWorld().getName()));
        
        private final BiConsumer<Location, MutableString> biConsumer;
        private final String holder;
        private LocationPlaceholders(final String holder, final BiConsumer<Location, MutableString> biConsumer)
        {
            this.holder=holder;
            this.biConsumer=biConsumer;
        }
        
        public BiConsumer<Location, MutableString> getBiConsumer()
        {
            return biConsumer;
        }
        
        @Override
        public String toString()
        {
            return holder;
        }
    }
    
    private final MutableString str;
    public Placeholder(final String str)
    {
        this.str=new MutableString(str);
    }
    
    private BiConsumer<Player, MutableString> player;
    public Placeholder checkPlayer()
    {
        player= Stream.of(PlayerPlaceholders.values())
                .filter(pp -> str.contains(pp.toString()))
                .map(PlayerPlaceholders::getBiConsumer)
                .reduce(BiConsumer::andThen)
                .orElse((p, s) -> {});
        return this;
    }
    
    private BiConsumer<Location, MutableString> location;
    public Placeholder checkLocation()
    {
        location = Stream.of(LocationPlaceholders.values())
                .filter(lp -> str.contains(lp.toString()))
                .map(LocationPlaceholders::getBiConsumer)
                .reduce(BiConsumer::andThen)
                .orElse((l, s) -> {});
        return this;
    }
    
    public String parse(final Location l)
    {
        MutableString toSend=str.clone();
        location.accept(l, toSend);
        return toSend.toString();
    }
    
    public String parse(final Player p, final Location l)
    {
        MutableString toSend=str.clone();
        player.accept(p, toSend);
        location.accept(l, toSend);
        return toSend.toString();
    }
    
}
