/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.MCVersion;
import me.parozzz.hopeitems.utilities.Utils;
import me.parozzz.hopeitems.utilities.reflection.API.ReflectionUtils;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.ItemNBT;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class BookGUI 
{
    private final BiConsumer<ItemStack, Player> openBook;
    protected BookGUI()
    {
        Class<?> nmsItemStack = ReflectionUtils.getNMSClass("ItemStack");
        Class<?> entityPlayer = ReflectionUtils.getNMSClass("EntityPlayer");
        if(MCVersion.V1_8.isEqual())
        {
            Method openBook = ReflectionUtils.getMethod(entityPlayer, "openBook", nmsItemStack);
            
            this.openBook = (item, p) -> 
            {
                ItemStack hand = p.getItemInHand();
                p.setItemInHand(item);
                
                try 
                {
                    openBook.invoke(API.getPacketManager().getHandle(p), new ItemNBT(item).getNMSObject());
                } 
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) 
                {
                    Logger.getLogger(BookGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                p.setItemInHand(hand);
            };
        }
        else
        {
            Class<?> enumHandClazz = ReflectionUtils.getNMSClass("EnumHand");
            Object enumHand = Stream.of(enumHandClazz.getEnumConstants()).filter(o -> o.toString().equals("MAIN_HAND")).findFirst().get();
            
            Method openBook = ReflectionUtils.getMethod(entityPlayer, "a", nmsItemStack, enumHandClazz);
            
            this.openBook = (item, p) -> 
            {
                ItemStack hand = p.getInventory().getItemInMainHand();
                p.getInventory().setItemInMainHand(item);
                
                try 
                {
                    openBook.invoke(API.getPacketManager().getHandle(p), new ItemNBT(item).getNMSObject(), enumHand);
                } 
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) 
                {
                    Logger.getLogger(BookGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                p.getInventory().setItemInMainHand(hand);
            };
        }
    }
    
    public void openBook(final ItemStack item, final Player p)
    {
        openBook.accept(item.clone(), p);
    }
}
