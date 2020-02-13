package org.vilutis.lt.revolut.backend.dao.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.vilutis.lt.revolut.backend.dao.AccountDao;
import org.vilutis.lt.revolut.backend.domain.Account;
import org.vilutis.lt.revolut.backend.storage.DBStorage;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AccountDaoJdbcImplTest {

    private static AccountDao accountDAO;

    @BeforeClass
    public static void setUp() {
        accountDAO = new AccountDaoJdbcImpl(new DBStorage("/test.db.properties"));
    }

    @Test
    public void create() {
        final Account account = accountDAO.create("create");

        assertThat("accountNumber must be not null", account.getAccountNumber(), notNullValue());
        assertThat("accountNumber must be non-negative number", account.getAccountNumber() >= 0);

        assertThat("accountName must be set", account.getAccountName(), equalTo("create"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_empty() {
        accountDAO.create("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_null() {
        accountDAO.create(null);
    }

    @Test
    public void findByAccountNumber() {
        final Account account = accountDAO.create("findByAccountNumber");

        final Account fromDB = accountDAO.findByAccountNumber(account.getAccountNumber());

        assertThat("accountNumber must be equal", fromDB.getAccountNumber(), equalTo(account.getAccountNumber()));
        assertThat("accountName must be equal", fromDB.getAccountName(), equalTo(account.getAccountName()));
        assertThat("balance must be equal", fromDB.getBalance(), equalTo(account.getBalance()));
    }

    @Test
    public void findByAccountNumber_notFound() {
        assertThat("result must be null", accountDAO.findByAccountNumber(Long.MIN_VALUE), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findByAccountNumber_nullValue() {
        accountDAO.findByAccountNumber(null);
    }

    @Test
    public void findAll_paging() {
        accountDAO.create("findAll");
        final ArrayList<Account> result = accountDAO.findAll(0, 1);

        assertThat("there must be a single result", result.size(), equalTo(1));
    }

    @Test
    public void transferBalance() {
        Account fromAcct = accountDAO.create("from");
        fromAcct.setBalance(100D);
        fromAcct = accountDAO.update(fromAcct);

        Account toAcct = accountDAO.create("to");

        accountDAO.transferBalance(fromAcct.getAccountNumber(), toAcct.getAccountNumber(), BigDecimal.TEN.setScale(2));

        fromAcct = accountDAO.findByAccountNumber(fromAcct.getAccountNumber());
        toAcct = accountDAO.findByAccountNumber(toAcct.getAccountNumber());

        assertThat("FROM balance must be updated in DB", fromAcct.getBalance(), equalTo(BigDecimal.valueOf(9000L, 2)));
        assertThat("TO balance must be updated in DB", toAcct.getBalance(), equalTo(BigDecimal.valueOf(1000L, 2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void transferBalance_fakeAcct() {
        accountDAO.transferBalance(98765432L, 98765433L, BigDecimal.TEN.setScale(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void transferBalance_zeroAmount() {
        Account fromAcct = accountDAO.create("from_zeroAmount");
        Account toAcct = accountDAO.create("to_zeroAmount");

        accountDAO.transferBalance(fromAcct.getAccountNumber(), toAcct.getAccountNumber(), BigDecimal.ZERO.setScale(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void transferBalance_negativeAmount() {
        Account fromAcct = accountDAO.create("from_negativeAmount");
        Account toAcct = accountDAO.create("to_negativeAmount");

        accountDAO.transferBalance(fromAcct.getAccountNumber(), toAcct.getAccountNumber(),
                BigDecimal.TEN.negate().setScale(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void transferBalance_notEnoughFunds() {
        Account fromAcct = accountDAO.create("from2");
        Account toAcct = accountDAO.create("to2");

        accountDAO.transferBalance(fromAcct.getAccountNumber(), toAcct.getAccountNumber(), BigDecimal.TEN.setScale(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void transferBalance_amountScaleInvalid() {
        Account fromAcct = accountDAO.create("from3");
        fromAcct.setBalance(100D);
        fromAcct = accountDAO.update(fromAcct);

        Account toAcct = accountDAO.create("to3");

        accountDAO.transferBalance(fromAcct.getAccountNumber(), toAcct.getAccountNumber(), BigDecimal.TEN.setScale(3));
    }
}