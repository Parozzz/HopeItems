/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.basics;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.NMS.NMSWrapper;
import me.parozzz.reflex.NMS.ReflectionUtil;
import me.parozzz.reflex.NMS.packets.Packet;
import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 *
 * @author Paros
 */
public class NMSServer implements NMSWrapper
{
    private static final Method getHandle;
    private static final Method sendAll;
    static
    {
        Class<?> craftServerClass = ReflectionUtil.getCraftbukkitClass("CraftServer");
        
        getHandle = ReflectionUtil.getMethod(craftServerClass, "getHandle");
        sendAll = Stream.of(ReflectionUtil.getNMSClass("DedicatedPlayerList").getMethods())
                .filter(m -> m.getName().contains("sendAll")).filter(m -> m.getParameterCount() == 1).findFirst().get();
    }
    
    private static NMSServer serverInstance;
    public static NMSServer getServer()
    {
        return Optional.ofNullable(serverInstance).orElseGet(() -> serverInstance = new NMSServer(Bukkit.getServer()));
    }
    
    private final Object nmsServer;
    private NMSServer(final Server server)
    {
        this.nmsServer = Debug.validateMethod(getHandle, server);
    }

    public <T extends Packet> void sendAll(final T packet)
    {
        Debug.validateMethod(sendAll, nmsServer, packet.getNMSObject());
    }
    
    @Override
    public Object getNMSObject() 
    {
        return nmsServer;
    }
}
