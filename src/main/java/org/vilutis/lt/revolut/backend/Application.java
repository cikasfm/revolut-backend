package org.vilutis.lt.revolut.backend;

import org.vilutis.lt.revolut.backend.api.AccountServiceEndpoint;
import org.vilutis.lt.revolut.backend.dao.AccountDAO;
import org.vilutis.lt.revolut.backend.dao.impl.AccountDaoJdbcImpl;
import org.vilutis.lt.revolut.backend.domain.Account;
import org.vilutis.lt.revolut.backend.storage.DBStorage;

/**
 * Main application entry class for Account REST API
 */
public class Application {

    public static void main(String[] args) {

        //
        // First we load the underlying JDBC driver.
        // You need this if you don't use the jdbc.drivers
        // system property.
        //
        System.out.println("Loading underlying JDBC driver.");
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Done.");

        final DBStorage dbStorage = new DBStorage("jdbc:h2:mem:test", true);

        final AccountDAO accountDAO = new AccountDaoJdbcImpl(dbStorage);

        new AccountServiceEndpoint(accountDAO);

    }

}
