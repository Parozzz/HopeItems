/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.utilities;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.reflex.Debug;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.NMS.itemStack.AdventureTag;
import me.parozzz.reflex.NMS.itemStack.ItemAttributeModifier;
import me.parozzz.reflex.NMS.itemStack.ItemNBT;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.classes.ComplexMapList;
import me.parozzz.reflex.classes.MapArray;
import me.parozzz.reflex.classes.SimpleMapList;
import me.parozzz.reflex.utilities.Util.ColorEnum;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

/**
 *
 * @author Paros
 */
public class ItemUtil 
{
    
    private static Predicate<ItemStack> isUnbreakable;
    public static boolean isUnbreakable(final ItemStack item)
    {
        return Optional.ofNullable(isUnbreakable).orElseGet(() -> 
        {
            if(MCVersion.V1_8.isEqual())
            {
                isUnbreakable = i -> false;
            }
            else if(MCVersion.contains(MCVersion.V1_9, MCVersion.V1_10))
            {
                isUnbreakable = i -> i.getItemMeta().spigot().isUnbreakable();
            }
            else
            {
                isUnbreakable = i -> i.getItemMeta().isUnbreakable();
            }
            return isUnbreakable;
        }).test(item);
    }
    
    private static BiConsumer<ItemMeta, Boolean> setUnbreakable;
    public static void setUnbreakable(final ItemMeta meta, final boolean unbreakable)
    {
        Optional.ofNullable(setUnbreakable).orElseGet(() -> 
        {
            if(MCVersion.V1_8.isEqual())
            {
                setUnbreakable = (m,b) -> {};
            }
            else if(MCVersion.contains(MCVersion.V1_9, MCVersion.V1_10))
            {
                setUnbreakable = (m,b) -> m.spigot().setUnbreakable(b);
            }
            else
            {
                setUnbreakable = (m,b) -> m.setUnbreakable(b);
            }
            
            return setUnbreakable;
        }).accept(meta, unbreakable);
    }
    
    public static ItemStack getItemByPath(final ConfigurationSection path)
    { 
        return getItemByPath(null, (short)0, path);
    }
    
