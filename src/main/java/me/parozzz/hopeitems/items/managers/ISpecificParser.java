/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers;

import me.parozzz.reflex.configuration.AbstractMapList;

/**
 *
 * @author Paros
 * @param <T>
 */
@FunctionalInterface
public interface ISpecificParser<P, T>
{
    public T parse(final P p);
}
