package com.jordanluyke.reversi.account;

import com.jordanluyke.reversi.account.model.AggregateAccount;
import com.jordanluyke.reversi.session.dto.AccountUpdateRequest;
import com.jordanluyke.reversi.session.dto.AccountProfileResponse;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.account.model.PlayerStats;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface AccountManager {

    Observable<AggregateAccount> getAccounts();

    Observable<AggregateAccount> createAccount(SessionCreationRequest req);

    Observable<AggregateAccount> updateAccount(String accountId, AccountUpdateRequest req);

    Observable<AggregateAccount> getAccountById(String accountId);

    Observable<AggregateAccount> getAccountBySessionRequest(SessionCreationRequest sessionCreationRequest);

    Observable<AccountProfileResponse> getProfile(String accountId);

    Observable<PlayerStats> getPlayerStats(String ownerId);

    Observable<PlayerStats> updatePlayerStats(PlayerStats playerStats);
}
