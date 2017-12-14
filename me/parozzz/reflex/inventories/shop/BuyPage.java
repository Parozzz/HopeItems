/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.inventories.shop;

import java.util.Optional;
import me.parozzz.reflex.NMS.itemStack.ItemNBT;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.classes.builders.ItemBuilder;
import me.parozzz.reflex.inventories.GUI;
import me.parozzz.reflex.utilities.HeadUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class BuyPage extends GUI
{
    private static final ItemStack[] buyContents = new ItemStack[9];
    static
    {
        
        ItemNBT plusNBT = new ItemNBT(HeadUtil.createHead("http://textures.minecraft.net/texture/b056bc1244fcff99344f12aba42ac23fee6ef6e3351d27d273c1572531f"));
        plusNBT.getTag().setString("Function", "Plus");
        
        buyContents[6] = new ItemBuilder(plusNBT.getBukkitItem()).amount(1).build();
        buyContents[7] = new ItemBuilder(plusNBT.getBukkitItem()).amount(16).build();
        buyContents[8] = new ItemBuilder(plusNBT.getBukkitItem()).amount(64).build();
        
        ItemNBT minusNBT = new ItemNBT(HeadUtil.createHead("http://textures.minecraft.net/texture/4e4b8b8d2362c864e062301487d94d3272a6b570afbf80c2c5b148c954579d46"));
        minusNBT.getTag().setString("Function", "Minus");
        
        buyContents[0] = new ItemBuilder(minusNBT.getBukkitItem()).amount(64).build();
        buyContents[1] = new ItemBuilder(minusNBT.getBukkitItem()).amount(16).build();
        buyContents[2] = new ItemBuilder(minusNBT.getBukkitItem()).amount(1).build();
    }
    
    public BuyPage(final String title, final ItemStack toBuy) 
    {
        super(title, 1);
        
        i.setContents(buyContents);

        setItem(toBuy);
    }

    private void setItem(final ItemStack item)
    {
        ItemNBT nbt = new ItemNBT(item);
        nbt.getTag().setString("Function", "Buy");
        i.setItem(4, nbt.getBukkitItem());
    }

    private ItemStack getItem()
    {
        return i.getItem(4);
    }

    @Override
    protected void onClick(InventoryClickEvent e) 
    {
        Optional.ofNullable(e.getCurrentItem()).map(ItemNBT::new).ifPresent(nbt -> 
        {
            NBTCompound tag = nbt.getTag();
            if(tag.hasKey("Function"))
            {
                switch(tag.getString("Function"))
                {
                    case "Buy":
                        tag.removeKey("Function");
                        tag.removeKey(ShopGUI.FUNCTION);
                        tag.removeKey(ShopGUI.UID);
                        
                        e.getWhoClicked().getInventory().addItem(nbt.getBukkitItem());
                        break;
                    case "Plus":
                        getItem().setAmount(getItem().getAmount() + e.getCurrentItem().getAmount() > 64 ? 64 : getItem().getAmount() + e.getCurrentItem().getAmount());
                        break;
                    case "Minus":
                        getItem().setAmount(getItem().getAmount() - e.getCurrentItem().getAmount() < 1 ? 1 : getItem().getAmount() - e.getCurrentItem().getAmount());
                        break;
                }
            }
        });
    }
        
}
