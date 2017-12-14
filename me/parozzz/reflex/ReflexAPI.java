/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.reflex.NMS.ReflectionUtil;
import me.parozzz.reflex.NMS.entity.EntityPlayer;
import me.parozzz.reflex.NMS.entity.EntityPlayer.PlayerConnection;
import me.parozzz.reflex.NMS.itemStack.ItemNBT;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.NMS.packets.ChatPacket;
import me.parozzz.reflex.NMS.packets.ChatPacket.MessageType;
import me.parozzz.reflex.NMS.packets.ParticlePacket;
import me.parozzz.reflex.NMS.packets.ParticlePacket.ParticleEnum;
import me.parozzz.reflex.NMS.packets.TitlePacket;
import me.parozzz.reflex.NMS.packets.TitlePacket.TitleType;
import me.parozzz.reflex.events.ArmorHandler;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class ReflexAPI extends JavaPlugin implements Listener
{
    public enum Property
    {
        ENTITYPLAYER_LISTENER, ARMOREVENTS_LISTENER;
    }
    
    @Override
    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents(this, this);
        enable(Property.ENTITYPLAYER_LISTENER);
    }
    
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e)
    {
        PlayerConnection connection = EntityPlayer.getNMSPlayer(e.getPlayer()).getPlayerConnection();
        connection.sendPacket(new ParticlePacket(ParticleEnum.CRIT, e.getPlayer().getLocation(), 1F, 30));
        connection.sendPacket(new TitlePacket(TitleType.TITLE, "", 10, 30, 10));
        connection.sendPacket(new TitlePacket(TitleType.SUBTITLE, "Cool SubTitle", 10, 10, 10));
        
        ItemNBT nbt = new ItemNBT(new ItemStack(Material.ARROW));
        NBTCompound tag = nbt.getTag();
        tag.setString("OK GOOGLE", "WAT");
        e.getPlayer().getInventory().addItem(nbt.getBukkitItem());
        
        connection.sendPacket(new ChatPacket(MessageType.SYSTEM, nbt.getTag().getString("OK GOOGLE")));
    }
    
    
    public static void enable(final Property... properties)
    {
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(ReflexAPI.class);
        Stream.of(properties).forEach(property -> 
        {
            try {
                switch(property)
                {
                    case ENTITYPLAYER_LISTENER:
                        Bukkit.getPluginManager().registerEvents((Listener)ReflectionUtil.getMethod(EntityPlayer.class, "getListener").invoke(null), plugin);
                        break;
                    case ARMOREVENTS_LISTENER:
                        Bukkit.getPluginManager().registerEvents(new ArmorHandler(), plugin);
                }
            }catch(final Exception ex){
                Logger.getLogger(ReflexAPI.class.getSimpleName()).log(Level.SEVERE, null, ex);
            }

        });
    }
}
