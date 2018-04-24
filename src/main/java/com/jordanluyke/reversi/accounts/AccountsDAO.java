package com.jordanluyke.reversi.accounts;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jordanluyke.reversi.accounts.model.Account;
import com.jordanluyke.reversi.accounts.model.AccountCreationRequest;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.util.RandomUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.exception.DataAccessException;
import rx.Observable;

import static org.jooq.sources.tables.Account.ACCOUNT;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Singleton
public class AccountsDAO {
    private static final Logger logger = LogManager.getLogger(AccountsDAO.class);

    private DbManager dbManager;

    @Inject
    public AccountsDAO(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public Observable<Account> getAccounts() {
        return Observable.empty();
    }

    public Observable<Account> createAccount(AccountCreationRequest req) {
        try {
//            logger.info("createAccount");
            String id = RandomUtil.generateRandom(12);
            return Observable.just(dbManager.getDslContext().insertInto(ACCOUNT, ACCOUNT.ID, ACCOUNT.EMAIL, ACCOUNT.PASSWORD)
                    .values(id, req.getEmail(), req.getPassword())
                    .execute())
                    .map(Void -> {
//                        logger.info("createAccount2");
                        Account account = new Account();
                        account.setId(id);
                        account.setEmail(req.getEmail());
                        account.setPassword(req.getPassword());
                        return account;
                    });
        } catch(DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
