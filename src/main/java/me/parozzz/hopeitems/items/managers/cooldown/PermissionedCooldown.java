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
public class PermissionedCooldown 
{
    private final String permission;
    private final long cooldownTime;
    public PermissionedCooldown(final String perm, final long time)
    {
        this.permission = perm;
        this.cooldownTime = time;
    }
    
    public String getPermission()
    {
        return permission;
    }
    
    public long getCooldownTime()
    {
        return cooldownTime;
    }
}
