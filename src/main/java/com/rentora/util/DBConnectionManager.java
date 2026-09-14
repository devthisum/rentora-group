package com.rentora.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton Pattern.
 * Provides a single, shared, thread-safe HikariCP connection pool
 * for the entire application. All DAO classes obtain connections
 * exclusively through this manager.
 */
public final class DBConnectionManager {

    private static volatile DBConnectionManager instance;
    private final HikariDataSource dataSource;

    private DBConnectionManager() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getProperty("rentora.db.url",
                "jdbc:mysql://localhost:3306/rentora_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"));
        config.setUsername(System.getProperty("rentora.db.user", "root"));
        config.setPassword(System.getProperty("rentora.db.password", "stp456"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(15);
        config.setMinimumIdle(3);
        config.setPoolName("RentoraPool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
    }

    public static DBConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DBConnectionManager.class) {
                if (instance == null) {
                    instance = new DBConnectionManager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
