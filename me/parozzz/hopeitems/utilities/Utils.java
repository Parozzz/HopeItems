/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopeitems.utilities.classes.ComplexMapList;
import me.parozzz.hopeitems.utilities.classes.SimpleMapList;
import me.parozzz.hopeitems.utilities.reflection.API;
import me.parozzz.hopeitems.utilities.reflection.HeadUtils;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.ItemNBT;
import me.parozzz.hopeitems.utilities.reflection.NBTTagManager.NBTType;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.ItemAttributeModifier;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.ItemAttributeModifier.AttributeSlot;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.ItemAttributeModifier.ItemAttribute;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.ItemAttributeModifier.Operation;
import me.parozzz.hopeitems.utilities.reflection.nbt.NBTCompound;
import me.parozzz.hopeitems.utilities.reflection.nbt.item.AdventureTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author Paros
 */
public final class Utils {
    /*
        INVENTORY TESTING
        Bukkit.getLogger().info("=====================");
        Bukkit.getLogger().info("InvType: "+(e.getInventory().getType()!=null?e.getInventory().getType().toString():""));
        Bukkit.getLogger().info("CurrentItem: "+(e.getCurrentItem()!=null?e.getCurrentItem().toString():""));
        Bukkit.getLogger().info("CursorItem: "+(e.getCursor()!=null?e.getCursor().toString():""));
        Bukkit.getLogger().info("SlotItem: "+(e.getInventory().getItem(e.getSlot())!=null?e.getInventory().getItem(e.getSlot()).toString():""));
        Bukkit.getLogger().info("RawSlotItem: "+(e.getInventory().getItem(e.getRawSlot())!=null?e.getInventory().getItem(e.getRawSlot()).toString():""));
        Bukkit.getLogger().info("HotbarSlotItem: "+(e.getInventory().getItem(e.getHotbarButton())!=null?e.getInventory().getItem(e.getHotbarButton()).toString():""));
        Bukkit.getLogger().info("PlayerCursorItem: "+(e.getWhoClicked().getItemOnCursor()!=null?e.getWhoClicked().getItemOnCursor().toString():""));
        Bukkit.getLogger().info("Action: "+(e.getAction()!=null?e.getAction().toString():""));
        Bukkit.getLogger().info("SlotType: "+(e.getSlotType()!=null?e.getSlotType().toString():""));
        Bukkit.getLogger().info("Click: "+(e.getClick()!=null?e.getClick().toString():""));
        Bukkit.getLogger().info("Slot: "+Integer.toString(e.getSlot()));
        Bukkit.getLogger().info("RawSlot: "+Integer.toString(e.getRawSlot()));
        Bukkit.getLogger().info("HotbarSlot: "+Integer.toString(e.getHotbarButton()));
        Bukkit.getLogger().info("=====================");
    */
    public static enum CreatureType
    {
        MOBALL,
        BAT,
        BLAZE,
        SPIDER, CAVE_SPIDER,
        CHICKEN,
        COW, MUSHROOM_COW,
        CREEPER,
        ENDER_DRAGON,
        ENDERMAN,
        ENDERMITE,
        GHAST,
        GUARDIAN,ELDER_GUARDIAN,
        HORSE, SKELETON_HORSE, ZOMBIE_HORSE, MULE, DONKEY, LLAMA,
        IRON_GOLEM, SNOWMAN,
        OCELOT,
        PARROT,
        PIG,
        POLAR_BEAR,
        RABBIT,
        SHEEP,
        SHULKER,
        SILVERFISH,
        SKELETON, WITHER_SKELETON, STRAY,
        SLIME, MAGMA_CUBE,
        SQUID,
        VILLAGER,
        VINDICATOR, EVOKER, ILLUSIONER,
        VEX,
        WITCH,
        WOLF,
        ZOMBIE, ZOMBIE_VILLAGER, PIG_ZOMBIE, HUSK, GIANT,
        WITHER,
        PLAYER;
        
        
        private static Function<LivingEntity, CreatureType> init()
        {
            if(MCVersion.V1_11.isHigher())
            {
                return ent -> CreatureType.valueOf(ent.getType().name());
            }
            else
            {
                return ent -> 
                {
                    switch(ent.getType())
                    {
                        case SKELETON:
                            switch(((Skeleton)ent).getSkeletonType())
                            {
                                case STRAY: 
                                    return CreatureType.STRAY;
                                case WITHER: 
                                    return CreatureType.WITHER_SKELETON;
                                default: 
                                    return CreatureType.SKELETON;
                            }
                        case HORSE:
                            switch(((Horse)ent).getVariant())
                            {
                                case DONKEY:
                                    return CreatureType.DONKEY;
                                case MULE: 
                                    return CreatureType.MULE;
                                case LLAMA: 
                                    return CreatureType.LLAMA;
                                case SKELETON_HORSE:
                                    return CreatureType.SKELETON_HORSE;
                                case UNDEAD_HORSE: 
                                    return CreatureType.ZOMBIE_HORSE;
                                default:
                                    return CreatureType.HORSE;
                            }
                        case ZOMBIE:
                            if(((Zombie)ent).isVillager()) 
                            {
                                if(MCVersion.V1_9.isHigher() && ((Zombie)ent).getVillagerProfession() == Villager.Profession.HUSK) 
                                { 
                                    return CreatureType.HUSK; 
                                }
                                return CreatureType.ZOMBIE_VILLAGER;
                            }
                            else 
                            { 
                                return CreatureType.ZOMBIE; 
                            }
                        case GUARDIAN: 
                            return ((Guardian)ent).isElder()?CreatureType.ELDER_GUARDIAN:CreatureType.GUARDIAN;
                        default: 
                            return CreatureType.valueOf(ent.getType().name());
                    }
                };
            }
        }
        
