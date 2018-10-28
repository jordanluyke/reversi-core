package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.model.AggregateAccount;
import com.jordanluyke.reversi.session.dto.AccountUpdateRequest;
import com.jordanluyke.reversi.session.dto.AccountProfileResponse;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.PlayerStats;
import com.jordanluyke.reversi.web.api.SocketManager;
import com.jordanluyke.reversi.web.api.events.OutgoingEvents;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

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
                .flatMap(this::getAggregateAccount);
    }

    @Override
    public Observable<AggregateAccount> createAccount(SessionCreationRequest req) {
        return accountDAO.createAccount(req)
                .flatMap(account -> accountDAO.createPlayerStats(account.getId())
                        .map(stats -> new AggregateAccount(account, stats)));
    }

    @Override
    public Observable<AggregateAccount> updateAccount(String accountId, AccountUpdateRequest req) {
        return accountDAO.updateAccount(accountId, req)
                .flatMap(this::getAggregateAccount)
                .doOnNext(Void -> socketManager.sendUpdateEvent(OutgoingEvents.Account, accountId));
    }

    @Override
    public Observable<AggregateAccount> getAccountById(String accountId) {
        return accountDAO.getAccountById(accountId)
                .flatMap(this::getAggregateAccount);
    }

    @Override
    public Observable<AggregateAccount> getAccountBySessionRequest(SessionCreationRequest sessionCreationRequest) {
        return Observable.defer(() -> {
            if(sessionCreationRequest.getFacebookUserId().isPresent())
                return accountDAO.getAccountByFacebookUserId(sessionCreationRequest.getFacebookUserId().get());
            if(sessionCreationRequest.getGoogleUserId().isPresent())
                return accountDAO.getAccountByGoogleUserId(sessionCreationRequest.getGoogleUserId().get());
            return Observable.empty();
        })
                .defaultIfEmpty(null)
                .flatMap(account -> {
                    if(account == null)
                        return createAccount(sessionCreationRequest);
                    return Observable.just(account);
                })
                .flatMap(this::getAggregateAccount);
    }

    @Override
    public Observable<AccountProfileResponse> getProfile(String accountId) {
        return getAccountById(accountId)
                .map(account -> AccountProfileResponse.builder()
                        .name(account.getName().orElse(null))
                        .stats(account.getStats())
                        .build());
    }

    @Override
    public Observable<PlayerStats> getPlayerStats(String ownerId) {
        return accountDAO.getPlayerStatsById(ownerId);
    }

    @Override
    public Observable<PlayerStats> updatePlayerStats(PlayerStats playerStats) {
        return accountDAO.updatePlayerStats(playerStats)
                .doOnNext(Void -> socketManager.sendUpdateEvent(OutgoingEvents.Account, playerStats.getOwnerId()));
    }

    private Observable<AggregateAccount> getAggregateAccount(Account account) {
        return getPlayerStats(account.getId())
                .map(stats -> new AggregateAccount(account, stats));
    }
}
