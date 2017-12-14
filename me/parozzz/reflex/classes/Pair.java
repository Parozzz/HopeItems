/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.classes;

import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Paros
 * @param <K>
 * @param <V>
 */
    
public class Pair<K, V> implements Map.Entry<K,V>
{
    private final K key;
    private V value;
    public Pair(final K k,final V v) 
    {
        key=k;
        value=v;
    }

    @Override
    public K getKey() 
    {
        return key;
    }

    @Override
    public V getValue()
    {
        return value;
    }

    public <Z> Z getKey(final Function<K, Z> cast)
    {
        return cast.apply(key);
    }

    
    public <Z> Z getValue(final Function<V, Z> cast)
    {
        return cast.apply(value);
    }

    @Override
    public V setValue(V v) 
    {
        value=v;
        return v;
    }
}
