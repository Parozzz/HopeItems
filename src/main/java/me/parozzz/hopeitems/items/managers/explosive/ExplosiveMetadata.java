/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.explosive;

import java.util.Set;
import java.util.function.BiConsumer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 *
 * @author Paros
 */
public class ExplosiveMetadata 
{
    public static final String FRIENDLY_METADATA="FriendlyCustomCreeper";
    public final static String METADATA="CustomExplosive";
    
    public boolean fire=false;
    public float power=4;
    public boolean blockDamage=true;
    
    public int modifierRange=-1;
    public BiConsumer<EntityExplodeEvent, Set<LivingEntity>> modifier = (e, set) -> {};
}