    public static ItemStack getItemByPath(final Material id, final short data, final ConfigurationSection path)
    {
        ItemStack item;
        ItemMeta meta;

        String type= id==null ? path.getString("id").toUpperCase() : id.name();
        switch(type)
        {
            case "SKULL_ITEM":
                if(path.contains("url"))
                {
                    if(path.getInt("data", data) == 3)
                    {
                        item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
                        meta = item.getItemMeta();
                        HeadUtil.addTexture((SkullMeta)meta, path.getString("url"));
                    }
                    else
                    {
                        throw new IllegalArgumentException("You need to set data to 3 for custom url heads");
                    }
                }
                else
                {
                    item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
                    meta = item.getItemMeta();
                }
                break;
            case "SPLASH_POTION":
            case "POTION":
            case "LINGERING_POTION":
            case "TIPPED_ARROW":
                if(MCVersion.V1_8.isEqual())
                {
                    Potion potion=new Potion(Debug.validateEnum(path.getString("type", "WATER"), PotionType.class));

                    if(type.equals("SPLASH_POTION"))
                    {
                        potion.splash();
                    }
                    
                    item=potion.toItemStack(1);
                }
                else
                {
                    item=new ItemStack(Material.valueOf(type));
                }

                meta=item.getItemMeta();
                if(path.contains("color") && MCVersion.V1_11.isHigher())
                { 
                    ((PotionMeta)meta).setColor(Debug.validateEnum(path.getString("color"), ColorEnum.class).getBukkitColor()); 
                }

                for(Iterator<String[]> it=path.getStringList("effect").stream().map(str -> str.split(":")).iterator();it.hasNext();)
                {
                    String[] array=it.next();

                    PotionEffectType pet=PotionEffectType.getByName(array[0].toUpperCase());
                    if(pet==null)
                    {
                        throw new IllegalArgumentException("A potion effect named "+array[0]+" does not exist");
                    }
                    ((PotionMeta)meta).addCustomEffect(new PotionEffect(pet,Integer.parseInt(array[1]),Integer.parseInt(array[2])), true);
                }
                break;
            case "MONSTER_EGG":
                EntityType et = Debug.validateEnum(path.getString("data" , "PIG"), EntityType.class);

                if(MCVersion.V1_8.isEqual()) 
                {
                    item=new SpawnEgg(et).toItemStack(1); 
                    meta=item.getItemMeta();
                    break;
                }

                item = new ItemStack(Material.MONSTER_EGG);
                if(MCVersion.V1_9.isEqual() || MCVersion.V1_10.isEqual()) 
                { 
                    meta = ItemNBT.setSpawnedType(item, et).getItemMeta(); 
                }
                else 
                {
                    meta = item.getItemMeta();
                    ((SpawnEggMeta)meta).setSpawnedType(et);   
                } 
                break;
            case "LEATHER_BOOTS":
            case "LEATHER_CHESTPLATE":
            case "LEATHER_HELMET":
            case "LEATHER_LEGGINGS":
                item = new ItemStack(Material.valueOf(type));
                meta = item.getItemMeta();
                
                if(path.contains("color"))
                {
                    Color c = Debug.validateEnum(path.getString("color"), ColorEnum.class).getBukkitColor();
                    ((LeatherArmorMeta)meta).setColor(c);
                }
                break;
            default:
                item=new ItemStack(Debug.validateEnum(type, Material.class), 1, (short)path.getInt("data", data));
                meta=item.getItemMeta();
                break;
        }
        
        meta.setDisplayName(Util.cc(path.getString("name", "")));
        meta.setLore(path.getStringList("lore").stream().map(Util::cc).collect(Collectors.toList()));
        meta.addItemFlags(path.getStringList("flag").stream().map(str -> Debug.validateEnum(str, ItemFlag.class)).toArray(ItemFlag[]::new));
        setUnbreakable(meta, path.getBoolean("unbreakable", false));

        item.setItemMeta(meta);
        item.setAmount(path.getInt("amount", 1));

        path.getMapList("enchant").stream().map(map -> (Map<String, Integer>)map).map(Map::entrySet).flatMap(Set::stream).forEach(e -> 
        {
            Enchantment ench=Enchantment.getByName(e.getKey().toUpperCase());
            if(ench==null)
            {
                throw new IllegalArgumentException("An enchantment with name "+e.getKey()+" does not exist");
            }
            item.addUnsafeEnchantment(ench, e.getValue());
        });
        
        ItemNBT nbt=new ItemNBT(item);
        NBTCompound compound=nbt.getTag();

        new SimpleMapList(path.getMapList("tag")).getValues().forEach((key, value) -> compound.setString(key, value.get(0)));
        
        new SimpleMapList(path.getMapList("adventure")).getValues().forEach((key, list) -> 
        {
            AdventureTag tag = Debug.validateEnum(key, AdventureTag.class);
            ItemNBT.setAdventureFlag(compound, tag, Stream.of(list.get(0).split(",")).map(str -> Debug.validateEnum(str, Material.class)).toArray(Material[]::new));
        });
        
        if(path.contains("Attribute"))
        {
            ItemAttributeModifier modifier=new ItemAttributeModifier();
            new ComplexMapList(path.getMapList("Attribute")).getMapArrays().forEach((attr, list) -> 
            {
                ItemAttributeModifier.ItemAttribute attribute=Debug.validateEnum(attr, ItemAttributeModifier.ItemAttribute.class);
                
                MapArray map = list.get(0);
                double value = map.getValue("value", Double::valueOf);
                ItemAttributeModifier.Operation op=ItemAttributeModifier.Operation.getById(map.getValue("operation", Integer::valueOf));
                ItemAttributeModifier.AttributeSlot slot=Debug.validateEnum(Optional.ofNullable(map.getValue("slot", Function.identity())).orElse("MAINHAND"), ItemAttributeModifier.AttributeSlot.class);

                modifier.addModifier(slot, attribute, op, value);
            });
            modifier.apply(compound);
        }
        
        return nbt.setTag(compound).getBukkitItem();
    }
    
    public static void parseItemVariable(final ItemStack item, final String placeholder, final Object replace)
    {
        ItemMeta meta = item.getItemMeta();
        parseMetaVariable(meta, placeholder, replace);
        item.setItemMeta(meta);
    }
    
    public static void parseMetaVariable(final ItemMeta meta, final String placeholder, final Object replace)
    {
        if(meta.hasDisplayName())
        {
            meta.setDisplayName(Util.cc(meta.getDisplayName().replace(placeholder, replace.toString())));
        }
       
        if(meta.hasLore())
        {
            meta.setLore(meta.getLore().stream().map(lore -> lore.replace(placeholder, replace.toString())).map(Util::cc).collect(Collectors.toList()));
        }
    }
    
    public static void decreaseItemStack(final ItemStack item, final Player p, final Inventory i)
    {
        decreaseItemStack(item, i);
        p.updateInventory();
    }
    
    public static void decreaseItemStack(final ItemStack item, final Inventory i)
    {
        if(item.getAmount() == 1)
        {
            i.removeItem(item);
            return;
        }
        
        item.setAmount(item.getAmount() - 1);
    }
    
    public static String getItemStackName(final ItemStack item)
    {
        if(item == null || !item.hasItemMeta())
        {
            return Material.AIR.name();
        }
        
        return getMetaName(item.getType(), item.getItemMeta());
    }
    
    public static String getMetaName(final Material type, final ItemMeta meta)
    {
        return meta.hasDisplayName() ? meta.getDisplayName() : type.name();
    }
    
}
