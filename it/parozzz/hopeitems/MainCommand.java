/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems;

import it.parozzz.hopeitems.core.CommandUtils;
import it.parozzz.hopeitems.core.ItemBuilder;
import it.parozzz.hopeitems.core.ItemDatabase;
import it.parozzz.hopeitems.core.Language;
import it.parozzz.hopeitems.core.Utils;
import java.io.IOException;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class MainCommand 
        implements CommandExecutor
{

    private final JavaPlugin pl;
    private final HopeItems hi;
    private final Shop shop;
    public MainCommand(final JavaPlugin pl, final HopeItems hi, final Shop shop)
    {
        this.pl=pl;
        this.hi=hi;
        this.shop=shop;
        
        parseItemList(Database.getDatabase());
    }
    
    private String itemList;
    private void parseItemList(final ItemDatabase id)
    {
        itemList=ChatColor.GREEN+"Items: "+ChatColor.GRAY+id.getKeyMap().keySet().stream().map(Object::toString).collect(Collectors.joining(", "));
    }
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] val) 
    {
        if(val.length==0) 
        { 
            if(shop!=null) { Language.sendWithPermission(cs, "shopCommand", Permission.command_shop); }
            return Language.sendMultipleMessageWithPermission(cs,Permission.command_admin,"reloadCommand","listCommand","removeCommand","getCommand","giveCommand","giveallCommand"); 
        }
        
        if(cs.hasPermission(Permission.command_admin))
        {
            ItemStack item;
            Player p;
            switch(val[0].toLowerCase())
            {
                case "reload":
                    try { hi.cLoad(true); } 
                    catch (IOException | InvalidConfigurationException ex) { ex.printStackTrace(); }
                    return Language.sendMessage(cs, "onReload");
                case "list":
                    cs.sendMessage(itemList);
                    return true;
                case "remove":
                    if(!CommandUtils.hasPermission(cs, Permission.command_admin)) { return true; }
                    else if(val.length<=2) { return Language.sendMessage(cs, "removeCommand"); }
                    
                    p=Bukkit.getPlayer(val[1]);
                    if(p==null) { return Language.sendMessage(cs, "playerNotFound"); }
                    
                    item=Database.getItem(val[2]);
                    if(item==null) { return Language.sendMessage(cs, "wrongItem"); }
                    
                    ItemBuilder ib=new ItemBuilder(item.clone());
                    if(val.length==4 && Utils.isNumber(val[3])) { ib.setAmount(Integer.parseInt(val[3])); }
                    p.getInventory().removeItem(ib.build());
                    return Language.sendParsedMessage(cs, "onItemRemove","%itemname%",item.getItemMeta().hasDisplayName()?item.getItemMeta().getDisplayName():item.getType().name().toLowerCase());
                case "get":
                    if(!CommandUtils.isPlayer(cs) || !CommandUtils.hasPermission(cs, Permission.command_admin)) { return true; }
                    else if(val.length==1) { return Language.sendMessage(cs, "getCommand"); }
                    
                    switch(Utils.giveCommand((Player)cs, Database.getItem(val[1]), val.length==3?val[2]:null))
                    {
                        case WRONGITEM: return Language.sendMessage(cs, "wrongItem");
                        default: return true;
                    }
                case "give":
                    if(!CommandUtils.hasPermission(cs, Permission.command_admin)) { return true; }
                    else if(val.length<3) { return Language.sendMessage(cs, "giveCommand"); }
                    
                    item=Database.getItem(val[2]);
                    switch(Utils.giveCommand(Bukkit.getPlayer(val[1]), item, val.length==4?val[3]:null))
                    {
                        case WRONGITEM: return Language.sendMessage(cs, "wrongItem");
                        case PLAYEROFFLINE: return Language.sendMessage(cs, "playerNotFound");
                        case ITEMGIVEN: return Language.sendParsedMessage(Bukkit.getPlayer(val[1]), "itemReceived","%itemname%",item.getItemMeta().hasDisplayName()?item.getItemMeta().getDisplayName():item.getType().name().toLowerCase().replace("_", " "));
                        case FULLINVENTORY: return Language.sendMessage(Bukkit.getPlayer(val[1]), "inventoryFull");
                        default: return true;
                    }
                case "giveall":
                    if(!CommandUtils.hasPermission(cs, Permission.command_admin)) { return true; }
                    else if(val.length<2) { return Language.sendMessage(cs, "giveallCommand"); }
                    
                    item=Database.getItem(val[1]);
                    if(item==null) { return Language.sendMessage(cs, "wrongItem"); }
                    Bukkit.getOnlinePlayers().forEach(pl -> 
                    {
                        switch(Utils.giveCommand(pl, item, val.length==4?val[3]:null))
                        {
                            case ITEMGIVEN: 
                                Language.sendParsedMessage(pl, "itemReceived","%itemname%",item.getItemMeta().hasDisplayName()?item.getItemMeta().getDisplayName():item.getType().name().toLowerCase().replace("_", " "));
                                break;
                            case FULLINVENTORY: 
                                Language.sendMessage(pl, "inventoryFull");
                                break;
                        }
                    });
                    return Language.sendParsedMessage(cs, "giveallGiven","%itemname%",item.getItemMeta().hasDisplayName()?item.getItemMeta().getDisplayName():item.getType().name().toLowerCase().replace("_", " "));
            }
        }
        
        switch(val[0].toLowerCase())
        {
            case "shop":
                if(!CommandUtils.isPlayer(cs) || !CommandUtils.hasPermission(cs, Permission.command_shop)) { return true; }
                else if(shop==null) { return Language.sendWithPermission(cs, "shopNotEnabled",Permission.command_shop); }
                shop.openShop((Player)cs);
                return true;
            default:
                return true;
        }
    }
    
}
