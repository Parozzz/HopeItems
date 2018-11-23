/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopeitems.items.ItemCollection;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.reflex.utilities.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 *
 * @author Paros
 */
public class BlockTable 
{
    private static final Logger logger = Logger.getLogger(BlockTable.class.getSimpleName());
    
    private final DatabaseManager databaseManager;
    protected BlockTable(final DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
        
        try (Connection con = databaseManager.getConnection()) {
            con.createStatement().execute("CREATE TABLE IF NOT EXISTS blocks (x INTEGER, y INTEGER, z INTEGER, world TEXT, id TEXT);");
        } catch(final SQLException ex) {
            logger.log(Level.SEVERE, "Error creative BlockTable", ex);
        }
    }
    
    private final String ADD_BLOCK = "INSERT INTO blocks (x, y, z, world, id) VALUES (?, ?, ?, ? , ?);";
    public void addBlock(final Location l, final ItemCollection collection)
    {
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        String world = l.getWorld().getName();
        
        String collectionId = collection.getId();
        
        TaskUtil.scheduleAsync(() -> 
        {
            try (Connection con = databaseManager.getConnection()) {
                PreparedStatement prepared = con.prepareStatement(ADD_BLOCK);
                prepared.setInt(1, x);
                prepared.setInt(2, y);
                prepared.setInt(3, z);
                prepared.setString(4, world);
                prepared.setString(5, collectionId);
                prepared.executeUpdate();
            } catch(final SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private final String REMOVE_BLOCK = "DELETE FROM blocks WHERE x = ? AND y = ? AND z = ? AND world = ?;";
    public void removeBlock(final Location l)
    {
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        String world = l.getWorld().getName();
        
        TaskUtil.scheduleAsync(() -> 
        {
            try (Connection con = databaseManager.getConnection()) {
                PreparedStatement prepared = con.prepareStatement(REMOVE_BLOCK);
                prepared.setInt(1, x);
                prepared.setInt(2, y);
                prepared.setInt(3, z);
                prepared.setString(4, world);
                prepared.executeUpdate();
            } catch(final SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }
    
    public Map<Location, String> getAllBlocks()
    {
        try(Connection con = databaseManager.getConnection()) {
            ResultSet set = con.prepareStatement("SELECT * FROM blocks").executeQuery();
            
            Map<Location, String> map = new HashMap<>();
            while(set.next())
            {
                Location loc = new Location(Bukkit.getWorld(set.getString("world")), set.getInt("x"), set.getInt("y"), set.getInt("z"));
                String id = set.getString("id");
            
                map.put(loc, id);
            }
            return map;
        } catch(final SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return new HashMap<>();
        }
    }
}
