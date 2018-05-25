package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.AccountCreationRequest;
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

    @Override
    public Observable<Account> getAccountById(String id) {
        return accountsDAO.getAccountById(id);
    }
}
