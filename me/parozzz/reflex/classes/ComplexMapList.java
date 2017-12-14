/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Paros
 */
public class ComplexMapList 
{
    private final Map<String, List<MapArray>> arrays;
    public ComplexMapList(final List<Map<?, ?>> list)
    {
        arrays = new HashMap<>();
        list.stream().map(Map::entrySet).forEach(set -> 
        {
            set.forEach(e -> arrays.computeIfAbsent(e.getKey().toString().toLowerCase(), temp -> new ArrayList<>()).add(new MapArray(e.getValue().toString().split(","))));
        });
    }
    
    public List<MapArray> getMapArrays(final String key)
    {
        return arrays.get(key.toLowerCase());
    }
    
    public Map<String, List<MapArray>> getMapArrays()
    {
        return new HashMap<>(arrays);
    }
}
