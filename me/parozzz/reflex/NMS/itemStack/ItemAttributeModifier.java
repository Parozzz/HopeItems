/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.itemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.NMS.nbt.NBTList;
import me.parozzz.reflex.NMS.nbt.NBTType;

/**
 *
 * @author Paros
 */
public class ItemAttributeModifier 
{
    public enum AttributeSlot
    {
        MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD;
    }
    
    private final static Map<String, ItemAttribute> ATTRIBUTE_NAMES = Stream.of(ItemAttribute.values()).collect(Collectors.toMap(i -> i.getName(), Function.identity()));
    public enum ItemAttribute
    {
        MAX_HEALTH("generic.maxHealth"),
        KNOCKBACK_RESISTANCE("generic.knockbackResistance"),
        MOVEMENT_SPEED("generic.movementSpeed"),
        ATTACK_DAMAGE("generic.attackDamage"),
        ATTACK_SPEED("generic.attackSpeed"),
        ARMOR("generic.armor"),
        ARMOR_TOUGHTNESS("generic.armorToughness"),
        LUCK("generic.luck");
        
        private final String name;
        private ItemAttribute(final String name)
        {
            this.name=name;
        }
        
        public String getName()
        {
            return name;
        }
        
        public static ItemAttribute getByName(final String name)
        {
            return ATTRIBUTE_NAMES.get(name);
        }
    }
    
    public enum Operation
    {
        ADD(0), PERCENTAGE_ADD(1), PERCENTAGE_MULTIPLY(2);
        
        private final int modifier;
        private Operation(final int modifier)
        {
            this.modifier = modifier;
        }
        
        public int getModifier()
        {
            return modifier;
        }
        
        public static Operation getById(final int id)
        {
            switch(id)
            {
                case 0:
                    return ADD;
                case 1:
                    return PERCENTAGE_ADD;
                case 2:
                    return PERCENTAGE_MULTIPLY;
                default:
                    return null;
            }
        }
    }
    
    
    private final UUID randomUUID;
    private final NBTList attributeList;
    public ItemAttributeModifier()
    {
        attributeList = new NBTList();
        randomUUID = UUID.randomUUID();
    }
    
    public ItemAttributeModifier(final NBTCompound compound)
    {
        randomUUID = UUID.randomUUID();
        attributeList = compound.hasKey("AttributeModifiers") ? compound.getList("AttributeModifiers", NBTType.COMPOUND) : new NBTList();
    }
    
    public ModifierSnapshot getModifier(final ItemAttribute attribute)
    {
        return IntStream.range(0, attributeList.size())
                .mapToObj(attributeList::getTag)
                .map(NBTCompound::new)
                .filter(compound -> compound.getString("AttributeName").equals(attribute.getName()))
                .findAny()
                .map(compound -> new ModifierSnapshot(attribute, 
                        compound.getDouble("Amount"),
                        Operation.getById(compound.getInt("Operation")),
                        AttributeSlot.valueOf(compound.getString("Slot").toUpperCase())))
                .orElse(null);
    }
    
    public Set<ModifierSnapshot> getModifiers()
    {
        return IntStream.range(0, attributeList.size())
                .mapToObj(attributeList::getTag)
                .map(NBTCompound::new)
                .map(compound -> new ModifierSnapshot(ItemAttribute.getByName(compound.getString("AttributeName")), 
                        compound.getDouble("Amount"),
                        Operation.getById(compound.getInt("Operation")),
                        AttributeSlot.valueOf(compound.getString("Slot").toUpperCase())))
                .collect(Collectors.toSet());
    }
    
    public void overrideModifier(final ItemAttribute attribute, final double value)
    {
        for(int j=0;j<attributeList.size();j++)
        {
            NBTCompound compound = new NBTCompound(attributeList.getTag(j));
            
            if(compound.getString("AttributeName").equals(attribute.getName()))
            {
                compound.setDouble("Amount", value);
                return;
            }
        }
    }
    
    public void addSnapshot(final ModifierSnapshot snap)
    {
        addModifier(snap.getSlot(), snap.getType(), snap.getOperation(), snap.getAmount());
    }
    
    public void addModifier(final AttributeSlot slot, final ItemAttribute attribute, final Operation op, final double value)
    {
        NBTCompound modifierCompound = new NBTCompound();
        modifierCompound.setString("AttributeName", attribute.getName());
        modifierCompound.setString("Name", attribute.getName());
        modifierCompound.setDouble("Amount", value);
        modifierCompound.setInt("Operation", op.getModifier());
        modifierCompound.setLong("UUIDLeast", randomUUID.getLeastSignificantBits());
        modifierCompound.setLong("UUIDMost", randomUUID.getMostSignificantBits());
        modifierCompound.setString("Slot", slot.name().toLowerCase());
        attributeList.addTag(modifierCompound);
    }
    
    public void apply(final NBTCompound compound)
    {
        compound.setTag("AttributeModifiers", attributeList);
    }
    
    public class ModifierSnapshot
    {
        private final ItemAttribute attribute;
        private final double amount;
        private final Operation op;
        private final AttributeSlot slot;
        public ModifierSnapshot(final ItemAttribute attr, final double amount, final Operation op, final AttributeSlot slot)
        {
            attribute=attr;
            this.amount=amount;
            this.op=op;
            this.slot=slot;
        }
        
        public ItemAttribute getType()
        {
            return attribute;
        }
        
        public double getAmount()
        {
            return amount;
        }
        
        public Operation getOperation()
        {
            return op;
        }
        
        public AttributeSlot getSlot()
        {
            return slot;
        }
    }
}
