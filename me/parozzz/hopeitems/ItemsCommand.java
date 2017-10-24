/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopeitems.shop.Shop;
import me.parozzz.hopeitems.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class ItemsCommand implements CommandExecutor
{
    public enum CommandMessageEnum
    {
        WRONG_COMMAND, WRONG_ITEM, PLAYER_OFFLINE, ITEM_RECEIVED, ITEM_SENT, RELOADED, RELOAD_FAIL, SHOP_INEXISTENT;
        
        @Override
        public String toString()
        {
            return Configs.otherMessages.get(this);
        }
    }
    
    public enum CommandEnum
    {
        GETITEM("hopeitems.command.getitem", Player.class::isInstance),
        GIVEITEM("hopeitems.command.giveitem", cs -> true),
        LIST("hopeitems.command.list", cs -> true),
        SHOP("hopeitems.command.shop", cs -> cs instanceof Player && Dependency.isEconomyHooked()),
        SHOPLIST("hopeitems.command.shoplist", cs -> cs instanceof Player && Dependency.isEconomyHooked()),
        RELOAD("hopeitems.command.reload", cs -> true);
        
        private final String perm;
        private final Predicate<CommandSender> canUse;
        private CommandEnum(final String perm, final Predicate<CommandSender> canUse)
        {
            this.perm=perm;
            this.canUse=canUse;
        }
        
        public void sendHelp(final CommandSender cs)
        {
            if(canUse(cs))
            {
                cs.sendMessage(getHelp());
            }
        }
        
        public boolean canUse(final CommandSender cs)
        {
            return cs.hasPermission(perm) && canUse.test(cs);
        }
        
        public String getPermission()
        {
            return perm;
        }
        
        public String getHelp()
        {
            return Configs.helpMessages.get(this);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] val) 
    {
        if(val.length==0)
        {
            Stream.of(CommandEnum.values()).forEach(ce -> ce.sendHelp(cs));
        }
        else
        {
            CommandEnum ce;
            try
            {
                ce=CommandEnum.valueOf(val[0].toUpperCase());
            }
            catch(IllegalArgumentException ex)
            {
                cs.sendMessage(CommandMessageEnum.WRONG_COMMAND.toString());
                return true;
            }
            
            if(!ce.canUse(cs))
            {
                return true;
            }
            
            switch(ce)
            {
                case RELOAD:
                    try 
                    {
                        JavaPlugin.getPlugin(HopeItems.class).reload();
                        cs.sendMessage(CommandMessageEnum.RELOADED.toString());
                    } 
                    catch (FileNotFoundException | UnsupportedEncodingException ex) 
                    {
                        Logger.getLogger(ItemsCommand.class.getName()).log(Level.SEVERE, null, ex);
                        cs.sendMessage(CommandMessageEnum.RELOAD_FAIL.toString());
                    }
                    break;
                case SHOPLIST:
                    cs.sendMessage(ChatColor.WHITE+Shop.getInstance().pageNames().stream().collect(Collectors.joining(", ")));
                    break;
                case SHOP:
                    if(val.length!=2)
                    {
                        cs.sendMessage(ce.getHelp());
                        return true;
                    }
                    
                    Optional.ofNullable(Shop.getInstance().getPageByName(val[1])).map(page -> 
                    {
                        if(cs.hasPermission(new StringBuilder().append(ce.getPermission()).append(".").append(page.getName().toLowerCase()).toString()))
                        {
                            ((Player)cs).openInventory(page.getInventory());
                        }
                        return page;
                    }).orElseGet(() -> 
                    {
                        cs.sendMessage(CommandMessageEnum.SHOP_INEXISTENT.toString());
                        return null;
                    });
                    break;
                case LIST:
                    cs.sendMessage(ChatColor.WHITE+Configs.getItemNames().stream().collect(Collectors.joining(", ")));
                    break;
                case GETITEM:
                    if(val.length<2)
                    {
                        cs.sendMessage(ce.getHelp());
                        return true;
                    }
                    
                    this.giveItemStack(cs, (Player)cs, val[1], val.length==3 && Utils.isNumber(val[2])? Integer.valueOf(val[2]): 1);
                    break;
                case GIVEITEM:
                    if(val.length<3)
                    {
                        cs.sendMessage(ce.getHelp());
                        return true;
                    }
                    
                    Optional.ofNullable(Bukkit.getPlayer(val[1])).map(p -> 
                    {
                        this.giveItemStack(cs, p, val[2], val.length==4 && Utils.isNumber(val[3])? Integer.valueOf(val[3]): 1);
                        return p;
                    }).orElseGet(() -> 
                    {
                        cs.sendMessage(CommandMessageEnum.PLAYER_OFFLINE.toString());
                        return null;
                    });
            }
        }
        return true;
    }
    
    private void giveItemStack(final CommandSender cs, final Player p, final String name, final int amount)
    {
        Optional.ofNullable(Configs.getItemInfo(name)).map(info -> 
        {
            ItemStack item=info.getItem().parse(p, p.getLocation());
            item.setAmount(amount);
            
            p.getInventory().addItem(item);
            
            p.sendMessage(CommandMessageEnum.ITEM_RECEIVED.toString()
                    .replace("%name%", item.hasItemMeta() && item.getItemMeta().hasDisplayName()? item.getItemMeta().getDisplayName() : item.getType().name())
                    .replace("%amount%", Objects.toString(item.getAmount())));
            
            if(!p.equals(cs))
            {
                cs.sendMessage(CommandMessageEnum.ITEM_SENT.toString()
                        .replace("%name%", info.getName())
                        .replace("%amount%", Objects.toString(item.getAmount()))
                        .replace("%player%", p.getName()));
            }
            return item;
        }).orElseGet(() -> 
        {
            cs.sendMessage(CommandMessageEnum.WRONG_ITEM.toString());
            return null;
        });
    }
    
}
