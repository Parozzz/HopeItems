/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems;

import it.parozzz.hopeitems.Enum.When;
import it.parozzz.hopeitems.core.ItemBuilder;
import it.parozzz.hopeitems.core.ItemDatabase;
import it.parozzz.hopeitems.reflection.NBT.AdventureAction;
import it.parozzz.hopeitems.core.Utils;
import it.parozzz.hopeitems.events.BlockDispenseCustomItemEvent;
import it.parozzz.hopeitems.manager.ConditionManager;
import it.parozzz.hopeitems.manager.EffectManager;
import it.parozzz.hopeitems.manager.ExplosiveManager;
import it.parozzz.hopeitems.manager.ItemManager;
import it.parozzz.hopeitems.manager.LuckyManager;
import it.parozzz.hopeitems.manager.MobManager;
import it.parozzz.hopeitems.manager.PlayerManager;
import it.parozzz.hopeitems.reflection.ItemNBT;
import it.parozzz.hopeitems.reflection.NBT;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public class ItemHandler implements Listener
{
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onItemThrow(final PlayerDropItemEvent e)
    {
        fetchManager(e.getItemDrop().getItemStack()).filter(im -> im.canHappen(When.DROP)).filter(im -> im.check(e.getPlayer())).ifPresent(im -> 
        {
            new BukkitRunnable()
            {
                @Override
                public void run() 
                {
                    if(e.getItemDrop().isOnGround())
                    {
                        this.cancel();
                        im.execute(e.getItemDrop().getLocation(), e.getPlayer(), true, false);
                        //im.mob(e.getItemDrop().getLocation());
                        //im.lucky(e.getPlayer());
                        
                        if(im.getRemoveOnUse())
                        {
                            if(e.getItemDrop().getItemStack().getAmount()==1) 
                            {
                                e.getItemDrop().remove(); 
                            }
                            else 
                            { 
                                e.getItemDrop().setItemStack(new ItemBuilder(e.getItemDrop().getItemStack()).setAmount(e.getItemDrop().getItemStack().getAmount()-1).build()); 
                            }
                        }
                        
                    }
                }
            }.runTaskTimer(HopeItems.getInstance(), 3L, 3L);
        });
    }
    
    private final List<BlockFace> checkOnMove=Arrays.asList(BlockFace.SELF,BlockFace.DOWN);
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onPlayerMove(final PlayerMoveEvent e)
    {
        if(!e.getFrom().getBlock().equals(e.getTo().getBlock()))
        {
            checkOnMove.stream().map(bf -> e.getTo().getBlock().getRelative(bf)).filter(b -> b.hasMetadata(Value.CustomBlockMetadata)).findFirst().ifPresent(b -> 
            {
                Optional.of((ItemManager)b.getMetadata(Value.CustomBlockMetadata).get(0).value())
                        .filter(im -> im.canHappen(When.BLOCKSTEP))
                        .filter(im -> im.check(e.getPlayer()))
                        .ifPresent(im ->
                        {
                            new BukkitRunnable()
                            {
                                @Override
                                public void run() 
                                {
                                    if(!e.getPlayer().getLocation().getBlock().equals(e.getTo().getBlock()))
                                    {
                                        this.cancel();
                                    }
                                    else if(b.getType()==Material.WEB || e.getPlayer().isOnGround())
                                    {
                                        im.execute(b.getLocation().add(0.5, 1, 0.5), true, false); 

                                        if(im.getRemoveOnUse())
                                        {
                                            Database.removeCustomBlock(b);
                                        }

                                        this.cancel();
                                    }
                                }
                            }.runTaskTimer(HopeItems.getInstance(), 0L, 1L);
                        });
            });
        }
    }
    
    @EventHandler(ignoreCancelled=false,priority=EventPriority.HIGHEST)
    private void onInteract(final PlayerInteractEvent e)
    {
        Optional.of(Utils.or(e.getAction(), Action.RIGHT_CLICK_BLOCK, Action.LEFT_CLICK_BLOCK) && e.getClickedBlock().hasMetadata(Value.CustomBlockMetadata))
                .filter(b -> b.equals(true))
                .flatMap(b -> Optional.of((ItemManager)e.getClickedBlock().getMetadata(Value.CustomBlockMetadata).get(0).value()))
                .filter(im -> im.canHappen(When.BLOCKINTERACT) && im.check(e.getPlayer(), e))
                .ifPresent(im -> 
                { 
                    Location l=e.getClickedBlock().getLocation().add(0.5, 1, 0.5);
                    im.execute(l, e.getPlayer(), true, false);
                    
                    if(im.getRemoveOnUse())
                    {
                        Database.removeCustomBlock(e.getClickedBlock());
                    }
                    
                    //im.mob(l);
                    //im.lucky(e.getPlayer());
                });
        
        fetchManager(e.getItem()).ifPresent(im -> 
        {
            if(im.canHappen(When.INTERACT)) 
            {  
                e.setCancelled(true);
                if(!im.check(e.getPlayer(), e)) { return; }

                //im.mob(e.getClickedBlock()!=null?e.getClickedBlock().getLocation().add(0.5, 1, 0.5):e.getPlayer().getLocation());
                im.execute(e.getPlayer().getLocation(), e.getPlayer(), true, false); 
                //im.lucky(e.getPlayer());
                im.itemRemoval(e.getItem(), e.getPlayer());
            }
            else if((im.canHappen(When.SPLASH) || im.canHappen(When.LINGERING)) && e.getItem().getType().name().contains("POTION")) 
            { 
                e.setCancelled(!im.getRemoveOnUse() && Utils.or(e.getAction(), Action.RIGHT_CLICK_AIR,Action.RIGHT_CLICK_BLOCK));
                if(!im.check(e.getPlayer()))
                {
                    e.setCancelled(true);
                    return;
                }
                else if(!e.isCancelled()) { return; }

                ((ProjectileSource)e.getPlayer()).launchProjectile(ThrownPotion.class).setItem(e.getItem());
            }
        });
    }
    
    @EventHandler(ignoreCancelled=false,priority=EventPriority.HIGHEST)
    private void onEntityInteract(final PlayerInteractAtEntityEvent e) 
    {
        if(Utils.bukkitVersion("1.8")) 
        { 
            e.setCancelled(Utils.getHand(e.getPlayer())!=null && hasManager(Utils.getHand(e.getPlayer()))); 
        }
        else { //Optional.ofNullable(Utils.getHand(e.getPlayer())).filter(item -> Optional.ofNullable(id.getKey(item.clone())).isPresent()).isPresent();
            e.setCancelled((Utils.getHand(e.getPlayer())!=null && hasManager(Utils.getHand(e.getPlayer())))
                || (e.getPlayer().getInventory().getItemInOffHand()!=null && hasManager(e.getPlayer().getInventory().getItemInOffHand()))); 
        }
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onConsume(final PlayerItemConsumeEvent e)
    {
        Optional<ItemManager> op=fetchManager(e.getItem()).filter(im -> im.canHappen(When.CONSUME));
        e.setCancelled(op.isPresent());
        op.filter(im -> im.check(e.getPlayer())).ifPresent(im -> 
        {
            im.execute(e.getPlayer().getLocation(), e.getPlayer(), true, false); 
            //im.mob(e.getPlayer().getLocation());
            //im.lucky(e.getPlayer());
            im.itemRemoval(e.getItem(), e.getPlayer());
        });
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.MONITOR)
    private void onDispense(final BlockDispenseEvent e)
    {
        if(!e.getBlock().getType().equals(Material.DISPENSER)) { return; }
        
        ItemStack used=(ItemStack)Arrays.stream(((Dispenser)e.getBlock().getState()).getInventory().getContents())
                .filter(Objects::nonNull).filter(item -> hasManager(item)).findFirst().orElse(e.getItem());
        
        ItemManager im=(ItemManager)id.getKey(used.clone());
        if(im==null) { return; }
        else if(im.canHappen(When.DISPENSE)) { e.setCancelled(true); }
        else if(used.getType().name().contains("ARROW") && im.canHappen(When.ARROW))
        { 
            Bukkit.getServer().getPluginManager().callEvent(new BlockDispenseCustomItemEvent((Dispenser)e.getBlock().getState(),e.getItem(),im));
            e.setCancelled(true);
            return;
        }
        else { return; }
        
        Location l=e.getBlock().getRelative(((DirectionalContainer)e.getBlock().getState().getData()).getFacing()).getLocation().add(0.5,0.5,0.5);
        im.execute(l,true, false);
        //im.mob(l);
        
        if(im.getRemoveOnUse()) { return; }
        
        if(!Arrays.stream(((Dispenser)e.getBlock().getState()).getInventory().getContents()).filter(Objects::nonNull).findAny().isPresent())
        {
            new BukkitRunnable()
            {
                @Override
                public void run() { ((Dispenser)e.getBlock().getState()).getInventory().clear(); }
            }.runTaskLater(HopeItems.getInstance(), 1L);
        }
        else { ((Dispenser)e.getBlock().getState()).getInventory().removeItem(new ItemBuilder(used.clone()).setAmount(1).build()); }
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onCustomDispenserLaunch(final BlockDispenseCustomItemEvent e)
    {
        Arrow arrow;
        switch(e.getItem().getType())
        {
            case ARROW: 
                arrow=e.getDispenser().getBlockProjectileSource().launchProjectile(Arrow.class);
                break;
            case TIPPED_ARROW:
                arrow=e.getDispenser().getBlockProjectileSource().launchProjectile(TippedArrow.class);
                ((PotionMeta)e.getItem().getItemMeta()).getCustomEffects().forEach(pe -> ((TippedArrow)arrow).addCustomEffect(pe, true));
                if(Utils.bukkitVersion("1.11","1.12")) { ((TippedArrow)arrow).setColor(((PotionMeta)e.getItem().getItemMeta()).getColor()); }
                break;
            case SPECTRAL_ARROW:
                arrow=e.getDispenser().getBlockProjectileSource().launchProjectile(SpectralArrow.class);
                break;
            default: return;
        }
        arrow.setMetadata(Value.CustomProjectileMetadata, new FixedMetadataValue(HopeItems.getInstance(),e.getItemManager()));
    }
  
    private final Set<UUID> ignore=new HashSet<>();
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    private void onProjectileLaunch(final ProjectileLaunchEvent e)
    {
        if(e.getEntity() instanceof Arrow && e.getEntity().getShooter() instanceof Player
                 && !ignore.remove(((Player)e.getEntity().getShooter()).getUniqueId()))
        {
            Player p=(Player)e.getEntity().getShooter();
            ItemStack arrow=Arrays.stream(p.getInventory().getContents())
                    .filter(item -> item!=null)
                    .filter(item -> item.getType().name().contains("ARROW"))
                    .filter(item -> hasManager(item)).findFirst().orElseGet(() -> null);
            if(arrow==null) { return; }
            e.setCancelled(true);

            fetchManager(arrow).filter(im -> im.check(p)).ifPresent(im -> 
            {
                if(im.getRemoveOnUse()) { p.getInventory().removeItem(new ItemBuilder(arrow.clone()).setAmount(1).build()); }
                ignore.add(p.getUniqueId());
                new BukkitRunnable()
                {
                    @Override
                    public void run() 
                    {
                        Arrow projectile=(Arrow)((ProjectileSource)p).launchProjectile(e.getEntity().getClass(),e.getEntity().getVelocity());
                        if(!Utils.bukkitVersion("1.8") && e.getEntity() instanceof TippedArrow)
                        {
                            if(((TippedArrow)e.getEntity()).hasCustomEffects()) 
                            { 
                                ((TippedArrow)e.getEntity()).getCustomEffects().forEach(pe -> ((TippedArrow)projectile).addCustomEffect(pe, true));
                            }
                            if(Utils.bukkitVersion("1.11","1.12")) { ((TippedArrow)projectile).setColor(((TippedArrow)e.getEntity()).getColor()); }
                        }

                        projectile.setCritical(((Arrow)e.getEntity()).isCritical());
                        projectile.setMetadata(Value.CustomProjectileMetadata, new FixedMetadataValue(HopeItems.getInstance(),im));
                    }
                }.runTaskLater(HopeItems.getInstance(), 1L);
            });
        }
    }
    
    @EventHandler(ignoreCancelled=false,priority=EventPriority.HIGHEST)
    private void onProjectileHit(final ProjectileHitEvent e)
    {
        if(e.getEntity() instanceof Arrow && e.getEntity().hasMetadata(Value.CustomProjectileMetadata))
        {
            ((ItemManager)e.getEntity().getMetadata(Value.CustomProjectileMetadata).get(0).value())
                    .execute(e.getEntity().getLocation(),e.getEntity().getShooter() instanceof Player?(Player)e.getEntity().getShooter():null, true, false);
            e.getEntity().remove();
        }
    }
    
    @EventHandler(ignoreCancelled=false,priority=EventPriority.HIGHEST)
    private void onSplash(final PotionSplashEvent e)
    {
        fetchManager(e.getEntity().getItem()).filter(im -> im.canHappen(When.SPLASH)).ifPresent(im -> 
        {
            im.execute(e.getEntity().getLocation(), true, false);
            
            e.getAffectedEntities().stream().forEach(ent -> im.execute(ent.getLocation(),ent.getType()==EntityType.PLAYER?(Player)ent:null, false, false));
        });
    }
    
    @EventHandler(ignoreCancelled=false,priority=EventPriority.HIGHEST)
    private void onCustomBlockPlace(final BlockPlaceEvent e)
    {
        fetchManager(e.getItemInHand())
                .filter(im -> im.canHappen(When.BLOCKINTERACT) || im.canHappen(When.BLOCKSTEP))
                .ifPresent(im -> Database.addCustomBlock(e.getBlockPlaced(), e.getPlayer().getUniqueId(), e.getPlayer().getName(), im));
    }
    
    @EventHandler(ignoreCancelled=false,priority=EventPriority.HIGHEST)
    private void onCustomBlockBreak(final BlockBreakEvent e)
    {
        if(Database.isCustomBlock(e.getBlock()))
        {
            e.setCancelled(Optional.ofNullable(Database.removeCustomBlock(e.getBlock()))
                    .filter(bm -> !bm.getOwner().equals(e.getPlayer().getUniqueId()) || e.getPlayer().hasPermission(Permission.block_admin)).isPresent());
        }
    }
    
    @EventHandler(ignoreCancelled=false,priority=EventPriority.HIGHEST)
    private void onCustomBlockExplosion(final EntityExplodeEvent e)
    {
        e.blockList().stream().filter(Database::isCustomBlock).forEach(b -> Database.removeCustomBlock(b));
    }
    
    private final Shop shop;
    private final ItemDatabase id;
    public ItemHandler(final Shop shop)
    {
        this.shop=shop;
        id=new ItemDatabase();
    }
    
    public void parse(final FileConfiguration c)
    {
        c.getKeys(false).stream().map(pathName -> c.getConfigurationSection(pathName)).forEach(path -> 
        {
            try 
            {
                ItemStack item=Utils.getItemByPath(path.getConfigurationSection("Item"));
                
                if(Value.useNBT)
                {
                    item=new ItemNBT(item).setNewValue("HopeItems", path.getName()).buildItem();
                }
                
                if(path.contains("ItemAttribute"))
                {
                    ConfigurationSection iaPath=path.getConfigurationSection("ItemAttribute");
                    if(iaPath.contains("canPlaceOn")) 
                    { 
                        item=NBT.setAdventureFlag(item, AdventureAction.CANPLACEON, 
                                iaPath.getStringList("canPlaceOn").stream()
                                        .map(str -> Material.valueOf(str.toUpperCase()))
                                        .filter(m -> m.isBlock()).toArray(Material[]::new)); 
                    }
                    
                    if(iaPath.contains("canDestroy")) 
                    { 
                        item=NBT.setAdventureFlag(item, AdventureAction.CANDESTROY, 
                                iaPath.getStringList("canDestroy").stream()
                                        .map(str -> Material.valueOf(str.toUpperCase())).toArray(Material[]::new)); 
                    }
                    
                    if(!Utils.bukkitVersion("1.8") && iaPath.contains("attackSpeed") && iaPath.contains("attackDamage")) 
                    {
                        item=NBT.changeAttribute(item, iaPath.getDouble("attackSpeed"), iaPath.getDouble("attackDamage"));
                    }
                }
                
                ItemManager im = new ItemManager(path.getName())
                        .setItem(item)
                        .setEffectManager(new EffectManager().parseStringList(path.getStringList("effect")))
                        .setPlayerManager(new PlayerManager().parseStringList(path.getStringList("player")))
                        .setConditionManager(new ConditionManager().parseStringList(path.getStringList("condition")))
                        .setMobManager(new MobManager().parse(path.contains("Mob")?path.getConfigurationSection("Mob"):null))
                        .setCreeperManager(new ExplosiveManager().parse(path.contains("CustomExplosive")?path.getConfigurationSection("CustomExplosive"):null))
                        .setLuckyManager(new LuckyManager().parse(path.contains("Lucky")?path.getConfigurationSection("Lucky"):null))
                        .setWhen(path.contains("when")?path.getStringList("when").stream().map(String::toUpperCase).map(When::valueOf).collect(Collectors.toSet()):new HashSet<>())
                        .setRemoveOnUse(path.getBoolean("removeOnUse",true))
                        .addCommand(path.getStringList("command"));
                id.addItem(im, im.getItem());
                Database.addItem(path.getName(), im.getItem().clone());
                Database.addItemManager(im, path.getName());
                
                if(shop!=null && path.contains("Shop"))
                {
                    ConfigurationSection sPath=path.getConfigurationSection("Shop");
                    if(sPath.getBoolean("inGui",false))  { shop.addItem(im.getItem(),sPath.getString("sign",null), sPath.getDouble("cost")); }
                }
                
                if(path.contains("Crafting"))
                {
                    ShapedRecipe recipe;
                    if(Utils.bukkitVersion("1.12")) 
                    { 
                        if(Bukkit.getRecipesFor(im.getItem()).stream().map(r -> (ShapedRecipe)r).findFirst().isPresent()) { return; }
                        else { recipe=new ShapedRecipe(new NamespacedKey(HopeItems.getInstance(),path.getName()),im.getItem());  }
                    }
                    else { recipe=new ShapedRecipe(im.getItem()); }
                    path.getConfigurationSection("Crafting").getKeys(false).forEach(str -> 
                    {
                        ConfigurationSection cPath=path.getConfigurationSection("Crafting");
                        switch(str.toLowerCase())
                        {
                            case "shape":
                                recipe.shape(cPath.getStringList(str).stream().map(String::toUpperCase).toArray(String[]::new));
                                break;
                            default:
                                recipe.setIngredient(str.toUpperCase().charAt(0), Material.valueOf(cPath.getString(str).toUpperCase()));
                                break;
                        }
                    });
                    
                    Bukkit.addRecipe(recipe);
                }
            } 
            catch (Exception ex) 
            { 
                ex.printStackTrace();
                Bukkit.getLogger().severe("Something wrong in the items configuration");
            }
        });
    }
    
    public void initializeHandler()
    {
        if(!Utils.bukkitVersion("1.8")) { Bukkit.getServer().getPluginManager().registerEvents(new LingeringPotionHandler(id), HopeItems.getInstance()); }
        Bukkit.getServer().getPluginManager().registerEvents(new CustomExplosiveHandler(), HopeItems.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new CustomMobHandler(), HopeItems.getInstance());
    }
    
    private Optional<ItemManager> fetchManager(final ItemStack item)
    {
        if(Value.useNBT)
        {
            return Optional.ofNullable(item).flatMap(temp -> 
            {

                try 
                { 
                    String manager=new ItemNBT(temp).getValue("HopeItems", String.class);
                    if(manager.isEmpty()) { return Optional.empty(); }
                    return Optional.ofNullable(Database.getItemManager(manager)); 
                }
                catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException ex) { ex.printStackTrace(); }
                return Optional.empty();
            });
        }
        else
        {
            return Optional.ofNullable(item).flatMap(temp -> Optional.ofNullable(id.getKey(temp.clone())).flatMap(o -> Optional.of((ItemManager)o)));
        }
    }
    
    private boolean hasManager(final ItemStack item)
    {
        return fetchManager(item).isPresent();
    }
}
