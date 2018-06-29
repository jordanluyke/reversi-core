package com.jordanluyke.reversi.account;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.dto.AccountCreationRequest;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.util.DateUtil;
import com.jordanluyke.reversi.util.RandomUtil;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.exception.DataAccessException;
import rx.Observable;

import java.util.Date;

import static org.jooq.sources.tables.Account.ACCOUNT;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Singleton
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
            Date createdAt = new Date();
            return Observable.just(dbManager.getDsl().insertInto(ACCOUNT, ACCOUNT.ID, ACCOUNT.CREATEDAT, ACCOUNT.EMAIL)
                    .values(id, DateUtil.getTimestamp(createdAt), req.getEmail())
                    .execute())
                    .map(Void -> Account.builder()
                            .id(id)
                            .createdAt(createdAt)
                            .email(req.getEmail())
                            .build());
        } catch(DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Observable<Account> getAccountById(String accountId) {
        return Observable.just(dbManager.getDsl().selectFrom(ACCOUNT)
                .where(ACCOUNT.ID.eq(accountId))
                .fetchOne())
                .map(Account::fromRecord);
    }

    public Observable<Account> getAccountByEmail(String email) {
        return Observable.just(dbManager.getDsl().selectFrom(ACCOUNT)
                .where(ACCOUNT.EMAIL.eq(email))
                .fetchOne())
                .map(Account::fromRecord);
    }
}
