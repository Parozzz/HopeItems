/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.inventories.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.parozzz.reflex.NMS.itemStack.ItemNBT;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.utilities.TaskUtil;
import me.parozzz.reflex.inventories.GUI;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class ShopGUI extends GUI
{
    protected static final String UID = "UID";
    protected final static String FUNCTION = "Function";

    public enum Function
    {
        SELL, BUY, NONE;
    }
    
    private final Map<String, Function> items;
    private final ShopGUI instance;
    public ShopGUI(final String title, final int rows) 
    {
        super(title, rows);
        this.onOpen(e -> Bukkit.getLogger().info("OPEN"));
        this.onClose(e -> Bukkit.getLogger().info("CLOSE"));
        items = new HashMap<>();
        instance = this;
    }
    
    public void addItem(final Function f, final ItemStack item)
    {
        UUID u = UUID.randomUUID();

        ItemNBT nbt = new ItemNBT(item);
        nbt.getTag().setString(UID, u.toString()).setString(FUNCTION, f.name());
        i.addItem(nbt.getBukkitItem());

        items.put(u.toString(), f);
    }
    
    public void setItem(final Function f, final int slot, final ItemStack item)
    {
        UUID u = UUID.randomUUID();

        ItemNBT nbt = new ItemNBT(item);
        nbt.getTag().setString(UID, u.toString()).setString(FUNCTION, f.name());
        i.setItem(slot, nbt.getBukkitItem());

        items.put(u.toString(), f);
    }
    
    @Override
    protected void onClick(InventoryClickEvent e) 
    {
        Optional.ofNullable(e.getCurrentItem()).map(ItemNBT::new).ifPresent(nbt -> 
        {
            NBTCompound tag = nbt.getTag();
            if(tag.hasKey(UID))
            {
                switch(items.get(tag.getString(UID)))
                {
                    case SELL:
                        tag.removeKey(UID);
                        tag.removeKey(FUNCTION);
                        new SellPage("Buy", nbt.getBukkitItem(), 10L)
                                .onClose(closeEvent -> TaskUtil.scheduleSync(1L, () -> open(closeEvent.getPlayer())))
                                .open(e.getWhoClicked());
                        break;
                    case BUY:
                        tag.removeKey(UID);
                        tag.removeKey(FUNCTION);
                        new BuyPage("Buy", nbt.getBukkitItem())
                                .onClose(closeEvent -> TaskUtil.scheduleSync(1L, () -> open(closeEvent.getPlayer())))
                                .open(e.getWhoClicked());
                        break;
                    case NONE:
                        e.setCancelled(true);
                        break;
                }
            }
        });
    }
    
}
