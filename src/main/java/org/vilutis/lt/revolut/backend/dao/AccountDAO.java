package org.vilutis.lt.revolut.backend.dao;

import org.vilutis.lt.revolut.backend.domain.Account;

import java.util.ArrayList;

public interface AccountDAO {

    /**
     * Finds ALL {@link Account} in the storage using pagination
     * @param pageNum - page number starting with "0"
     * @param pageSize - page size, e.g. 10, 20 & etc
     * @return
     */
    ArrayList<Account> findAll(int pageNum, int pageSize);

    /**
     * Finds {@link Account} in the storage by Account Number
     * @param accountNumber
     * @return an {@link Account} from storage or <code>null</code> if one not found
     */
    Account findByAccountNumber(Long accountNumber);

    /**
     * Saves an account to the DB. Will create a new Account Number for a brand new object, if not set
     *
     * @param accountName
     * @return new instance of an {@link Account} with accountNumber set from DB
     */
    Account create(String accountName);
}
