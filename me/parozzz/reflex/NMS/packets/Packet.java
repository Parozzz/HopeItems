/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.packets;

import me.parozzz.reflex.NMS.NMSWrapper;
import me.parozzz.reflex.NMS.ReflectionUtil;

/**
 *
 * @author Paros
 */
public abstract class Packet implements NMSWrapper
{
    private final static Class<?> packetClass;
    static
    {
        packetClass = ReflectionUtil.getNMSClass("Packet");
    }
    
    public static Class<?> getNMSClass()
    {
        return packetClass;
    }
    
    @Override
    public abstract Object getNMSObject();
}
