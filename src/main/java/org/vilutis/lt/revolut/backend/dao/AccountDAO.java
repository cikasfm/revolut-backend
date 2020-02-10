package org.vilutis.lt.revolut.backend.dao;

import org.vilutis.lt.revolut.backend.domain.Account;

public interface AccountDAO {
    /**
     * Finds {@link Account} in the storage by Account Number
     * @param accountNumber
     * @return an {@link Account} from storage or <code>null</code> if one not found
     */
    Account findByAccountNumber(Long accountNumber);
}