        private static Function<LivingEntity, CreatureType> getByEntity;
        public static CreatureType getByLivingEntity(final LivingEntity ent)
        {
            return Optional.ofNullable(getByEntity).orElseGet(() -> getByEntity=CreatureType.init()).apply(ent);
        }
    }
    
    public static enum ColorEnum
    {
        AQUA(Color.AQUA, ChatColor.AQUA),BLACK(Color.BLACK, ChatColor.BLACK),FUCHSIA(Color.FUCHSIA, ChatColor.LIGHT_PURPLE),
        GRAY(Color.GRAY, ChatColor.GRAY),GREEN(Color.GREEN, ChatColor.GREEN),LIME(Color.LIME, ChatColor.GREEN),
        MAROON(Color.MAROON, ChatColor.GRAY),NAVY(Color.NAVY, ChatColor.DARK_BLUE),OLIVE(Color.OLIVE, ChatColor.DARK_GREEN),
        ORANGE(Color.ORANGE, ChatColor.GOLD),PURPLE(Color.PURPLE, ChatColor.DARK_PURPLE),RED(Color.RED, ChatColor.RED),
        BLUE(Color.BLUE, ChatColor.BLUE),SILVER(Color.SILVER, ChatColor.DARK_GRAY),TEAL(Color.TEAL, ChatColor.GRAY),
        WHITE(Color.WHITE, ChatColor.WHITE),YELLOW(Color.YELLOW, ChatColor.YELLOW);
        
        private final Color color;
        private final ChatColor chat;
        private ColorEnum(Color color, final ChatColor chat)
        {
            this.chat=chat;
            this.color=color;
        }
        
        public ChatColor getChatColor()
        {
            return chat;
        }
        
        public Color getBukkitColor()
        {
            return color;
        }
    }
    
    private static final String FIREWORK_DATA="Firework.NoDamage";
    
    public static EnumSet<BlockFace> cardinals=EnumSet.of(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH);
    public static void registerFireworkDamageListener()
    {
        if(MCVersion.V1_11.isHigher())
        {
            Bukkit.getServer().getPluginManager().registerEvents(new Listener()
            {
                @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
                private void onFireworkDamage(final EntityDamageByEntityEvent e)
                {
                    e.setCancelled(e.getDamager().getType()==EntityType.FIREWORK && e.getDamager().hasMetadata(FIREWORK_DATA));
                }
            }, JavaPlugin.getProvidingPlugin(Utils.class));
        }
    }
    
