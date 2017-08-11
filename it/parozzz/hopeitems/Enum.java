/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems;

import java.util.Arrays;
import java.util.NoSuchElementException;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class Enum 
{
    public static enum EffectType
    {
        WORLDSOUND("SOUND"),
        THUNDER("THUNDER"),
        FIREWORK("FIREWORK"),
        WORLDMESSAGE("WORLDMESSAGE"),
        WORLDTITLE("WORLDTITLE"),
        WORLDACTIONBAR("WORLDACTIONBAR"),
        WORLDPARTICLE("WORLDPARTICLE"),
        EXPLOSION("EXPLOSION");
        
        private final String startWith;
        private EffectType(final String startWith)
        {
            this.startWith=startWith;
        }
        public String getStartWith() { return startWith; }
        
        public static EffectType getByStarting(final String start)
        {
            try {  return Arrays.stream(EffectType.values()).filter(et -> et.getStartWith().equals(start.toUpperCase())).findAny().get(); }
            catch(NoSuchElementException ex) {  throw new IllegalArgumentException(start+" is not a valid effect"); }
        }
    }
    
    public static enum PlayerType
    {
        HEAL("HEAL"),
        DAMAGE("DAMAGE"),
        FOOD("FOOD"),
        SATURATION("SATURATION"),
        EXP("EXP"),
        LEVEL("LEVEL"),
        FIRE("FIRE"),
        SOUND("SOUND"),
        POTION("POTION"),
        CURE("CURE"),
        MESSAGE("MESSAGE"),
        ACTIONBAR("ACTIONBAR"),
        TITLE("TITLE"),
        ITEM("ITEM"),
        MONEY("MONEY"),
        PARTICLE("PARTICLE"),
        THUNDER("THUNDER"),
        TELEPORT("TELEPORT");
        
        private final String startWith;
        private PlayerType(final String startWith)
        {
            this.startWith=startWith;
        }
        public String getStartWith() { return startWith; }
        
        public static PlayerType getByStarting(final String start)
        {
            try {  return Arrays.stream(PlayerType.values()).filter(et -> et.getStartWith().equals(start.toUpperCase())).findAny().get(); }
            catch(NoSuchElementException ex) {  throw new IllegalArgumentException(start+" is not a valid player effect"); }
        }
    }
    
    public static enum ConditionType
    {
        WORLD("WORLD"),
        PERMISSION("PERMISSION"),
        COOLDOWN("COOLDOWN"),
        WORLDGUARD("WORLDGUARD"),
        ACTION("ACTION"),
        LEVEL("LEVEL");
        
        private final String startWith;
        private ConditionType(final String startWith)
        {
            this.startWith=startWith;
        }
        public String getStartWith() { return startWith; }
        
        public static ConditionType getByStarting(final String start)
        {
            try {  return Arrays.stream(ConditionType.values()).filter(et -> et.getStartWith().equals(start.toUpperCase())).findAny().get(); }
            catch(NoSuchElementException ex) {  throw new IllegalArgumentException(start+" is not a valid condition"); }
        }
    }
    
    public static enum CreeperAction
    {
        ENDER("ENDER"),
        STACKER("STACKER"),
        POTION("POTION"),
        FIRE("FIRE"),
        FRIENDLY("FRIENDLY"),
        TRANSMUTATION("TRANSMUTATION"),
        SPAWNER("SPAWNER"),
        THUNDER("THUNDER");
        
        private final String startWith;
        private CreeperAction(final String startWith)
        {
            this.startWith=startWith;
        }
        public String getStartWith() { return startWith; }
        
        public static CreeperAction getByStarting(final String start)
        {
            try {  return Arrays.stream(CreeperAction.values()).filter(et -> et.getStartWith().equals(start.toUpperCase())).findAny().get(); }
            catch(NoSuchElementException ex) {  throw new IllegalArgumentException(start+" is not a valid condition"); }
        }
    }
    
    public static enum PassiveType
    {
        POTION("POTION"),
        DEFLECT("DEFLECT"),
        SHIELD("SHIELD"),
        PUSH("PUSH"),
        DAMAGE("DAMAGE"),
        LIGHTER("LIGHTER"),
        SNIPER("SNIPER"),
        SPAWN("SPAWN");
        
        private final String startWith;
        private PassiveType(final String startWith)
        {
            this.startWith=startWith;
        }
        public String getStartWith() { return startWith; }
        
        public static PassiveType getByStarting(final String start)
        {
            try {  return Arrays.stream(PassiveType.values()).filter(et -> et.getStartWith().equals(start.toUpperCase())).findAny().get(); }
            catch(NoSuchElementException ex) {  throw new IllegalArgumentException(start+" is not a valid condition"); }
        }
    }
    
    public static enum When
    {
        DROP,INTERACT,CONSUME,SPLASH,LINGERING,DISPENSE,ARROW,BLOCKINTERACT,BLOCKSTEP;
    }
}
