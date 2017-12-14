/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.NMS.NMSWrapper;
import me.parozzz.reflex.NMS.ReflectionUtil;
import me.parozzz.reflex.NMS.blocks.BlockPosition;
import me.parozzz.reflex.NMS.packets.Packet;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Paros
 */
public class EntityPlayer implements NMSWrapper
{
    private static final Class<?> entityPlayerClazz;
    
    private static final Method getHandle;
    private static final Method breakBlock;
    
    private static final Field playerConnectionField;
    private static final Method sendPacket;
    
    private static final Field playerInteractManagerField;
    static
    {
        
        getHandle = ReflectionUtil.getMethod(ReflectionUtil.getCraftbukkitClass("entity.CraftPlayer"),"getHandle",new Class[0]);

        entityPlayerClazz = ReflectionUtil.getNMSClass("EntityPlayer");
        playerConnectionField = ReflectionUtil.getField(entityPlayerClazz, "playerConnection");
        
        Class<?> playerConnectionClazz = ReflectionUtil.getNMSClass("PlayerConnection");
        sendPacket = ReflectionUtil.getMethod(playerConnectionClazz, "sendPacket", Packet.getNMSClass());
        playerInteractManagerField = ReflectionUtil.getField(entityPlayerClazz, "playerInteractManager");
        
        Class<?> playerInteractManagerClazz = ReflectionUtil.getNMSClass("PlayerInteractManager");
        breakBlock = ReflectionUtil.getMethod(playerInteractManagerClazz, "breakBlock", BlockPosition.getNMSClass());
    }
    
    public static Class<?> getNMSClass()
    {
        return entityPlayerClazz;
    }
    
    private final static Map<Player, EntityPlayer> players = new HashMap<>();
    private static Listener getListener()
    {
        return new Listener()
        {
            @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
            private void onPlayerJoin(final PlayerJoinEvent e)
            {
                players.put(e.getPlayer(), new EntityPlayer(e.getPlayer()));
            }
            
            @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
            private void onPlayerQuit(final PlayerQuitEvent e)
            {
                players.remove(e.getPlayer());
            }
        };
    }
    
    public static EntityPlayer getNMSPlayer(final Player p)
    {
        return players.get(p);
    }
    
    private final Object entityPlayer;
    private EntityPlayer(final Player p)
    {
        entityPlayer = Debug.validateMethod(getHandle, p);
    }
    
    private PlayerInteractManager manager;
    public PlayerInteractManager getInteractManager()
    {
        return Optional.ofNullable(manager).orElseGet(() -> manager = new PlayerInteractManager());
    }

    private PlayerConnection connection;
    public PlayerConnection getPlayerConnection()
    {
        return Optional.ofNullable(connection).orElseGet(() -> connection = new PlayerConnection());
    }
    
    @Override
    public Object getNMSObject() 
    {
        return entityPlayer;
    }
    
    public class PlayerConnection implements NMSWrapper
    {
        private final Object playerConnection;
        public PlayerConnection()
        {
            playerConnection = Debug.validateField(playerConnectionField, entityPlayer);
        }
        
        public <T extends Packet> void sendPacket(T packet)
        {
            Debug.validateMethod(sendPacket, playerConnection, packet.getNMSObject());
        }
        
        @Override
        public Object getNMSObject() 
        {
            return playerConnection;
        }
        
    }
    
    public class PlayerInteractManager implements NMSWrapper
    {
        private final Object interactManager;
        private PlayerInteractManager()
        {
            interactManager = Debug.validateField(playerInteractManagerField, entityPlayer);
        }
        
        public void breakBlock(final Block b)
        {
            breakBlock(b.getLocation());
        }
        
        public void breakBlock(final Location l)
        {
            Debug.validateMethod(breakBlock, interactManager, new BlockPosition(l.getBlockX(), l.getBlockY(), l.getBlockZ()).getNMSObject());
        }

        @Override
        public Object getNMSObject() 
        {
            return interactManager;
        }
    }
}
