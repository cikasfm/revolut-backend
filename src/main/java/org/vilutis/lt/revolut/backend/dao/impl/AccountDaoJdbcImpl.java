package org.vilutis.lt.revolut.backend.dao.impl;

import org.vilutis.lt.revolut.backend.storage.DBStorage;
import org.vilutis.lt.revolut.backend.storage.ExceptionHelper;
import org.vilutis.lt.revolut.backend.dao.AccountDAO;
import org.vilutis.lt.revolut.backend.domain.Account;
import spark.utils.Assert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class AccountDaoJdbcImpl implements AccountDAO {

    private final DBStorage dbStorage;

    public AccountDaoJdbcImpl(DBStorage dbStorage) {
        this.dbStorage = dbStorage;
    }

    @Override
    public ArrayList<Account> findAll(int pageNum, int pageSize) {
        try {
            return dbStorage.runSQL(connection -> findAll(pageNum, pageSize, connection));
        } catch (SQLException ex) {
            throw ExceptionHelper.convertException(ex);
        }
    }

    private ArrayList<Account> findAll(int pageNum, int pageSize, Connection connection) throws SQLException {
        Assert.isTrue(pageNum >= 0,  "pageNum must be positive");
        Assert.isTrue(pageSize > 0,  "pageSize must be more than zero");
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
}
