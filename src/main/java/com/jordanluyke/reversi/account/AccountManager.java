package com.jordanluyke.reversi.account;

import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.dto.AccountCreationRequest;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface AccountManager {

    Observable<Account> getAccounts();

    Observable<Account> createAccount(AccountCreationRequest account);

    Observable<Account> getAccountById(String id);

    Observable<Account> getAccountByEmail(String email);
}
