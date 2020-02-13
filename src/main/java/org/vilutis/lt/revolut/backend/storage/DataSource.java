package org.vilutis.lt.revolut.backend.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A data source using HikariCP providing a basic access to JDBC {@link Connection}
 */
public class DataSource {

    private final HikariConfig config;

    private final HikariDataSource hikariDataSource;

    /**
     * Instantiates {@link DataSource} and created JDBC {@link Connection} pool to be accessed using
     * {@link #getConnection}
     *
     * @param propertyFileName HikariCP property file location in the class path.
     * @see <a href="https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby">HikariCP configuration</a>
     */
    public DataSource(String propertyFileName) {
        config = new HikariConfig(propertyFileName);
        hikariDataSource = new HikariDataSource(config);
    }

    /**
     * @return a JDBC {@link Connection} from Connection Pool by HikariCP
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }
}
