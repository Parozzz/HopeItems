/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Paros
 */
public class ItemRegistry 
{
    private final static Map<String, ItemCollection> collections = new HashMap<>();
    public static void addCollection(final @Nonnull ItemCollection collection)
    {
        if(collection == null)
        {
            return;
        }
        
        collections.put(collection.getId().toLowerCase(), collection);
    }
    
    public static Set<String> getIds()
    {
        return collections.keySet();
    }
    
    public static @Nullable ItemCollection getCollection(final @Nonnull String id)
    {
        if(id == null || id.isEmpty())
        {
            return null;
        }
        
        return collections.get(id.toLowerCase());
    }
    
    public static void clearCollections()
    {
        collections.clear();
    }
}
