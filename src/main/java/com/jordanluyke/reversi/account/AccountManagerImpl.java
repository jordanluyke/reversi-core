package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.PlayerStats;
import com.jordanluyke.reversi.web.model.WebException;
import io.netty.handler.codec.http.HttpResponseStatus;
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

    @Override
    public Observable<Account> getAccounts() {
        return accountDAO.getAccounts();
    }

    @Override
    public Observable<Account> createAccount(SessionCreationRequest req) {
        return accountDAO.createAccount(req)
                .flatMap(account -> accountDAO.createPlayerStats(account.getId())
                        .map(stats -> account));
    }

    @Override
    public Observable<Account> getAccountById(String id) {
        return accountDAO.getAccountById(id);
    }

    @Override
    public Observable<Account> getAccountBySessionRequest(SessionCreationRequest sessionCreationRequest) {
        return Observable.defer(() -> {
            if(sessionCreationRequest.getFacebookUserId().isPresent())
                return accountDAO.getAccountByFacebookUserId(sessionCreationRequest.getFacebookUserId().get());
            if(sessionCreationRequest.getGoogleUserId().isPresent())
                return accountDAO.getAccountByFacebookUserId(sessionCreationRequest.getGoogleUserId().get());
            return Observable.empty();
        })
                .defaultIfEmpty(null)
                .flatMap(account -> {
                    if(account == null)
                        return createAccount(sessionCreationRequest);
                    return Observable.just(account);
                });
    }

    @Override
    public Observable<PlayerStats> getPlayerStats(String ownerId) {
        return accountDAO.getPlayerStatsById(ownerId);
    }

    @Override
    public Observable<PlayerStats> updatePlayerStats(PlayerStats playerStats) {
        return accountDAO.updatePlayerStats(playerStats);
    }
}