    public static void registerArmorStandInvicibleListener()
    {
        if(MCVersion.V1_8.isEqual())
        {
            Bukkit.getServer().getPluginManager().registerEvents(new Listener()
            {
                @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
                private void onInvisibleArmorStandDamage(final EntityDamageByEntityEvent e)
                {
                    e.setCancelled(e.getEntityType()==EntityType.ARMOR_STAND && !((ArmorStand)e.getEntity()).isVisible());
                }
            }, JavaPlugin.getProvidingPlugin(Utils.class));
        }
    }
    
    public static UUID fromFileName(final File file)
    {
        return UUID.fromString(file.getName().replace(".yml", ""));
    }
    
    public static <T extends Event> T callEvent(final T event)
    {
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }
    
    public static Inventory cloneChestInventory(final Inventory i)
    {
        Inventory newInv=Bukkit.createInventory(i.getHolder(), i.getSize(), i.getTitle());
        newInv.setContents(i.getContents());
        return newInv;
    }
    
    public static void setExp(final Player p, final int exp)
    {
        p.setTotalExperience(0);
        p.setLevel(0);
        p.setExp(0);
        
        p.giveExp(exp);
    }
    
    public static void addItem(final EntityEquipment equip, final EquipmentSlot slot, final ItemStack item)
    {
        addItem(equip, slot, item, null);
    }
    
    public static void addItem(final EntityEquipment equip, final EquipmentSlot slot, final ItemStack item, final Float dropChance)
    {
        switch(slot)
        {
            case HEAD:
                equip.setHelmet(item);
                if(dropChance!=null)
                {
                    equip.setHelmetDropChance(dropChance);
                }
                break;
            case CHEST:
                equip.setChestplate(item);
                if(dropChance!=null)
                {
                    equip.setChestplateDropChance(dropChance);
                }
                break;
            case LEGS:
                equip.setLeggings(item);
                if(dropChance!=null)
                {
                    equip.setLeggingsDropChance(dropChance);
                }
                break;
            case FEET:
                equip.setBoots(item);
                if(dropChance!=null)
                {
                    equip.setBootsDropChance(dropChance);
                }
                break;
            case HAND:
                Utils.setMainHand(equip, item);
                if(dropChance!=null)
                {
                    if(MCVersion.V1_8.isEqual())
                    {
                        equip.setItemInHandDropChance(dropChance);
                    }
                    else
                    {
                        equip.setItemInMainHandDropChance(dropChance);
                    }
                }
                break;
            case OFF_HAND:
                equip.setItemInOffHand(item);
                if(dropChance!=null)
                {
                    equip.setItemInOffHandDropChance(dropChance);
                }
                break;
        }
    }
    
    private static Function<EntityEquipment, ItemStack> getHand;
    public static ItemStack getMainHand(final EntityEquipment equip)
    {
        return Optional.ofNullable(getHand)
                .orElseGet(() -> getHand = MCVersion.V1_8.isEqual()? eq -> eq.getItemInHand() : eq -> eq.getItemInMainHand())
                .apply(equip);
    }
    
    private static BiConsumer<EntityEquipment, ItemStack> setHand;
    public static void setMainHand(final EntityEquipment equip, final ItemStack item)
    {
        Optional.ofNullable(setHand)
                .orElseGet(() -> setHand = MCVersion.V1_8.isEqual()? (eq, i) -> eq.setItemInHand(i) : (eq, i) -> eq.setItemInMainHand(i))
                .accept(equip, item);
    }
    
