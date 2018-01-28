/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.managers.actions.parsers;

import me.parozzz.hopeitems.items.managers.actions.parsers.single.ActionRegistry;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.parozzz.hopeitems.Dependency;
import me.parozzz.hopeitems.items.managers.AbstractRegistry;
import me.parozzz.hopeitems.items.managers.IParser;
import me.parozzz.hopeitems.items.managers.ISpecificParser;
import me.parozzz.hopeitems.items.managers.ManagerUtils;
import me.parozzz.hopeitems.items.managers.actions.parsers.single.AbstractAction;
import me.parozzz.hopeitems.items.managers.actions.ActionType;
import me.parozzz.hopeitems.items.managers.actions.IAction;
import me.parozzz.hopeitems.items.managers.actions.parsers.bi.BiActionRegistry;
import me.parozzz.hopeitems.items.managers.actions.parsers.bi.SpecificBiActionParser;
import me.parozzz.hopeitems.items.managers.actions.parsers.single.SpecificActionParser;
import me.parozzz.reflex.NMS.entity.EntityPlayer;
import me.parozzz.reflex.NMS.packets.ChatPacket;
import me.parozzz.reflex.NMS.packets.ChatPacket.MessageType;
import me.parozzz.reflex.classes.SoundManager;
import me.parozzz.reflex.configuration.MapArray;
import me.parozzz.reflex.configuration.SimpleMapList;
import me.parozzz.reflex.placeholders.Placeholder;
import me.parozzz.reflex.utilities.EntityUtil;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public class ActionParser implements IParser
{
    private static final Logger logger = Logger.getLogger(ActionParser.class.getSimpleName());
    
    private final Map<ActionType, ActionRegistry> speficiParserMap;
    private final Map<ActionType, BiActionRegistry> speficiBiParserMap;
    public ActionParser()
    {
        speficiParserMap = new EnumMap(ActionType.class);
        speficiBiParserMap = new EnumMap(ActionType.class);
    }
    
    @Override
    public void registerDefaultSpecificParsers() 
    {
        Stream.of(ActionType.values()).forEach(type -> 
        {
            LivingEntityRegistry<? extends LivingEntity> genericEntityRegistry = null; //Used for both PLAYER and MOB actionType. So i don't repeat actions and load for both of them as well.
            switch(type)
            {
                case PLAYER:
                    genericEntityRegistry = new PlayerRegistry();
                    this.speficiParserMap.put(type, genericEntityRegistry);
                    
                    PlayerRegistry playerRegistry = (PlayerRegistry)genericEntityRegistry;
                    playerRegistry.addRegistered("addfood", value -> ManagerUtils.getNumberFunction(value, Number::intValue, (p, d) ->
                    {
                        if(p.getFoodLevel() + d >= 20)
                        {
                            p.setFoodLevel(20);
                        }
                        else
                        {
                            p.setFoodLevel(p.getFoodLevel() + d);
                        }
                    }));
                    playerRegistry.addRegistered("setfood", value -> ManagerUtils.getNumberFunction(value, Number::intValue, Player::setFoodLevel));
                    playerRegistry.addRegistered("addsaturation", value -> ManagerUtils.getNumberFunction(value, Number::floatValue, (p, d) -> p.setSaturation(p.getSaturation()+d)));
                    playerRegistry.addRegistered("setsaturation", value -> ManagerUtils.getNumberFunction(value, Number::floatValue, Player::setSaturation));
                    playerRegistry.addRegistered("giveexp", value -> ManagerUtils.getNumberFunction(value, Number::intValue, (p, d)-> p.giveExp(d)));
                    playerRegistry.addRegistered("setexp", value -> ManagerUtils.getNumberFunction(value, Number::intValue, EntityUtil::setExp));
                    playerRegistry.addRegistered("addlevel", value -> ManagerUtils.getNumberFunction(value, Number::intValue, (p,d)-> p.setLevel(p.getLevel()+d)));
                    playerRegistry.addRegistered("setlevel", value -> ManagerUtils.getNumberFunction(value, Number::intValue, Player::setLevel));
                    playerRegistry.addRegistered("setfly", value -> 
                    {
                        boolean fly = Boolean.valueOf(value);
                        return p -> 
                        {
                            p.setAllowFlight(fly);
                            p.setFlying(fly);
                        };
                    });
                    playerRegistry.addRegistered("setgamemode", value -> 
                    {
                        GameMode gm=GameMode.valueOf(value.toUpperCase());
                        return p -> p.setGameMode(gm);
                    });
                    playerRegistry.addRegistered("teleport", value -> ManagerUtils.teleportPlayer(new MapArray(value)));
                    playerRegistry.addRegistered("playercommand", value -> 
                    {
                        Placeholder playerCommand=new Placeholder(value).checkLocation().checkPlayer();
                        return p -> p.performCommand(playerCommand.parse(p, p.getLocation()));
                    });
                    playerRegistry.addRegistered("consolecommand", value -> 
                    {
                        Placeholder consoleCommand=new Placeholder(value).checkLocation().checkPlayer();
                        return p -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand.parse(p, p.getLocation()));
                    });
                    playerRegistry.addRegistered("opcommand", value -> 
                    {
                        Placeholder opCommand=new Placeholder(value).checkLocation().checkPlayer();
                        return p -> 
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
                        };
                    });
                    playerRegistry.addRegistered("title", value -> ManagerUtils.getTitle(new MapArray(value)).getConsumer());
                    playerRegistry.addRegistered("actionbar", value -> 
                    {
                        Placeholder actionBar= new Placeholder(Util.cc(value)).checkPlayer().checkLocation();
                        return p -> EntityPlayer.getNMSPlayer(p).getPlayerConnection().sendPacket(new ChatPacket(MessageType.ACTIOBAR, actionBar.parse(p, p.getLocation())));  
                    });
                    playerRegistry.addRegistered("message", value -> 
                    {
                        Placeholder message=new Placeholder(Util.cc(value)).checkPlayer().checkLocation();
                        return p -> p.sendMessage(message.parse(p, p.getLocation()));
                    });
                    playerRegistry.addRegistered("chat", value -> 
                    {
                        Placeholder chat=new Placeholder(Util.cc(value)).checkLocation().checkPlayer();
                        return p -> p.chat(chat.parse(p, p.getLocation()));
                    });
                    
                    if(Dependency.isEconomyHooked())
                    {
                        playerRegistry.addRegistered("money", value -> 
                        {
                            double money = Double.valueOf(value);
                            return money >= 0? p -> Dependency.economy.depositPlayer(p, money) : p -> Dependency.economy.withdrawPlayer(p, money);
                        }); 
                    }
                case MOB:
                    if(genericEntityRegistry == null)
                    {
                        genericEntityRegistry = new MobRegistry();
                        this.speficiParserMap.put(type, genericEntityRegistry);
                    }
                    
                    genericEntityRegistry.addRegistered("sethealth", value -> ManagerUtils.getNumberFunction(value, Function.identity(), (liv, d)-> 
                    {
                        double maxHealth = EntityUtil.getMaxHealth(liv);
                        liv.setHealth(d > maxHealth ? maxHealth : d);
                    }));
                    genericEntityRegistry.addRegistered("givehealth", value -> ManagerUtils.getNumberFunction(value, Function.identity(), (liv, d)-> 
                    {
                        double maxHealth = EntityUtil.getMaxHealth(liv);
                        liv.setHealth(liv.getHealth() + d > maxHealth ? maxHealth : liv.getHealth()+d);
                    }));
                    genericEntityRegistry.addRegistered("setfire", value -> ManagerUtils.getNumberFunction(value, Number::intValue, LivingEntity::setFireTicks));
                    genericEntityRegistry.addRegistered("addpotion",  value ->                           
                    { 
                        PotionEffect pe = ManagerUtils.getPotionEffect(new MapArray(value));
                        if(pe == null)
                        {
                            logger.log(Level.WARNING, "Error parsing a setpotion action in mob ActionType");
                            return liv -> {};
                        }
                        return liv ->  liv.addPotionEffect(pe, true);
                    });
                    genericEntityRegistry.addRegistered("removepotion", value -> 
                    {
                        if(value.equalsIgnoreCase("all"))
                        {
                            return liv -> liv.getActivePotionEffects().stream().map(pe -> pe.getType()).forEach(pet -> liv.removePotionEffect(pet));
                        }
                        else
                        {
                            PotionEffectType pet = PotionEffectType.getByName(value.toUpperCase());
                            if(pet == null)
                            {
                                logger.log(Level.WARNING, "Error parsing removepotion action. The potion effect named {0} does not exist. Skipping.", value);
                                return liv -> {};
                            }
                            return liv -> liv.removePotionEffect(pet);
                        }
                    });
                    
                    break;
                case WORLDEFFECT:
                    WorldEffectRegistry worldEffectRegistry = new WorldEffectRegistry();
                    this.speficiParserMap.put(type, worldEffectRegistry);
                    
                    worldEffectRegistry.addRegistered("spawn", value -> ManagerUtils.spawnMobs(new MapArray(value)));
                    worldEffectRegistry.addRegistered("message", value -> 
                    {
                        Placeholder message=new Placeholder(Util.cc(value)).checkLocation();
                        return l -> l.getWorld().getPlayers().forEach(p -> p.sendMessage(message.parse(l)));
                    });
                    worldEffectRegistry.addRegistered("actionbar", value -> 
                    {
                        Placeholder actionbar=new Placeholder(Util.cc(value)).checkLocation();
                        return l -> l.getWorld().getPlayers().forEach(p -> EntityPlayer.getNMSPlayer(p).getPlayerConnection().sendPacket(new ChatPacket(MessageType.ACTIOBAR, actionbar.parse(l))));
                    });
                    worldEffectRegistry.addRegistered("title", value -> ManagerUtils.getTitle(new MapArray(value)).getWorldConsumer());
                    worldEffectRegistry.addRegistered("thunder", value -> 
                    {
                        boolean damage = Boolean.valueOf(value.toUpperCase());
                        return damage ? l -> l.getWorld().strikeLightning(l) : l -> l.getWorld().strikeLightningEffect(l);
                    });
                    worldEffectRegistry.addRegistered("particle", value -> ManagerUtils.spawnParticle(new MapArray(value)));
                    worldEffectRegistry.addRegistered("explosion", value -> ManagerUtils.createExplosion(new MapArray(value)));
                    worldEffectRegistry.addRegistered("command", value -> 
                    {
                        Placeholder command=new Placeholder(value).checkLocation();
                        return l -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.parse(l));
                    });
                    break;
                case PLAYEREFFECT:
                    PlayerEffectRegistry playerEffectRegistry = new PlayerEffectRegistry();
                    this.speficiBiParserMap.put(type, playerEffectRegistry);
                    
                    playerEffectRegistry.addRegistered("particle", value -> ManagerUtils.spawnPlayerParticle(new MapArray(value)));
                    playerEffectRegistry.addRegistered("teleport", value -> (p, l) -> p.teleport(l));
                    playerEffectRegistry.addRegistered("sound", value -> 
                    {
                        SoundManager playerSound = ManagerUtils.getSound(new MapArray(value));
                        return (p, l) -> playerSound.play(l, p);
                    });
                    break;
            }
        });
    }
    
    /*
    @Override
    public void addSpecificParser(final ActionType type, final String key, final ISpecificParser<?, IAction> specificParser) 
    {
        if(type.isDouble())
        {
            this.speficiBiParserMap.get(type).addRegistered(key, specificParser);
        }
        else
        {
            this.speficiParserMap.get(type).addRegistered(key, specificParser);
        }
    }
*/
    @Override
    public ActionManager parse(final ConfigurationSection path)
    {
        ActionManager manager = new ActionManager();
        for(String key : path.getKeys(false))
        {
            ActionType type;
            try {
                type = ActionType.valueOf(key.toUpperCase());
            } catch(final IllegalArgumentException ex) {
                logger.log(Level.WARNING, "The action type {0} does not exist. Skipping type.", key);
                continue;
            }
            
            if(!path.isList(key))
            {
                logger.log(Level.WARNING, "Wrong format of action {0}. Skipping type.", key);
                continue;
            }
            
            AbstractRegistry<ISpecificParser<String, IAction>> registry = type.isDouble() ? this.speficiBiParserMap.get(type) : this.speficiParserMap.get(type);
            
            for(Map.Entry<String, List<String>> entry : new SimpleMapList(path.getMapList(key)).getView().entrySet())
            {
                String specificKey = entry.getKey();
                List<String> localList = entry.getValue();
                
                ISpecificParser<String, IAction> parser = registry.getRegistered(specificKey);
                if(parser == null)
                {
                    logger.log(Level.WARNING, "The action subType {0} does not exist. Skipping.", specificKey);
                }
                else if(!localList.isEmpty())
                {
                    IAction action = parser.parse(localList.get(0));
                    if(action == null)
                    {
                        logger.log(Level.WARNING, "There was a problem loading the action subType {0}", specificKey);
                        continue;
                    }
                    manager.addAction(action);
                }
            }
        }
        
        return manager;
    }
    
    private abstract class LivingEntityRegistry<T extends LivingEntity> extends ActionRegistry<T>
    {
        public LivingEntityRegistry(final ActionType at) 
        {
            super(at);
        }
    }
    
    private class PlayerRegistry extends LivingEntityRegistry<Player>
    {
        public PlayerRegistry() 
        {
            super(ActionType.PLAYER);
        }
    }
    
    private class MobRegistry extends LivingEntityRegistry<LivingEntity>
    {
        
        public MobRegistry() 
        {
            super(ActionType.MOB);
        }
        
    }    
    
    private class WorldEffectRegistry extends ActionRegistry<Location>
    {
        public WorldEffectRegistry() 
        {
            super(ActionType.WORLDEFFECT);
        }
    }
    
    private class PlayerEffectRegistry extends BiActionRegistry<Player, Location>
    {
        
        public PlayerEffectRegistry() 
        {
            super(ActionType.PLAYEREFFECT);
        }
        
    }
}
