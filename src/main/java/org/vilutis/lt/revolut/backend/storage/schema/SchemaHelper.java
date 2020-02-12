package org.vilutis.lt.revolut.backend.storage.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vilutis.lt.revolut.backend.storage.ExceptionHelper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility to setup DB Schema for testing purposes. In a real project this should be move to smth like "Liquibase"
 * scripts
 */
public interface SchemaHelper {

    Logger logger = LoggerFactory.getLogger(SchemaHelper.class);

    /**
     * Creates required database tables
     *
     * @param dataSource the DB connection
     */
    static void setupSchema(DataSource dataSource) {
        try( Connection connection = dataSource.getConnection() ) {
            logger.info("START: setupSchema - creating tables");
            try ( Statement statement = connection.createStatement() ) {
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS account ( "
                                + " accountNumber IDENTITY NOT NULL PRIMARY KEY, "
                                + " accountName VARCHAR NOT NULL, "
                                + " balance DECIMAL(20, 2) DEFAULT 0.00 "
                                + " );"
                );
                try (ResultSet rs = statement.getResultSet()){
                    if ( rs != null ) do {
                        // next
                    } while ( rs.next() );
                }
            }
            logger.info("END: setupSchema - tables created");
        } catch (SQLException e) {
            logger.error(ExceptionHelper.convertException(e).getMessage(), e);
            throw new Error(e);
        }
    }
}
