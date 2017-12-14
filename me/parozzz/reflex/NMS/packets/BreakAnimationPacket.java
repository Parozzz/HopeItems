/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.packets;

import java.lang.reflect.Constructor;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.NMS.ReflectionUtil;
import me.parozzz.reflex.NMS.blocks.BlockPosition;

/**
 *
 * @author Paros
 */
public class BreakAnimationPacket extends Packet
{
    private static final Constructor<?> constructor;
    static
    {
        constructor = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutBlockBreakAnimation"), int.class, BlockPosition.getNMSClass(), int.class);
    }
    
    private final Object packet;
    public BreakAnimationPacket(final int id, final BlockPosition pos, final int damage) 
    {
        packet = Debug.validateConstructor(constructor, id, pos.getNMSObject(), damage);
    }

    @Override
    public Object getNMSObject() 
    {
        return packet;
    }
}
