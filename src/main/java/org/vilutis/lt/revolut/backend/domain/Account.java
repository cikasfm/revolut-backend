package org.vilutis.lt.revolut.backend.domain;

import spark.utils.Assert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents a very simplified version of a bank account
 */
public class Account implements Serializable {

    private static final int SCALE = 2;

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);

    /**
     * Account number. Must be unique
     */
    private final Long accountNumber;

    /**
     * Account name
     */
    private String accountName;

    /**
     * Account balance in local currency.
     */
    private BigDecimal balance = ZERO;

    /**
     * Creates an
     * @param accountNumber the {@link #accountNumber} to set
     * @throws IllegalArgumentException if the param accountNumber is null
     */
    private Account(Long accountNumber) {
        Assert.notNull(accountNumber, "Param accountNumber cannot be null");
        this.accountNumber = accountNumber;
    }

    public Account(Long accountNumber, String accountName) {
        this(accountNumber);
        this.accountName = accountName;
    }

    public static Account from(long accountNumber, String accountName, double balance) {
        return new Account(accountNumber, accountName).withBalance(balance);
    }

    private Account withBalance(double balance) {
        this.setBalance(balance);
        return this;
    }

    /**
     * @return {@link #accountNumber}
     */
    public Long getAccountNumber() {
        return accountNumber;
    }

    /**
     * @return {@link #accountName}
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Set {@link #accountName}
     * @param accountName the {@link #accountName} to set
     * @throws IllegalArgumentException if the param accountName is empty
     */
    public void setAccountName(String accountName) {
        Assert.hasLength(accountName, "Account name cannot be empty");
        this.accountName = accountName;
    }

    /**
     * @return the Account {@link #balance}
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Set the Account {@link #balance}. Rounds "HALF_UP" if scale more than {@link #SCALE} decimal digits
     *
     * @param balance the {@link #balance} to set
     * @throws IllegalArgumentException if the param balance is <code>null</code>
     */
    public void setBalance(BigDecimal balance) {
        Assert.notNull(balance, "Balance cannot be null!");
        this.balance = balance.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Set the Account {@link #balance}. Rounds "HALF_UP" if scale more than {@link #SCALE} decimal digits
     *
     * @param balance the {@link #balance} to set
     * @throws IllegalArgumentException if the param balance is <code>null</code>
     */
    public void setBalance(Double balance) {
        Assert.notNull(balance, "Balance cannot be null!");
        this.balance = new BigDecimal(balance).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Generated toString() method
     * @return a String representation of the {@link Account} instance
     */
    @Override
    public String toString() {
        return "Account{" + "accountNumber=" + accountNumber + ", accountName='" + accountName + '\'' + ", balance="
                + balance + '}';
    }
}
