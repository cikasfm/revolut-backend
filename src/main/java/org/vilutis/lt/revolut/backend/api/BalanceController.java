package org.vilutis.lt.revolut.backend.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vilutis.lt.revolut.backend.dao.AccountDao;
import org.vilutis.lt.revolut.backend.domain.Account;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.Assert;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.math.BigDecimal;

import static org.vilutis.lt.revolut.backend.api.StandardResponse.*;

/**
 * REST API for {@link Account} management
 */
public class BalanceController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AccountDao accountDAO;

    private final Gson gson;

    public final Route transfer = ( req, res ) -> transferBalance( req, res );
    public final Route deposit = ( req, res ) -> deposit( req, res );
    public final Route withdraw = ( req, res ) -> withdraw( req, res );

    /**
     * Initializes Balance REST API Endpoint routes
     *
     * @param accountDAO required for Data interactions
     */
    public BalanceController(AccountDao accountDAO, Gson gson) {
        Assert.notNull(accountDAO);
        Assert.notNull(gson);
        this.accountDAO = accountDAO;
        this.gson = gson;
    }

    /**
     * Transfers a balance from one account to another.
     * Expects {@link Request} to contain body with a JSON compatible with {@link TransferDTO}
     * @param req
     * @param res
     * @return
     */
    protected StandardResponse transferBalance(Request req, Response res) {
        try {
            res.status(HttpServletResponse.SC_OK);

            final TransferDTO transferDTO = gson.fromJson(req.body(), TransferDTO.class);

            accountDAO.transferBalance(transferDTO.fromAcct, transferDTO.toAcct, transferDTO.amount);

            return respondOK(null);
        } catch (IllegalArgumentException | JsonSyntaxException e) {
            logger.debug(e.getMessage(), e);

            res.status(HttpServletResponse.SC_BAD_REQUEST);
            return respond(400, e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);

            res.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return respond(500, e.getMessage());
        }
    }

    protected StandardResponse deposit(Request req, Response res) {
        try {
            res.status(HttpServletResponse.SC_OK);

            final TransferDTO transferDTO = gson.fromJson(req.body(), TransferDTO.class);

            return respondOK(accountDAO.deposit(transferDTO.toAcct, transferDTO.amount));
        } catch (IllegalArgumentException | JsonSyntaxException e) {
            logger.debug(e.getMessage(), e);

            res.status(HttpServletResponse.SC_BAD_REQUEST);
            return respond(400, e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);

            res.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return respond(500, e.getMessage());
        }
    }

    protected StandardResponse withdraw(Request req, Response res) {
        try {
            res.status(HttpServletResponse.SC_OK);

            final TransferDTO transferDTO = gson.fromJson(req.body(), TransferDTO.class);

            return respondOK(accountDAO.withdraw(transferDTO.fromAcct, transferDTO.amount));
        } catch (IllegalArgumentException | JsonSyntaxException e) {
            logger.debug(e.getMessage(), e);

            res.status(HttpServletResponse.SC_BAD_REQUEST);
            return respond(400, e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);

            res.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return respond(500, e.getMessage());
        }
    }

    /**
     * Used for JSON Data Transfer
     */
    static class TransferDTO implements Serializable {
        Long fromAcct;
        Long toAcct;
        BigDecimal amount;
    }
}
