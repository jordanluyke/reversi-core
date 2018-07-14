package com.jordanluyke.reversi.session;

import com.google.inject.Inject;
import com.jordanluyke.reversi.db.DbManager;
import com.jordanluyke.reversi.session.model.Session;
import com.jordanluyke.reversi.util.RandomUtil;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.jooq.sources.Tables.SESSION;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class SessionDAO {
    private static final Logger logger = LogManager.getLogger(SessionDAO.class);

    private DbManager dbManager;

    public Observable<Session> createSession(String ownerId) {
        String id = RandomUtil.generateId();
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(1, ChronoUnit.DAYS);
        return Observable.just(dbManager.getDsl().insertInto(SESSION, SESSION.ID, SESSION.CREATEDAT, SESSION.OWNERID, SESSION.EXPIRESAT)
                .values(id, createdAt, ownerId, expiresAt)
                .execute())
                .map(Void -> Session.builder()
                        .id(id)
                        .createdAt(createdAt)
                        .ownerId(ownerId)
                        .expiresAt(Optional.of(expiresAt))
                        .build());
    }
}
