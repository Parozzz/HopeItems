/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.parozzz.hopeitems;

import it.parozzz.hopeitems.core.ItemDatabase;
import it.parozzz.hopeitems.core.Utils;
import it.parozzz.hopeitems.manager.ItemManager;
import it.parozzz.hopeitems.manager.MobManager.RevertBlock;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 *
 * @author Paros
 */
public class Database 
{
    public static void newDatabase() 
    { 

        id=new ItemDatabase(); 
        
        database=new HashSet<>();
        
        revert=new HashMap<>();
        itemManagers=new HashMap<>();
        blocks=new HashMap<>();
        players=new HashMap<>();
    }
    
    private static Set<Entity> database;
    public static void addEntity(final Entity ent) { database.add(ent); }
    public static Set<Entity> getEntities() { return database; }
    public static void removeEntity(final Entity ent) { database.remove(ent); }
    
    private static Map<Block,RevertBlock> revert;
    public static RevertBlock addRevert(final Block b,final RevertBlock rb) 
    { 
        revert.put(b,rb); 
        return rb;
    }
    public static void removeRevert(final Block b) { revert.remove(b); }
    public static void revertAll() { revert.forEach((b,rb) -> rb.revert(b)); }
    
    private static ItemDatabase id;

    public static void addItem(final String name, final ItemStack item) { id.addItem(name, item); }
    public static ItemStack getItem(final String name) { return id.getItem(name); }
    public static String getName(final ItemStack item) { return (String)id.getKey(item.clone()); }
    public static ItemDatabase getDatabase() { return id; }
    
    
    private static Map<String,ItemManager> itemManagers;
    
    public static void addItemManager(final ItemManager im, final String name) { itemManagers.put(name, im); }
    public static ItemManager getItemManager(final String name) { return itemManagers.get(name); }
    
    private static Map<Block,BlockManager> blocks=new HashMap<>();
    private static Map<UUID,PlayerManager> players=new HashMap<>();
    /**
     * Return Get the block manager of a block, null if not present
     * @param b The block to search the manager of
     * @return The manager of the block
     */
    public static BlockManager getBlockManager(final Block b)
    {
        return blocks.get(b);
    }
    
    public static BlockManager removeCustomBlock(final Block b)
    {
        return Optional.ofNullable(blocks.remove(b)).flatMap(bm -> 
        {
            Optional.ofNullable(players.get(bm.getOwner())).ifPresent(pm ->  pm.removeBlock(b));
            b.removeMetadata(Value.CustomBlockMetadata, HopeItems.getInstance());
            return Optional.of(bm);
        }).orElseGet(() -> null);
    }
    
    public static boolean isCustomBlock(final Block b)
    {
        return b.hasMetadata(Value.CustomBlockMetadata);
    }
    
    public static void addCustomBlock(final Block b, final UUID owner, final String ownerName, final ItemManager im)
    {
        b.setMetadata(Value.CustomBlockMetadata, new FixedMetadataValue(HopeItems.getInstance(),im));
        blocks.put(b, new BlockManager(owner,im));
        Optional.ofNullable(players.get(owner)).orElseGet(() -> 
        {
            PlayerManager pm=new PlayerManager(owner, ownerName);
            players.put(owner, pm);
            return pm;
        }).addBlock(b, im.getName());
    }
    
    public static void loadData(final File dataFolder)
    {
        Stream.of(dataFolder.listFiles()).forEach(file -> 
        {
            FileConfiguration c=YamlConfiguration.loadConfiguration(file);
            //Utils.join(";", b.getWorld().getName(),b.getX(),b.getY(),b.getZ()));
            UUID u=UUID.fromString(file.getName().replace(".yml", ""));
            String ownerName=c.getString("playerName");
            c.getStringList("blocks").stream().map(str -> str.split(";")).filter(array -> Bukkit.getWorld(array[0])!=null).forEach(array -> 
            { 
                Optional.ofNullable(Database.getItemManager(array[4])).ifPresent(im -> 
                {
                    Block b=Bukkit.getWorld(array[0]).getBlockAt(Integer.valueOf(array[1]), Integer.valueOf(array[2]), Integer.valueOf(array[3]));        
                    Database.addCustomBlock(b, u, ownerName, Database.getItemManager(array[4]));
                });                        
            });
        });
    }
    
