package controllers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPoolController {
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource dataSource;

    static {
        // Configure the HikariCP settings
        config.setJdbcUrl("jdbc:mysql://localhost:3306/crud_app"); // Update as needed
        config.setUsername("root"); // Update as needed
        config.setPassword("14qr!$QR25wt@%WT"); // Update as needed
        config.setMaximumPoolSize(10); // Set the desired maximum pool size

        dataSource = new HikariDataSource(config);
    }

    // Retrieve a connection from the pool
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}