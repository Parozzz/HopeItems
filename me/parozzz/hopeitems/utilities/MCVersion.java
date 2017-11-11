/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities;

import java.util.Optional;
import java.util.stream.Stream;
import org.bukkit.Bukkit;

/**
 *
 * @author Paros
 */
public enum MCVersion 
{
    V1_8("1.8", (byte)1),
    V1_9("1.9", (byte)2), 
    V1_10("1.10", (byte)3), 
    V1_11("1.11", (byte)4), 
    V1_12("1.12", (byte)5);
    
    private final String ver;
    private final byte id;
    private MCVersion(final String ver, final byte id)
    {
        this.ver = ver;
        this.id = id;
    }
    
    public boolean isHigher()
    {
        return actual().id >= id;
    }
    
    public boolean isLower()
    {
        return actual().id <= id;
    }
    
    public boolean isEqual()
    {
        return actual() == this;
    }
    
    public static boolean contains(final MCVersion... vs)
    {
        return Stream.of(vs).anyMatch(v -> v == actual());
    }
    
    private static String version;
    public static String nms()
    {
        return Optional.ofNullable(version).orElseGet(() -> 
        {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            return version = name.substring(name.lastIndexOf('.') + 1) + ".";
        });
    }
    
    private static MCVersion actualVersion;
    public static MCVersion actual()
    {
        return Optional.ofNullable(actualVersion).orElseGet(() -> Stream.of(MCVersion.values()).filter(v -> Bukkit.getVersion().contains(v.ver)).findFirst().get());
    }
}
