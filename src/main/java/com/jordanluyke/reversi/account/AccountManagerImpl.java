package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.model.AggregateAccount;
import com.jordanluyke.reversi.session.dto.AccountUpdateRequest;
import com.jordanluyke.reversi.session.dto.ProfileResponse;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.PlayerStats;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.api.model.SocketChannel;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class AccountManagerImpl implements AccountManager {
    private static final Logger logger = LogManager.getLogger(AccountManagerImpl.class);

    private AccountDAO accountDAO;
    private SocketManager socketManager;

    @Override
    public Observable<AggregateAccount> getAccounts() {
        return accountDAO.getAccounts()
                .flatMap(account -> getAggregateAccount(account).toObservable());
    }

    @Override
    public Single<AggregateAccount> createAccount(SessionCreationRequest req) {
        return accountDAO.createAccount(req)
                .flatMap(account -> accountDAO.createPlayerStats(account.getId())
                        .map(stats -> new AggregateAccount(account, stats)));
    }

    @Override
    public Single<AggregateAccount> updateAccount(String accountId, AccountUpdateRequest req) {
        return accountDAO.updateAccount(accountId, req)
                .flatMap(this::getAggregateAccount)
                .doOnSuccess(Void -> socketManager.send(SocketChannel.Account, accountId));
    }

    @Override
    public Single<AggregateAccount> getAccountById(String accountId) {
        return accountDAO.getAccountById(accountId)
                .flatMap(this::getAggregateAccount);
    }

    @Override
    public Single<AggregateAccount> getAccountBySessionRequest(SessionCreationRequest sessionCreationRequest) {
        return Maybe.defer(() -> {
            if(sessionCreationRequest.getFacebookUserId().isPresent())
                return accountDAO.getAccountByFacebookUserId(sessionCreationRequest.getFacebookUserId().get()).toMaybe();
            if(sessionCreationRequest.getGoogleUserId().isPresent())
                return accountDAO.getAccountByGoogleUserId(sessionCreationRequest.getGoogleUserId().get()).toMaybe();
            return Maybe.empty();
        })
                .toSingle()
                .flatMap(this::getAggregateAccount)
                .onErrorResumeNext(e -> createAccount(sessionCreationRequest));
    }

    @Override
    public Single<ProfileResponse> getProfile(String accountId) {
        return getAccountById(accountId)
                .map(aggregateAccount -> ProfileResponse.builder()
                        .name(aggregateAccount.getAccount().getName().orElse(null))
                        .stats(aggregateAccount.getStats())
                        .build());
    }

    @Override
    public Single<PlayerStats> getPlayerStats(String ownerId) {
        return accountDAO.getPlayerStatsById(ownerId);
    }

    @Override
    public Single<PlayerStats> updatePlayerStats(PlayerStats playerStats) {
        return accountDAO.updatePlayerStats(playerStats)
                .doOnSuccess(Void -> socketManager.send(SocketChannel.Account, playerStats.getOwnerId()));
    }

    private Single<AggregateAccount> getAggregateAccount(Account account) {
        return getPlayerStats(account.getId())
                .map(stats -> new AggregateAccount(account, stats));
    }
}
