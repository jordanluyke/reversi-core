package com.jordanluyke.reversi.session;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.util.DateUtil;
import com.jordanluyke.reversi.util.RandomUtil;
import lombok.AllArgsConstructor;
import rx.Observable;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.jooq.sources.Tables.SESSION;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Singleton
@AllArgsConstructor(onConstructor = @__(@Inject))
public class SessionDAO {

    private DbManager dbManager;

    public Observable<Session> createSession(String ownerId) {
        String id = RandomUtil.generateId();
        Date createdAt = new Date();
        Date expiresAt = DateUtil.addTime(createdAt, 1, TimeUnit.DAYS);
        return Observable.just(dbManager.getDsl().insertInto(SESSION, SESSION.ID, SESSION.CREATEDAT, SESSION.OWNERID, SESSION.EXPIRESAT)
                .values(id, DateUtil.getTimestamp(createdAt), ownerId, DateUtil.getTimestamp(expiresAt))
                .execute())
                .map(Void -> Session.builder()
                        .id(id)
                        .createdAt(createdAt)
                        .ownerId(ownerId)
                        .expiresAt(Optional.of(expiresAt))
                        .build());
    }
}
