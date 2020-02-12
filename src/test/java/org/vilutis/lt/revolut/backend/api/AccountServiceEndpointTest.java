package org.vilutis.lt.revolut.backend.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vilutis.lt.revolut.backend.dao.AccountDao;
import org.vilutis.lt.revolut.backend.dao.impl.AccountDaoJdbcImpl;
import org.vilutis.lt.revolut.backend.domain.Account;
import org.vilutis.lt.revolut.backend.storage.DBStorage;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AccountServiceEndpointTest {

    private static AccountServiceEndpoint endpoint;
    private static Account[] testAccounts;

    @BeforeClass
    public static void init() {
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

        int localPort;

        // find a random available port
        try (ServerSocket s = new ServerSocket(0)) {
            localPort = s.getLocalPort();
        } catch (IOException e) {
            throw new Error(e.getMessage(), e);
        }

        Spark.port(localPort);

        DBStorage dbStorage = new DBStorage("jdbc:h2:mem:test", true);

        AccountDao accountDAO = new AccountDaoJdbcImpl(dbStorage);

        endpoint = new AccountServiceEndpoint(accountDAO);

        testAccounts = new Account[] {
                accountDAO.create("first"),
                accountDAO.create("second"),
                accountDAO.create("third")
        };
    }

    private final Gson gson = new Gson();

    @Test
    public void findAllAccounts_happyPath() {
        final Request req = mock(Request.class);
        when(req.queryParamOrDefault(eq("pageSize"), anyString())).thenReturn("20");
        when(req.queryParamOrDefault(eq("pageNum"), anyString())).thenReturn("0");

        final Response res = mock(Response.class);

        final String result = endpoint.findAllAccounts(req, res);

        verify(res).type(eq("application/json"));
        verify(res).status(eq(200));

        final AccountServiceEndpoint.StandardResponse<ArrayList<Account>> standardResponse =
                gson.fromJson(result, new TypeToken<AccountServiceEndpoint.StandardResponse<ArrayList<Account>>>(){}.getType());

        assertThat(standardResponse.status, equalTo(200));
        assertThat(standardResponse.message, equalTo("OK"));
        assertThat(standardResponse.data, isA(ArrayList.class));

        assertThat("there must be 3 or more results", standardResponse.data.size() >= 3);
    }

    @Test
    public void findAccountByNumber_happyPath() {
        final Request req = mock(Request.class);
        when(req.params(eq("accountNumber"))).thenReturn(testAccounts[0].getAccountNumber().toString());

        final Response res = mock(Response.class);

        final String result = endpoint.findAccountByNumber(req, res);

        verify(res).type(eq("application/json"));
        verify(res).status(eq(200));

        final AccountServiceEndpoint.StandardResponse<Account> standardResponse =
                gson.fromJson(result, new TypeToken<AccountServiceEndpoint.StandardResponse<Account>>(){}.getType());

        assertThat("response status", standardResponse.status, equalTo(200));
        assertThat("response message", standardResponse.message, equalTo("OK"));
        assertThat("response data", standardResponse.data, isA(Account.class));

        assertThat("accountNumber must match", standardResponse.data.getAccountNumber(),
                equalTo(testAccounts[0].getAccountNumber()));
        assertThat("accountName must match", standardResponse.data.getAccountName(), equalTo(testAccounts[0].getAccountName()));
        assertThat("balance must match", standardResponse.data.getBalance(),
                equalTo(testAccounts[0].getBalance()));
    }
}