    public static void saveData(final File dataFolder)
    {
        players.forEach((u,pm) -> 
        {
            File file=new File(dataFolder,u.toString()+".yml");            
            if(pm.getStringBlocks().isEmpty())
            {
                file.delete();
                return;
            }
            
            pm.getBlocks().forEach(b -> b.removeMetadata(Value.CustomBlockMetadata, HopeItems.getInstance()));
            
            FileConfiguration c=new YamlConfiguration();
            c.set("playerName", pm.getOwnerName());
            c.set("blocks", new ArrayList<>(pm.getStringBlocks()));
            
            try { c.save(file); } 
            catch (IOException ex) { ex.printStackTrace(); }
        });
    }
    
    private static class PlayerManager
    {
        final UUID owner;
        final String ownerName;
        public PlayerManager(final UUID owner, final String ownerName)
        {
            this.owner=owner;
            this.ownerName=ownerName;
        }
        
        public UUID getOwnerUUID() { return owner; }
        public String getOwnerName() { return ownerName; }
        
        public final Map<Block,String> blocks=new HashMap<>();
        public PlayerManager addBlock(final Block b, final String itemManagerName)
        {
            blocks.put(b, Utils.join(";", b.getWorld().getName(),b.getX(),b.getY(),b.getZ(),itemManagerName));
            return this;
        }
        
        public void removeBlock(final Block b)
        {
            blocks.remove(b);
        }
        
        public Collection<String> getStringBlocks()
        {
            return blocks.values();
        }
        
        public Set<Block> getBlocks()
        {
            return blocks.keySet();
        }
        
        public boolean hasBlock(final Block b) 
        {
            return blocks.containsKey(b);
        }
    }
    
    /*
    private static class WorldManager
    {
        private final Map<String,ChunkManager> chunks=new HashMap<>();
        private final String worldName;
        
        public WorldManager(final World w) { worldName=w.getName(); }
        
        public String getWorldName() { return worldName; }
        
        public Map<Block, BlockManager> getBlocksMap() 
        { 
            return chunks.values().stream()
                    .map(ChunkManager::getBlocksMap)
                    .map(Map::entrySet)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        
        public void addBlock(final Block b, final UUID owner, final ItemManager im)
        {
            Optional.ofNullable(chunks.get(Utils.chunkToString(b.getChunk()))).orElseGet(() -> 
            {
                ChunkManager cm=new ChunkManager();
                chunks.put(Utils.chunkToString(b.getChunk()),cm);
                return cm;
            }).addBlock(b, owner, im);
        }
        
        public boolean isBlock(final Block b)
        {
            return Optional.ofNullable(chunks.get(Utils.chunkToString(b.getChunk()))).flatMap(cm -> Optional.of(cm.isBlock(b))).orElseGet(() -> false);
        }
        
        public BlockManager removeBlock(final Block b) 
        {
            return Optional.ofNullable(chunks.get(Utils.chunkToString(b.getChunk()))).flatMap(cm -> Optional.ofNullable(cm.removeBlock(b))).orElseGet(() -> null);
        }
        
        public BlockManager getBlockManager(final Block b)
        {
            return Optional.ofNullable(chunks.get(Utils.chunkToString(b.getChunk()))).orElseGet(() -> 
            {
                ChunkManager cm=new ChunkManager();
                chunks.put(Utils.chunkToString(b.getChunk()),cm);
                return cm;
            }).getBlockManager(b);
        }
    }
    
    private static class ChunkManager
    {
        private final Map<Block, BlockManager> blocks=new HashMap<>();
        
        public Map<Block, BlockManager> getBlocksMap() { return blocks; }
        public void addBlock(final Block b, final UUID owner, final ItemManager im) { blocks.put(b, new BlockManager(owner,im)); }
        
        public boolean isBlock(final Block b) { return blocks.containsKey(b); }
        public BlockManager removeBlock(final Block b) { return blocks.remove(b); }
        public BlockManager getBlockManager(final Block b) { return blocks.get(b); }
    }
    */
    public static class BlockManager
    {
        private final UUID owner;
        private final ItemManager im;
        public BlockManager(final UUID owner, final ItemManager im) 
        { 
            this.owner=owner;
            this.im=im; 
        }
        
        public UUID getOwner() { return owner; }
        public ItemManager getItemManager() { return im; }
    }
}
