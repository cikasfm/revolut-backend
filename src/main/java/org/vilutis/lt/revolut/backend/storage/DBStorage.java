package org.vilutis.lt.revolut.backend.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * JDBC Data Storage accessor util
 */
public class DBStorage {

    private final DataSource dataSource;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Database Actions to be performed using a JDBC {@link Connection}
     * @param <T> the action return type
     */
    public interface DBAction<T> {
        /**
         * Implement any action using the database {@link Connection}
         * @param connection the JDBC {@link Connection}
         * @return to be determined by the implementing class
         * @throws SQLException in case any DB error occurs
         */
        T doSQL(Connection connection) throws SQLException;
    }

    /**
     * Instantiates {@link DBStorage} and creates JDBC {@link Connection} pool to be accessed using {@link DataSource}
     *
     * @param propertyFileName HikariCP property file location in the class path.
     * @see <a href="https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby">HikariCP configuration</a>
     */
    public DBStorage(String propertyFileName) {
        this.dataSource = new DataSource(propertyFileName);
    }

    /**
     * Runs SQL in default JDBC {@link Connection} (auto) commit mode. Does not create a transaction
     *
     * @param action the {@link DBAction} to run
     * @param <T> the {@link DBAction} return type
     * @return result from {@link DBAction#doSQL(Connection)} if successful
     * @throws SQLException in case of DB error
     */
    public <T extends Serializable> T runSQL(DBAction<T> action) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return action.doSQL(connection);
        }
    }

    /**
     * Runs SQL in default a transaction. Commits transaction if successful, rollbacks on exception
     *
     * @param action the {@link DBAction} to run
     * @param <T> the {@link DBAction} return type
     * @return result from {@link DBAction#doSQL(Connection)} if successful
     * @throws SQLException in case of DB error
     */
    public <T extends Serializable> T runInTransaction(DBAction<T> action) throws SQLException {
        return runInTransaction(action, true);
    }

    /**
     * Runs SQL in default a transaction. Commits transaction if successful, rollbacks on exception
     *
     * @param action the {@link DBAction} to run
     * @param rollbackOnError if true - will rollback the transacation on any exception thrown.
     * @param <T> the {@link DBAction} return type
     * @return result from {@link DBAction#doSQL(Connection)} if successful
     * @throws SQLException in case of DB error
     */
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
                } else {
                    connection.releaseSavepoint( savepoint );
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