    private static Function<LivingEntity, Double> getMaxHealth;
    public static double getMaxHealth(final LivingEntity ent)
    {
        return Optional.ofNullable(getMaxHealth)
                .orElseGet(() -> getMaxHealth = MCVersion.V1_8.isEqual()? e -> e.getMaxHealth() : e -> e.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
                .apply(ent);
    }
    
    private static BiConsumer<LivingEntity, Double> setMaxHealth;
    public static void setMaxHealth(final LivingEntity ent, final Double health)
    {
        Optional.ofNullable(setMaxHealth)
                .orElseGet(() -> setMaxHealth = MCVersion.V1_8.isEqual()? (e,h) -> e.setMaxHealth(h) : (e,h) -> e.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(h))
                .accept(ent, health);
        ent.setHealth(health.intValue());
    }
    
    private static Predicate<ItemStack> isUnbreakable;
    public static boolean isUnbreakable(final ItemStack item)
    {
        return Optional.ofNullable(isUnbreakable).orElseGet(() -> 
        {
            if(MCVersion.V1_8.isEqual())
            {
                isUnbreakable = i -> false;
            }
            else if(MCVersion.contains(MCVersion.V1_9, MCVersion.V1_10))
            {
                isUnbreakable = i -> i.getItemMeta().spigot().isUnbreakable();
            }
            else
            {
                isUnbreakable = i -> i.getItemMeta().isUnbreakable();
            }
            return isUnbreakable;
        }).test(item);
    }
    
    private static BiConsumer<ItemMeta, Boolean> setUnbreakable;
    public static void setUnbreakable(final ItemMeta meta, final boolean unbreakable)
    {
        Optional.ofNullable(setUnbreakable).orElseGet(() -> 
        {
            if(MCVersion.V1_8.isEqual())
            {
                setUnbreakable = (m,b) -> {};
            }
            else if(MCVersion.contains(MCVersion.V1_9, MCVersion.V1_10))
            {
                setUnbreakable = (m,b) -> m.spigot().setUnbreakable(b);
            }
            else
            {
                setUnbreakable = (m,b) -> m.setUnbreakable(b);
            }
            
            return setUnbreakable;
        }).accept(meta, unbreakable);
    }

    public static void decreaseItemStack(final ItemStack item, final Inventory i)
    {
        if(item.getAmount()==1)
        {
            i.remove(item);
            return;
        }
        
        item.setAmount(item.getAmount()-1);
    }
    
    public static boolean or(final Object o, final Object... array)
    {
        return Arrays.stream(array).anyMatch(ob -> ob.equals(o));
    }
    
    public static boolean and(final Object o, final Object... array)
    {
        return Arrays.stream(array).allMatch(ob -> ob.equals(o));
    }
    
    public static boolean isNumber(final String str) 
    {
        return str.chars().allMatch(c -> Character.isDigit((char)c)); 
    }
    
    public static String color(final String s)
    {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    
    public static String stripColor(final String s)
    {
        return ChatColor.stripColor(s);
    }
    
    public static List<String> colorList(final List<String> list)
    {
        return list.stream().map(Utils::color).collect(Collectors.toList());
    }
    
    public static FileConfiguration fileStartup(final File file) throws FileNotFoundException, UnsupportedEncodingException 
    {
        if(!file.exists()) 
        {
            JavaPlugin pl=JavaPlugin.getProvidingPlugin(Utils.class);
            pl.saveResource(file.getPath().replace("plugins"+File.separator+pl.getName()+File.separator, ""), true);
        }
        return YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    }
    
    public static ArmorStand spawnHologram(final Location l, final String str)
    {
        ArmorStand as=(ArmorStand)l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        as.setCustomName(str);
        as.setCustomNameVisible(true);
        as.setVisible(false);
        as.setGravity(false);
        as.setMarker(true);
        as.setRemoveWhenFarAway(false);
        as.setBasePlate(false);
        if(MCVersion.V1_9.isHigher()) 
        {
            as.setSilent(true);
            as.setInvulnerable(true); 
        }
        return as;
    }
    
    public static Item spawnFloatingItem(final Location l, final String str, final ItemStack stack)
    {
        return Optional.ofNullable(stack).map(t -> 
        {
            Item item=l.getWorld().dropItem(l, t);
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setVelocity(new Vector(0,0,0));
            item.setCustomName(str);
            item.setCustomNameVisible(true);
            if(MCVersion.V1_9.isEqual()) 
            { 
                item.setGravity(false);
                item.setSilent(true);
                item.setInvulnerable(true); 
            }  
            return item;
        }).orElseGet(() -> null);
    }
    
    public static Item spawnFloatingItem(final Location l, final String str, final Material type)
    {
        return spawnFloatingItem(l, str, new ItemStack(type));
    }
    
    public static void sendTitle(final Player p,final String title, final String subtitle, final int fadein, final int stay, final int fadeout)
    {
        API.getTitle().sendTitleAndSubTitle(title, subtitle, p, fadein, stay, fadeout);
    }

    public static String chunkToString(final Chunk c)
    { 
        return new StringBuilder().append(c.getX()).append(c.getZ()).toString(); 
    }
    
    public static ItemStack getItemByPath(final ConfigurationSection path)
    { 
        return getItemByPath(null, (short)0, path);
    }
    
    public static ItemStack getItemByPath(final Material id, final short data, final ConfigurationSection path)
    {
        ItemStack item;
        ItemMeta meta;

        String type=id==null?path.getString("id").toUpperCase():id.name();
        switch(type)
        {
            case "SKULL_ITEM":
                if(path.contains("url") && path.getInt("data", 3)==3)
                {
                    item=HeadUtils.createHead(path.getString("url"));
                    meta=item.getItemMeta();
                }
                else
                {
                    throw new IllegalArgumentException("You need to set data to 3 for custom url heads");
                }
                break;
            case "SPLASH_POTION":
            case "POTION":
            case "LINGERING_POTION":
            case "TIPPED_ARROW":
                if(MCVersion.V1_8.isEqual())
                {
                    Potion potion=new Potion(Debug.validateEnum(path.getString("type", "WATER"), PotionType.class));

                    if(type.equals("SPLASH_POTION"))
                    {
                        potion.splash();
                    }
                    item=potion.toItemStack(1);
                }
                else
                {
                    item=new ItemStack(Material.valueOf(type));
                }

                meta=item.getItemMeta();
                if(path.contains("color") && MCVersion.V1_11.isHigher())
                { 
                    ((PotionMeta)meta).setColor(Debug.validateEnum(path.getString("color"), ColorEnum.class).getBukkitColor()); 
                }

                for(Iterator<String[]> it=path.getStringList("effect").stream().map(str -> str.split(":")).iterator();it.hasNext();)
                {
                    String[] array=it.next();

                    PotionEffectType pet=PotionEffectType.getByName(array[0].toUpperCase());
                    if(pet==null)
                    {
                        throw new IllegalArgumentException("A potion effect named "+array[0]+" does not exist");
                    }
                    ((PotionMeta)meta).addCustomEffect(new PotionEffect(pet,Integer.parseInt(array[1]),Integer.parseInt(array[2])), true);
                }
                break;
            case "MONSTER_EGG":
                EntityType et = Debug.validateEnum(path.getString("data" , "PIG"), EntityType.class);

                if(MCVersion.V1_8.isEqual()) 
                {
                    item=new SpawnEgg(et).toItemStack(1); 
                    meta=item.getItemMeta();
                    break;
                }

                item=new ItemStack(Material.MONSTER_EGG);
                if(MCVersion.V1_9.isEqual() || MCVersion.V1_10.isEqual()) 
                { 
                    meta=ItemNBT.setSpawnedType(item, et).getItemMeta(); 
                }
                else 
                {
                    meta = item.getItemMeta();
                    ((SpawnEggMeta)meta).setSpawnedType(et);   
                } 
                break;
            case "LEATHER_BOOTS":
            case "LEATHER_CHESTPLATE":
            case "LEATHER_HELMET":
            case "LEATHER_LEGGINGS":
                item = new ItemStack(Material.valueOf(type));
                meta = item.getItemMeta();
                
                if(path.contains("color"))
                {
                    Color c = Debug.validateEnum(path.getString("color"), ColorEnum.class).getBukkitColor();
                    ((LeatherArmorMeta)meta).setColor(c);
                }
                break;
            default:
                item=new ItemStack(Debug.validateEnum(type, Material.class), 1, (short)path.getInt("data", data));
                meta=item.getItemMeta();
                break;
        }
        
        meta.setDisplayName(color(path.getString("name", "")));
        meta.setLore(colorList(path.getStringList("lore")));
        meta.addItemFlags(path.getStringList("flag").stream().map(str -> Debug.validateEnum(str, ItemFlag.class)).toArray(ItemFlag[]::new));
        setUnbreakable(meta, path.getBoolean("unbreakable", false));

        item.setItemMeta(meta);
        item.setAmount(path.getInt("amount", 1));

        path.getMapList("enchant").stream().map(map -> (Map<String, Integer>)map).map(Map::entrySet).flatMap(Set::stream).forEach(e -> 
        {
            Enchantment ench=Enchantment.getByName(e.getKey().toUpperCase());
            if(ench==null)
            {
                throw new IllegalArgumentException("An enchantment with name "+e.getKey()+" does not exist");
            }
            item.addUnsafeEnchantment(ench, e.getValue());
        });
        
        ItemNBT nbt=new ItemNBT(item);
        NBTCompound compound=nbt.getTag();

        new SimpleMapList(path.getMapList("tag")).getValues().forEach((key, value) -> 
        {
            compound.setValue(key, NBTType.STRING, value);
        });
        
        new SimpleMapList(path.getMapList("adventure")).getValues().forEach((key, value) -> 
        {
            AdventureTag tag = Debug.validateEnum(key, AdventureTag.class);
            ItemNBT.setAdventureFlag(compound, tag, Stream.of(value.split(",")).map(str -> Debug.validateEnum(str, Material.class)).toArray(Material[]::new));
        });
        
        if(path.contains("Attribute"))
        {
            ItemAttributeModifier modifier=new ItemAttributeModifier();
            new ComplexMapList(path.getMapList("Attribute")).getMapArrays().forEach((attr, map) -> 
            {
                ItemAttribute attribute=Debug.validateEnum(attr, ItemAttribute.class);

                double value=map.getValue("value", Double::valueOf);
                Operation op=Operation.getById(map.getValue("operation", Integer::valueOf));
                AttributeSlot slot=Debug.validateEnum(Optional.ofNullable(map.getValue("slot", Function.identity())).orElse("MAINHAND"), AttributeSlot.class);

                modifier.addModifier(slot, attribute, op, value);
            });
            modifier.apply(compound);
        }
        
        return nbt.setTag(compound).getBukkitItem();
    }
    
    public static void parseItemVariable(final ItemStack item, final String placeholder, final Object replace)
    {
        ItemMeta meta = item.getItemMeta();
        parseMetaVariable(meta, placeholder, replace);
        item.setItemMeta(meta);
    }
    
    public static void parseMetaVariable(final ItemMeta meta, final String placeholder, final Object replace)
    {
        if(meta.hasDisplayName())
        {
            meta.setDisplayName(Utils.color(meta.getDisplayName().replace(placeholder, replace.toString())));
        }
       
        if(meta.hasLore())
        {
            meta.setLore(Utils.colorList(meta.getLore().stream().map(lore -> lore.replace(placeholder, replace.toString())).collect(Collectors.toList())));
        }
    }
    
    public static class FireworkBuilder
    {
        private final Builder builder;
        public FireworkBuilder()
        {
            builder=FireworkEffect.builder();
        }
        
        public FireworkBuilder addColor(final Color... colors)
        {
            builder.withColor(colors);
            return this;
        }
        
        public FireworkBuilder addFadeColor(final Color... colors)
        {
            builder.withFade(colors);
            return this;
        }
        
        public FireworkBuilder setType(final org.bukkit.FireworkEffect.Type t)
        {
            builder.with(t);
            return this;
        }
        
        private FireworkEffect effect;
        public FireworkBuilder build()
        {
            effect=builder.build();
            return this;
        }
        
        public void spawn(final Location l, final int detonateTicks)
        {
            Firework fw=(Firework)l.getWorld().spawnEntity(l, EntityType.FIREWORK);

            FireworkMeta meta=fw.getFireworkMeta();
            meta.addEffect(effect);
            fw.setFireworkMeta(meta);
            
            JavaPlugin plugin=JavaPlugin.getProvidingPlugin(Utils.class);
            
            fw.setMetadata(Utils.FIREWORK_DATA, new FixedMetadataValue(plugin, true));

            new BukkitRunnable()
            {
                @Override
                public void run() 
                {
                    fw.detonate();
                }
            }.runTaskLater(plugin, detonateTicks);
        }
    }
}
