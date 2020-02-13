package org.vilutis.lt.revolut.backend.api;

import org.junit.BeforeClass;
import org.junit.Test;
import org.vilutis.lt.revolut.backend.dao.AccountDao;
import org.vilutis.lt.revolut.backend.dao.impl.AccountDaoJdbcImpl;
import org.vilutis.lt.revolut.backend.domain.Account;
import org.vilutis.lt.revolut.backend.storage.DBStorage;
import org.vilutis.lt.revolut.backend.test.TestUtil;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AccountControllerTest {

    private static AccountController controller;
    private static Account[] testAccounts;

    @BeforeClass
    public static void init() {

        Spark.port(TestUtil.findRandomOpenPort());

        AccountDao accountDAO = new AccountDaoJdbcImpl(new DBStorage("/test.db.properties"));

        controller = new AccountController(accountDAO);

        testAccounts =
                new Account[] {accountDAO.create("first"), accountDAO.create("second"), accountDAO.create("third")};
    }

    @Test
    public void findAllAccounts_happyPath() {
        final Request req = mock(Request.class);
        when(req.queryParamOrDefault(eq("pageSize"), anyString())).thenReturn("20");
        when(req.queryParamOrDefault(eq("pageNum"), anyString())).thenReturn("0");

        final Response res = mock(Response.class);

        final StandardResponse<ArrayList<Account>> result = controller.findAllAccounts(req, res);

        verify(res).type(eq("application/json"));
        verify(res).status(eq(200));

        assertThat(result.status, equalTo(200));
        assertThat(result.message, equalTo("OK"));
        assertThat(result.data, isA(ArrayList.class));

        assertThat("there must be 3 or more results", result.data.size() >= 3);
    }

    @Test
    public void findAccountByNumber_happyPath() {
        final Request req = mock(Request.class);
        when(req.params(eq("accountNumber"))).thenReturn(testAccounts[0].getAccountNumber().toString());

        final Response res = mock(Response.class);

        final StandardResponse<Account> result = controller.findAccountByNumber(req, res);

        verify(res).status(eq(200));

        assertThat("status doesn't match", result.status, equalTo(200));
        assertThat("message doesn't match", result.message, equalTo("OK"));
        assertThat("data type doesn't match", result.data, isA(Account.class));

        assertThat("accountNumber must match", result.data.getAccountNumber(),
                equalTo(testAccounts[0].getAccountNumber()));
        assertThat("accountName must match", result.data.getAccountName(), equalTo(testAccounts[0].getAccountName()));
        assertThat("balance must match", result.data.getBalance(), equalTo(testAccounts[0].getBalance()));
    }

    @Test
    public void findAccountByNumber_notFound() {
        final Request req = mock(Request.class);
        when(req.params(eq("accountNumber"))).thenReturn("987654321");

        final Response res = mock(Response.class);

        final StandardResponse<Account> result = controller.findAccountByNumber(req, res);

        verify(res).status(eq(404));

        assertThat("status doesn't match", result.status, equalTo(404));
        assertThat("message message doesn't match", result.message, equalTo("Account not found"));
        assertThat("data must not be set", result.data, is(nullValue()));
    }
}