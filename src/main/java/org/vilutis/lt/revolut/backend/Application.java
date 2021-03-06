package org.vilutis.lt.revolut.backend;

import com.google.gson.Gson;
import org.eclipse.jetty.http.MimeTypes;
import org.vilutis.lt.revolut.backend.api.AccountController;
import org.vilutis.lt.revolut.backend.api.BalanceController;
import org.vilutis.lt.revolut.backend.dao.AccountDao;
import org.vilutis.lt.revolut.backend.dao.impl.AccountDaoJdbcImpl;
import org.vilutis.lt.revolut.backend.storage.DBStorage;
import spark.Service;

import static spark.Spark.*;

/**
 * Main application entry class for Account REST API
 */
public class Application {

    public static void main(String[] args) {

        final Gson gson = new Gson();

        final DBStorage dbStorage = new DBStorage("/prod.db.properties");

        final AccountDao accountDAO = new AccountDaoJdbcImpl(dbStorage);

        final AccountController accountController = new AccountController(accountDAO);
        final BalanceController balanceController = new BalanceController(accountDAO, gson);

        ProcessBuilder process = new ProcessBuilder();

        Integer port = Service.SPARK_DEFAULT_PORT;

        // This tells our app that if Heroku sets a port for us, we need to use that port.
        // Otherwise, if they do not, continue using port 4567.

        if (process.environment().get("PORT") != null) {
            port = Integer.parseInt(process.environment().get("PORT"));
        }

        port(port);


        // Configure Spark
        staticFiles.location("/public");
        staticFiles.expireTime(600L);

        // thread pool setup
        threadPool(8, 2, 30000);

        // setup API routes
        path("/api", ()->{
            before((req, res) -> res.type(MimeTypes.Type.APPLICATION_JSON.asString()));
            // account API
            path("/account", () -> {
                put("", accountController.create, gson::toJson);
                get("/all", accountController.findAll, gson::toJson);
                get("/:accountNumber", accountController.findAccountByNumber, gson::toJson);
            });
            path("/balance", () -> {
                post("/transfer", balanceController.transfer, gson::toJson);
                post("/deposit", balanceController.deposit, gson::toJson);
                post("/withdraw", balanceController.withdraw, gson::toJson);
            });
        });

    }

}
