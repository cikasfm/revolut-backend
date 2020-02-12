package org.vilutis.lt.revolut.backend.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

public class DBStorage {

    private final DataSource dataSource;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public interface DBAction<T> {
        T doSQL(Connection connection) throws SQLException;
    }

    public DBStorage(String propertyFileName) {
        this.dataSource = new DataSource(propertyFileName);
    }

    public <T extends Serializable> T runSQL(DBAction<T> action) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return action.doSQL(connection);
        }
    }

    public <T extends Serializable> T runInTransaction(DBAction<T> action) throws SQLException {
        return runInTransaction(action, true);
    }

    public <T extends Serializable> T runInTransaction(DBAction<T> action, boolean rollbackOnError) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            logger.debug("connection opened");
            connection.setAutoCommit(false);
            final Savepoint savepoint = connection.setSavepoint();
            try {
                final T result = action.doSQL(connection);
                connection.commit();
                logger.debug("transaction commit");
                return result;
            } catch ( Throwable t ) {
                if ( rollbackOnError ) {
                    connection.rollback( savepoint );
                    logger.debug("transaction rollback");
                }
                // to be handled by the consumer
                throw t;
            } finally {
                connection.setAutoCommit(true);
            }
        } finally {
            logger.debug("connection closed");
        }
    }

}
