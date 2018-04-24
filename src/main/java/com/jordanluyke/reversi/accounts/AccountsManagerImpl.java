package com.jordanluyke.reversi.accounts;

import com.google.inject.Inject;
import com.jordanluyke.reversi.accounts.model.Account;
import com.jordanluyke.reversi.accounts.model.AccountCreationRequest;
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

    @Override
    public Observable<Account> createAccount(AccountCreationRequest account) {
        return accountsDAO.createAccount(account);
    }
}
