/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection.nbt.item;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.reflection.NBTTagManager.NBTType;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTCompound;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTList;

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
    
    private static Map<String, ItemAttribute> names;
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
            return Optional.ofNullable(names)
                    .orElseGet(() -> names=new HashMap<>(Stream.of(ItemAttribute.values()).collect(Collectors.toMap(i -> i.getName(), Function.identity()))))
                    .get(name);
        }
    }
    
    public enum Operation
    {
        ADD(0), PERCENTAGE_ADD(1), PERCENTAGE_MULTIPLY(2);
        
        private final int modifier;
        private Operation(final int modifier)
        {
            this.modifier=modifier;
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
        attributeList=new NBTList();
        randomUUID=UUID.randomUUID();
    }
    
    public ItemAttributeModifier(final NBTCompound compound)
    {
        randomUUID=UUID.randomUUID();
        
        if(compound.hasKey("AttributeModifiers"))
        {
            attributeList=compound.getList("AttributeModifiers", NBTType.COMPOUND);
        }
        else
        {
            attributeList=new NBTList();
        }
    }
    
    public ModifierSnapshot getModifier(final ItemAttribute attribute)
    {
        for(int j=0;j<attributeList.size();j++)
        {
            NBTCompound compound=new NBTCompound(attributeList.getTag(j));
            if(compound.getKey("AttributeName", String.class).equals(attribute.getName()))
            {
                return new ModifierSnapshot(attribute, 
                        compound.getKey("Amount", double.class),
                        Operation.getById(compound.getKey("Operation", int.class)),
                        AttributeSlot.valueOf(compound.getKey("Slot", String.class).toUpperCase()));
            }
        }
        return null;
    }
    
    public Set<ModifierSnapshot> getModifiers()
    {
        Set<ModifierSnapshot> set=new HashSet<>();
        for(int j=0;j<attributeList.size();j++)
        {
            NBTCompound compound=new NBTCompound(attributeList.getTag(j));
            
            set.add(new ModifierSnapshot(ItemAttribute.getByName(compound.getKey("AttributeName", String.class)), 
                    compound.getKey("Amount", double.class),
                    Operation.getById(compound.getKey("Operation", int.class)),
                    AttributeSlot.valueOf(compound.getKey("Slot", String.class).toUpperCase())));
        }
        return set;
    }
    
    public void overrideModifier(final ItemAttribute attribute, final double value)
    {
        for(int j=0;j<attributeList.size();j++)
        {
            NBTCompound compound=new NBTCompound(attributeList.getTag(j));
            
            if(compound.getKey("AttributeName", String.class).equals(attribute.getName()))
            {
                compound.setValue("Amount", NBTType.DOUBLE, value);
                return;
            }
        }
    }
    
    public void addSnapshot(final ModifierSnapshot snap)
    {
        NBTCompound modifierCompound=new NBTCompound();
        modifierCompound.setValue("AttributeName", NBTType.STRING, snap.getType().getName());
        modifierCompound.setValue("Name", NBTType.STRING, snap.getType().getName());
        modifierCompound.setValue("Amount", NBTType.DOUBLE, snap.getAmount());
        modifierCompound.setValue("Operation", NBTType.INT, snap.getOperation().getModifier());
        modifierCompound.setValue("UUIDLeast", NBTType.LONG, randomUUID.getLeastSignificantBits());
        modifierCompound.setValue("UUIDMost", NBTType.LONG, randomUUID.getMostSignificantBits());
        modifierCompound.setValue("Slot", NBTType.STRING, snap.getSlot().name().toLowerCase());
        attributeList.addTag(modifierCompound);
    }
    
    public void addModifier(final AttributeSlot slot, final ItemAttribute attribute, final Operation op, final double value)
    {
        NBTCompound modifierCompound=new NBTCompound();
        modifierCompound.setValue("AttributeName", NBTType.STRING, attribute.getName());
        modifierCompound.setValue("Name", NBTType.STRING, attribute.getName());
        modifierCompound.setValue("Amount", NBTType.DOUBLE, value);
        modifierCompound.setValue("Operation", NBTType.INT, op.getModifier());
        modifierCompound.setValue("UUIDLeast", NBTType.LONG, randomUUID.getLeastSignificantBits());
        modifierCompound.setValue("UUIDMost", NBTType.LONG, randomUUID.getMostSignificantBits());
        modifierCompound.setValue("Slot", NBTType.STRING, slot.name().toLowerCase());
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
