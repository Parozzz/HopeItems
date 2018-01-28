/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.mobs;

import me.parozzz.reflex.utilities.EntityUtil;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class MobEquipmentPart 
{
    private final EquipmentSlot slot;
    private final ItemStack itemStack;
    private final float chance;
    public MobEquipmentPart(final EquipmentSlot slot, final ItemStack itemStack, final float chance)
    {
        this.slot = slot;
        this.itemStack = itemStack;
        this.chance = chance;
    }
    
    public void addToEquipment(final EntityEquipment equip)
    {
        EntityUtil.addItem(equip, slot, itemStack, chance);
    }
}
