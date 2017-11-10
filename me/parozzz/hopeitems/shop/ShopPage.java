/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.shop;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopeitems.Configs;
import me.parozzz.hopeitems.Dependency;
import me.parozzz.hopeitems.items.HItem;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.shop.Shop.ShopMessage;
import me.parozzz.hopeitems.utilities.Debug;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.ItemNBT;
import me.parozzz.hopeitems.utilities.reflection.NBTTagManager.NBTType;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class ShopPage 
{
    public enum ShopFunction
    {
        NONE, BUY, SHOP, SELL;
    }
    
    private final static String FUNCTION_NBT="Function";
    
    private final static String SHOP_NBT="Shop";
    
    private final static String COST_NBT="Cost";
    private final static String CUSTOMITEM_NBT="CustomItem";
    
    private final String name;
    private final Inventory i;
    protected ShopPage(final String name, final ConfigurationSection path)
    {
        this.name=name;
        i = Bukkit.createInventory(null, path.getInt("rows")*9, Utils.color(path.getString("title")));
        
        ConfigurationSection iPath=path.getConfigurationSection("Items");
        iPath.getKeys(false).stream().map(iPath::getConfigurationSection).forEach(sPath -> 
        {
            Set<Integer> slots=Stream.of(sPath.getName().split(",")).map(Integer::valueOf).collect(Collectors.toSet());
            ItemStack item=Utils.getItemByPath(sPath);
            
            ShopFunction f = Debug.validateEnum(sPath.getString("function"), ShopFunction.class);
            
            ItemNBT nbt=new ItemNBT(item);
            NBTCompound tag=nbt.getTag();
            
            tag.setValue(FUNCTION_NBT, NBTType.STRING, f.name());
            
            switch(f)
            {
                case NONE:
                    break;
                case BUY:
                case SELL:
                    tag.setValue(COST_NBT, NBTType.DOUBLE, sPath.getDouble("money"));
                    tag.setValue(CUSTOMITEM_NBT, NBTType.STRING, sPath.getString("item"));
                    break;
                case SHOP:
                    tag.setValue(SHOP_NBT, NBTType.STRING, sPath.getString("shop"));
                    break;
            }
            
            item = nbt.setTag(tag).getBukkitItem();

            for(int slot:slots)
            {
                i.setItem(slot, item);
            }
        });
    }
    
    public String getName()
    {
        return name;
    }
    
    public Inventory getInventory()
    {
        return i;
    }
    
    public void onEvent(final InventoryClickEvent e)
    {
        e.setCancelled(true);
        if(!e.getInventory().equals(e.getClickedInventory()) || e.getCurrentItem().getType()==Material.AIR)
        {
            return;
        }
        
        ItemNBT nbt=new ItemNBT(e.getCurrentItem());
        NBTCompound tag=nbt.getTag();

        ShopFunction f=ShopFunction.valueOf(tag.getKey(FUNCTION_NBT, String.class));
        switch(f)
        {
            case SHOP:
                Optional.ofNullable(Shop.getInstance().getPageByName(tag.getKey(SHOP_NBT, String.class)))
                        .ifPresent(page -> e.getWhoClicked().openInventory(page.getInventory()));
                break;
            case SELL:
                Optional.ofNullable(Configs.getItemInfo(tag.getKey(CUSTOMITEM_NBT, String.class))).ifPresent(info -> 
                {
                    double cost=tag.getKey(COST_NBT, double.class);
                    
                    double toGive = Stream.of(e.getWhoClicked().getInventory().getContents()).filter(Objects::nonNull)
                            .map(HItem::new)
                            .filter(HItem::isValid)
                            .filter(h -> h.getStringId().equals(info.getName()))
                            .mapToDouble(hitem -> 
                            {
                                Utils.decreaseItemStack(hitem.getItem(), e.getWhoClicked().getInventory());
                                return hitem.getItem().getAmount() * cost;
                            }).sum();
                    Dependency.eco.depositPlayer((Player)e.getWhoClicked(), toGive);
                });
                break;
            case BUY:
                ItemInfo info=Configs.getItemInfo(tag.getKey(CUSTOMITEM_NBT, String.class));
                if(info!=null)
                {
                    double cost=tag.getKey(COST_NBT, double.class);
                    if(Dependency.eco.withdrawPlayer((Player)e.getWhoClicked(), cost).transactionSuccess())
                    {
                        e.getWhoClicked().getInventory().addItem(info.getItem().parse((Player)e.getWhoClicked(), e.getWhoClicked().getLocation()));

                        e.getWhoClicked().sendMessage(ShopMessage.ITEM_BOUGHT.getMessage().replace("{balance}", Objects.toString(Dependency.eco.getBalance((Player)e.getWhoClicked()))));
                    }
                    else
                    {
                        e.getWhoClicked().sendMessage(ShopMessage.LOW_MONEY.getMessage().replace("{money}", Objects.toString(cost)));
                    }
                }
                break;
        }
    }
}
