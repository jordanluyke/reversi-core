package com.jordanluyke.reversi.account;

import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.PlayerStats;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface AccountManager {

    Observable<Account> getAccounts();

    Observable<Account> createAccount(SessionCreationRequest req);

    Observable<Account> getAccountById(String id);

    Observable<Account> getAccountByEmail(String email);

    Observable<PlayerStats> getPlayerStats(String ownerId);

    Observable<PlayerStats> updatePlayerStats(PlayerStats playerStats);
}
