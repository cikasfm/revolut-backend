package org.vilutis.lt.revolut.backend.dao.impl;

import org.vilutis.lt.revolut.backend.storage.DBStorage;
import org.vilutis.lt.revolut.backend.storage.ExceptionHelper;
import org.vilutis.lt.revolut.backend.dao.AccountDao;
import org.vilutis.lt.revolut.backend.domain.Account;
import spark.utils.Assert;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class AccountDaoJdbcImpl implements AccountDao {

    private final DBStorage dbStorage;

    public AccountDaoJdbcImpl(DBStorage dbStorage) {
        this.dbStorage = dbStorage;
    }

    /**
     * {@inheritDoc}
     * @throws RuntimeException in case of DB/SQL error
     * @throws IllegalArgumentException in case pageNum or pageSize params are invalid ( negative, or zero page size )
     */
    @Override
    public ArrayList<Account> findAll(int pageNum, int pageSize) {
        Assert.isTrue(pageNum >= 0,  "pageNum must be positive");
        Assert.isTrue(pageSize > 0,  "pageSize must be more than zero");
        try {
            return dbStorage.runSQL(connection -> findAll(pageNum, pageSize, connection));
        } catch (SQLException ex) {
            throw ExceptionHelper.convertException(ex);
        }
    }

    private ArrayList<Account> findAll(int pageNum, int pageSize, Connection connection) throws SQLException {
        ArrayList<Account> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                " SELECT accountNumber, accountName, balance "
                        + " FROM account "
                        + " LIMIT ? OFFSET ? ")){

            statement.setInt(1, pageSize);
            statement.setInt(2, pageNum * pageSize);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs != null && rs.next()) {
                    result.add(accountFromResultSet(rs));
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * @throws RuntimeException in case of DB/SQL error
     * @throws IllegalArgumentException in case accountNumber param is null
     */
    @Override
    public Account findByAccountNumber(Long accountNumber) {
        Assert.notNull(accountNumber, "accountNumber must be not null");
        try {
            return dbStorage.runSQL(connection -> findAccountByNumber(accountNumber, connection));
        } catch (SQLException ex) {
            throw ExceptionHelper.convertException(ex);
        }
    }

    private Account findAccountByNumber(Long accountNumber, Connection connection) throws SQLException {
        try (PreparedStatement fetchAccountStmt = connection.prepareStatement(
                " SELECT "
                        + " accountNumber, accountName, balance "
                        + " FROM account "
                        + " WHERE accountNumber = ?")){

            fetchAccountStmt.setLong(1, accountNumber);

            try (ResultSet rs = fetchAccountStmt.executeQuery()) {
                return (rs != null && rs.next()) ? accountFromResultSet(rs) : null;
            }
        }
    }

    private Account accountFromResultSet(ResultSet resultSet) throws SQLException {
        long dbAccountNumber = resultSet.getLong("accountNumber");
        String dbAccountName = resultSet.getString("accountName");
        double dbBalance = resultSet.getDouble("balance");

        return Account.from(dbAccountNumber, dbAccountName, dbBalance);
    }

    /**
     * {@inheritDoc}
     * @throws RuntimeException in case of DB/SQL error
     * @throws IllegalArgumentException in case account name is empty
     */
    @Override
    public Account create(String accountName) {
        Assert.hasLength(accountName, "account name must not be empty");
        try {
            return dbStorage.runInTransaction(connection -> create(accountName, connection));
        } catch (SQLException ex) {
            throw ExceptionHelper.convertException(ex);
        }
    }

    private Account create(String accountName, Connection connection) throws SQLException {
        try (PreparedStatement insertAccountStmt = connection.prepareStatement(
                " INSERT INTO account ( "
                        + " accountNumber, accountName ) "
                        + " VALUES ( NULL, ? ) ",
                Statement.RETURN_GENERATED_KEYS)) {

            insertAccountStmt.setString(1, accountName);

            if (insertAccountStmt.executeUpdate() == 0) {
                throw new SQLException("Creating account failed, no rows affected.");
            }

            try (ResultSet rs = insertAccountStmt.getGeneratedKeys()) {
                if (rs != null && rs.next()) {
                    Long accountNumber = rs.getLong(1);
                    return new Account(accountNumber, accountName);
                } else {
                    throw new RuntimeException("failed to insert???");
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * @throws RuntimeException in case of DB/SQL error
     * @throws IllegalArgumentException in case account obj is null
     */
    @Override
    public Account update(Account account) {
        Assert.notNull(account, "account must not be null");
        try {
            return dbStorage.runInTransaction(connection -> update(account, connection));
        } catch (SQLException ex) {
            throw ExceptionHelper.convertException(ex);
        }
    }

    private Account update(Account account, Connection connection) throws SQLException {
        try (PreparedStatement insertAccountStmt = connection.prepareStatement(
                " UPDATE account "
                        + " SET accountName = ? , balance = ? "
                        + " WHERE accountNumber = ? ")) {

            insertAccountStmt.setString(1, account.getAccountName());
            insertAccountStmt.setBigDecimal(2, account.getBalance());
            insertAccountStmt.setLong(3, account.getAccountNumber());

            if (insertAccountStmt.executeUpdate() == 0) {
                throw new SQLException("Update account failed, no rows affected.");
            }

            return account;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Note</b>: if the amount.scale is greater than {@link Account#SCALE}, it will be rounded to
     * {@link Account#SCALE} using {@link java.math.RoundingMode#HALF_UP}</p>
     *
     * @throws RuntimeException in case of DB/SQL error
     * @throws IllegalArgumentException in fromAcctNum or toAcctNum is null; or amount has a negative value, or scale
     * greater than {@link Account#SCALE}
     */
    @Override
    public void transferBalance(Long fromAcctNum, Long toAcctNum, BigDecimal amount) {
        Assert.notNull(fromAcctNum, "fromAcctNum must be set");
        Assert.notNull(toAcctNum, "toAcctNum must be set");
        Assert.notNull(amount, "amount must be set");
        Assert.isTrue(!fromAcctNum.equals(toAcctNum), "FROM and TO accounts cannot be the same");
        Assert.isTrue(amount.scale() <= Account.SCALE,
                "amount scale must NOT greater than " + Account.SCALE);
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "amount must be positive");
        try {
            dbStorage.runInTransaction( connection -> {
                // 1. find "from" account ( if exists & check if balance is enough )
                // 2. find "to" account
                // 3. update balance in "to" account
                // 4. update balance in "from" account
                // 5. TODO : write audit log

                final Account fromAcct = findByAccountNumber(fromAcctNum);
                Assert.notNull(fromAcct, "'from' Account not found!");
                Assert.isTrue(fromAcct.getBalance().compareTo(amount) >= 0,
                        "From account balance is not enough for transfer");

                final Account toAcct = findByAccountNumber(toAcctNum);
                Assert.notNull(toAcct, "'to' Account not found");

                withdrawAmount(fromAcctNum, amount, connection);

                depositAmount(toAcctNum, amount, connection);

                return true;
            });
        } catch (SQLException ex) {
            throw ExceptionHelper.convertException(ex);
        }
    }

    @Override
    public Account deposit(Long accountNumber, BigDecimal amount) {
        Assert.notNull(accountNumber, "accountNumber must be set");
        Assert.notNull(amount, "amount must be set");
        Assert.isTrue(amount.scale() <= Account.SCALE,
                "amount scale must NOT greater than " + Account.SCALE);
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "amount must be positive");
        try {
            return dbStorage.runInTransaction( connection -> {
                final Account account = findAccountByNumber(accountNumber, connection);
                Assert.notNull(account, "'from' Account not found!");

                depositAmount(accountNumber, amount, connection);

                account.setBalance(account.getBalance().add(amount));

                return account;
            });
        } catch (SQLException ex){
            throw ExceptionHelper.convertException(ex);
        }
    }

    private void depositAmount(Long accountNumber, BigDecimal amount, Connection connection) throws SQLException {
        try ( PreparedStatement updateStatement = connection.prepareStatement(
                " UPDATE account "
                        + " SET balance = balance + ? "
                        + " WHERE accountNumber = ? "
        ) ) {
            updateStatement.setBigDecimal(1, amount);
            updateStatement.setLong(2, accountNumber);
            if ( updateStatement.executeUpdate() != 1 ) {
                throw new SQLException("Update 'to' account failed", updateStatement.getWarnings());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account withdraw(Long accountNumber, BigDecimal amount) {
        Assert.notNull(accountNumber, "accountNumber must be set");
        Assert.notNull(amount, "amount must be set");
        Assert.isTrue(amount.scale() <= Account.SCALE,
                "amount scale must NOT greater than " + Account.SCALE);
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "amount must be positive");
        try {
            return dbStorage.runInTransaction( connection -> {
                final Account account = findAccountByNumber(accountNumber, connection);
                Assert.notNull(account, "'from' Account not found!");

                Assert.isTrue(account.getBalance().compareTo(amount) >= 0,
                        "account balance is not enough for withdrawal");

                withdrawAmount(accountNumber, amount, connection);

                account.setBalance(account.getBalance().subtract(amount));

                return account;
            });
        } catch (SQLException ex){
            throw ExceptionHelper.convertException(ex);
        }
    }

    public void withdrawAmount(Long accountNumber, BigDecimal amount, Connection connection) throws SQLException {
        try ( PreparedStatement updateStatement = connection.prepareStatement(
                " UPDATE account "
                        + " SET balance = balance - ? "
                        + " WHERE accountNumber = ? "
        ) ) {
            updateStatement.setBigDecimal(1, amount);
            updateStatement.setLong(2, accountNumber);
            if ( updateStatement.executeUpdate() != 1 ) {
                throw new SQLException("Update 'from' account failed", updateStatement.getWarnings());
            }
        }
    }

}
