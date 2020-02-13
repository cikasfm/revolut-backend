package org.vilutis.lt.revolut.backend.api;

import com.google.gson.Gson;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

public class BalanceControllerTest {

    private static BalanceController controller;
    private static List<Account> testAccounts;
    private static AccountDao accountDAO;

    @BeforeClass
    public static void init() {

        Spark.port(TestUtil.findRandomOpenPort());

        accountDAO = new AccountDaoJdbcImpl(new DBStorage("/test.db.properties"));

        final Gson gson = new Gson();
        controller = new BalanceController(accountDAO, gson);

        testAccounts =
                Arrays.stream(new String[] {"first", "second", "third", "fourth"}).map(name -> accountDAO.create(name))
                        .map(account -> accountDAO.deposit(account.getAccountNumber(), BigDecimal.TEN))
                        .collect(Collectors.toList());
    }

    private final Gson gson = new Gson();

    @Test
    public void deposit() {
        final Request req = mock(Request.class);
        final Response res = mock(Response.class);

        final BalanceController.TransferDTO transferDTO = new BalanceController.TransferDTO();

        transferDTO.toAcct = testAccounts.get(2).getAccountNumber();
        transferDTO.amount = BigDecimal.TEN;

        when(req.body()).thenReturn(gson.toJson(transferDTO));

        final StandardResponse<Account> deposit = controller.deposit(req, res);
        Account accountTo = deposit.data;

        assertThat(accountTo.getBalance().compareTo(BigDecimal.valueOf(20)), equalTo(0));

        accountTo = accountDAO.findByAccountNumber(transferDTO.toAcct);

        assertThat(accountTo.getBalance().compareTo(BigDecimal.valueOf(20)), equalTo(0));
    }

    @Test
    public void withdraw() {
        final Request req = mock(Request.class);
        final Response res = mock(Response.class);

        final BalanceController.TransferDTO transferDTO = new BalanceController.TransferDTO();

        transferDTO.fromAcct = testAccounts.get(3).getAccountNumber();
        transferDTO.amount = BigDecimal.ONE;

        when(req.body()).thenReturn(gson.toJson(transferDTO));

        final StandardResponse<Account> deposit = controller.withdraw(req, res);
        Account accountFrom = deposit.data;

        assertThat(gson.toJson(accountFrom), accountFrom.getBalance().compareTo(BigDecimal.valueOf(9)), equalTo(0));

        accountFrom = accountDAO.findByAccountNumber(transferDTO.fromAcct);

        assertThat(gson.toJson(accountFrom), accountFrom.getBalance().compareTo(BigDecimal.valueOf(9)), equalTo(0));
    }

    @Test
    public void transferBalance() {
        final Request req = mock(Request.class);
        final Response res = mock(Response.class);

        final BalanceController.TransferDTO transferDTO = new BalanceController.TransferDTO();

        transferDTO.fromAcct = testAccounts.get(0).getAccountNumber();
        transferDTO.toAcct = testAccounts.get(1).getAccountNumber();
        transferDTO.amount = BigDecimal.TEN;

        when(req.body()).thenReturn(gson.toJson(transferDTO));

        final StandardResponse response = controller.transferBalance(req, res);

        assertThat(response.status, equalTo(200));

        final Account accountFrom = accountDAO.findByAccountNumber(testAccounts.get(0).getAccountNumber());
        final Account accountTo = accountDAO.findByAccountNumber(testAccounts.get(1).getAccountNumber());

        assertThat(gson.toJson(accountFrom), BigDecimal.ZERO.compareTo(accountFrom.getBalance()), equalTo(0));
        assertThat(gson.toJson(accountFrom), BigDecimal.valueOf(20D).compareTo(accountTo.getBalance()), equalTo(0));
    }
}