/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.reflex.MCVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 *
 * @author Paros
 */
public class Util 
{
    
    public static enum ColorEnum
    {
        AQUA(Color.AQUA, ChatColor.AQUA),BLACK(Color.BLACK, ChatColor.BLACK),FUCHSIA(Color.FUCHSIA, ChatColor.LIGHT_PURPLE),
        GRAY(Color.GRAY, ChatColor.GRAY),GREEN(Color.GREEN, ChatColor.GREEN),LIME(Color.LIME, ChatColor.GREEN),
        MAROON(Color.MAROON, ChatColor.GRAY),NAVY(Color.NAVY, ChatColor.DARK_BLUE),OLIVE(Color.OLIVE, ChatColor.DARK_GREEN),
        ORANGE(Color.ORANGE, ChatColor.GOLD),PURPLE(Color.PURPLE, ChatColor.DARK_PURPLE),RED(Color.RED, ChatColor.RED),
        BLUE(Color.BLUE, ChatColor.BLUE),SILVER(Color.SILVER, ChatColor.DARK_GRAY),TEAL(Color.TEAL, ChatColor.GRAY),
        WHITE(Color.WHITE, ChatColor.WHITE),YELLOW(Color.YELLOW, ChatColor.YELLOW);
        
        private final Color color;
        private final ChatColor chat;
        private ColorEnum(Color color, final ChatColor chat)
        {
            this.chat=chat;
            this.color=color;
        }
        
        public ChatColor getChatColor()
        {
            return chat;
        }
        
        public Color getBukkitColor()
        {
            return color;
        }
    }
    
    public static EnumSet<BlockFace> cardinals=EnumSet.of(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH);
    public static void registerArmorStandInvicibleListener()
    {
        if(MCVersion.V1_8.isEqual())
        {
            Bukkit.getServer().getPluginManager().registerEvents(new Listener()
            {
                @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
                private void onInvisibleArmorStandDamage(final EntityDamageByEntityEvent e)
                {
                    e.setCancelled(e.getEntityType()==EntityType.ARMOR_STAND && !((ArmorStand)e.getEntity()).isVisible());
                }
            }, JavaPlugin.getProvidingPlugin(Util.class));
        }
    }
    
    public static UUID fromFileName(final File file)
    {
        return UUID.fromString(file.getName().replace(".yml", ""));
    }
    
    public static <T extends Event> T callEvent(final T event)
    {
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }
    
    public static <T> T ifCheck(final boolean check, final Supplier<T> trueSupplier, final Supplier<T> falseSupplier)
    {
        if(check)
        {
            return trueSupplier.get();
        }
        else
        {
            return falseSupplier.get();
        }
    }
    
    public static void ifCheck(boolean check, final Runnable trueStatement, final Runnable falseStatement)
    {
        if(check)
        {
            trueStatement.run();
        }
        else
        {
            falseStatement.run();
        }
    }
    
    public static void ifCheck(final boolean check, final Runnable trueStatement)
    {
        if(check)
        {
            trueStatement.run();
        }
    }
    
    public static boolean or(final Object o, final Object... array)
    {
        return Arrays.stream(array).anyMatch(ob -> ob.equals(o));
    }
    
    public static boolean and(final Object o, final Object... array)
    {
        return Arrays.stream(array).allMatch(ob -> ob.equals(o));
    }
    
    public static boolean isNumber(final String str) 
    {
        return str.chars().allMatch(c -> Character.isDigit((char)c)); 
    }
    
    public static String cc(final String s)
    {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    
    public static String stripCC(final String s)
    {
        return ChatColor.stripColor(s);
    }
    
    public static FileConfiguration fileStartup(final File file) throws FileNotFoundException, UnsupportedEncodingException 
    {
        if(!file.exists()) 
        {
            JavaPlugin pl = JavaPlugin.getProvidingPlugin(Util.class);
            pl.saveResource(file.getPath().replace("plugins" + File.separator + pl.getName() + File.separator, ""), true);
        }
        
        return loadUTF(file);
    }
    
    public static FileConfiguration loadUTF(final File file)
    {
        try {
            return YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
         
    }
    
    public static ArmorStand spawnHologram(final Location l, final String str)
    {
        ArmorStand as=(ArmorStand)l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        as.setCustomName(str);
        as.setCustomNameVisible(true);
        as.setVisible(false);
        as.setGravity(false);
        as.setMarker(true);
        as.setRemoveWhenFarAway(false);
        as.setBasePlate(false);
        if(MCVersion.V1_9.isHigher()) 
        {
            as.setSilent(true);
            as.setInvulnerable(true); 
        }
        return as;
    }
    
    public static Item spawnFloatingItem(final Location l, final String str, final ItemStack stack)
    {
        return Optional.ofNullable(stack).map(t -> 
        {
            Item item=l.getWorld().dropItem(l, t);
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setVelocity(new Vector(0,0,0));
            item.setCustomName(str);
            item.setCustomNameVisible(true);
            if(MCVersion.V1_9.isHigher()) 
            { 
                item.setGravity(false);
                item.setSilent(true);
                item.setInvulnerable(true); 
            }  
            return item;
        }).orElseGet(() -> null);
    }
    
    public static Item spawnFloatingItem(final Location l, final String str, final Material type)
    {
        return spawnFloatingItem(l, str, new ItemStack(type));
    }

    public static String chunkToString(final Chunk c)
    { 
        return new StringBuilder().append(c.getX()).append(c.getZ()).toString(); 
    }
    
    public static String longToTime(final long l)
    {
        String date = new String();
        
        long loop = l;
        while(loop > 0)
        {
            if(loop >= 86400)
            {
                date = new StringBuilder().append(date).append(loop / 86400).append("d ").toString();
                loop %= 86400;
            }
            else if(loop >= 3600)
            {
                date = new StringBuilder().append(date).append(loop / 3600).append("h ").toString();
                loop %= 3600;
            }
            else if(loop >= 60)
            {
                date = new StringBuilder().append(date).append(loop / 60).append("m ").toString();
                loop %= 60;
            }
            else
            {
                date = new StringBuilder().append(date).append(loop).append("s").toString();
                loop = 0L;
            }   
        }
        
        return date;
    }
}
