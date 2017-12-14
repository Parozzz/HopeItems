/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.classes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Paros
 */
public class ComplexMapList 
{
    private final Map<String, MapArray> arrays;
    public ComplexMapList(final List<Map<?, ?>> list)
    {
        arrays = list.stream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(e -> e.getKey().toString().toLowerCase(), e->
                {
                    String[] t =e.getValue().toString().split(",");
                    return new MapArray(t);
                }));
    }
    
    public MapArray getMapArray(final String key)
    {
        return arrays.get(key.toLowerCase());
    }
    
    public Map<String, MapArray> getMapArrays()
    {
        return new HashMap<>(arrays);
    }
}
