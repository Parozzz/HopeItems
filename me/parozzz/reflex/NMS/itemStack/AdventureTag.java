/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.NMS.itemStack;

/**
 *
 * @author Paros
 */
public enum AdventureTag 
{
    CANPLACEON("CanPlaceOn"),
    CANDESTROY("CanDestroy");

    private final String value;
    private AdventureTag(final String str) 
    {
        value=str; 
    }

    public String getValue() 
    {
        return value; 
    }
}
