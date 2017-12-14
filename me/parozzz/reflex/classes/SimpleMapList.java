/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Paros
 */
public class SimpleMapList
{
    private final Map<String, List<String>> values;
    public SimpleMapList(final List<Map<?, ?>> list)
    {
        values = new HashMap<>();
        list.stream().map(Map::entrySet).forEach(set -> 
        {
            set.forEach(e -> values.computeIfAbsent(e.getKey().toString().toLowerCase(), temp -> new ArrayList<>()).add(e.getValue().toString()));
        });
    }
    
    public <T> T getValue(final String key, final int i, final Function<String, T> convert)
    {
        return Optional.ofNullable(values.get(key.toLowerCase())).map(list -> list.get(i)).map(convert).orElse(null);
    }
    
    public <A,B> Map<A, List<B>> getConvertedValues(final Function<String, A> convertKey, final Function<String, B> convertValue)
    {
        return values.entrySet().stream().collect(Collectors.toMap(e -> convertKey.apply(e.getKey()), e -> e.getValue().stream().map(convertValue).collect(Collectors.toList())));
    }
    
    public <A,B> Map<A, B> getConvertedValues(final Function<String, A> convertKey, final Function<String, B> convertValue, final int i)
    {
        return values.entrySet().stream().collect(Collectors.toMap(e -> convertKey.apply(e.getKey()), e -> convertValue.apply(e.getValue().get(i))));
    }
    
    public Map<String, List<String>> getValues()
    {
        return new HashMap<>(values);
    }
}
