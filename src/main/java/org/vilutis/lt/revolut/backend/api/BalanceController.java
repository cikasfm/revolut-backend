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

/**
 * REST API for {@link Account} management
 */
public class BalanceController {

    public static final String APPLICATION_JSON = "application/json";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AccountDao accountDAO;

    private final Gson gson = new Gson();

    public final Route transfer = ( req, res ) -> transferBalance( req, res );

    /**
     * Initializes Balance REST API Endpoint routes
     *
     * @param accountDAO required for Data interactions
     */
    public BalanceController(AccountDao accountDAO) {
        this.accountDAO = accountDAO;
    }

    protected String transferBalance(Request req, Response res) {
        res.type(APPLICATION_JSON);
        try {
            res.status(HttpServletResponse.SC_OK);

            final TransferDTO transferDTO = gson.fromJson(req.body(), TransferDTO.class);

            accountDAO.transferBalance(transferDTO.fromAcct, transferDTO.toAcct, transferDTO.balance);

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

    /**
     * Responds with default status 200, message "OK" and serializes given data to a JSON
     *
     * @param data the object to return in the "data" attribute of the JSON
     * @return a serialized {@link StandardResponse} to a JSON string
     */
    private <T extends Serializable> String respondOK(T data) {
        return gson.toJson(new StandardResponse<>(200, "OK", data));
    }

    /**
     * Responds with given status and message. Used mostly for "error" responses.
     *
     * @param status the status code
     * @param message the message to be returned
     * @return a serialized {@link StandardResponse} to a JSON string
     */
    private String respond(int status, String message) {
        return gson.toJson(new StandardResponse(status, message));
    }

    /**
     * Serializable standard response DTO
     */
    class StandardResponse<T extends Serializable> implements Serializable {

        final int status;

        final String message;

        final T data;

        StandardResponse(int status, String message, T data) {
            Assert.notNull(status, "status param cannot be null");
            Assert.notNull(message, "message param cannot be null");
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public StandardResponse(int status, String message) {
            this(status, message, null);
        }

        @Override
        public String toString() {
            return "Response{" + "status='" + status + '\'' + ", message='" + message + '\'' + ", data=" + data + '}';
        }
    }

    class TransferDTO implements Serializable {
        final Long fromAcct;
        final Long toAcct;
        final BigDecimal balance;

        TransferDTO(Long fromAcct, Long toAcct, BigDecimal balance) {
            this.fromAcct = fromAcct;
            this.toAcct = toAcct;
            this.balance = balance;
        }
    }
}
