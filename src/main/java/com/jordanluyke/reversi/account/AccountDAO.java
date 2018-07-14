package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.dto.AccountCreationRequest;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.util.RandomUtil;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.exception.DataAccessException;
import rx.Observable;

import java.time.Instant;

import static org.jooq.sources.tables.Account.ACCOUNT;

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

    public Observable<Account> createAccount(AccountCreationRequest req) {
        try {
            String id = RandomUtil.generateId();
            return Observable.just(dbManager.getDsl().insertInto(ACCOUNT, ACCOUNT.ID, ACCOUNT.EMAIL)
                    .values(id, req.getEmail())
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
                .map(Account::fromRecord);
    }
}
