package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.AccountCreationRequest;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class AccountManagerImpl implements AccountManager {

    private AccountDAO accountDAO;

    @Inject
    public AccountManagerImpl(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Override
    public Observable<Account> getAccounts() {
        return accountDAO.getAccounts();
    }

    @Override
    public Observable<Account> createAccount(AccountCreationRequest account) {
        return accountDAO.createAccount(account);
    }

    @Override
    public Observable<Account> getAccountById(String id) {
        return accountDAO.getAccountById(id);
    }
}
