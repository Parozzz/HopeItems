/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.cooldown;

/**
 *
 * @author Paros
 */
public class Cooldown 
{
    private long toWait;
    private long timestamp;
    public Cooldown(final long toWait)
    {
        this.toWait = toWait;
        timestamp = System.currentTimeMillis();
    }

    public void setToWait(final long toWait)
    {
        this.toWait = toWait;
    }
    
    public void reset()
    {
        timestamp = System.currentTimeMillis();
    }

    public long getRemaining()
    {
        return toWait - (System.currentTimeMillis() - timestamp);
    }

    public boolean hasExpired()
    {
        return System.currentTimeMillis() - timestamp > toWait;
    }
}
