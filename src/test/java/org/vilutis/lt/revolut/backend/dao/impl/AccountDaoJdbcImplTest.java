package org.vilutis.lt.revolut.backend.dao.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.vilutis.lt.revolut.backend.dao.AccountDAO;
import org.vilutis.lt.revolut.backend.domain.Account;
import org.vilutis.lt.revolut.backend.storage.DBStorage;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AccountDaoJdbcImplTest {

    private static AccountDAO accountDAO;

    @BeforeClass
    public static void setUp() {

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

        DBStorage dbStorage = new DBStorage("jdbc:h2:mem:test", true);

        accountDAO = new AccountDaoJdbcImpl(dbStorage);
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
}