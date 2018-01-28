/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Paros
 */
public class ItemRegistry 
{
    private final static Map<String, ItemCollection> collections = new HashMap<>();
    public static void addCollection(final ItemCollection collection)
    {
        collections.put(collection.getId().toLowerCase(), collection);
    }
    
    public static Set<String> getIds()
    {
        return collections.keySet();
    }
    
    public static ItemCollection getCollection(final String id)
    {
        return collections.get(id.toLowerCase());
    }
    
    public static void clearCollections()
    {
        collections.clear();
    }
}
