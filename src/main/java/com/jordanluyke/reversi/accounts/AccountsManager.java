package com.jordanluyke.reversi.accounts;

import com.jordanluyke.reversi.accounts.model.Account;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface AccountsManager {

    Observable<Account> getAccounts();
}
