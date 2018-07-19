package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.PlayerStats;
import lombok.AllArgsConstructor;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class AccountManagerImpl implements AccountManager {

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
