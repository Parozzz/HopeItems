/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.placeholders;

/**
 *
 * @author Paros
 */
public class MutableString implements Cloneable
{
    private String string;
    public MutableString(final String string)
    {
        this.string=string;
    }

    public void set(final String newString)
    {
        string=newString;
    }

    public void replace(final String holder, final String replace)
    {
        string = string.replace(holder, replace);
    }

    public void replace(final String holder, final Object replace)
    {
        string = string.replace(holder, replace.toString());
    }

    public boolean contains(final String contain)
    {
        return string.contains(contain);
    }

    @Override
    public String toString()
    {
        return string;
    }

    @Override
    public MutableString clone()
    {
        try
        {
            return (MutableString)super.clone();
        }
        catch(final CloneNotSupportedException e)
        {
            return null;
        }
    }
}
