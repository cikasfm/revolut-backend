package org.vilutis.lt.revolut.backend.dao.impl;

import org.vilutis.lt.revolut.backend.storage.DBStorage;
import org.vilutis.lt.revolut.backend.storage.ExceptionHelper;
import org.vilutis.lt.revolut.backend.dao.AccountDAO;
import org.vilutis.lt.revolut.backend.domain.Account;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDaoJdbcImpl implements AccountDAO {

    private final DBStorage dbStorage;

    public AccountDaoJdbcImpl(DBStorage dbStorage) {
        this.dbStorage = dbStorage;
    }

    @Override
    public Account findByAccountNumber(Long accountNumber) {
        try {
            return dbStorage.runSQL(connection -> {
                PreparedStatement fetchAccountStmt = connection.prepareStatement(
                        " select "
                                + " accountNumber, accountName, balance "
                                + " from account "
                                + " where accountNumber = ?");
                fetchAccountStmt.setLong(1, accountNumber);

                try (ResultSet rs = fetchAccountStmt.getResultSet()) {
                    return (rs != null && rs.next()) ? accountFromResultSet(rs) : null;
                }
            });
        } catch (SQLException ex) {
            throw ExceptionHelper.convertException(ex);
        }
    }

    private Account accountFromResultSet(ResultSet resultSet) throws SQLException {
        long dbAccountNumber = resultSet.getLong(0);
        String dbAccountName = resultSet.getString(1);
        double dbBalance = resultSet.getDouble(2);

        return Account.from(dbAccountNumber, dbAccountName, dbBalance);
    }
}
