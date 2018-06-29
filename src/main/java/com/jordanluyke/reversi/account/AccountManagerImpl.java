package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.dto.AccountCreationRequest;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.session.model.Session;
import lombok.AllArgsConstructor;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class AccountManagerImpl implements AccountManager {

    private AccountDAO accountDAO;
    private SessionManager sessionManager;

    @Override
    public Observable<Account> getAccounts() {
        return accountDAO.getAccounts();
    }

    @Override
    public Observable<Session> createAccount(AccountCreationRequest account) {
        return accountDAO.createAccount(account)
                .flatMap(account1 -> sessionManager.createSession(account1.getId()));
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
