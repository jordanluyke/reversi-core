package com.jordanluyke.reversi.account;

import com.jordanluyke.reversi.account.model.AggregateAccount;
import com.jordanluyke.reversi.session.dto.AccountUpdateRequest;
import com.jordanluyke.reversi.session.dto.ProfileResponse;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.account.model.PlayerStats;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface AccountManager {

    Observable<AggregateAccount> getAccounts();

    Single<AggregateAccount> createAccount(SessionCreationRequest req);

    Single<AggregateAccount> updateAccount(String accountId, AccountUpdateRequest req);

    Single<AggregateAccount> getAccountById(String accountId);

    Single<AggregateAccount> getAccountBySessionRequest(SessionCreationRequest sessionCreationRequest);

    Single<ProfileResponse> getProfile(String accountId);

    Single<PlayerStats> getPlayerStats(String ownerId);

    Single<PlayerStats> updatePlayerStats(PlayerStats playerStats);
}
