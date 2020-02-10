package org.vilutis.lt.revolut.backend.api;

import com.google.gson.Gson;
import org.vilutis.lt.revolut.backend.dao.AccountDAO;
import org.vilutis.lt.revolut.backend.domain.Account;
import spark.Spark;
import spark.utils.Assert;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * REST API for {@link Account} management
 */
public class AccountServiceEndoint {

    public static final String APPLICATION_JSON = "application/json";

    private final AccountDAO accountDAO;

    private final Gson gson = new Gson();

    public AccountServiceEndoint(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;

        routeFindAccountByNumber();
    }

    private void routeFindAccountByNumber() {
        Spark.get("/account/:accountNumber", (req, res) -> {
            final String accountNumber = req.params("accountNumber");
            Assert.notNull(accountNumber, "param accountNumber cannot be null");

            res.type(APPLICATION_JSON);
            try {
                long accountNbr = Long.parseLong(accountNumber);

                res.status(HttpServletResponse.SC_OK);

                Account account = accountDAO.findByAccountNumber(accountNbr);

                if (account != null) {
                    return respondOK(account);
                } else {
                    return respond(404, "Account not found");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace(System.out);

                res.status(HttpServletResponse.SC_BAD_REQUEST);
                return respond(400, "Invalid number format for param 'accountNumber'");
            } catch (RuntimeException e) {
                e.printStackTrace(System.err);

                res.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return respond(500, e.getMessage());
            }
        });
    }

    private String respondOK(Serializable data) {
        return gson.toJson(new Response(200, "OK", data));
    }

    private String respond(int status, String message) {
        return gson.toJson(new Response(status, message));
    }

    class Response implements Serializable {

        private final int status;

        private final String message;

        private final Serializable data;

        public Response(int status, String message, Serializable data) {
            Assert.notNull(status, "status param cannot be null");
            Assert.notNull(message, "message param cannot be null");
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public Response(int status, String message) {
            this(status, message, null);
        }

        @Override
        public String toString() {
            return "Response{" + "status='" + status + '\'' + ", message='" + message + '\'' + ", data=" + data + '}';
        }
    }
}
