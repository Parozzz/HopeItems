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
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopeitems.items.ItemInfo;
import me.parozzz.hopeitems.items.ItemInfo.When;
import me.parozzz.reflex.utilities.TaskUtil;

/**
 *
 * @author Paros
 */
public class MobTable 
{
    private final static Logger logger = Logger.getLogger(MobTable.class.getSimpleName());
    
    private final DatabaseManager databaseManager;
    protected MobTable(final DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
        
        try (Connection con = databaseManager.getConnection()) {
            con.createStatement().execute("CREATE TABLE IF NOT EXISTS mobs (uuid TEXT UNIQUE, id TEXT, whenEnum TEXT);");
        } catch(final SQLException ex) {
            logger.log(Level.SEVERE, "Error creating MobTable", ex);
        }
    }
    
    private final String ADD_MOB = "INSERT OR IGNORE INTO mobs (uuid, id, whenEnum) VALUES (?, ?, ?);";
    public void addMob(final UUID u, final ItemInfo itemInfo)
    {
        String uuid = u.toString();
        String collectionId = itemInfo.getCollection().getId();
        String when = itemInfo.getWhens().stream().findFirst().get().name();
        
        TaskUtil.scheduleAsync(() -> 
        {
            try (Connection con = databaseManager.getConnection()) {
                PreparedStatement prepared = con.prepareStatement(ADD_MOB);
                prepared.setString(1, uuid);
                prepared.setString(2, collectionId);
                prepared.setString(3, when);
                prepared.executeUpdate();
            } catch(final SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private final String REMOVE_MOB = "DELETE FROM mobs WHERE uuid = ?;";
    public void removeMob(final UUID u)
    {
        String uuid = u.toString();
        
        TaskUtil.scheduleAsync(() -> 
        {
            try (Connection con = databaseManager.getConnection()) {
                PreparedStatement prepared = con.prepareStatement(REMOVE_MOB);
                prepared.setString(1, uuid);
                prepared.executeUpdate();
            } catch(final SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }
    
    public List<SQLMobInfo> getAllMobs()
    {
        try (Connection con = databaseManager.getConnection()) {
            ResultSet set = con.prepareStatement("SELECT * FROM mobs").executeQuery();
            
            List<SQLMobInfo> list = new LinkedList<>();
            while(set.next())
            {
                list.add(new SQLMobInfo(UUID.fromString(set.getString("uuid")), set.getString("id"), When.valueOf(set.getString("whenEnum"))));
            }
            return list;
        } catch(final SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return new LinkedList<>();
        }
    }
    
    public class SQLMobInfo
    {
        private final UUID uuid;
        private final String collectionId;
        private final When when;
        private SQLMobInfo(final UUID uuid, final String collectionId, final When when)
        {
            this.uuid = uuid;
            this.collectionId = collectionId;
            this.when = when;
        }
        
        public UUID getUUID()
        {
            return uuid;
        }
        
        public String getCollectionId()
        {
            return collectionId;
        }
        
        public When getWhen()
        {
            return when;
        }
    }
}
