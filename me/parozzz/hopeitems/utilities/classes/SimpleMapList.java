/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities.classes;

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
    private final Map<String, String> values;
    public SimpleMapList(final List<Map<?, ?>> list)
    {
        values = list.stream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(e -> e.getKey().toString().toLowerCase(), e-> e.getValue().toString()));
    }
    
    public <T> T getValue(final String key, final Function<String, T> convert)
    {
        return Optional.ofNullable(values.get(key.toLowerCase())).map(convert).orElse(null);
    }
    
    public Map<String, String> getValues()
    {
        return new HashMap<>(values);
    }
}
