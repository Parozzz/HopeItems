/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Paros
 */
public class OfflinePlayerManager 
{
    private static OfflinePlayerManager instance;
    public static OfflinePlayerManager getInstance()
    {
        return Optional.ofNullable(instance).orElseGet(() -> instance=new OfflinePlayerManager());
    }
    
    private final Map<String, UUID> uuids;
    private final Map<UUID, String> names;
    private OfflinePlayerManager()
    {
        uuids=new HashMap<>();
        names=new HashMap<>();
    }
    
    public boolean hasUUID(final UUID u)
    {
        return names.containsKey(u);
    }
    
    public void addUUID(final UUID u, final String name)
    {
        uuids.put(name, u);
        names.put(u, name);
    }
    
    public String getName(final UUID u)
    {
        return Optional.ofNullable(names.get(u)).map(str -> 
        {
            OfflinePlayer op=Bukkit.getOfflinePlayer(u);
            if(!str.equals(op.getName()))
            {
                replaceOffline(str, u, op);
                return op.getName();
            }
            return str;
        }).orElseGet(() -> null);
    }
    
    public UUID getUUID(final String name)
    {
        return Optional.ofNullable(uuids.get(name)).map(u -> 
        {
            OfflinePlayer op=Bukkit.getOfflinePlayer(u);
            if(!name.equals(op.getName()))
            {
                replaceOffline(name, u, op);
            }
            return u;
        }).orElseGet(() -> null);
    }
    
    private void replaceOffline(final String name, final UUID u, final OfflinePlayer op)
    {
        names.replace(u, name, op.getName());
        uuids.remove(name, u);
        uuids.put(op.getName(), u);
    }
    
    public void loadAllUUIDs(final File folder)
    {
        Stream.of(folder.listFiles())
                .map(Utils::fromFileName)
                .map(Bukkit::getOfflinePlayer)
                .forEach(op -> 
                {
                    uuids.put(op.getName(), op.getUniqueId());
                    names.put(op.getUniqueId(), op.getName());
                });
    }
}
