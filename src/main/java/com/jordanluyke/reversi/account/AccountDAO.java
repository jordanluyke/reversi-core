package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.dto.AccountUpdateRequest;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.PlayerStats;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.util.RandomUtil;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.jooq.sources.tables.Account.ACCOUNT;
import static org.jooq.sources.tables.PlayerStats.PLAYER_STATS;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class AccountDAO {
    private static final Logger logger = LogManager.getLogger(AccountDAO.class);

    private DbManager dbManager;

    public Observable<Account> getAccounts() {
        return Observable.fromIterable(dbManager.getDsl().selectFrom(ACCOUNT).fetch())
                .map(Account::fromRecord);
    }

    public Single<Account> createAccount(SessionCreationRequest req) {
        String id = RandomUtil.generateId();
        return Single.just(dbManager.getDsl().insertInto(ACCOUNT, ACCOUNT.ID, ACCOUNT.NAME, ACCOUNT.FACEBOOKUSERID, ACCOUNT.GOOGLEUSERID, ACCOUNT.ISGUEST)
                .values(id, "Player", req.getFacebookUserId().orElse(null), req.getGoogleUserId().orElse(null), !req.getFacebookUserId().isPresent() && !req.getGoogleUserId().isPresent())
                .execute())
                .flatMap(Void -> getAccountById(id));
    }

    public Single<Account> updateAccount(String accountId, AccountUpdateRequest req) {
        return getAccountById(accountId)
                .map(account -> dbManager.getDsl().update(ACCOUNT)
                        .set(ACCOUNT.NAME, req.getName().orElse(account.getName().orElse("Player")))
                        .where(ACCOUNT.ID.eq(accountId))
                        .execute())
                .flatMap(Void -> getAccountById(accountId));
    }

    public Single<Account> getAccountById(String accountId) {
        return Single.just(dbManager.getDsl().selectFrom(ACCOUNT)
                .where(ACCOUNT.ID.eq(accountId))
                .fetchAny())
                .map(Account::fromRecord);
    }

    public Single<Account> getAccountByFacebookUserId(String id) {
        return Single.just(dbManager.getDsl().selectFrom(ACCOUNT)
                .where(ACCOUNT.FACEBOOKUSERID.eq(id))
                .fetchAny())
                .map(Account::fromRecord);
    }

    public Single<Account> getAccountByGoogleUserId(String id) {
        return Single.just(dbManager.getDsl().selectFrom(ACCOUNT)
                .where(ACCOUNT.GOOGLEUSERID.eq(id))
                .fetchAny())
                .map(Account::fromRecord);
    }

    public Single<PlayerStats> createPlayerStats(String ownerId) {
        return Single.just(dbManager.getDsl().insertInto(PLAYER_STATS, PLAYER_STATS.OWNERID)
                .values(ownerId)
                .execute())
                .flatMap(Void -> getPlayerStatsById(ownerId));
    }

    public Single<PlayerStats> getPlayerStatsById(String ownerId) {
        return Single.just(dbManager.getDsl().selectFrom(PLAYER_STATS)
                .where(PLAYER_STATS.OWNERID.eq(ownerId))
                .fetchAny())
                .map(PlayerStats::fromRecord);
    }

    public Single<PlayerStats> updatePlayerStats(PlayerStats playerStats) {
        return Single.just(dbManager.getDsl().update(PLAYER_STATS)
                .set(PLAYER_STATS.MATCHES, playerStats.getMatches())
                .where(PLAYER_STATS.OWNERID.eq(playerStats.getOwnerId()))
                .execute())
                .map(Void -> playerStats);
    }
}
