package org.vilutis.lt.revolut.backend.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {

    private final HikariConfig config;

    private final HikariDataSource hikariDataSource;

    public DataSource(String propertyFileName) {
        config = new HikariConfig(propertyFileName);
        hikariDataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }
}
