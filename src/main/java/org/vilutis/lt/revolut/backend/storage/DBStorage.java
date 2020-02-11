package org.vilutis.lt.revolut.backend.storage;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vilutis.lt.revolut.backend.storage.schema.SchemaHelper;

import javax.sql.DataSource;
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

    public DBStorage(String connectionString, boolean setupSchema) {
        this.dataSource = setupDataSource(connectionString);
        if (setupSchema) {
            SchemaHelper.setupSchema(dataSource);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
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
            connection.setAutoCommit(false);
            final Savepoint savepoint = connection.setSavepoint();
            try {
                final T result = action.doSQL(connection);
                connection.commit();
                return result;
            } catch ( Throwable t ) {
                if ( rollbackOnError ) {
                    connection.rollback( savepoint );
                }
                // to be handled by the consumer
                throw t;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static DataSource setupDataSource(String connectURI) {

        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, null);

        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        return new PoolingDataSource<>(connectionPool);
    }

}
