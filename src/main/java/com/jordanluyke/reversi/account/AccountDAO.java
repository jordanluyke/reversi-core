package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.session.dto.SessionCreationRequest;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.PlayerStats;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.util.RandomUtil;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.exception.DataAccessException;
import rx.Observable;

import java.util.Optional;

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
        return Observable.from(dbManager.getDsl().selectFrom(ACCOUNT).fetch())
                .map(Account::fromRecord);
    }

    public Observable<Account> createAccount(SessionCreationRequest req) {
        try {
            String id = RandomUtil.generateId();
            return Observable.just(dbManager.getDsl().insertInto(ACCOUNT, ACCOUNT.ID, ACCOUNT.EMAIL, ACCOUNT.NAME, ACCOUNT.GUEST)
                    .values(id, req.getEmail().orElse(null), req.getName().orElse(null), !req.getEmail().isPresent())
                    .execute())
                    .flatMap(Void -> getAccountById(id));
        } catch(DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Observable<Account> getAccountById(String accountId) {
        return Observable.just(dbManager.getDsl().selectFrom(ACCOUNT)
                .where(ACCOUNT.ID.eq(accountId))
                .fetchAny())
                .map(Account::fromRecord);
    }

    public Observable<Account> getAccountByEmail(String email) {
        return Observable.just(dbManager.getDsl().selectFrom(ACCOUNT)
                .where(ACCOUNT.EMAIL.eq(email))
                .fetchAny())
                .flatMap(record -> {
                    if(record == null)
                        return Observable.empty();
                    return Observable.just(Account.fromRecord(record));
                });
    }

    public Observable<PlayerStats> createPlayerStats(String ownerId) {
        return Observable.just(dbManager.getDsl().insertInto(PLAYER_STATS, PLAYER_STATS.OWNERID)
                .values(ownerId)
                .execute())
                .flatMap(Void -> getPlayerStatsById(ownerId));
    }

    public Observable<PlayerStats> getPlayerStatsById(String ownerId) {
        return Observable.just(dbManager.getDsl().selectFrom(PLAYER_STATS)
                .where(PLAYER_STATS.OWNERID.eq(ownerId))
                .fetchAny())
                .map(PlayerStats::fromRecord);
    }

    public Observable<PlayerStats> updatePlayerStats(PlayerStats playerStats) {
        return Observable.just(dbManager.getDsl().update(PLAYER_STATS)
                .set(PLAYER_STATS.MATCHES, playerStats.getMatches())
                .where(PLAYER_STATS.OWNERID.eq(playerStats.getOwnerId()))
                .execute())
                .map(Void -> playerStats);
    }
}
