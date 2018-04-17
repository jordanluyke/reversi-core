package com.jordanluyke.reversi.accounts;

import com.google.inject.Inject;
import com.jordanluyke.reversi.accounts.model.Account;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class AccountsDAO {

    @Inject
    public AccountsDAO() {
    }

    public Observable<Account> getAccounts() {
        return Observable.empty();
    }
}
