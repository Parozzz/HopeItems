/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems;

import it.parozzz.hopeitems.Enum.When;
import it.parozzz.hopeitems.core.ItemDatabase;
import it.parozzz.hopeitems.manager.ItemManager;
import java.util.Optional;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 *
 * @author Paros
 */
public class LingeringPotionHandler 
        implements Listener
{
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onLingeringSplash(final LingeringPotionSplashEvent e)
    {
        Optional.ofNullable((ItemManager)id.getKey(e.getEntity().getItem())).filter(im -> im.canHappen(When.LINGERING)).ifPresent(im -> 
        {
            im.execute(e.getAreaEffectCloud().getLocation(), true, false);
            e.getAreaEffectCloud().setMetadata(Value.CustomLingeringMetadata, new FixedMetadataValue(HopeItems.getInstance(),im));
        });
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onCloudEffect(final AreaEffectCloudApplyEvent e)
    {
        if(e.getEntity().hasMetadata(Value.CustomLingeringMetadata)) 
        {
            ItemManager im=(ItemManager)e.getEntity().getMetadata(Value.CustomLingeringMetadata).get(0).value();
            e.getAffectedEntities().forEach(ent ->  im.execute(ent.getLocation(), ent.getType()==EntityType.PLAYER?(Player)ent:null , true, false));
        }
    }
    
    private final ItemDatabase id;
    public LingeringPotionHandler(final ItemDatabase id)
    {
        this.id=id;
    }
}
