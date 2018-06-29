package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.dto.AccountCreationRequest;
import lombok.AllArgsConstructor;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class AccountManagerImpl implements AccountManager {

    private AccountDAO accountDAO;

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

    @Override
    public Observable<Account> getAccountByEmail(String email) {
        return accountDAO.getAccountByEmail(email);
    }
}
