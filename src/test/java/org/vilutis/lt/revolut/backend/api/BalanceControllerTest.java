package org.vilutis.lt.revolut.backend.api;

import com.google.gson.Gson;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

    @Test
    public void transferBalance_stress() {
        final Request req = mock(Request.class);
        final Response res = mock(Response.class);

        final BalanceController.TransferDTO transferDTO = new BalanceController.TransferDTO();

        Account stress_one = accountDAO.create("stress_one");
        Account stress_two = accountDAO.create("stress_two");

        accountDAO.deposit(stress_one.getAccountNumber(), BigDecimal.valueOf(1_000_000D));

        transferDTO.fromAcct = stress_one.getAccountNumber();
        transferDTO.toAcct = stress_two.getAccountNumber();

        transferDTO.amount = BigDecimal.TEN;

        when(req.body()).thenReturn(gson.toJson(transferDTO));

        final ExecutorService pool = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 10_000; i++) {
            final int iteration = i;
            // repeatedly transfer from the same account to the same acct to check for DB locks & etc
            pool.submit(() -> controller.transferBalance(req, res));
        }
        try {
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);

            stress_one = accountDAO.findByAccountNumber(stress_one.getAccountNumber());
            stress_two = accountDAO.findByAccountNumber(stress_two.getAccountNumber());
            assertThat(stress_one.getBalance().compareTo(BigDecimal.valueOf(1_000_000D - 100_000D)), equalTo(0));
            assertThat(stress_two.getBalance().compareTo(BigDecimal.valueOf(100_000D)), equalTo(0));
        } catch (InterruptedException e) {
            throw new Error(e.getMessage(), e);
        }
    }

    /**
     * Apparently two-way transfer ( a->b and b->a ) in parallel causes dead-locks between
     * transactions for a few seconds ( as defined in the driver properties ) and will fail.
     * Only 5% of all transactions are successful in this test
     */
    @Test
    @Ignore
    public void transferBalance_stressTwoWay() {
        final Request req1 = mock(Request.class);
        final Request req2 = mock(Request.class);
        final Response res = mock(Response.class);

        final BalanceController.TransferDTO transferDTO = new BalanceController.TransferDTO();

        Account stress_one = accountDAO.create("stress_one");
        Account stress_two = accountDAO.create("stress_two");

        accountDAO.deposit(stress_one.getAccountNumber(), BigDecimal.valueOf(1_000_000D));
        accountDAO.deposit(stress_two.getAccountNumber(), BigDecimal.valueOf(1_000_000D));

        transferDTO.fromAcct = stress_one.getAccountNumber();
        transferDTO.toAcct = stress_two.getAccountNumber();

        transferDTO.amount = BigDecimal.TEN;

        final String req1body = gson.toJson(transferDTO);
        when(req1.body()).thenReturn(req1body);

        // reverse
        transferDTO.fromAcct = stress_two.getAccountNumber();
        transferDTO.toAcct = stress_one.getAccountNumber();
        when(req2.body()).thenReturn(gson.toJson(transferDTO));

        final ExecutorService pool = Executors.newFixedThreadPool(100);
        final AtomicInteger successful = new AtomicInteger(0);
        final AtomicInteger total = new AtomicInteger(0);
        for (int i = 0; i < 5_000; i++) {
            final int iteration = i;
            // repeatedly transfer from the same account to the same acct to check for DB locks & etc
            pool.submit(() -> {
                final StandardResponse response = controller.transferBalance(req1, res);
                switch (response.status) {
                    case 200:
                        successful.incrementAndGet();
                    default:
                        total.incrementAndGet();
                }
            });
            // and send the money back at the same time
            pool.submit(() -> {
                final StandardResponse response = controller.transferBalance(req2, res);
                switch (response.status) {
                    case 200:
                        successful.incrementAndGet();
                    default:
                        total.incrementAndGet();
                }
            });
        }
        try {
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);

            double successRate = successful.doubleValue() / total.doubleValue();

            stress_one = accountDAO.findByAccountNumber(stress_one.getAccountNumber());
            stress_two = accountDAO.findByAccountNumber(stress_two.getAccountNumber());

            assertThat("success rate less than 95%, but is " + successRate * 100 + "%",
                    successRate > .90D, is(true));

            System.out.println(gson.toJson(new Account[]{ stress_one, stress_two }));
        } catch (InterruptedException e) {
            throw new Error(e.getMessage(), e);
        }
    }

}