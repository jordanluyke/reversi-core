package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.dto.AccountCreationRequest;
import com.jordanluyke.reversi.account.model.PlayerStats;
import com.jordanluyke.reversi.session.SessionManager;
import com.jordanluyke.reversi.session.model.Session;
import lombok.AllArgsConstructor;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class AccountManagerImpl implements AccountManager {

    private AccountDAO accountDAO;
    private SessionManager sessionManager;

    @Override
    public Observable<Account> getAccounts() {
        return accountDAO.getAccounts();
    }

    @Override
    public Observable<Session> createAccount(AccountCreationRequest req) {
        return accountDAO.createAccount(req)
                .flatMap(account -> Observable.zip(
                        sessionManager.createSession(account.getId()),
                        accountDAO.createPlayerStats(account.getId()),
                        (session, stats) -> session));
    }

    @Override
    public Observable<Account> getAccountById(String id) {
        return accountDAO.getAccountById(id);
    }

    @Override
    public Observable<Account> getAccountByEmail(String email) {
        return accountDAO.getAccountByEmail(email);
    }

    @Override
    public Observable<PlayerStats> getPlayerStats(String ownerId) {
        return accountDAO.createPlayerStats(ownerId);
    }

    @Override
    public Observable<PlayerStats> updatePlayerStats(PlayerStats playerStats) {
        return accountDAO.updatePlayerStats(playerStats);
    }
}
