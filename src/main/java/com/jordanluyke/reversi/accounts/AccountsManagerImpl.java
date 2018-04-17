package com.jordanluyke.reversi.accounts;

import com.google.inject.Inject;
import com.jordanluyke.reversi.accounts.model.Account;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class AccountsManagerImpl implements AccountsManager {

    private AccountsDAO accountsDAO;

    @Inject
    public AccountsManagerImpl(AccountsDAO accountsDAO) {
        this.accountsDAO = accountsDAO;
    }

    @Override
    public Observable<Account> getAccounts() {
        return accountsDAO.getAccounts();
    }
}
