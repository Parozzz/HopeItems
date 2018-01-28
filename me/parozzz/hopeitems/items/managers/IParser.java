/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers;

import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Paros
 */
public interface IParser
{
    public void registerDefaultSpecificParsers();
    //public void addSpecificParser(final E e, final String key, ISpecificParser<? , T> specificParser);
    public IManager parse(final ConfigurationSection path);
}
