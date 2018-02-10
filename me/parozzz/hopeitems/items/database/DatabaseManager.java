/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopeitems.items.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import me.parozzz.hopeitems.HopeItems;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paros
 */
public final class DatabaseManager 
{
    private final HikariDataSource source;
    private final BlockTable blockTable;
    private final MobTable mobTable;
    public DatabaseManager(final HopeItems hopeItems)
    {
        HikariConfig config = new HikariConfig();
        config.setConnectionTestQuery("SELECT 1;");
        config.setPoolName("HopeItemsSQlitePool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + hopeItems.getDataFolder().getAbsolutePath() + File.separator + "database.db");
        config.setMaximumPoolSize(20);
        
        source = new HikariDataSource(config);
        
        blockTable = new BlockTable(this);
        mobTable = new MobTable(this);
    }
    
    public MobTable getMobTable()
    {
        return mobTable;
    }
    
    public BlockTable getBlockTable()
    {
        return blockTable;
    }
    
    public synchronized Connection getConnection() throws SQLException
    {
        return source.getConnection();
    }
}
