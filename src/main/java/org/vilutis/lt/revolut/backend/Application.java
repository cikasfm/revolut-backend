package org.vilutis.lt.revolut.backend;

import org.vilutis.lt.revolut.backend.api.AccountServiceController;
import org.vilutis.lt.revolut.backend.dao.AccountDao;
import org.vilutis.lt.revolut.backend.dao.impl.AccountDaoJdbcImpl;
import org.vilutis.lt.revolut.backend.storage.DBStorage;
import spark.Spark;

import static spark.Spark.*;

/**
 * Main application entry class for Account REST API
 */
public class Application {

    public static void main(String[] args) {

        final DBStorage dbStorage = new DBStorage("/prod.db.properties");

        final AccountDao accountDAO = new AccountDaoJdbcImpl(dbStorage);

        final AccountServiceController accountService = new AccountServiceController(accountDAO);

        // Configure Spark
        staticFiles.location("/public");
        staticFiles.expireTime(600L);

        // thread pool setup
        threadPool(8, 2, 30000);

        // setup API routes
        path("/api", ()->{
            // account API
            path("/account", () -> {
                get("/all", accountService.findAll);
                get("/:accountNumber", accountService.findAccountByNumber);
            });
        });

    }

}
