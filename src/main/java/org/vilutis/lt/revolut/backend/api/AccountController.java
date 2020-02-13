package org.vilutis.lt.revolut.backend.api;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vilutis.lt.revolut.backend.dao.AccountDao;
import org.vilutis.lt.revolut.backend.domain.Account;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.Assert;

import javax.servlet.http.HttpServletResponse;

import static org.vilutis.lt.revolut.backend.api.StandardResponse.respond;
import static org.vilutis.lt.revolut.backend.api.StandardResponse.respondOK;

/**
 * REST API for {@link Account} management
 */
public class AccountController {

    public static final String APPLICATION_JSON = "application/json";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AccountDao accountDAO;

    public final Route findAll = (req, res) -> findAllAccounts(req, res);
    public final Route findAccountByNumber = (req, res) -> findAccountByNumber(req, res);
    public final Route create = (req, res) -> create(req, res);

    /**
     * Initializes Account REST API Endpoint and exposes available API routes
     * @param accountDAO required for Data interactions
     */
    public AccountController(AccountDao accountDAO) {
        this.accountDAO = accountDAO;
    }

    protected StandardResponse create(Request req, Response res) {
        res.type(APPLICATION_JSON);
        try {
            res.status(HttpServletResponse.SC_OK);

            String accountName = req.queryParams("accountName");

            Assert.hasLength(accountName, "accountName query param must be not empty");

            return respondOK(accountDAO.create(accountName));
        } catch (IllegalArgumentException e) {
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
     * Find all accounts endpoint with paging parameter
     * @return all accounts array wrapped in a {@link StandardResponse} data and serialized as JSON string.
     */
    protected StandardResponse findAllAccounts(Request req, Response res) {
        res.type(APPLICATION_JSON);
        try {
            res.status(HttpServletResponse.SC_OK);

            int pageNum = Integer.parseInt(req.queryParamOrDefault("pageNum", "0"));
            int pageSize = Integer.parseInt(req.queryParamOrDefault("pageSize", "20"));

            return respondOK(accountDAO.findAll(pageNum, pageSize));
        } catch (NumberFormatException e) {
            logger.debug(e.getMessage(), e);

            res.status(HttpServletResponse.SC_BAD_REQUEST);
            return respond(400, "Invalid number format for param 'pageNum' or 'pageSize");
        } catch (IllegalArgumentException e) {
            logger.debug(e.getMessage(), e);

            res.status(HttpServletResponse.SC_BAD_REQUEST);
            return respond(400, e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);

            res.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return respond(500, e.getMessage());
        }
    }

    protected StandardResponse findAccountByNumber(Request req, Response res) {
        final String accountNumber = req.params("accountNumber");
        Assert.notNull(accountNumber, "param accountNumber cannot be null");
        try {
            long accountNbr = Long.parseLong(accountNumber);

            res.status(HttpServletResponse.SC_OK);

            Account account = accountDAO.findByAccountNumber(accountNbr);

            if (account != null) {
                return respondOK(account);
            } else {
                res.status(HttpServletResponse.SC_NOT_FOUND);
                return respond(404, "Account not found");
            }
        } catch (NumberFormatException e) {
            logger.debug(e.getMessage(), e);

            res.status(HttpServletResponse.SC_BAD_REQUEST);
            return respond(400, "Invalid number format for param 'accountNumber'");
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);

            res.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return respond(500, e.getMessage());
        }
    }
}
