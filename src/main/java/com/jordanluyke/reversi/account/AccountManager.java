package com.jordanluyke.reversi.account;

import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.dto.AccountCreationRequest;
import com.jordanluyke.reversi.session.model.Session;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface AccountManager {

    Observable<Account> getAccounts();

    Observable<Session> createAccount(AccountCreationRequest account);

    Observable<Account> getAccountById(String id);

    Observable<Account> getAccountByEmail(String email);
}
