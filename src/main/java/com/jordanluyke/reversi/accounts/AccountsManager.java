package com.jordanluyke.reversi.accounts;

import com.jordanluyke.reversi.accounts.model.Account;
import com.jordanluyke.reversi.accounts.model.AccountCreationRequest;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface AccountsManager {

    Observable<Account> getAccounts();

    Observable<Account> createAccount(AccountCreationRequest account);
}
