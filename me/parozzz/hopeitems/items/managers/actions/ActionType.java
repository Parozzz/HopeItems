/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions;

import java.util.Optional;
import java.util.function.Function;
import me.parozzz.hopeitems.Dependency;
import me.parozzz.hopeitems.items.managers.ManagerUtils;
import me.parozzz.reflex.NMS.entity.EntityPlayer;
import me.parozzz.reflex.NMS.packets.ChatPacket;
import me.parozzz.reflex.NMS.packets.ChatPacket.MessageType;
import me.parozzz.reflex.classes.MapArray;
import me.parozzz.reflex.classes.SimpleMapList;
import me.parozzz.reflex.classes.SoundManager;
import me.parozzz.reflex.placeholders.Placeholder;
import me.parozzz.reflex.utilities.EntityUtil;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public enum ActionType 
{
    PLAYER(true){
        @Override
        public ActionManager getActionManager(final SimpleMapList mapList)
        {
            SingleActionManager<Player> player=new SingleActionManager<>(this);
            mapList.getValues().forEach((key, list) -> 
            {
                list.forEach(value -> 
                {
                    switch(key)
                    {
                        case "addfood":
                            player.addAction(ManagerUtils.getNumberFunction(value, Number::intValue, (p, d) -> p.setFoodLevel(p.getFoodLevel()+d)));
                            break;
                        case "setfood":
                            player.addAction(ManagerUtils.getNumberFunction(value, Number::intValue, Player::setFoodLevel));
                            break;
                        case "addsaturation":
                            player.addAction(ManagerUtils.getNumberFunction(value, Number::floatValue, (p, d)->p.setSaturation(p.getSaturation()+d)));
                            break;
                        case "setsaturation":
                            player.addAction(ManagerUtils.getNumberFunction(value, Number::floatValue, Player::setSaturation));
                            break;
                        case "sethealth":
                            player.addAction(ManagerUtils.getNumberFunction(value, Function.identity(), (p, d)-> 
                            {
                                double maxHealth = EntityUtil.getMaxHealth(p);
                                p.setHealth(d>maxHealth ? maxHealth : d);
                            }));
                            break;
                        case "givehealth":
                            player.addAction(ManagerUtils.getNumberFunction(value, Function.identity(), (p ,d)-> 
                            {
                                double maxHealth = EntityUtil.getMaxHealth(p);
                                p.setHealth(p.getHealth()+d > maxHealth ? maxHealth : p.getHealth()+d);
                            }));
                            break;
                        case "giveexp":
                            player.addAction(ManagerUtils.getNumberFunction(value, Number::intValue, (p, d)-> p.giveExp(d)));
                            break;
                        case "setexp":
                            player.addAction(ManagerUtils.getNumberFunction(value, Number::intValue, EntityUtil::setExp));
                            break;
                        case "givelevel":
                            player.addAction(ManagerUtils.getNumberFunction(value, Number::intValue, (p,d)-> p.setLevel(p.getLevel()+d)));
                            break;
                        case "setlevel":
                            player.addAction(ManagerUtils.getNumberFunction(value, Number::intValue, Player::setLevel));
                            break;
                        case "setfly":
                            boolean fly=Boolean.valueOf(value);
                            player.addAction(p -> 
                            {
                                p.setAllowFlight(fly);
                                p.setFlying(fly);
                            });
                            break;
                        case "setfire":
                            int fireTicks=Integer.valueOf(value);
                            player.addAction(p -> p.setFireTicks(fireTicks));
                            break;
                        case "addpotion":
                            PotionEffect pe=Optional.ofNullable(ManagerUtils.getPotionEffect(new MapArray(value)))
                                    .orElseThrow(() -> new IllegalArgumentException("Invalid potion effect type"));
                            player.addAction(p -> p.addPotionEffect(pe, true));
                            break;
                        case "removepotion":
                            if(value.equalsIgnoreCase("all"))
                            {
                                player.addAction(p -> p.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(p::removePotionEffect));
                            }
                            else
                            {
                                Optional.ofNullable(PotionEffectType.getByName(value)).map(pet -> 
                                {
                                    player.addAction(p -> p.removePotionEffect(pet));
                                    return pet;
                                }).orElseThrow(() -> new IllegalArgumentException("Invalid potion effect type: "+value));
                            }
                            break;
                        case "money":
                            if(Dependency.isEconomyHooked())
                            {
                                double money=Double.valueOf(value);
                                player.addAction(money>=0? p -> Dependency.eco.depositPlayer(p, money) : p -> Dependency.eco.withdrawPlayer(p, money));
                            }
                            break;
                        case "setgamemode":
                            GameMode gm=GameMode.valueOf(value.toUpperCase());
                            player.addAction(p -> p.setGameMode(gm));
                            break;
                        case "teleport":
                            player.addAction(ManagerUtils.teleportPlayer(new MapArray(value)));
                            break;
                        case "playercommand":
                            Placeholder playerCommand=new Placeholder(value).checkLocation().checkPlayer();
                            player.addAction(p -> p.performCommand(playerCommand.parse(p, p.getLocation())));
                            break;
                        case "consolecommand":
                            Placeholder consoleCommand=new Placeholder(value).checkLocation().checkPlayer();
                            player.addAction(p -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand.parse(p, p.getLocation())));
                            break;
                        case "opcommand":
                            Placeholder opCommand=new Placeholder(value).checkLocation().checkPlayer();
                            player.addAction(p -> 
                            {
                                if(p.isOp())
                                {
                                    p.performCommand(opCommand.parse(p, p.getLocation()));
                                }
                                else
                                {
                                    p.setOp(true);
                                    p.performCommand(opCommand.parse(p, p.getLocation()));
                                    p.setOp(false);
                                }

                            });
                            break;
                        case "title":
                            ManagerUtils.Title playerTitle = ManagerUtils.getTitle(new MapArray(value));
                            player.addAction(playerTitle.getConsumer());
                            break;
                        case "actionbar":
                            Placeholder actionBar= new Placeholder(Util.cc(value)).checkPlayer().checkLocation();
                            player.addAction(p -> EntityPlayer.getNMSPlayer(p).getPlayerConnection().sendPacket(new ChatPacket(MessageType.ACTIOBAR, actionBar.parse(p, p.getLocation()))));
                            break;
                        case "message":
                            Placeholder message=new Placeholder(Util.cc(value)).checkPlayer().checkLocation();
                            player.addAction(p -> p.sendMessage(message.parse(p, p.getLocation())));
                            break;
                        case "chat":
                            Placeholder chat=new Placeholder(Util.cc(value)).checkLocation().checkPlayer();
                            player.addAction(p -> p.chat(chat.parse(p, p.getLocation())));
                            break;
                    }  
                });
            });
            return player;
        }
    }, 
    WORLDEFFECT(false){
        @Override
        public ActionManager getActionManager(final SimpleMapList mapList)
        {
            SingleActionManager<Location> world=new SingleActionManager<>(this);
            mapList.getValues().forEach((key, list) -> 
            {
                list.forEach(value -> 
                {
                    switch(key)
                    {
                        case "message":
                            Placeholder message=new Placeholder(Util.cc(value)).checkLocation();
                            world.addAction(l -> l.getWorld().getPlayers().forEach(p -> p.sendMessage(message.parse(l))));
                            break;
                        case "actionbar":
                            Placeholder actionbar=new Placeholder(Util.cc(value)).checkLocation();
                            world.addAction(l -> l.getWorld().getPlayers().forEach(p -> EntityPlayer.getNMSPlayer(p).getPlayerConnection().sendPacket(new ChatPacket(MessageType.ACTIOBAR, actionbar.parse(l)))));
                            break;
                        case "title":
                            ManagerUtils.Title title=ManagerUtils.getTitle(new MapArray(value));
                            world.addAction(title.getWorldConsumer());
                            break;
                        case "thunder":
                            boolean damage=Boolean.valueOf(value);
                            world.addAction(damage ? l -> l.getWorld().strikeLightning(l) : l -> l.getWorld().strikeLightningEffect(l));
                            break;
                        case "particle":
                            world.addAction(ManagerUtils.spawnParticle(new MapArray(value)));
                            break;
                        case "explosion":
                            world.addAction(ManagerUtils.createExplosion(new MapArray(value)));
                            break;
                        case "command":
                            Placeholder command=new Placeholder(value).checkLocation();
                            world.addAction(l -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.parse(l)));
                    }                    
                });
            });
            return world;
        }
    }, 
    PLAYEREFFECT(true){
        @Override
        public ActionManager getActionManager(final SimpleMapList mapList)
        {
            DoubleActionManager<Player, Location> playerEffect=new DoubleActionManager<>(this);
            mapList.getValues().forEach((key, list) -> 
            {
                list.forEach(value ->
                {
                    switch(key)
                    {
                        case "particle":
                            playerEffect.addAction(ManagerUtils.spawnPlayerParticle(new MapArray(value)));
                            break;
                        case "teleport":
                            playerEffect.addAction((p, l) -> p.teleport(l));
                            break;
                        case "sound":
                            SoundManager playerSound = ManagerUtils.getSound(new MapArray(value));
                            playerEffect.addAction((p, l) -> playerSound.play(l, p));
                            break;
                    }
                });
            });
            return playerEffect;
        }
    };
    
    private final boolean playerRelated;
    private ActionType(final boolean playerRelated)
    {
        this.playerRelated=playerRelated;
    }
    
    public boolean isPlayerRelated()
    {
        return playerRelated;
    }
    
    public ActionManager getActionManager(final SimpleMapList list)
    {
        throw new UnsupportedOperationException();
    }
}
