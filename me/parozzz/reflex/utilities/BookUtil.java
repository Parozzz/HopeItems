/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.utilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.NMS.ReflectionUtil;
import me.parozzz.reflex.NMS.entity.EntityPlayer;
import me.parozzz.reflex.NMS.itemStack.ItemNBT;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class BookUtil 
{
    private static final BiConsumer<ItemStack, Player> openBookConsumer;
    static
    {
        Class<?> nmsItemStack = ReflectionUtil.getNMSClass("ItemStack");
        if(MCVersion.V1_8.isEqual())
        {
            Method openBook = ReflectionUtil.getMethod(EntityPlayer.getNMSClass(), "openBook", nmsItemStack);
            
            openBookConsumer = (item, p) -> 
            {
                ItemStack hand = p.getItemInHand();
                p.setItemInHand(item);
                
                try {
                    openBook.invoke(EntityPlayer.getNMSPlayer(p).getNMSObject(), new ItemNBT(item).getNMSObject());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(BookUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                p.setItemInHand(hand);
            };
        }
        else
        {
            Class<?> enumHandClazz = ReflectionUtil.getNMSClass("EnumHand");
            Object enumHand = Stream.of(enumHandClazz.getEnumConstants()).filter(o -> o.toString().equals("MAIN_HAND")).findFirst().get();
            
            Method openBook = ReflectionUtil.getMethod(EntityPlayer.getNMSClass(), "a", nmsItemStack, enumHandClazz);
            
            openBookConsumer = (item, p) -> 
            {
                ItemStack hand = p.getInventory().getItemInMainHand();
                p.getInventory().setItemInMainHand(item);
                
                try {
                    openBook.invoke(EntityPlayer.getNMSPlayer(p).getNMSObject(), new ItemNBT(item).getNMSObject(), enumHand);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(BookUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                p.getInventory().setItemInMainHand(hand);
            };
        }
    }
    
    public static void openBook(final ItemStack item, final Player p)
    {
        openBookConsumer.accept(item.clone(), p);
    }
}
