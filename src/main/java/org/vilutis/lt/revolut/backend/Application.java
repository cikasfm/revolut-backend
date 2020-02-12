package org.vilutis.lt.revolut.backend;

import org.vilutis.lt.revolut.backend.api.AccountServiceEndpoint;
import org.vilutis.lt.revolut.backend.dao.AccountDao;
import org.vilutis.lt.revolut.backend.dao.impl.AccountDaoJdbcImpl;
import org.vilutis.lt.revolut.backend.storage.DBStorage;

/**
 * Main application entry class for Account REST API
 */
public class Application {

    public static void main(String[] args) {

        final DBStorage dbStorage = new DBStorage("prod.db.properties");

        final AccountDao accountDAO = new AccountDaoJdbcImpl(dbStorage);

        new AccountServiceEndpoint(accountDAO);

    }

}
