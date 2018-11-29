/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 *
 * @author Paros
 * @param <T>
 */
public abstract class AbstractRegistry<T> 
{
    private final Map<String, T> map;
    public AbstractRegistry()
    {
        map = new HashMap<>();
    }
    
    public void addRegistered(final String key, final T t)
    {
        map.put(key.toLowerCase(), t);
    }
    
    public @Nullable T getRegistered(final String key)
    {
        return map.get(key.toLowerCase());
    }
    
    public Set<String> getAllKeys()
    {
        return map.keySet();
    }
    
    public Map<String, T> getView()
    {
        return Collections.unmodifiableMap(map);
    }
}
